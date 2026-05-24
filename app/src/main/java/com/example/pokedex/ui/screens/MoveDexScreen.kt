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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pokedex.ui.components.getTypeColor
import com.example.pokedex.viewmodel.MoveUiModel
import com.example.pokedex.viewmodel.MoveUiState
import com.example.pokedex.viewmodel.MoveViewModel

// Enum quản lý xem Bottom Sheet nào đang được mở
enum class MoveSheetType { NONE, GEN, TYPE, CATEGORY, DETAIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveDexScreen(
    onOpenMenu: () -> Unit,
    viewModel: MoveViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var activeSheet by remember { mutableStateOf(MoveSheetType.NONE) }
    var selectedMove by remember { mutableStateOf<MoveUiModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Move Dex", color = Color(0xFF334F6A)) },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, tint = Color(0xFF7B8E9C), contentDescription = "Menu") }
                },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Settings, tint = Color(0xFF7B8E9C), contentDescription = "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6F8))
            )
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- 1. THANH BỘ LỌC (GEN, TYPE, CATEGORY) ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterButton(text = viewModel.selectedGen, modifier = Modifier.weight(1f)) { activeSheet = MoveSheetType.GEN }
                FilterButton(text = viewModel.selectedType, modifier = Modifier.weight(1f)) { activeSheet = MoveSheetType.TYPE }
                FilterButton(text = viewModel.selectedCategory, modifier = Modifier.weight(1f)) { activeSheet = MoveSheetType.CATEGORY }
            }

            // --- 2. TIÊU ĐỀ CỘT CHO DANH SÁCH ---
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF8B9CB6).copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Move", color = Color(0xFF8B9CB6), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Power", color = Color(0xFF8B9CB6), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                Text("Acc.", color = Color(0xFF8B9CB6), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                Text("PP", color = Color(0xFF8B9CB6), fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
            }

            // --- 3. DANH SÁCH KỸ NĂNG ---
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState) {
                    is MoveUiState.Loading -> { CircularProgressIndicator(color = Color(0xFF334F6A), modifier = Modifier.align(Alignment.Center)) }
                    is MoveUiState.Error -> { Text("Error loading moves", color = Color.Red, modifier = Modifier.align(Alignment.Center)) }
                    is MoveUiState.Success -> {
                        val moves = uiState.moves
                        if (moves.isEmpty()) {
                            Text("No moves found...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(moves) { move ->
                                    MoveListItem(
                                        move = move,
                                        onClick = {
                                            selectedMove = move
                                            activeSheet = MoveSheetType.DETAIL
                                        }
                                    )
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
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
    if (activeSheet != MoveSheetType.NONE) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = MoveSheetType.NONE },
            containerColor = Color(0xFFF4F6F8),
            dragHandle = null // Ẩn thanh kéo để UI giống concept hơn
        ) {
            when (activeSheet) {
                MoveSheetType.GEN -> GenFilterSheet(
                    currentSelected = viewModel.selectedGen,
                    onSelect = { viewModel.selectedGen = it; viewModel.applyFilters(); activeSheet = MoveSheetType.NONE }
                )
                MoveSheetType.TYPE -> TypeFilterSheet(
                    currentSelected = viewModel.selectedType,
                    onSelect = { viewModel.selectedType = it; viewModel.applyFilters(); activeSheet = MoveSheetType.NONE }
                )
                MoveSheetType.CATEGORY -> CategoryFilterSheet(
                    currentSelected = viewModel.selectedCategory,
                    onSelect = { viewModel.selectedCategory = it; viewModel.applyFilters(); activeSheet = MoveSheetType.NONE }
                )
                MoveSheetType.DETAIL -> selectedMove?.let { MoveDetailSheet(it) }
                else -> {}
            }
        }
    }
}

