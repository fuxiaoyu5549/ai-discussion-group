package com.marvis.aigroup.model

/** 讨论会话（群聊或单聊） */
data class Conversation(
    val id: String,
    val title: String,
    /** group=群聊 single=单聊 */
    val type: String = "group",
    val createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis(),
    /** 主讲AI角色id（群聊用） */
    var hostRoleId: String = "",
    /** 参与讨论的AI角色id列表 */
    var participantRoleIds: List<String> = emptyList()
)
