package com.danteandroid.hoteldemo.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.danteandroid.hoteldemo.data.model.Booking
import com.danteandroid.hoteldemo.data.model.BookingStatus
import com.danteandroid.hoteldemo.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(navController: NavController, vm: OrdersViewModel = viewModel()) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("我的订单") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SearchMode.entries.forEach { mode ->
                            FilterChip(
                                selected = vm.searchMode == mode,
                                onClick = { vm.searchMode = mode; vm.searchQuery = "" },
                                label = { Text(if (mode == SearchMode.BY_PHONE) "手机号查询" else "订单号查询") },
                                shape = MaterialTheme.shapes.large,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }

                    OutlinedTextField(
                        value = vm.searchQuery,
                        onValueChange = { vm.searchQuery = it },
                        label = { Text(if (vm.searchMode == SearchMode.BY_PHONE) "输入手机号" else "输入订单号") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (vm.searchMode == SearchMode.BY_PHONE) KeyboardType.Phone else KeyboardType.Text,
                            imeAction = ImeAction.Search,
                        ),
                        keyboardActions = KeyboardActions(onSearch = {
                            keyboardController?.hide()
                            vm.search()
                        }),
                        trailingIcon = {
                            IconButton(onClick = {
                                keyboardController?.hide()
                                vm.search()
                            }) {
                                Icon(Icons.Outlined.Search, contentDescription = "搜索")
                            }
                        },
                    )

                    Button(
                        onClick = { keyboardController?.hide(); vm.search() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = MaterialTheme.shapes.large,
                        enabled = vm.searchQuery.isNotBlank() && uiState !is OrdersUiState.Loading,
                    ) {
                        Text("查询")
                    }
                }
            }

            when (val state = uiState) {
                is OrdersUiState.Initial -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyOrdersState("输入手机号或订单号查询您的订单")
                    }
                }
                is OrdersUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is OrdersUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is OrdersUiState.Success -> {
                    Text("共找到 ${state.bookings.size} 条订单", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                        items(state.bookings) { booking ->
                            BookingCard(
                                booking = booking,
                                onCheckIn = { code ->
                                    navController.navigate(Screen.CheckIn.createRoute(code))
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking, onCheckIn: (String) -> Unit) {
    val status = BookingStatus.fromValue(booking.status)
    val canCheckIn = status == BookingStatus.CONFIRMED || status == BookingStatus.PENDING
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(booking.roomName.ifBlank { "房间" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                StatusChip(status)
            }
            Text(
                "订单号：${booking.bookingCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("入住", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(booking.checkInDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("晚数", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${booking.nights ?: "–"} 晚", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("退房", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(booking.checkOutDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("入住人：${booking.guestName}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "¥${booking.totalPrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            if (canCheckIn) {
                FilledTonalButton(
                    onClick = { onCheckIn(booking.bookingCode) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(Icons.Outlined.MeetingRoom, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("办理入住", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val (containerColor, contentColor) = when (status) {
        BookingStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        BookingStatus.CHECKED_IN -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        BookingStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        BookingStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        BookingStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(status.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyOrdersState(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.AutoMirrored.Outlined.ReceiptLong, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}
