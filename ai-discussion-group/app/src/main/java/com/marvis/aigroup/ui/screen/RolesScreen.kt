package com.marvis.aigroup.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.marvis.aigroup.ui.common.RoleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolesScreen(onBack: () -> Unit, onEdit: (String) -> Unit, onNew: () -> Unit) {
    val app = LocalAppState.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("角色管理", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onNew) {
                        Icon(Icons.Default.Add, contentDescription = "新增角色")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "点击角色可编辑 API 地址、密钥、模型、签名；开关控制是否参与讨论",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(app.roles, key = { it.id }) { role ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEdit(role.id) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoleAvatar(role = role, size = 44, showStatus = true)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(role.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                role.model + " · " + role.baseUrl.replaceFirst("https://", "").replaceFirst("http://", ""),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                            Text(
                                "签名：" + role.signature,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                maxLines = 1
                            )
                        }
                        Switch(
                            checked = role.enabled,
                            onCheckedChange = { checked ->
                                app.repo.saveRole(role.copy(enabled = checked))
                                app.refresh()
                            }
                        )
                    }
                }
            }
        }
    }
}
