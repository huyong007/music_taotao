package com.kidsenglishsongs.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kidsenglishsongs.player.ui.library.LibraryScreen
import com.kidsenglishsongs.player.ui.parent.ParentControlScreen
import com.kidsenglishsongs.player.ui.player.PlayerScreen
import com.kidsenglishsongs.player.ui.settings.SettingsScreen

/**
 * 导航路由定义
 */
object Routes {
    const val PLAYER = "player"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val PARENT_CONTROL = "parent_control"
    const val PLAYLIST = "playlist"
}

/**
 * App 导航组件
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.PLAYER
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 播放界面 - 主界面
        composable(Routes.PLAYER) {
            PlayerScreen(
                onNavigateToLibrary = {
                    navController.navigate(Routes.LIBRARY)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }
        
        // 歌曲库界面
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToParentControl = {
                    navController.navigate(Routes.PARENT_CONTROL)
                }
            )
        }
        
        // 设置界面
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // 家长控制界面
        composable(Routes.PARENT_CONTROL) {
            ParentControlScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
