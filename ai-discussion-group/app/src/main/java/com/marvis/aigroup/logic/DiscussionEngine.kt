package com.marvis.aigroup.logic

import com.marvis.aigroup.data.AppRepository
import com.marvis.aigroup.model.Conversation
import com.marvis.aigroup.model.Message
import com.marvis.aigroup.model.Role
import com.marvis.aigroup.network.LlmClient
import com.marvis.aigroup.network.LlmResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 讨论引擎：
 * 提问 → 按顺序逐个作答 → 看别人回答 → 有分歧辩论（一致即停，最多maxRounds轮）→ 主讲总结
 */
class DiscussionEngine(
    private val repo: AppRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    /** 状态回调 */
    var onThinking: ((roleName: String) -> Unit)? = null
    var onMessage: ((Message) -> Unit)? = null
    var onFinished: ((Boolean) -> Unit)? = null

    private var job: Job? = null

    fun cancel() {
        job?.cancel()
    }

    fun startGroupDiscussion(
        conv: Conversation,
        question: String,
        roles: List<Role>,
        maxRounds: Int
    ) {
        job = scope.launch {
            // 存用户消息
            onMessage?.invoke(
                Message(
                    id = repo.newId(), convId = conv.id, senderType = "user",
                    content = question, createdAt = System.currentTimeMillis()
                )
            )

            val allViews = mutableListOf<Pair<Role, String>>()
            val host = roles.firstOrNull { it.id == conv.hostRoleId } ?: roles.lastOrNull()
            val participants = roles.filter { it.id != conv.hostRoleId }

            // 第1轮：各自独立回答
            for (role in roles) {
                if (job?.isActive == false) return@launch
                onThinking?.invoke(role.name)
                val result = callLlm(role, listOf(
                    LlmClient.ChatMsg("system", "你是${role.name}，请直接回答用户的问题，观点明确，用简洁的中文。"),
                    LlmClient.ChatMsg("user", question)
                ))
                val text = if (result.success) result.content else "[${result.errorMsg}]"
                allViews.add(role to text)
                emitAiMessage(conv, role, text, round = 1)
                if (!result.success) updateRoleStatus(role, result.errorType, result.errorMsg)
            }

            // 辩论轮：2..maxRounds
            var converged = false
            for (round in 2..maxRounds) {
                if (job?.isActive == false) return@launch
                val othersText = allViews.joinToString("\n\n") { (r, t) -> "${r.name}：$t" }
                val newViews = mutableListOf<Pair<Role, String>>()
                var allAgree = true

                for (role in roles) {
                    if (job?.isActive == false) return@launch
                    onThinking?.invoke(role.name)
                    val prompt = buildString {
                        append("你是${role.name}，正在参加一场多人AI讨论。\n")
                        append("用户的问题是：$question\n")
                        append("以下是其他AI的观点：\n$othersText\n")
                        append("请你：1）若有不同意见或发现错误，请明确指出并反驳；2）若你同意其他人的观点，请只回复【无异议】三个字。")
                    }
                    val result = callLlm(role, listOf(
                        LlmClient.ChatMsg("system", "你是${role.name}，参与AI圆桌讨论。"),
                        LlmClient.ChatMsg("user", prompt)
                    ))
                    val text = if (result.success) result.content else "[${result.errorMsg}]"
                    newViews.add(role to text)
                    emitAiMessage(conv, role, text, round = round)
                    if (!result.success) {
                        updateRoleStatus(role, result.errorType, result.errorMsg)
                    } else if (text.contains("无异议") || text.contains("同意")) {
                        // 无异议视为同意
                    } else {
                        allAgree = false
                    }
                }
                allViews.clear()
                allViews.addAll(newViews)
                if (allAgree) { converged = true; break }
            }

            // 主讲总结（若指定了主讲）
            if (host != null && job?.isActive == true) {
                onThinking?.invoke("${host.name}(主讲)")
                val summaryPrompt = buildString {
                    append("你是本次AI讨论的主讲人${host.name}，请综合以下所有AI的发言，给出一个条理清晰、结论明确的最终总结。\n")
                    append("用户的问题：$question\n")
                    append("讨论记录：\n")
                    allViews.forEachIndexed { i, (r, t) -> append("${i + 1}. ${r.name}：$t\n") }
                    append("\n请输出格式：\n【结论】\n（简明结论）\n【理由】\n（关键理由1-3条）")
                }
                val result = callLlm(host, listOf(
                    LlmClient.ChatMsg("system", "你是AI圆桌讨论的主讲人，负责汇总结论。"),
                    LlmClient.ChatMsg("user", summaryPrompt)
                ))
                val text = if (result.success) result.content else "[总结失败：${result.errorMsg}]"
                emitAiMessage(conv, host, text, round = maxRounds, isConclusion = true)
                if (!result.success) updateRoleStatus(host, result.errorType, result.errorMsg)
            }

            onFinished?.invoke(converged)
        }
    }

    /** 单聊：一问一答 */
    fun startSingleChat(
        conv: Conversation,
        role: Role,
        question: String,
        history: List<Message>
    ) {
        job = scope.launch {
            onMessage?.invoke(
                Message(
                    id = repo.newId(), convId = conv.id, senderType = "user",
                    content = question, createdAt = System.currentTimeMillis()
                )
            )
            onThinking?.invoke(role.name)
            val msgs = history.filter { it.senderType != "system" }
                .takeLast(20)
                .map { m ->
                    if (m.senderType == "user") LlmClient.ChatMsg("user", m.content)
                    else LlmClient.ChatMsg("assistant", m.content)
                } + LlmClient.ChatMsg("user", question)
            val result = callLlm(role, msgs)
            val text = if (result.success) result.content else "[${result.errorMsg}]"
            emitAiMessage(conv, role, text, round = 0)
            if (!result.success) updateRoleStatus(role, result.errorType, result.errorMsg)
            onFinished?.invoke(result.success)
        }
    }

    private suspend fun callLlm(role: Role, messages: List<LlmClient.ChatMsg>): LlmResult =
        withContext(Dispatchers.IO) {
            LlmClient.chat(role.baseUrl, role.apiKey, role.model, messages)
        }

    private fun emitAiMessage(conv: Conversation, role: Role, text: String, round: Int, isConclusion: Boolean = false) {
        val msg = Message(
            id = repo.newId(), convId = conv.id, senderType = "ai",
            roleId = role.id, roleName = role.name, colorHex = role.colorHex,
            content = text, round = round, isConclusion = isConclusion,
            createdAt = System.currentTimeMillis()
        )
        onMessage?.invoke(msg)
    }

    private fun updateRoleStatus(role: Role, errorType: String, errorMsg: String) {
        val updated = when (errorType) {
            "warn" -> role.copy(status = "warn", statusText = errorMsg)
            "error" -> role.copy(status = "error", statusText = errorMsg)
            "network" -> role.copy(status = "warn", statusText = "网络异常")
            else -> role.copy(status = "ok", statusText = "正常在线")
        }
        repo.saveRole(updated)
        if (errorType != "ok") {
            repo.addAlert("【${updated.name}】状态提醒", errorMsg)
        }
    }
}
