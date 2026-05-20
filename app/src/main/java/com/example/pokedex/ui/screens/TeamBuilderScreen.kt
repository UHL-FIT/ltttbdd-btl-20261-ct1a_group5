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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pokedex.viewmodel.PokemonUiState
import com.example.pokedex.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamBuilderScreen(
    onOpenMenu: () -> Unit,
    viewModel: PokemonViewModel = viewModel()
) {
    var isSelectingPokemon by remember { mutableStateOf(false) }
    var teamName by remember { mutableStateOf("") }
    var selectedTeam by remember { mutableStateOf(listOf<PokemonUiModel>()) }
    var searchQuery by remember { mutableStateOf("") }

    val uiState = viewModel.uiState
    val allPokemon = if (uiState is PokemonUiState.Success) uiState.pokemonList else emptyList()

    val filteredPokemon = allPokemon.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.id.toString() == searchQuery
    }

    if (isSelectingPokemon) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Pokémon", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { isSelectingPokemon = false; searchQuery = "" }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name and number") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                LazyColumn {
                    items(filteredPokemon) { pokemon ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedTeam.size < 6 && !selectedTeam.contains(pokemon)) {
                                        selectedTeam = selectedTeam + pokemon
                                    }
                                    isSelectingPokemon = false
                                    searchQuery = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = pokemon.imageUrl,
                                contentDescription = pokemon.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0F0F0))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "#${pokemon.id.toString().padStart(3, '0')}   ${pokemon.name}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    pokemon.types.forEach { type ->
                                        Text(
                                            text = type.uppercase(),
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.DarkGray).padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }
                            }

                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Fav", tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))

                            if (selectedTeam.contains(pokemon)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color.Green)
                            } else {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = "Select", tint = Color.LightGray)
                            }
                        }
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Team Editor", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                    navigationIcon = {
                        IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                    },
                    actions = {
                        TextButton(onClick = { /* Xử lý Lưu đội hình */ }) {
                            Text("Save", color = Color.Gray, fontSize = 16.sp)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("TEAM NAME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    placeholder = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("POKÉMON PARTY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row {
                        // Nút CLEAR để xóa nhanh cả đội hình
                        Button(
                            onClick = { selectedTeam = emptyList() },
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color.Black)
                        ) {
                            Text("CLEAR", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { isSelectingPokemon = true },
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334F6A))
                        ) {
                            Text("ADD", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)).padding(16.dp).defaultMinSize(minHeight = 120.dp)
                ) {
                    if (selectedTeam.isEmpty()) {
                        Text("No Pokémon added yet. Click ADD.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            selectedTeam.forEach { pokemon ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                                    AsyncImage(
                                        model = pokemon.imageUrl,
                                        contentDescription = pokemon.name,
                                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.White)
                                    )
                                    Text(text = pokemon.name, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp), maxLines = 1)
                                }
                            }
                        }
                    }
                }

                if (selectedTeam.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // =======================================================
                    // --- TEAM STATS (ĐÃ ĐƯỢC TÍNH TOÁN THÔNG MINH LÊN UI) ---
                    // =======================================================
                    Text("Team Stats", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)).padding(16.dp)) {
                        Column {
                            Text("Each stat is an average of the same stat from your entire team.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text("Min", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                                Text("Max", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                            }

                            // Tính chỉ số trung bình (đảm bảo không crash nếu chưa có đủ thuộc tính)
                            val avgHp = selectedTeam.map { it.hp }.average().toInt()
                            val avgAtk = selectedTeam.map { it.attack }.average().toInt()
                            val avgDef = selectedTeam.map { it.defense }.average().toInt()
                            val avgSpAtk = selectedTeam.map { it.spAttack }.average().toInt()
                            val avgSpDef = selectedTeam.map { it.spDefense }.average().toInt()
                            val avgSpd = selectedTeam.map { it.speed }.average().toInt()
                            val totalStats = avgHp + avgAtk + avgDef + avgSpAtk + avgSpDef + avgSpd

                            StatRow("HP", avgHp, Color(0xFFFF5959), calcMinHp(avgHp), calcMaxHp(avgHp))
                            StatRow("Attack", avgAtk, Color(0xFFF5AC78), calcMinStat(avgAtk), calcMaxStat(avgAtk))
                            StatRow("Defense", avgDef, Color(0xFFFAE078), calcMinStat(avgDef), calcMaxStat(avgDef))
                            StatRow("Sp. Attack", avgSpAtk, Color(0xFF9DB7F5), calcMinStat(avgSpAtk), calcMaxStat(avgSpAtk))
                            StatRow("Sp. Defense", avgSpDef, Color(0xFFA7DB8D), calcMinStat(avgSpDef), calcMaxStat(avgSpDef))
                            StatRow("Speed", avgSpd, Color(0xFFFA92B2), calcMinStat(avgSpd), calcMaxStat(avgSpd))

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Total   $totalStats", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.CenterHorizontally))

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("The ranges shown on the right are for a level 100 Pokémon. Max values are based on a beneficial nature, 252 EVs, 31 IVs; Min values are based on a hindering nature, 0 EVs, 0 IVs.",
                                fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TYPE EFFECTIVENESS (Chưa làm logic phần này) ---
                    Text("Type Effectiveness", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Text("Based on type of Pokémon in your team, it is:", fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Weak Against...", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            EffectivenessChip("Water", "-1", Color(0xFF6390F0), Color(0xFF8DB0F5))
                            EffectivenessChip("Rock", "-2", Color(0xFFB6A136), Color(0xFFD5C77F))
                            EffectivenessChip("Ground", "-2", Color(0xFFE2BF65), Color(0xFFEFDF9E))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Resistant Against...", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            EffectivenessChip("Steel", "1", Color(0xFFB7B7CE), Color(0xFFD1D1E0))
                            EffectivenessChip("Poison", "1", Color(0xFFA33EA1), Color(0xFFC183C0))
                            EffectivenessChip("Ice", "4", Color(0xFF96D9D6), Color(0xFFC0EBE9))
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

// --- CÁC HÀM UI PHỤ TRỢ ---

@Composable
fun StatRow(name: String, value: Int, color: Color, min: Int, max: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) {
        Text(name, modifier = Modifier.weight(0.3f), fontSize = 14.sp, color = Color.DarkGray)
        Text(value.toString(), modifier = Modifier.weight(0.15f), fontSize = 14.sp, fontWeight = FontWeight.Bold)

        LinearProgressIndicator(
            progress = { value / 255f },
            modifier = Modifier.weight(0.35f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.3f),
        )

        Spacer(modifier = Modifier.width(16.dp))
        Text(min.toString(), modifier = Modifier.width(40.dp), fontSize = 14.sp)
        Text(max.toString(), modifier = Modifier.width(40.dp), fontSize = 14.sp)
    }
}

@Composable
fun EffectivenessChip(typeName: String, multiplier: String, bgColor: Color, badgeColor: Color) {
    Row(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(bgColor), verticalAlignment = Alignment.CenterVertically) {
        Text(typeName, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
        Text(multiplier, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(badgeColor).padding(horizontal = 8.dp, vertical = 6.dp))
    }
}

// --- CÔNG THỨC LEVEL 100 CHUẨN CỦA POKÉMON ---
fun calcMinHp(base: Int) = base * 2 + 110
fun calcMaxHp(base: Int) = base * 2 + 204

// Chỉ số khác bị ảnh hưởng bởi Nature (Tăng 10% hoặc giảm 10%)
fun calcMinStat(base: Int) = ((base * 2 + 5) * 0.9).toInt()
fun calcMaxStat(base: Int) = ((base * 2 + 99) * 1.1).toInt()