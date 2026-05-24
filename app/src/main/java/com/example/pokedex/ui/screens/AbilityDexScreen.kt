package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pokedex.ui.components.getTypeColor
import com.example.pokedex.viewmodel.AbilityUiModel
import com.example.pokedex.viewmodel.AbilityUiState
import com.example.pokedex.viewmodel.AbilityViewModel

enum class AbilitySheetType { NONE, GEN, DETAIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityDexScreen(
    onOpenMenu: () -> Unit,
    viewModel: AbilityViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var activeSheet by remember { mutableStateOf(AbilitySheetType.NONE) }
    var selectedAbility by remember { mutableStateOf<AbilityUiModel?>(null) }

    Scaffold(
        topBar = {
            if (viewModel.isSearchActive) {
                // --- THANH TÌM KIẾM KHI BẤM BONG BÓNG ---
                TopAppBar(
                    title = {
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = {
                                viewModel.searchQuery = it
                                viewModel.applyFilters()
                            },
                            placeholder = { Text("Enter ability name...", color = Color.Gray, fontSize = 18.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.isSearchActive = false
                            viewModel.searchQuery = ""
                            viewModel.applyFilters()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF334F6A))
                        }
                    },
                    actions = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                viewModel.searchQuery = ""
                                viewModel.applyFilters()
                            }) { Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray) }
                        } else {
                            IconButton(onClick = { }) { Icon(Icons.Default.Settings, tint = Color(0xFF334F6A), contentDescription = "Settings") }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            } else {
                // --- THANH TOP BAR MẶC ĐỊNH ---
                TopAppBar(
                    title = { Text("Ability Dex", color = Color(0xFF334F6A)) },
                    navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, tint = Color(0xFF7B8E9C), contentDescription = "Menu") } },
                    actions = { IconButton(onClick = { }) { Icon(Icons.Default.Settings, tint = Color(0xFF7B8E9C), contentDescription = "Settings") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6F8))
                )
            }
        },
        floatingActionButton = {
            // --- BONG BÓNG MỞ TÌM KIẾM (Chỉ hiện khi chưa tìm kiếm) ---
            if (!viewModel.isSearchActive) {
                FloatingActionButton(
                    onClick = { viewModel.isSearchActive = true },
                    containerColor = Color(0xFF334F6A),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.FilterAlt, contentDescription = "Search/Filter") // Dùng Icon Phễu giống thiết kế của bạn
                }
            }
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- NÚT BỘ LỌC GEN Ở TRÊN CÙNG ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD6DFE8))
                    .clickable { activeSheet = AbilitySheetType.GEN }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(viewModel.selectedGen, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B9CB6))
            }

            // --- DANH SÁCH ABILITY ---
            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (uiState) {
                    is AbilityUiState.Loading -> { CircularProgressIndicator(color = Color(0xFF334F6A), modifier = Modifier.align(Alignment.Center)) }
                    is AbilityUiState.Error -> { Text("Error loading abilities", color = Color.Red, modifier = Modifier.align(Alignment.Center)) }
                    is AbilityUiState.Success -> {
                        val abilities = uiState.abilities
                        if (abilities.isEmpty()) {
                            Text("No abilities found...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(abilities) { ability ->
                                    AbilityCard(ability) {
                                        selectedAbility = ability
                                        activeSheet = AbilitySheetType.DETAIL
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // QUẢN LÝ BOTTOM SHEETS ĐA NĂNG
    // =========================================================
    if (activeSheet != AbilitySheetType.NONE) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = AbilitySheetType.NONE },
            containerColor = if (activeSheet == AbilitySheetType.DETAIL) Color(0xFFF4F6F8) else Color.White,
            dragHandle = null
        ) {
            when (activeSheet) {
                AbilitySheetType.GEN -> AbilityGenFilterSheet(
                    currentSelected = viewModel.selectedGen,
                    onSelect = {
                        viewModel.selectedGen = it
                        viewModel.applyFilters()
                        activeSheet = AbilitySheetType.NONE
                    }
                )
                AbilitySheetType.DETAIL -> selectedAbility?.let { AbilityDetailSheet(it) }
                else -> {}
            }
        }
    }
}

// --- THẺ BÀI DANH SÁCH ABILITY ---
@Composable
fun AbilityCard(ability: AbilityUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Info, contentDescription = "Info", tint = Color(0xFFD6DFE8), modifier = Modifier.align(Alignment.CenterStart).size(20.dp))
                Text(ability.name, fontSize = 18.sp, color = Color(0xFF334F6A), modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(ability.shortEffect, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// --- SHEET LỌC GEN ---
@Composable
fun AbilityGenFilterSheet(currentSelected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select generation", fontSize = 20.sp, color = Color(0xFF8B9CB6), modifier = Modifier.padding(bottom = 16.dp, top = 8.dp))

        @Composable
        fun GenBtn(text: String) {
            val isSelected = currentSelected == text
            val bgColor = if (isSelected) Color(0xFF334F6A) else Color(0xFF8B9CB6)
            val realBgColor = if (text == "ALL GENS" && !isSelected) Color(0xFFD6DFE8) else bgColor
            val textColor = if (text == "ALL GENS" && !isSelected) Color.Gray else Color.White

            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(realBgColor).clickable { onSelect(text) }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Text(text, fontSize = 14.sp, color = textColor)
            }
        }

        GenBtn("ALL GENS")
        val gens = listOf("GEN 3", "GEN 4", "GEN 5", "GEN 6", "GEN 7", "GEN 8", "GEN 9")
        gens.forEach { GenBtn(it) }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- SHEET CHI TIẾT ABILITY (CHUẨN THIẾT KẾ CỦA BẠN) ---
@Composable
fun AbilityDetailSheet(ability: AbilityUiModel) {
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {

        // Header Xanh đậm
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF5A7E9A)).padding(top = 24.dp, bottom = 24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(ability.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Ability", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {

            // Các Card Nội dung
            InfoTextCard("GAME DESCRIPTION", ability.gameDescription)
            Spacer(modifier = Modifier.height(16.dp))
            InfoTextCard("EFFECT", ability.effect)
            Spacer(modifier = Modifier.height(16.dp))
            InfoTextCard("IN-DEPTH EFFECT", ability.inDepthEffect)

            Spacer(modifier = Modifier.height(32.dp))

            // Tiêu đề danh sách Pokemon
            Text("POKÉMON WITH THIS ABILITY", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7E9A), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))

            // Danh sách Pokemon nằm ngang với màu nền theo Hệ (Type)
            ability.pokemonList.forEach { poke ->
                val primaryTypeColor = getTypeColor(poke.types.first().lowercase())

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).height(80.dp),
                    colors = CardDefaults.cardColors(containerColor = primaryTypeColor.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {

                        // Cột trái: ID, Tên, Hệ
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${poke.id.toString().padStart(3, '0')}", fontSize = 14.sp, color = Color.Black.copy(alpha = 0.4f), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(poke.name, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                poke.types.forEach { type ->
                                    Box(modifier = Modifier.clip(RoundedCornerShape(50)).border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 4.dp), contentAlignment = Alignment.Center) {
                                        Text(type, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Cột phải: Hình ảnh Pokemon lồng trong vòng tròn mờ
                        Box(modifier = Modifier.fillMaxHeight().width(100.dp), contentAlignment = Alignment.Center) {
                            Box(modifier = Modifier.size(70.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f)))
                            AsyncImage(model = poke.imageUrl, contentDescription = poke.name, modifier = Modifier.size(80.dp).offset(x = 10.dp), contentScale = ContentScale.Fit)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun InfoTextCard(title: String, content: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7E9A))
            Spacer(modifier = Modifier.height(12.dp))
            Text(content, fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
        }
    }
}