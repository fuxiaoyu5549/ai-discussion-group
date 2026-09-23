package com.marvis.aigroup.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marvis.aigroup.LocalAppState
import com.marvis.aigroup.model.Conversation
import com.marvis.aigroup.ui.common.RoleAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    onNewDiscussion: () -> Unit,
    onOpenGroup: (String) -> Unit,
    onOpenSingle: (String) -> Unit,
    onRoles: () -> Unit,
    onSettings: () -> Unit,
    onAlerts: () -> Unit
) {
    val app = LocalAppState.current
    val conversations = app.conversations
    val unread = app.unreadAlerts()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的讨论", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    Box {
                        IconButton(onClick = onAlerts) {
                            Icon(Icons.Default.Notifications, contentDescription = "系统提醒")
                        }
                        if (unread > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 2.dp)
                            ) { Text("$unread") }
                        }
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewDiscussion) {
                Icon(Icons.Default.Add, contentDescription = "新建讨论")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // 角色快捷入口行（单聊入口 + 状态签名）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                app.roles.filter { it.enabled }.take(5).forEach { role ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                // 查找或创建该角色的单聊会话
                                val existing = app.conversations.firstOrNull {
                                    it.type == "single" && it.participantRoleIds == listOf(role.id)
                                }
                                val id = existing?.id ?: run {
                                    val conv = com.marvis.aigroup.model.Conversation(
                                        id = app.repo.newId(),
                                        title = role.name,
                                        type = "single",
                                        participantRoleIds = listOf(role.id)
                                    )
                                    app.repo.saveConversation(conv)
                                    app.refresh()
                                    conv.id
                                }
                                onOpenSingle(id)
                            }
                            .padding(4.dp)
                    ) {
                        RoleAvatar(role = role, size = 48, showStatus = true)
                        Text(role.name, fontSize = 11.sp, maxLines = 1)
                    }
                }
                if (app.roles.isEmpty()) {
                    Text(
                        "还没有AI角色，点右上角设置→角色管理添加",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            if (conversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "还没有讨论，点右下角 + 开一个吧",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(conversations, key = { it.id }) { conv ->
                        ConversationItem(
                            conv = conv,
                            roleNames = conv.participantRoleIds
                                .mapNotNull { app.getRole(it)?.name },
                            onClick = {
                                if (conv.type == "single") onOpenSingle(conv.id)
                                else onOpenGroup(conv.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conv: Conversation,
    roleNames: List<String>,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    conv.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    if (conv.type == "single") "单聊" else roleNames.joinToString("、"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(conv.updatedAt)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}
