package com.marvis.aigroup.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var maxRounds by remember { mutableIntStateOf(3) }
    var autoSummarize by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("自动收敛总结", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("讨论多轮后自动由主讲输出结论", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Switch(checked = autoSummarize, onCheckedChange = { autoSummarize = it })
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("讨论轮次上限", fontSize = 15.sp, modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = maxRounds.toString(),
                    onValueChange = { v -> v.toIntOrNull()?.let { maxRounds = it.coerceIn(1, 10) } },
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .padding(start = 8.dp)
                )
            }

            Text(
                "使用说明",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            Text(
                "1. 在「角色管理」中为每个 AI 填写 OpenAI 兼容的 API 地址、Key、模型名\n" +
                    "2. 支持的接口格式：/v1/chat/completions\n" +
                    "3. 新建讨论：输入主题、勾选参与 AI、选择主讲\n" +
                    "4. 讨论自动进行多轮，可随时点「提前总结」收尾\n" +
                    "5. 点头像可进入与单个 AI 的私聊\n" +
                    "6. 所有对话本地存储，不上传任何服务器",
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
