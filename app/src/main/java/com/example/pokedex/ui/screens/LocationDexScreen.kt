package com.example.pokedex.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pokedex.viewmodel.EncounterGroup
import com.example.pokedex.viewmodel.LocationUiModel
import com.example.pokedex.viewmodel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDexScreen(
    onOpenMenu: () -> Unit,
    viewModel: LocationViewModel = viewModel()
) {
    var isFilterOpen by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LocationUiModel?>(null) }

    // NẾU CÓ CHỌN ĐỊA ĐIỂM -> HIỂN THỊ MÀN HÌNH CHI TIẾT
    if (selectedLocation != null) {
        LocationDetailScreen(
            location = selectedLocation!!,
            viewModel = viewModel,
            onBack = { selectedLocation = null }
        )
        return
    }

    // MÀN HÌNH DANH SÁCH CHÍNH
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location Dex", color = Color(0xFF334F6A)) },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, tint = Color(0xFF7B8E9C), contentDescription = "Menu") }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Settings, tint = Color(0xFF7B8E9C), contentDescription = "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6F8))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isFilterOpen = true },
                containerColor = Color(0xFF334F6A),
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Default.FilterAlt, contentDescription = "Filter")
            }
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Thanh hiển thị version đang chọn (Scarlet / Violet) giống thiết kế
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (viewModel.selectedVersion == "Scarlet") Color(0xFFE62828) else Color.LightGray).padding(8.dp), contentAlignment = Alignment.Center) {
                    Text("Scarlet", color = if (viewModel.selectedVersion == "Scarlet") Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (viewModel.selectedVersion == "Violet") Color(0xFF5A2E8A) else Color.LightGray).padding(8.dp), contentAlignment = Alignment.Center) {
                    Text("Violet", color = if (viewModel.selectedVersion == "Violet") Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(viewModel.locationList) { location ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedLocation = location },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(location.name, fontSize = 18.sp, color = Color(0xFF334F6A))
                                Text(" (Biome) in ${location.region}", fontSize = 14.sp, color = Color.Gray)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                            Text("${location.pokemonCount} Pokémon", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET CHỌN GAME VERSION ---
    if (isFilterOpen) {
        ModalBottomSheet(onDismissRequest = { isFilterOpen = false }, containerColor = Color.White) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Select game version", fontSize = 20.sp, color = Color(0xFF6A7F9C), modifier = Modifier.padding(bottom = 16.dp))

                // Mẫu nhanh vài Gen (Tái sử dụng style của bạn)
                Text("Gen 8", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                VersionRow(listOf("Sword", "Shield"), { viewModel.setVersion(it); isFilterOpen = false })
                VersionRow(listOf("Brilliant Diamond", "Shining Pearl"), { viewModel.setVersion(it); isFilterOpen = false })
                VersionRow(listOf("Legends: Arceus"), { viewModel.setVersion(it); isFilterOpen = false })

                Text("Gen 9", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                VersionRow(listOf("Scarlet", "Violet"), { viewModel.setVersion(it); isFilterOpen = false })

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// =========================================================================
// MÀN HÌNH CHI TIẾT ĐỊA ĐIỂM (OVERWORLD, NIGHTTIME...)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(location: LocationUiModel, viewModel: LocationViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, tint = Color(0xFF334F6A), contentDescription = "Back") } },
                actions = { IconButton(onClick = { }) { Icon(Icons.Default.Settings, tint = Color(0xFF7B8E9C), contentDescription = "Settings") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {

            // Header
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(location.name, fontSize = 28.sp, color = Color(0xFF334F6A))
                    Text(" (Biome) ${location.region}", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, top = 6.dp))
                }

                // Cảnh báo vàng (Giống thiết kế)
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF9D1)).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Work on this location is still in progress.\nData may be incomplete and/or incorrect.", color = Color(0xFFB8860B), textAlign = TextAlign.Center, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Expand", tint = Color(0xFF334F6A))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Locations with this biome", color = Color(0xFF334F6A), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // LẶP QUA CÁC NHÓM ENCOUNTER (Overworld, Nighttime...)
            val dynamicEncounters = remember(location.id) {
                viewModel.getEncountersForLocation(location.id)
            }

            // LẶP QUA CÁC NHÓM ENCOUNTER (Overworld, Nighttime...)
            dynamicEncounters.forEach { group ->
                EncounterGroupSection(group)
            }
        }
    }
}

@Composable
fun EncounterGroupSection(group: EncounterGroup) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Tên nhóm (Overworld / Nighttime)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (group.icon == "moon") {
                Icon(Icons.Default.DarkMode, contentDescription = "Night", tint = Color(0xFF334F6A), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            } else if (group.icon == "water") {
                // Thêm icon giọt nước cho phần Câu cá
                Icon(Icons.Default.WaterDrop, contentDescription = "Water", tint = Color(0xFF334F6A), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(group.methodTitle, fontSize = 22.sp, color = Color(0xFF334F6A))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vẽ từng thẻ Pokémon
        group.encounters.forEach { encounter ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFC)),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Cột thông tin bên trái
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(encounter.pokemonName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A2E8A))
                            Text(encounter.levelRange, fontSize = 14.sp, color = Color(0xFF334F6A))
                            // Tỉ lệ % có khung nền nhạt
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(encounter.rateColor).copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(encounter.rate, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(encounter.rateColor))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tag Version (Scarlet / Violet)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            encounter.versions.forEach { version ->
                                val vColor = if (version == "Scarlet") Color(0xFFE62828) else Color(0xFF5A2E8A)
                                Box(
                                    modifier = Modifier.weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White)
                                        .border(1.dp, vColor.copy(alpha = 0.3f), RoundedCornerShape(50))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(version, fontSize = 12.sp, color = vColor)
                                }
                            }
                        }
                    }

                    // Ảnh Pokémon bên phải
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(
                        model = encounter.imageUrl,
                        contentDescription = encounter.pokemonName,
                        modifier = Modifier.size(70.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

// --- HÀM TÁI SỬ DỤNG CHO BẢNG FILTER ---
@Composable
fun VersionRow(versions: List<String>, onSelect: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        versions.forEach { version ->
            val bgColor = getVersionColor(version)
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(bgColor).clickable { onSelect(version) }.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val isLightColor = version.contains("White") || version == "Yellow" || version == "Let's Go Pikachu"
                Text(text = version, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isLightColor) Color.DarkGray else Color.White)
            }
        }
        if (versions.size == 1) Spacer(modifier = Modifier.weight(1f))
    }
}