// --- ITEM DANH SÁCH ---
@Composable
fun MoveListItem(move: MoveUiModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(move.name, fontSize = 16.sp, color = Color(0xFF334F6A), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(move.power, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
            Text(move.accuracy, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
            Text(move.pp, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            // Nút "Type | [Tên Hệ]"
            Row(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(getTypeColor(move.type.lowercase())).weight(1.5f)) {
                Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Type", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Box(modifier = Modifier.weight(1f).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                    Text(move.type, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nút "Category"
            Box(
                modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(getCategoryColor(move.category)).weight(1f).padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(move.category, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

// --- CÁC HÀM GIAO DIỆN PHỤ TRỢ ---
@Composable
fun FilterButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isActive = text != "ALL GENS" && text != "ALL TYPES" && text != "ALL CAT."
    val bgColor = if (isActive) Color(0xFF334F6A) else Color(0xFFE5E9EC)
    val textColor = if (isActive) Color.White else Color(0xFF8B9CB6)

    Box(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(bgColor).clickable { onClick() }.padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun SheetButton(text: String, bgColor: Color, textColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(bgColor).clickable { onClick() }.padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// --- 3 SHEET LỌC (GEN, TYPE, CATEGORY) ---
@Composable
fun GenFilterSheet(currentSelected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select generation", fontSize = 20.sp, color = Color(0xFF8B9CB6), modifier = Modifier.padding(bottom = 16.dp, top = 8.dp))

        SheetButton("ALL GENS", if(currentSelected=="ALL GENS") Color(0xFF8B9CB6) else Color(0xFFD6DFE8), if(currentSelected=="ALL GENS") Color.White else Color.Gray) { onSelect("ALL GENS") }

        val gens = listOf("GEN 1", "GEN 2", "GEN 3", "GEN 4", "GEN 5", "GEN 6", "GEN 7", "GEN 8", "GEN 9")
        gens.forEach { gen ->
            val isSelected = currentSelected == gen
            SheetButton(gen, if(isSelected) Color(0xFF334F6A) else Color(0xFF8B9CB6), Color.White) { onSelect(gen) }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun TypeFilterSheet(currentSelected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select type", fontSize = 20.sp, color = Color(0xFF8B9CB6), modifier = Modifier.padding(bottom = 16.dp, top = 8.dp))
        SheetButton("ALL TYPES", if(currentSelected=="ALL TYPES") Color(0xFF8B9CB6) else Color(0xFFD6DFE8), if(currentSelected=="ALL TYPES") Color.White else Color.Gray) { onSelect("ALL TYPES") }

        val types = listOf("NORMAL", "FIGHTING", "FLYING", "POISON", "GROUND", "ROCK", "BUG", "GHOST", "STEEL", "FIRE", "WATER", "GRASS", "ELECTRIC", "PSYCHIC", "ICE", "DRAGON", "DARK", "FAIRY")
        types.forEach { type ->
            SheetButton(type, getTypeColor(type.lowercase()), Color.White) { onSelect(type) }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CategoryFilterSheet(currentSelected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Select category", fontSize = 20.sp, color = Color(0xFF8B9CB6), modifier = Modifier.padding(bottom = 16.dp, top = 8.dp))
        SheetButton("ALL CAT.", if(currentSelected=="ALL CAT.") Color(0xFF8B9CB6) else Color(0xFFD6DFE8), if(currentSelected=="ALL CAT.") Color.White else Color.Gray) { onSelect("ALL CAT.") }
        SheetButton("STATUS", getCategoryColor("STATUS"), Color.White) { onSelect("STATUS") }
        SheetButton("PHYSICAL", getCategoryColor("PHYSICAL"), Color.White) { onSelect("PHYSICAL") }
        SheetButton("SPECIAL", getCategoryColor("SPECIAL"), Color.White) { onSelect("SPECIAL") }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- SHEET CHI TIẾT MOVE ---
@Composable
fun MoveDetailSheet(move: MoveUiModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Nền xanh header
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF5A7E9A)).padding(top = 24.dp, bottom = 24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(move.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Move", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            // Type & Category
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(getTypeColor(move.type.lowercase())).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(move.type, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Type", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(getCategoryColor(move.category)).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(move.category, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontStyle = FontStyle.Italic)
                        Text("Category", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(move.power, fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    Text("Power", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(move.accuracy, fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    Text("Accuracy", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(move.pp, fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    Text("PP", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Description Cards
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GAME DESCRIPTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7E9A))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(move.gameDescription, fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EFFECT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7E9A))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(move.effect, fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun getCategoryColor(category: String): Color {
    return when (category.uppercase()) {
        "PHYSICAL" -> Color(0xFFC22E28) // Đỏ
        "SPECIAL" -> Color(0xFF334F6A)  // Xanh dương đậm
        "STATUS" -> Color(0xFF8B9CB6)   // Xám tím
        else -> Color.Gray
    }
}