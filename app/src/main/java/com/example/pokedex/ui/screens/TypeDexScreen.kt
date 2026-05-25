package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pokedex.ui.components.getTypeColor
import com.example.pokedex.viewmodel.ALL_POKEMON_TYPES
import com.example.pokedex.viewmodel.TypeViewModel
import androidx.compose.foundation.lazy.items

enum class TypeSelectorState { NONE, PRIMARY, SECONDARY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeDexScreen(
    onOpenMenu: () -> Unit,
    viewModel: TypeViewModel = viewModel()
) {
    var selectorState by remember { mutableStateOf(TypeSelectorState.NONE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Type Dex", color = Color(0xFF334F6A)) },
                navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, tint = Color(0xFF7B8E9C), contentDescription = "Menu") } },
                actions = { IconButton(onClick = { }) { Icon(Icons.Default.Settings, tint = Color(0xFF7B8E9C), contentDescription = "Settings") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF334F6A))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {

                // --- THANH CHỌN HỆ (Primary & Secondary) ---
                Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Cột 1
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if(viewModel.primaryType != null) getTypeColor(viewModel.primaryType!!.lowercase()) else Color(0xFFD6DFE8)).clickable { selectorState = TypeSelectorState.PRIMARY }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(viewModel.primaryType ?: "ALL TYPES", color = if(viewModel.primaryType != null) Color.White else Color(0xFF8B9CB6), fontWeight = FontWeight.Bold)
                        }
                        Text("Primary type", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                    // Cột 2
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if(viewModel.secondaryType != null) getTypeColor(viewModel.secondaryType!!.lowercase()) else Color(0xFFD6DFE8)).clickable { selectorState = TypeSelectorState.SECONDARY }.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(viewModel.secondaryType ?: "ALL TYPES", color = if(viewModel.secondaryType != null) Color.White else Color(0xFF8B9CB6), fontWeight = FontWeight.Bold)
                        }
                        Text("Secondary type", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- TRẠNG THÁI TRỐNG HOẶC ĐÃ CHỌN ---
                if (viewModel.primaryType == null && viewModel.secondaryType == null) {
                    EmptyStateCards()
                } else {
                    PopulatedDataCards(viewModel)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // --- BOTTOM SHEET CHỌN HỆ ---
    if (selectorState != TypeSelectorState.NONE) {
        ModalBottomSheet(onDismissRequest = { selectorState = TypeSelectorState.NONE }, containerColor = Color.White) {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                item {
                    Text("Select type", fontSize = 20.sp, color = Color(0xFF8B9CB6), modifier = Modifier.padding(bottom = 16.dp, top = 8.dp))

                    // Nút CLEAR (ALL TYPES)
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFD6DFE8)).clickable {
                        if (selectorState == TypeSelectorState.PRIMARY) viewModel.primaryType = null else viewModel.secondaryType = null
                        selectorState = TypeSelectorState.NONE
                    }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text("ALL TYPES", color = Color.Gray, fontWeight = FontWeight.Bold) }
                }

                items(ALL_POKEMON_TYPES) { type ->
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(getTypeColor(type.lowercase())).clickable {
                        if (selectorState == TypeSelectorState.PRIMARY) viewModel.primaryType = type else viewModel.secondaryType = type
                        selectorState = TypeSelectorState.NONE
                    }.padding(vertical = 12.dp), contentAlignment = Alignment.Center) { Text(type, color = Color.White, fontWeight = FontWeight.Bold) }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

// --- CÁC COMPOSABLE PHỤ TRỢ ---

@Composable
fun EmptyStateCards() {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Damage taken", fontSize = 20.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Text("Select a primary or/and secondary type\nto view damage relations.", color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Damage dealt: Primary type", fontSize = 20.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Text("Select a type to view its effectiveness\nagainst other types.", color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Damage dealt: Secondary type", fontSize = 20.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Text("Select a type to view its effectiveness\nagainst other types.", color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(32.dp))
        }
    }
}

