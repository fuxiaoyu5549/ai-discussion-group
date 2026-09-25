package com.marvis.aigroup.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marvis.aigroup.ui.screen.AlertScreen
import com.marvis.aigroup.ui.screen.ConversationListScreen
import com.marvis.aigroup.ui.screen.GroupChatScreen
import com.marvis.aigroup.ui.screen.NewDiscussionScreen
import com.marvis.aigroup.ui.screen.RoleEditScreen
import com.marvis.aigroup.ui.screen.RolesScreen
import com.marvis.aigroup.ui.screen.SettingsScreen
import com.marvis.aigroup.ui.screen.SingleChatScreen

object Routes {
    const val CONVERSATIONS = "conversations"
    const val NEW_DISCUSSION = "new_discussion"
    const val GROUP_CHAT = "group/{convId}"
    const val SINGLE_CHAT = "single/{convId}"
    const val ROLES = "roles"
    const val ROLE_EDIT = "role_edit/{roleId}"
    const val SETTINGS = "settings"
    const val ALERTS = "alerts"

    fun groupChat(id: String) = "group/$id"
    fun singleChat(id: String) = "single/$id"
    fun roleEdit(id: String) = "role_edit/$id"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.CONVERSATIONS) {
        composable(Routes.CONVERSATIONS) {
            ConversationListScreen(
                onNewDiscussion = { nav.navigate(Routes.NEW_DISCUSSION) },
                onOpenGroup = { id -> nav.navigate(Routes.groupChat(id)) },
                onOpenSingle = { id -> nav.navigate(Routes.singleChat(id)) },
                onRoles = { nav.navigate(Routes.ROLES) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onAlerts = { nav.navigate(Routes.ALERTS) }
            )
        }
        composable(Routes.NEW_DISCUSSION) {
            NewDiscussionScreen(onBack = { nav.popBackStack() })
        }
        composable(
            Routes.GROUP_CHAT,
            arguments = listOf(navArgument("convId") { type = NavType.StringType })
        ) { entry ->
            val convId = entry.arguments?.getString("convId").orEmpty()
            GroupChatScreen(convId = convId, onBack = { nav.popBackStack() })
        }
        composable(
            Routes.SINGLE_CHAT,
            arguments = listOf(navArgument("convId") { type = NavType.StringType })
        ) { entry ->
            val convId = entry.arguments?.getString("convId").orEmpty()
            SingleChatScreen(convId = convId, onBack = { nav.popBackStack() })
        }
        composable(Routes.ROLES) {
            RolesScreen(
                onBack = { nav.popBackStack() },
                onEdit = { id -> nav.navigate(Routes.roleEdit(id)) },
                onNew = { nav.navigate(Routes.roleEdit("")) }
            )
        }
        composable(
            Routes.ROLE_EDIT,
            arguments = listOf(navArgument("roleId") { type = NavType.StringType })
        ) { entry ->
            val roleId = entry.arguments?.getString("roleId").orEmpty()
            RoleEditScreen(roleId = roleId, onBack = { nav.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() }, onRoles = { nav.navigate(Routes.ROLES) })
        }
        composable(Routes.ALERTS) {
            AlertScreen(onBack = { nav.popBackStack() })
        }
    }
}
