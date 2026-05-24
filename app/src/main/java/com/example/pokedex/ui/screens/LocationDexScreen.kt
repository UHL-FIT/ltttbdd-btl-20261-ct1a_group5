package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
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
    var selectedLocation by remember { mutableStateOf<LocationUiModel?>(null) }
    var isVersionSheetOpen by remember { mutableStateOf(false) }

    // MÀN HÌNH CHI TIẾT
    if (selectedLocation != null) {
        LocationDetailScreen(location = selectedLocation!!, viewModel = viewModel, onBack = { selectedLocation = null })
        return
    }

    // MÀN HÌNH DANH SÁCH CHÍNH
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location Dex", color = Color(0xFF334F6A)) },
                navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, tint = Color(0xFF7B8E9C), contentDescription = "Menu") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6F8))
            )
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- NÚT BẤM CHỌN GAME VERSION Ở TRÊN CÙNG ---
            val currentVersionColor = getVersionColorLocation(viewModel.selectedVersion)
            val isLightText = viewModel.selectedVersion.contains("White") || viewModel.selectedVersion == "Yellow" || viewModel.selectedVersion.contains("Let's Go Pikachu")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(currentVersionColor)
                    .clickable { isVersionSheetOpen = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(viewModel.selectedVersion.uppercase(), color = if (isLightText) Color.DarkGray else Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // Lọc địa điểm theo Vùng đất (Region) của Game Version đang chọn
            val targetRegion = viewModel.getRegionForVersion(viewModel.selectedVersion)
            val filteredLocations = viewModel.allLocations.filter { it.region == targetRegion }

            if (filteredLocations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No locations found for this region.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredLocations) { location ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selectedLocation = location },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Place, contentDescription = "Location", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(location.name, fontSize = 18.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold)
                                }
                                Text(" (Biome) in ${location.region}", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))
                                Text("${location.pokemonCount} Pokémon", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET CHỌN VERSION (TỪ GEN 1 ĐẾN GEN 9) ---
    if (isVersionSheetOpen) {
        ModalBottomSheet(onDismissRequest = { isVersionSheetOpen = false }, containerColor = Color.White) {
            Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Select game version", fontSize = 20.sp, color = Color(0xFF6A7F9C), modifier = Modifier.padding(bottom = 16.dp))

                @Composable
                fun VRow(v1: String, v2: String? = null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(getVersionColorLocation(v1)).clickable { viewModel.setVersion(v1); isVersionSheetOpen = false }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(v1, color = if (v1.contains("White") || v1 == "Yellow") Color.DarkGray else Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (v2 != null) {
                            Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(getVersionColorLocation(v2)).clickable { viewModel.setVersion(v2); isVersionSheetOpen = false }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(v2, color = if (v2.contains("White") || v2 == "Yellow") Color.DarkGray else Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else { Spacer(modifier = Modifier.weight(1f)) }
                    }
                }

                Text("Gen 1", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Red", "Blue"); VRow("Yellow")
                Text("Gen 2", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Gold", "Silver"); VRow("Crystal")
                Text("Gen 3", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Ruby", "Sapphire"); VRow("Emerald"); VRow("FireRed", "LeafGreen")
                Text("Gen 4", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Diamond", "Pearl"); VRow("Platinum"); VRow("HeartGold", "SoulSilver")
                Text("Gen 5", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Black", "White"); VRow("Black 2", "White 2")
                Text("Gen 6", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("X", "Y"); VRow("Omega Ruby", "Alpha Sapphire")
                Text("Gen 7", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Sun", "Moon"); VRow("Ultra Sun", "Ultra Moon")
                Text("Gen 8", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Sword", "Shield"); VRow("Brilliant Diamond", "Shining Pearl"); VRow("Legends: Arceus")
                Text("Gen 9", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)); VRow("Scarlet", "Violet")

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// =========================================================================
// MÀN HÌNH CHI TIẾT
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(location: LocationUiModel, viewModel: LocationViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, tint = Color(0xFF334F6A), contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(location.name, fontSize = 28.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold)
                Text(" (Biome) in ${location.region}", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF9D1)).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Encounters are dynamically generated based on biome.", color = Color(0xFFB8860B), textAlign = TextAlign.Center, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val dynamicEncounters = remember(location.id) { viewModel.getEncountersForLocation(location.id) }
            dynamicEncounters.forEach { group -> EncounterGroupSection(group) }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun EncounterGroupSection(group: EncounterGroup) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (group.icon == "moon") { Icon(Icons.Default.DarkMode, contentDescription = "Night", tint = Color(0xFF334F6A), modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)) }
            else if (group.icon == "water") { Icon(Icons.Default.WaterDrop, contentDescription = "Water", tint = Color(0xFF334F6A), modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(8.dp)) }
            Text(group.methodTitle, fontSize = 22.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold)
        }
        if (group.methodDescription.isNotEmpty()) { Text(group.methodDescription, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp), textAlign = TextAlign.Center) }

        Spacer(modifier = Modifier.height(16.dp))

        group.encounters.forEach { encounter ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(encounter.typeColor)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(encounter.pokemonName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(encounter.levelRange, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                                Text(encounter.rate, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            encounter.versions.forEach { version ->
                                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.25f)).padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                    Text(version, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(model = encounter.imageUrl, contentDescription = encounter.pokemonName, modifier = Modifier.size(70.dp), contentScale = ContentScale.Fit)
                }
            }
        }
    }
}

// Bảng màu cho Game Version (Tích hợp sẵn để tránh lỗi thiếu hàm)
fun getVersionColorLocation(version: String): Color {
    return when (version) {
        "Red", "FireRed" -> Color(0xFFE3350D)
        "Blue" -> Color(0xFF3165BC)
        "Yellow" -> Color(0xFFF6D830)
        "Gold", "HeartGold" -> Color(0xFFDAA520)
        "Silver", "SoulSilver" -> Color(0xFFC0C0C0)
        "Crystal" -> Color(0xFF4DD0E1)
        "Ruby", "Omega Ruby" -> Color(0xFFA00000)
        "Sapphire", "Alpha Sapphire" -> Color(0xFF0000A0)
        "Emerald" -> Color(0xFF008000)
        "LeafGreen" -> Color(0xFF32CD32)
        "Diamond", "Brilliant Diamond" -> Color(0xFFAAAADD)
        "Pearl", "Shining Pearl" -> Color(0xFFFFAABB)
        "Platinum" -> Color(0xFF999999)
        "Black", "Black 2" -> Color(0xFF444444)
        "White", "White 2" -> Color(0xFFE3E3E3)
        "X" -> Color(0xFF0055A4)
        "Y" -> Color(0xFFEF4135)
        "Sun", "Ultra Sun" -> Color(0xFFF18E38)
        "Moon", "Ultra Moon" -> Color(0xFF5394CE)
        "Sword" -> Color(0xFF00A2E8)
        "Shield" -> Color(0xFFED1C24)
        "Scarlet" -> Color(0xFFE62828)
        "Violet" -> Color(0xFF5A2E8A)
        "Legends: Arceus" -> Color(0xFF2A595C)
        else -> Color(0xFF334F6A)
    }
}