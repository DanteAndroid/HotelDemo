@file:OptIn(ExperimentalMaterial3Api::class)

package com.danteandroid.hoteldemo.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.danteandroid.hoteldemo.data.model.Room
import com.danteandroid.hoteldemo.navigation.Screen

@Composable
fun BookingScreen(navController: NavController, roomId: String) {
    val vm: BookingViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = BookingViewModel(roomId) as T
    })
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val room by vm.room.collectAsStateWithLifecycle()

    // 预订成功后跳转
    LaunchedEffect(uiState) {
        if (uiState is BookingUiState.Success) {
            val code = (uiState as BookingUiState.Success).bookingCode
            navController.navigate(Screen.BookingSuccess.createRoute(code)) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    var showCheckInPicker by remember { mutableStateOf(false) }
    var showCheckOutPicker by remember { mutableStateOf(false) }
    val checkInState = rememberDatePickerState()
    val checkOutState = rememberDatePickerState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("填写预订信息") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 房间摘要卡片
            RoomSummaryCard(room = room)

            Text("入住日期", style = MaterialTheme.typography.titleMedium)

            // 入住日期选择
            FilledTonalButton(
                onClick = { showCheckInPicker = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (vm.checkInDateMillis > 0) "入住：${vm.checkInDateMillis.toDisplayDate()}" else "选择入住日期")
            }

            // 退房日期选择
            FilledTonalButton(
                onClick = { showCheckOutPicker = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (vm.checkOutDateMillis > 0) "退房：${vm.checkOutDateMillis.toDisplayDate()}" else "选择退房日期")
            }

            // 入住天数统计
            if (vm.nights > 0) {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("${vm.nights} 晚", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text("合计 ¥${vm.totalPrice.toInt()}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Person, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Text("入住人信息", style = MaterialTheme.typography.titleMedium)
            }

            OutlinedTextField(
                value = vm.guestName,
                onValueChange = { vm.guestName = it },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
            )
            OutlinedTextField(
                value = vm.guestPhone,
                onValueChange = { vm.guestPhone = it },
                label = { Text("手机号码") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
            )
            OutlinedTextField(
                value = vm.guestIdNumber,
                onValueChange = { vm.guestIdNumber = it },
                label = { Text("身份证号") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
            )
            OutlinedTextField(
                value = vm.specialRequests,
                onValueChange = { vm.specialRequests = it },
                label = { Text("特殊需求（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                minLines = 2,
                maxLines = 4,
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { vm.submitBooking() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                enabled = uiState !is BookingUiState.Loading,
            ) {
                if (uiState is BookingUiState.Loading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("确认预订", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // 错误弹窗
    if (uiState is BookingUiState.Error) {
        AlertDialog(
            onDismissRequest = { vm.clearError() },
            title = { Text("提示") },
            text = { Text((uiState as BookingUiState.Error).message) },
            confirmButton = { TextButton(onClick = { vm.clearError() }) { Text("知道了") } },
        )
    }

    // 入住日期选择器
    if (showCheckInPicker) {
        DatePickerDialog(
            onDismissRequest = { showCheckInPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    checkInState.selectedDateMillis?.let { vm.checkInDateMillis = it }
                    showCheckInPicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showCheckInPicker = false }) { Text("取消") } },
        ) {
            DatePicker(state = checkInState)
        }
    }

    // 退房日期选择器
    if (showCheckOutPicker) {
        DatePickerDialog(
            onDismissRequest = { showCheckOutPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    checkOutState.selectedDateMillis?.let { vm.checkOutDateMillis = it }
                    showCheckOutPicker = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showCheckOutPicker = false }) { Text("取消") } },
        ) {
            DatePicker(state = checkOutState)
        }
    }
}

@Composable
private fun RoomSummaryCard(room: Room?) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        // Reserve a stable height so the page doesn't jump when room loads.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 84.dp)
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (room == null) {
                    PlaceholderLine(widthFraction = 0.55f, height = 18.dp)
                    PlaceholderLine(widthFraction = 0.30f, height = 14.dp)
                } else {
                    Text(
                        room.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        room.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                }
            }

            if (room == null) {
                PlaceholderLine(widthFraction = 0.22f, height = 18.dp)
            } else {
                Text(
                    "¥${room.pricePerNight.toInt()}/晚",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun PlaceholderLine(widthFraction: Float, height: Dp) {
    val base = MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .background(base.copy(alpha = 0.18f)),
    )
}
