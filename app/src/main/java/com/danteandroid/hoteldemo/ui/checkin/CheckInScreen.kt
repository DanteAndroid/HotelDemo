package com.danteandroid.hoteldemo.ui.checkin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RoomService
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.danteandroid.hoteldemo.data.model.Booking
import com.danteandroid.hoteldemo.data.model.BookingStatus
import com.danteandroid.hoteldemo.navigation.Screen
import com.danteandroid.hoteldemo.ui.orders.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    navController: NavController,
    initialBookingCode: String? = null,
    vm: CheckInViewModel = viewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val autoReturnHandled = remember { mutableStateOf(false) }

    LaunchedEffect(initialBookingCode) {
        val code = initialBookingCode?.trim().orEmpty()
        if (code.isNotBlank() && vm.bookingCode.isBlank()) {
            vm.bookingCode = code
            vm.searchBooking()
        }
    }

    LaunchedEffect(uiState, initialBookingCode) {
        val code = initialBookingCode?.trim().orEmpty()
        if (!autoReturnHandled.value && code.isNotBlank() && uiState is CheckInUiState.CheckedIn) {
            autoReturnHandled.value = true
            navController.previousBackStackEntry?.savedStateHandle?.set("orders_reset", true)
            navController.navigate(Screen.Orders.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("办理入住") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 说明文字
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Row(modifier = Modifier.padding(18.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.RoomService, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "请输入预订时获得的订单号，核实信息后即可办理入住。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 搜索区
            OutlinedTextField(
                value = vm.bookingCode,
                onValueChange = { vm.bookingCode = it.uppercase() },
                label = { Text("订单号") },
                placeholder = { Text("例：A1B2C3D4") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                enabled = uiState is CheckInUiState.Initial || uiState is CheckInUiState.NotFound || uiState is CheckInUiState.Error,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    keyboardController?.hide()
                    vm.searchBooking()
                }),
                trailingIcon = { Icon(Icons.Outlined.Search, null) },
            )

            Button(
                onClick = {
                    keyboardController?.hide()
                    when (uiState) {
                        is CheckInUiState.CheckedIn -> vm.reset()
                        else -> vm.searchBooking()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                enabled = uiState !is CheckInUiState.Searching && uiState !is CheckInUiState.ProcessingCheckIn,
            ) {
                Text(if (uiState is CheckInUiState.CheckedIn) "重新查询" else "查询订单",
                    style = MaterialTheme.typography.titleMedium)
            }

            // 状态区域
            when (val state = uiState) {
                is CheckInUiState.Initial -> { /* 等待输入 */ }
                is CheckInUiState.Searching, is CheckInUiState.ProcessingCheckIn -> {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator()
                            Text(if (state is CheckInUiState.Searching) "正在查询..." else "正在办理入住...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                is CheckInUiState.NotFound -> {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Text(state.message,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center)
                    }
                }
                is CheckInUiState.Error -> {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    ) {
                        Text("错误：${state.message}",
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                is CheckInUiState.Found -> BookingFoundCard(state.booking, onConfirm = { vm.confirmCheckIn() })
                is CheckInUiState.CheckedIn -> CheckInSuccessCard(state.booking)
            }
        }
    }
}

@Composable
private fun BookingFoundCard(booking: Booking, onConfirm: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("找到订单", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            BookingInfoRow("订单号", booking.bookingCode)
            BookingInfoRow("房间", "${booking.roomName} · ${booking.roomType}")
            BookingInfoRow("入住人", booking.guestName)
            BookingInfoRow("手机号", booking.guestPhone)
            BookingInfoRow("入住日期", booking.checkInDate)
            BookingInfoRow("退房日期", booking.checkOutDate)
            booking.nights?.let { BookingInfoRow("入住晚数", "${it}晚") }
            BookingInfoRow("状态", "") {
                StatusChip(BookingStatus.fromValue(booking.status))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.large) {
                Text("确认办理入住", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun CheckInSuccessCard(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.CheckCircle, null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text("入住办理成功！",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("欢迎入住 ${booking.roomName}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center)
            Text("祝您入住愉快",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun BookingInfoRow(label: String, value: String, trailingContent: @Composable (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (trailingContent != null) trailingContent()
        else Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
