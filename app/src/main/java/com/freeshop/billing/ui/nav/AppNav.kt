package com.freeshop.billing.ui.nav

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.freeshop.billing.data.SessionStore
import com.freeshop.billing.ui.screen.*
import com.freeshop.billing.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first

private const val ROUTE_LANDING   = "landing"
private const val ROUTE_REGISTER  = "register"
private const val ROUTE_LOGIN     = "login"
private const val ROUTE_DASHBOARD = "dashboard/{shopId}"
private const val ROUTE_SETTINGS  = "settings/{shopId}"
private const val ROUTE_BILLS     = "bills/{shopId}"
private const val ROUTE_ITEMS     = "items/{shopId}"
private const val ROUTE_CUSTOMERS = "customers/{shopId}"

@Composable
fun AppNav() {
    val ctx = LocalContext.current
    val navController = rememberNavController()
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.Factory(ctx))
    val session = remember { SessionStore(ctx) }
    val sessionShopId by session.shopIdFlow.collectAsStateWithLifecycle(initialValue = null)

    // Gate: redirect to dashboard if already logged in
    var gateChecked by remember { mutableStateOf(false) }
    LaunchedEffect(sessionShopId, gateChecked) {
        if (!gateChecked && sessionShopId != null) {
            gateChecked = true
            navController.navigate("dashboard/$sessionShopId") {
                popUpTo(ROUTE_LANDING) { inclusive = true }
            }
        } else if (!gateChecked && sessionShopId == null) {
            // Still waiting; check for null explicitly after first emit
        }
    }

    NavHost(navController = navController, startDestination = ROUTE_LANDING) {

        composable(ROUTE_LANDING) {
            LandingScreen(
                onRegister = { navController.navigate(ROUTE_REGISTER) },
                onLogin    = { navController.navigate(ROUTE_LOGIN)    },
            )
        }

        composable(ROUTE_REGISTER) {
            // On success, session is set; we navigate based on session
            RegisterScreen(
                onSuccess = {
                    // Session is already set in AuthViewModel.register
                    // Read shopId from session flow and navigate
                    navController.navigate(ROUTE_DASHBOARD.replace("{shopId}", sessionShopId.toString())) {
                        popUpTo(ROUTE_LANDING) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(ROUTE_LOGIN) {
            LoginScreen(
                onSuccess = {
                    navController.navigate(ROUTE_DASHBOARD.replace("{shopId}", sessionShopId.toString())) {
                        popUpTo(ROUTE_LANDING) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = ROUTE_DASHBOARD,
            arguments = listOf(navArgument("shopId") { type = NavType.LongType }),
        ) { back ->
            val shopId = back.arguments!!.getLong("shopId")
            DashboardScreen(
                shopId    = shopId,
                onSettings  = { navController.navigate("settings/$shopId") },
                onBills     = { navController.navigate("bills/$shopId")    },
                onItems     = { navController.navigate("items/$shopId")    },
                onCustomers = { navController.navigate("customers/$shopId")},
                onLogout    = {
                    authVm.logout {
                        navController.navigate(ROUTE_LANDING) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }

        composable(
            route = ROUTE_SETTINGS,
            arguments = listOf(navArgument("shopId") { type = NavType.LongType }),
        ) { back ->
            val shopId = back.arguments!!.getLong("shopId")
            SettingsScreen(
                shopId  = shopId,
                onBack  = { navController.popBackStack() },
                onLogout = {
                    authVm.logout {
                        navController.navigate(ROUTE_LANDING) { popUpTo(0) { inclusive = true } }
                    }
                },
            )
        }

        composable(
            route = ROUTE_BILLS,
            arguments = listOf(navArgument("shopId") { type = NavType.LongType }),
        ) { back ->
            val shopId = back.arguments!!.getLong("shopId")
            BillsScreen(shopId = shopId, onBack = { navController.popBackStack() })
        }

        composable(
            route = ROUTE_ITEMS,
            arguments = listOf(navArgument("shopId") { type = NavType.LongType }),
        ) { back ->
            val shopId = back.arguments!!.getLong("shopId")
            ItemsScreen(shopId = shopId, onBack = { navController.popBackStack() })
        }

        composable(
            route = ROUTE_CUSTOMERS,
            arguments = listOf(navArgument("shopId") { type = NavType.LongType }),
        ) { back ->
            val shopId = back.arguments!!.getLong("shopId")
            CustomersScreen(shopId = shopId, onBack = { navController.popBackStack() })
        }
    }
}