@Composable
fun PopulatedDataCards(viewModel: TypeViewModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

        // --- 1. DAMAGE TAKEN (PHÒNG THỦ) ---
        Text("Damage taken", fontSize = 20.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val dmgMap = viewModel.calculateDamageTaken()

                // Hàm chia list và vẽ UI
                @Composable
                fun DrawSection(title: String, filterCondition: (Float) -> Boolean) {
                    val filtered = dmgMap.filterValues(filterCondition)
                    if (filtered.isNotEmpty()) {
                        Text(title, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                        filtered.keys.chunked(2).forEach { rowKeys ->
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowKeys.forEach { key -> TypeBadge(key, dmgMap[key]!!, Modifier.weight(1f)) }
                                if (rowKeys.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                DrawSection("Weak against...") { it > 1f }
                DrawSection("Resistant against...") { it < 1f && it > 0f }
                DrawSection("Immune to...") { it == 0f }
                DrawSection("Normal damage from...") { it == 1f }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. DAMAGE DEALT: PRIMARY TYPE ---
        if (viewModel.primaryType != null) {
            Text("Damage dealt: ${viewModel.primaryType}", fontSize = 20.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val atkMap = viewModel.getDamageDealt(viewModel.primaryType!!.uppercase())

                    @Composable
                    fun DrawAtkSection(title: String, filterCondition: (Float) -> Boolean) {
                        val filtered = atkMap.filterValues(filterCondition)
                        if (filtered.isNotEmpty()) {
                            Text(title, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                            filtered.keys.chunked(2).forEach { rowKeys ->
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowKeys.forEach { key -> TypeBadge(key, atkMap[key]!!, Modifier.weight(1f)) }
                                    if (rowKeys.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    DrawAtkSection("Strong against...") { it > 1f }
                    DrawAtkSection("Ineffective against...") { it < 1f }
                    DrawAtkSection("Normal damage to...") { it == 1f }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. DAMAGE DEALT: SECONDARY TYPE ---
        if (viewModel.secondaryType != null && viewModel.secondaryType != viewModel.primaryType) {
            Text("Damage dealt: ${viewModel.secondaryType}", fontSize = 20.sp, color = Color(0xFF334F6A), fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val atkMap = viewModel.getDamageDealt(viewModel.secondaryType!!.uppercase())

                    // Vẽ tương tự như Primary
                    @Composable
                    fun DrawAtkSection(title: String, filterCondition: (Float) -> Boolean) {
                        val filtered = atkMap.filterValues(filterCondition)
                        if (filtered.isNotEmpty()) {
                            Text(title, color = Color.Gray, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                            filtered.keys.chunked(2).forEach { rowKeys ->
                                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowKeys.forEach { key -> TypeBadge(key, atkMap[key]!!, Modifier.weight(1f)) }
                                    if (rowKeys.size == 1) Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    DrawAtkSection("Strong against...") { it > 1f }
                    DrawAtkSection("Ineffective against...") { it < 1f }
                    DrawAtkSection("Normal damage to...") { it == 1f }
                }
            }
        }
    }
}

// --- THIẾT KẾ THẺ BÀI UI (Type Badge) ---
@Composable
fun TypeBadge(typeName: String, multiplier: Float, modifier: Modifier = Modifier) {
    val bgColor = getTypeColor(typeName.lowercase())

    // Định dạng số nhân (VD: 0.5f -> x 1/2)
    val textMult = when (multiplier) {
        0.25f -> "× ¼"
        0.5f -> "× ½"
        0f -> "× 0"
        1f -> "× 1"
        2f -> "× 2"
        4f -> "× 4"
        else -> "× $multiplier"
    }

    Row(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(bgColor), verticalAlignment = Alignment.CenterVertically) {
        // Tên Hệ
        Text(typeName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(vertical = 8.dp), textAlign = TextAlign.Center)

        // Khung số nhân sẫm màu
        Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(textMult, color = Color.White, fontSize = 12.sp)
        }
    }
}