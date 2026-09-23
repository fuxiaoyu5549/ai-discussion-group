package com.marvis.aigroup.model

/** 一条消息 */
data class Message(
    val id: String,
    val convId: String,
    /** user=用户 ai=AI回答 system=系统/结论 */
    val senderType: String,
    val roleId: String = "",
    val roleName: String = "",
    val colorHex: String = "#4FC3F7",
    val content: String,
    val round: Int = 0,
    /** 是否主讲结论 */
    val isConclusion: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
