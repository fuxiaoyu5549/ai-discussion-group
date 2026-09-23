package com.marvis.aigroup.model

/** 系统提醒（额度不足/报错等），单独聊天窗展示 */
data class SystemAlert(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    var read: Boolean = false
)
