package com.marvis.aigroup.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marvis.aigroup.model.Role

/** AI 头像：圆形色块 + 名字首字，右下角状态点 */
@Composable
fun RoleAvatar(
    role: Role,
    size: Int = 40,
    showStatus: Boolean = false
) {
    val color = try {
        Color(android.graphics.Color.parseColor(role.colorHex))
    } catch (e: Exception) {
        Color(0xFF4FC3F7)
    }
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = role.name.take(1),
                color = Color.White,
                fontSize = (size * 0.4f).sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (showStatus) {
            val dotColor = when (role.status) {
                "warn" -> Color(0xFFFFB300)
                "error" -> Color(0xFFF44336)
                "off" -> Color(0xFF9E9E9E)
                else -> Color(0xFF4CAF50)
            }
            Box(
                modifier = Modifier
                    .size((size * 0.28f).dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.White, CircleShape)
                    .background(dotColor)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}
