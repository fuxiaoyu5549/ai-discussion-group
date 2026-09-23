package com.marvis.aigroup.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.marvis.aigroup.LocalAppState
import com.marvis.aigroup.logic.DiscussionEngine
import com.marvis.aigroup.ui.common.MessageBubble
import com.marvis.aigroup.ui.common.RoleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleChatScreen(convId: String, onBack: () -> Unit) {
    val app = LocalAppState.current
    val conv = app.getConversation(convId)
    val dark = isSystemInDarkTheme()

    if (conv == null) {
        Text("会话不存在")
        return
    }
    val role = conv.participantRoleIds.firstOrNull()?.let { app.getRole(it) }
    if (role == null) {
        Text("角色不存在")
        return
    }

    val engine = remember { DiscussionEngine(app.repo) }
    var input by remember { mutableStateOf("") }
    var thinking by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(app.repo.getMessages(convId)) }
    var running by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(convId) { messages = app.repo.getMessages(convId) }

    DisposableEffect(engine) {
        engine.onThinking = { name -> thinking = "$name 正在思考…" }
        engine.onMessage = { msg ->
            app.repo.addMessage(msg)
            messages = app.repo.getMessages(convId)
            app.refresh()
        }
        engine.onFinished = { thinking = ""; running = false; app.refresh() }
        onDispose { engine.cancel() }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoleAvatar(role = role, size = 36, showStatus = true)
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(role.name, fontWeight = FontWeight.Bold)
                            Text(
                                role.statusText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        role = if (msg.roleId.isNotEmpty()) app.getRole(msg.roleId) else null,
                        darkTheme = dark
                    )
                }
                if (thinking.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                            Text(thinking, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("和${role.name}单独聊…") },
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4
                )
                IconButton(
                    onClick = {
                        val q = input.trim()
                        if (q.isEmpty() || running) return@IconButton
                        input = ""
                        running = true
                        engine.startSingleChat(conv, role, q, messages)
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                }
            }
        }
    }
}
