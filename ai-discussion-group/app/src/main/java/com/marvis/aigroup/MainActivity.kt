package com.marvis.aigroup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.marvis.aigroup.data.AppRepository
import com.marvis.aigroup.model.Conversation
import com.marvis.aigroup.model.Role
import com.marvis.aigroup.model.SystemAlert
import com.marvis.aigroup.ui.AppNav
import com.marvis.aigroup.ui.theme.AiGroupTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 全局状态（简单状态管理，避免引入过重框架） */
class AppState(context: android.content.Context) {
    val repo = AppRepository(context)
    var roles by mutableStateOf(repo.getRoles())
        private set
    var conversations by mutableStateOf(repo.getConversations())
        private set
    var alerts by mutableStateOf(repo.getAlerts())
        private set

    fun refresh() {
        roles = repo.getRoles()
        conversations = repo.getConversations()
        alerts = repo.getAlerts()
    }

    fun getRole(id: String): Role? = roles.firstOrNull { it.id == id }
    fun getConversation(id: String): Conversation? = conversations.firstOrNull { it.id == id }
    fun unreadAlerts(): Int = alerts.count { !it.read }
}

val LocalAppState = staticCompositionLocalOf<AppState> {
    error("AppState not provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appState = AppState(applicationContext)
        setContent {
            AiGroupTheme {
                CompositionLocalProvider(LocalAppState provides appState) {
                    AppNav()
                }
            }
        }
    }
}
