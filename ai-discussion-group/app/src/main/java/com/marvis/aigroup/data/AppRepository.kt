package com.marvis.aigroup.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.marvis.aigroup.model.Conversation
import com.marvis.aigroup.model.Message
import com.marvis.aigroup.model.Role
import com.marvis.aigroup.model.SystemAlert
import java.util.UUID

/**
 * 本地存储仓库：SharedPreferences + Gson（JSON序列化）
 * 数据量小（个人角色/会话/消息），足够用且零额外依赖风险
 */
class AppRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_group_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    private inline fun <reified T> loadList(key: String): MutableList<T> {
        val json = prefs.getString(key, null) ?: return mutableListOf()
        return try {
            gson.fromJson(json, object : TypeToken<MutableList<T>>() {}.type) ?: mutableListOf()
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    private fun <T> saveList(key: String, list: List<T>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }

    // ---------- 角色 ----------
    fun getRoles(): MutableList<Role> = loadList(KEY_ROLES)

    fun getRole(id: String): Role? = getRoles().firstOrNull { it.id == id }

    fun saveRole(role: Role) {
        val roles = getRoles()
        val idx = roles.indexOfFirst { it.id == role.id }
        if (idx >= 0) roles[idx] = role else roles.add(0, role)
        saveList(KEY_ROLES, roles)
    }

    fun deleteRole(roleId: String) {
        saveList(KEY_ROLES, getRoles().filter { it.id != roleId })
    }

    fun newId(): String = UUID.randomUUID().toString()

    // ---------- 会话 ----------
    fun getConversations(): MutableList<Conversation> = loadList(KEY_CONVERSATIONS)

    fun getConversation(id: String): Conversation? =
        getConversations().firstOrNull { it.id == id }

    fun saveConversation(conv: Conversation) {
        val list = getConversations()
        val idx = list.indexOfFirst { it.id == conv.id }
        if (idx >= 0) list[idx] = conv else list.add(0, conv)
        saveList(KEY_CONVERSATIONS, list)
    }

    fun deleteConversation(id: String) {
        saveList(KEY_CONVERSATIONS, getConversations().filter { it.id != id })
        saveList(KEY_MESSAGES, getMessages(id))
        prefs.edit().putString(KEY_MESSAGES, "[]").apply()
        saveList(KEY_MESSAGES, getMessagesRaw().filter { it.convId != id })
    }

    // ---------- 消息 ----------
    private fun getMessagesRaw(): MutableList<Message> = loadList(KEY_MESSAGES)

    fun getMessages(convId: String): List<Message> =
        getMessagesRaw().filter { it.convId == convId }

    fun addMessage(msg: Message) {
        val all = getMessagesRaw()
        all.add(msg)
        saveList(KEY_MESSAGES, all)
        getConversation(msg.convId)?.let {
            saveConversation(it.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun addMessages(list: List<Message>) {
        val all = getMessagesRaw()
        all.addAll(list)
        saveList(KEY_MESSAGES, all)
    }

    // ---------- 系统提醒 ----------
    fun getAlerts(): MutableList<SystemAlert> = loadList(KEY_ALERTS)

    fun addAlert(title: String, content: String) {
        val alerts = getAlerts()
        alerts.add(0, SystemAlert(id = newId(), title = title, content = content))
        saveList(KEY_ALERTS, alerts)
    }

    fun markAlertsRead() {
        saveList(KEY_ALERTS, getAlerts().map { it.copy(read = true) })
    }

    fun markAlertRead(id: String) {
        saveList(KEY_ALERTS, getAlerts().map { if (it.id == id) it.copy(read = true) else it })
    }

    fun unreadAlertCount(): Int = getAlerts().count { !it.read }

    companion object {
        private const val KEY_ROLES = "roles"
        private const val KEY_CONVERSATIONS = "conversations"
        private const val KEY_MESSAGES = "messages"
        private const val KEY_ALERTS = "alerts"
    }
}
