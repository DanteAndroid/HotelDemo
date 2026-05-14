@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.danteandroid.hoteldemo.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.danteandroid.hoteldemo.data.model.Room
import com.danteandroid.hoteldemo.navigation.Screen

@Composable
fun RoomDetailScreen(navController: NavController, roomId: String) {
    val vm: RoomDetailViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = RoomDetailViewModel(roomId) as T
    })
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("房间详情") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            if (uiState is RoomDetailUiState.Success) {
                val room = (uiState as RoomDetailUiState.Success).room
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 3.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("¥${room.pricePerNight.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                            Text("/ 晚", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { navController.navigate(Screen.Booking.createRoute(roomId)) },
                            modifier = Modifier.height(48.dp),
                            shape = MaterialTheme.shapes.large,
                        ) {
                            Text("立即预订", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is RoomDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is RoomDetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is RoomDetailUiState.Success -> RoomDetailContent(state.room, Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun RoomDetailContent(room: Room, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box {
            AsyncImage(
                model = room.imageUrl ?: "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800",
                contentDescription = room.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(300.dp),
            )
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.92f),
                tonalElevation = 1.dp,
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(room.name, style = MaterialTheme.typography.titleLarge)
                    Text(room.type, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            // 基本信息
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    room.floor?.let { SpecItem(icon = { Icon(Icons.Outlined.Layers, null, Modifier.size(20.dp)) }, label = "楼层", value = "${it}层") }
                    room.sizeSqm?.let { SpecItem(icon = { Icon(Icons.Outlined.SquareFoot, null, Modifier.size(20.dp)) }, label = "面积", value = "${it}㎡") }
                    SpecItem(icon = { Icon(Icons.Outlined.People, null, Modifier.size(20.dp)) }, label = "入住人数", value = "${room.capacity}人")
                }
            }

            // 房间设施
            if (room.amenities.isNotEmpty()) {
                Text("房间设施", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    room.amenities.forEach { amenity ->
                        AssistChip(
                            onClick = {},
                            label = { Text(amenity) },
                            leadingIcon = { Icon(Icons.Outlined.CheckCircle, null, Modifier.size(16.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                leadingIconContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            border = null,
                        )
                    }
                }
            }

            // 房间描述
            if (room.description.isNotBlank()) {
                Text("房间介绍", style = MaterialTheme.typography.titleMedium)
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Text(
                        room.description,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                    )
                }
            }

            // 底部留白，避免被 bottomBar 遮挡
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SpecItem(icon: @Composable () -> Unit, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            icon()
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
