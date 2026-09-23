package com.marvis.aigroup.model

/** AI角色配置 */
data class Role(
    val id: String,
    val name: String,
    val colorHex: String = "#4FC3F7",
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val signature: String = "我是AI，我在线",
    val enabled: Boolean = true,
    /** 状态：ok=正常 warn=额度不足 error=报错 off=停用 */
    var status: String = "ok",
    var statusText: String = "正常在线",
    val createdAt: Long = System.currentTimeMillis()
)
