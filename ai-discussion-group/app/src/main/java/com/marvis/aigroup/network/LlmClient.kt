package com.marvis.aigroup.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** OpenAI 兼容接口调用结果 */
data class LlmResult(
    val success: Boolean,
    val content: String = "",
    /** ok=正常 warn=额度不足 error=接口错误 network=网络错误 */
    val errorType: String = "ok",
    val errorMsg: String = ""
)

/**
 * 通用 OpenAI 兼容 API 客户端：
 * 只需 baseUrl + apiKey + model 即可调用 DeepSeek/千问/混元/豆包/智谱/硅基流动等
 */
object LlmClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    data class ChatMsg(val role: String, val content: String)

    suspend fun chat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMsg>,
        temperature: Double = 0.7
    ): LlmResult = withContext(Dispatchers.IO) {
        try {
            val url = if (baseUrl.endsWith("/")) baseUrl + "chat/completions"
                      else baseUrl + "/chat/completions"
            val body = JSONObject().apply {
                put("model", model)
                put("temperature", temperature)
                put("messages", JSONArray().apply {
                    messages.forEach { put(JSONObject().put("role", it.role).put("content", it.content)) }
                })
            }
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(body.toString().toRequestBody(jsonMedia))
                .build()

            client.newCall(request).execute().use { resp ->
                val respBody = resp.body?.string().orEmpty()
                when {
                    resp.isSuccessful -> {
                        val json = JSONObject(respBody)
                        val content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        LlmResult(success = true, content = content)
                    }
                    resp.code == 401 -> LlmResult(false, errorType = "error", errorMsg = "API Key 无效(401)")
                    resp.code == 402 -> LlmResult(false, errorType = "warn", errorMsg = "余额/额度不足(402)")
                    resp.code == 429 -> LlmResult(false, errorType = "warn", errorMsg = "请求太频繁(429)")
                    resp.code in 400..499 -> LlmResult(false, errorType = "error", errorMsg = "接口错误(${resp.code})")
                    else -> LlmResult(false, errorType = "error", errorMsg = "服务端错误(${resp.code})")
                }
            }
        } catch (e: Exception) {
            LlmResult(false, errorType = "network", errorMsg = "网络异常: ${e.message ?: "连接失败"}")
        }
    }
}
