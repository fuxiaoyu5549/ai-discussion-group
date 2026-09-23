package com.marvis.aigroup.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marvis.aigroup.LocalAppState
import com.marvis.aigroup.Routes
import com.marvis.aigroup.model.Conversation
import com.marvis.aigroup.ui.common.RoleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewDiscussionScreen(onBack: () -> Unit) {
    val app = LocalAppState.current
    var title by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(app.roles.filter { it.enabled }.map { it.id }.toSet()) }
    var hostId by remember { mutableStateOf(app.roles.filter { it.enabled }.firstOrNull()?.id.orEmpty()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新建讨论", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("讨论主题") },
                placeholder = { Text("例如：帮我选一台抽油烟机") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "选择参与的AI",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            app.roles.forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = role.id in selected,
                        onCheckedChange = { checked ->
                            selected = if (checked) selected + role.id else selected - role.id
                            if (!checked && hostId == role.id) hostId = ""
                        }
                    )
                    RoleAvatar(role = role, size = 32)
                    Text(
                        role.name + "  ·  " + role.model,
                        modifier = Modifier.padding(start = 8.dp),
                        fontSize = 14.sp
                    )
                }
            }
            if (app.roles.isEmpty()) {
                Text(
                    "还没有AI角色，请先到 设置→角色管理 添加",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                "选择主讲（负责汇总结论）",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
            )
            app.roles.filter { it.id in selected }.forEach { role ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    RadioButton(
                        selected = hostId == role.id,
                        onClick = { hostId = role.id }
                    )
                    Text(role.name, fontSize = 14.sp)
                }
            }

            Button(
                onClick = {
                    val ids = selected.toList()
                    if (title.isBlank() || ids.isEmpty()) return@Button
                    val conv = Conversation(
                        id = app.repo.newId(),
                        title = title.trim(),
                        type = "group",
                        hostRoleId = hostId,
                        participantRoleIds = ids
                    )
                    app.repo.saveConversation(conv)
                    app.refresh()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("开始讨论", fontSize = 16.sp)
            }
        }
    }
}
