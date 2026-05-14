package com.danteandroid.hoteldemo

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danteandroid.hoteldemo.navigation.Screen
import com.danteandroid.hoteldemo.ui.booking.BookingScreen
import com.danteandroid.hoteldemo.ui.booking.BookingSuccessScreen
import com.danteandroid.hoteldemo.ui.checkin.CheckInScreen
import com.danteandroid.hoteldemo.ui.detail.RoomDetailScreen
import com.danteandroid.hoteldemo.ui.home.HomeScreen
import com.danteandroid.hoteldemo.ui.orders.OrdersScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Orders, "我的订单", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    BottomNavItem(Screen.CheckIn, "办理入住", Icons.Filled.MeetingRoom, Icons.Outlined.MeetingRoom),
)

private val bottomNavRouteOrder = listOf("home", "orders", "orders_checkin", "checkin")

@Composable
fun HotelApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route.orEmpty()
    val routeTop = currentRoute.substringBefore("?")
    val onOrdersList = routeTop == Screen.Orders.route
    val onOrdersCheckInFlow = routeTop == "orders_checkin"
    val ordersTabSelected = onOrdersList || onOrdersCheckInFlow
    val showBottomBar =
        routeTop == Screen.Home.route ||
            routeTop == Screen.Orders.route ||
            routeTop == "orders_checkin" ||
            routeTop == "checkin"

    Scaffold(containerColor = MaterialTheme.colorScheme.background) {padding->
        Box(modifier = Modifier.fillMaxSize()) {
            val targetBottomPadding = if (showBottomBar) 80.dp else 0.dp
            val navBarBottomPadding by animateDpAsState(
                targetValue = targetBottomPadding,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                label = "navBarBottomPadding",
            )

            // Keep content layout stable: bottom nav is an overlay, and we reserve space only for the bottom-nav tabs.
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize().padding(bottom = navBarBottomPadding),
                enterTransition = {
                    val direction = transitionDirection(initialState.destination.route, targetState.destination.route)
                    softEnter(direction)
                },
                exitTransition = {
                    val direction = transitionDirection(initialState.destination.route, targetState.destination.route)
                    softExit(direction)
                },
                popEnterTransition = { softEnter(direction = -1) },
                popExitTransition = { softExit(direction = -1) },
            ) {
                composable(Screen.Home.route) { HomeScreen(navController) }
                composable(Screen.Orders.route) { OrdersScreen(navController) }
                composable(
                    route = Screen.CheckIn.route,
                    arguments = listOf(navArgument("code") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }),
                ) { backStack ->
                    val code = backStack.arguments?.getString("code")
                    CheckInScreen(navController = navController, initialBookingCode = code)
                }
                composable(
                    route = Screen.OrdersCheckIn.route,
                    arguments = listOf(navArgument("code") {
                        type = NavType.StringType
                    }),
                ) { backStack ->
                    val code = backStack.arguments?.getString("code") ?: return@composable
                    CheckInScreen(
                        navController = navController,
                        initialBookingCode = code,
                        allowOrderSearch = false,
                    )
                }

                composable(
                    route = Screen.RoomDetail.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                ) { backStack ->
                    val roomId = backStack.arguments?.getString("roomId") ?: return@composable
                    RoomDetailScreen(navController, roomId)
                }

                composable(
                    route = Screen.Booking.route,
                    arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
                ) { backStack ->
                    val roomId = backStack.arguments?.getString("roomId") ?: return@composable
                    BookingScreen(navController, roomId)
                }

                composable(
                    route = Screen.BookingSuccess.route,
                    arguments = listOf(navArgument("bookingCode") { type = NavType.StringType }),
                ) { backStack ->
                    val code = backStack.arguments?.getString("bookingCode") ?: return@composable
                    BookingSuccessScreen(navController, code)
                }
            }

            AnimatedVisibility(
                visible = showBottomBar,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn(animationSpec = tween(120)) + slideInVertically(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 3 },
                ),
                exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    targetOffsetY = { it / 3 },
                ),
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0),
                ) {
                    bottomNavItems.forEach { item ->
                        val base = when (item.screen) {
                            Screen.Home -> "home"
                            Screen.Orders -> "orders"
                            Screen.CheckIn -> "checkin"
                            else -> item.screen.route
                        }
                        val selected = when (item.screen) {
                            Screen.Orders -> ordersTabSelected
                            Screen.CheckIn -> routeTop == "checkin"
                            else -> routeTop == base
                        }
                        NavigationBarItem(
                            icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            onClick = {
                                if (item.screen == Screen.Orders) {
                                    if (!onOrdersList) {
                                        if (onOrdersCheckInFlow) navController.popBackStack()
                                        else {
                                            navController.navigate(Screen.Orders.route) {
                                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    return@NavigationBarItem
                                }
                                if (!selected) {
                                    val route = if (item.screen == Screen.CheckIn) Screen.CheckIn.createRoute() else item.screen.route
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun transitionDirection(fromRoute: String?, toRoute: String?): Int {
    val fromIndex = bottomNavRouteOrder.indexOf(fromRoute?.substringBefore("?")?.substringBefore("/"))
    val toIndex = bottomNavRouteOrder.indexOf(toRoute?.substringBefore("?")?.substringBefore("/"))
    if (fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex) {
        return if (toIndex > fromIndex) 1 else -1
    }
    return 1
}

private fun softEnter(direction: Int): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 40)) +
        slideInHorizontally(
            animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
            initialOffsetX = { fullWidth -> direction * fullWidth / 12 },
        )

private fun softExit(direction: Int): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = 140)) +
        slideOutHorizontally(
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            targetOffsetX = { fullWidth -> -direction * fullWidth / 18 },
        )
