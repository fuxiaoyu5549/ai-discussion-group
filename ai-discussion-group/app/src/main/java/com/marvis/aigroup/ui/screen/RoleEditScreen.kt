package com.marvis.aigroup.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marvis.aigroup.LocalAppState
import com.marvis.aigroup.model.Role
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleEditScreen(roleId: String, onBack: () -> Unit) {
    val app = LocalAppState.current
    val existing = roleId.takeIf { it.isNotEmpty() }?.let { app.getRole(it) }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var baseUrl by remember { mutableStateOf(existing?.baseUrl ?: "https://api.deepseek.com/v1") }
    var apiKey by remember { mutableStateOf(existing?.apiKey ?: "") }
    var model by remember { mutableStateOf(existing?.model ?: "deepseek-chat") }
    var colorHex by remember { mutableStateOf(existing?.colorHex ?: "#4FC3F7") }
    var signature by remember { mutableStateOf(existing?.signature ?: "我是AI，我在线") }
    var enabled by remember { mutableStateOf(existing?.enabled ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "新增角色" else "编辑角色", fontWeight = FontWeight.Bold) },
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
                value = name,
                onValueChange = { name = it },
                label = { Text("角色名称") },
                placeholder = { Text("例如：DeepSeek、通义千问") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("API 地址（OpenAI兼容）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("模型名称") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            OutlinedTextField(
                value = colorHex,
                onValueChange = { colorHex = it },
                label = { Text("头像颜色（#RRGGBB）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            OutlinedTextField(
                value = signature,
                onValueChange = { signature = it },
                label = { Text("签名") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("启用该角色", fontSize = 15.sp, modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }

            Button(
                onClick = {
                    if (name.isBlank() || baseUrl.isBlank() || apiKey.isBlank() || model.isBlank()) return@Button
                    val role = Role(
                        id = existing?.id ?: UUID.randomUUID().toString(),
                        name = name.trim(),
                        baseUrl = baseUrl.trim().trimEnd('/'),
                        apiKey = apiKey.trim(),
                        model = model.trim(),
                        colorHex = colorHex.trim(),
                        signature = signature.trim().ifEmpty { "我是AI，我在线" },
                        enabled = enabled,
                        status = existing?.status ?: "on",
                        statusText = existing?.statusText ?: "在线"
                    )
                    app.repo.saveRole(role)
                    app.refresh()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("保存", fontSize = 16.sp)
            }

            if (existing != null) {
                IconButton(
                    onClick = {
                        app.repo.deleteRole(existing.id)
                        app.refresh()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "删除角色")
                    Text("删除该角色", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
