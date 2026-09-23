package com.marvis.aigroup.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marvis.aigroup.model.Message
import com.marvis.aigroup.model.Role
import com.marvis.aigroup.ui.theme.BubbleDark
import com.marvis.aigroup.ui.theme.BubbleLight
import com.marvis.aigroup.ui.theme.ChatGreen
import com.marvis.aigroup.ui.theme.SystemGray

/** 聊天气泡（仿微信） */
@Composable
fun MessageBubble(
    message: Message,
    role: Role?,
    darkTheme: Boolean
) {
    val bubbleColor = if (darkTheme) BubbleDark else BubbleLight
    val textColor = MaterialTheme.colorScheme.onSurface

    when (message.senderType) {
        "user" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = if (darkTheme) Color(0xFF2E7D32) else ChatGreen,
                            shape = RoundedCornerShape(12.dp, 2.dp, 12.dp, 12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .widthIn(max = 300.dp)
                ) {
                    Text(message.content, color = if (darkTheme) Color.White else Color(0xFF111111), fontSize = 15.sp)
                }
            }
        }
        "ai" -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                role?.let { RoleAvatar(role = it, size = 36) }
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        message.roleName + (if (message.isConclusion) "（主讲·结论）" else ""),
                        fontSize = 12.sp,
                        color = try {
                            Color(android.graphics.Color.parseColor(message.colorHex))
                        } catch (e: Exception) {
                            SystemGray
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(
                                color = if (message.isConclusion) {
                                    if (darkTheme) Color(0xFF1B3A2A) else Color(0xFFE8F5E9)
                                } else bubbleColor,
                                shape = RoundedCornerShape(2.dp, 12.dp, 12.dp, 12.dp)
                            )
                            .border(
                                width = if (message.isConclusion) 1.dp else 0.dp,
                                color = if (message.isConclusion) Color(0xFF4CAF50) else Color.Transparent,
                                shape = RoundedCornerShape(2.dp, 12.dp, 12.dp, 12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .widthIn(max = 300.dp)
                    ) {
                        Text(message.content, color = textColor, fontSize = 15.sp)
                    }
                }
            }
        }
        else -> { // system / 结论等
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    message.content,
                    fontSize = 12.sp,
                    color = SystemGray,
                    modifier = Modifier
                        .background(
                            color = if (darkTheme) Color(0xFF2A2A2A) else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}
