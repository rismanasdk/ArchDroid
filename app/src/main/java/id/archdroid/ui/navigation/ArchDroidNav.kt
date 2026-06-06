package id.archdroid.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.archdroid.ui.home.HomeRoute
import id.archdroid.ui.packages.PackagesRoute
import id.archdroid.ui.sessions.SessionsRoute
import id.archdroid.ui.settings.SettingsRoute
import id.archdroid.ui.splash.SplashRoute
import id.archdroid.ui.storage.StorageRoute
import id.archdroid.ui.terminal.TerminalRoute

@Composable
fun ArchDroidNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") { SplashRoute(onReady = { nav.navigate("home") { popUpTo("splash") { inclusive = true } } }) }
        composable("home") { HomeRoute(nav) }
        composable("terminal/{sessionId}") { backStack ->
            TerminalRoute(sessionId = backStack.arguments?.getString("sessionId").orEmpty())
        }
        composable("sessions") { SessionsRoute(nav) }
        composable("packages") { PackagesRoute(nav) }
        composable("storage") { StorageRoute(nav) }
        composable("settings") { SettingsRoute(nav) }
    }
}
