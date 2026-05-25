package com.example.pokedex.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pokedex.ui.components.getTypeColor

import com.example.pokedex.viewmodel.PokemonUiState
import com.example.pokedex.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamBuilderScreen(
    onOpenMenu: () -> Unit,
    viewModel: PokemonViewModel = viewModel()
) {
    // Chỉ giữ lại trạng thái UI tìm kiếm ở local
    var isSelectingPokemon by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val uiState = viewModel.uiState
    val allPokemon = if (uiState is PokemonUiState.Success) uiState.pokemonList else emptyList()

    val filteredPokemon = allPokemon.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.id.toString() == searchQuery
    }

    if (isSelectingPokemon) {
        // --- MÀN HÌNH CHỌN POKÉMON ---
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
                                    // SỬ DỤNG LƯU TRỮ TỪ VIEWMODEL
                                    if (viewModel.selectedTeam.size < 6 && !viewModel.selectedTeam.any { it.id == pokemon.id }) {
                                        viewModel.selectedTeam = viewModel.selectedTeam + pokemon
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
                                            modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(getTypeColor(type.lowercase())).padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }
                            }

                            if (viewModel.selectedTeam.any { it.id == pokemon.id }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = Color(0xFF2ECC71))
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
        // --- MÀN HÌNH CHỈNH SỬA ĐỘI HÌNH CHÍNH ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Team Editor", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF334F6A)) },
                    navigationIcon = {
                        IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF7B8E9C)) }
                    },
                    actions = {
                        TextButton(onClick = {
                            if (viewModel.teamName.isBlank()) {
                                Toast.makeText(context, "Please enter a team name!", Toast.LENGTH_SHORT).show()
                            } else if (viewModel.selectedTeam.isEmpty()) {
                                Toast.makeText(context, "Please add at least 1 Pokémon!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveTeam(viewModel.teamName, viewModel.selectedTeam)
                                Toast.makeText(context, "Team saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Save", color = Color(0xFF334F6A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6F8))
                )
            },
            containerColor = Color(0xFFF4F6F8)
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("TEAM NAME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                OutlinedTextField(
                    value = viewModel.teamName,
                    onValueChange = { viewModel.teamName = it },
                    placeholder = { Text("Enter Team Name...") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("POKÉMON PARTY (${viewModel.selectedTeam.size}/6)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row {
                        Button(
                            onClick = { viewModel.selectedTeam = emptyList() },
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E9EC), contentColor = Color.DarkGray)
                        ) { Text("CLEAR", fontSize = 12.sp) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { isSelectingPokemon = true },
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334F6A)),
                            enabled = viewModel.selectedTeam.size < 6
                        ) { Text("ADD", fontSize = 12.sp) }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp).defaultMinSize(minHeight = 120.dp)
                ) {
                    if (viewModel.selectedTeam.isEmpty()) {
                        Text("No Pokémon added yet. Click ADD.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            viewModel.selectedTeam.chunked(3).forEach { rowList ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowList.forEach { pokemon ->
                                        Box(
                                            modifier = Modifier.weight(1f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(65.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFFF4F6F8)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AsyncImage(
                                                        model = pokemon.imageUrl,
                                                        contentDescription = pokemon.name,
                                                        modifier = Modifier.size(55.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = pokemon.name,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF334F6A),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE74C3C))
                                                    .align(Alignment.TopEnd)
                                                    .clickable { viewModel.selectedTeam = viewModel.selectedTeam - pokemon },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(10.dp))
                                            }
                                        }
                                    }

                                    if (rowList.size < 3) {
                                        repeat(3 - rowList.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (viewModel.selectedTeam.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Team Stats (Averages)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334F6A), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp)) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Text("Min", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(40.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                                Text("Max", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(40.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
                            }

                            val avgHp = viewModel.selectedTeam.map { it.hp }.average().toInt()
                            val avgAtk = viewModel.selectedTeam.map { it.attack }.average().toInt()
                            val avgDef = viewModel.selectedTeam.map { it.defense }.average().toInt()
                            val avgSpAtk = viewModel.selectedTeam.map { it.spAttack }.average().toInt()
                            val avgSpDef = viewModel.selectedTeam.map { it.spDefense }.average().toInt()
                            val avgSpd = viewModel.selectedTeam.map { it.speed }.average().toInt()
                            val totalStats = avgHp + avgAtk + avgDef + avgSpAtk + avgSpDef + avgSpd

                            StatRow("HP", avgHp, Color(0xFFFF5959), calcMinHp(avgHp), calcMaxHp(avgHp))
                            StatRow("Attack", avgAtk, Color(0xFFF5AC78), calcMinStat(avgAtk), calcMaxStat(avgAtk))
                            StatRow("Defense", avgDef, Color(0xFFFAE078), calcMinStat(avgDef), calcMaxStat(avgDef))
                            StatRow("Sp. Atk", avgSpAtk, Color(0xFF9DB7F5), calcMinStat(avgSpAtk), calcMaxStat(avgSpAtk))
                            StatRow("Sp. Def", avgSpDef, Color(0xFFA7DB8D), calcMinStat(avgSpDef), calcMaxStat(avgSpDef))
                            StatRow("Speed", avgSpd, Color(0xFFFA92B2), calcMinStat(avgSpd), calcMaxStat(avgSpd))

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF4F6F8))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total Average: $totalStats", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF334F6A), modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Type Effectiveness Analysis", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334F6A), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp)) {
                        Column {
                            val analysis = remember(viewModel.selectedTeam) { calculateTeamEffectiveness(viewModel.selectedTeam) }

                            Text("Weak Against (Team Vulnerabilities)", fontWeight = FontWeight.Bold, color = Color(0xFFE74C3C), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (analysis.weaknesses.isEmpty()) {
                                Text("Your team has no shared type weaknesses! Perfectly balanced.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.heightIn(max = 200.dp)
                                ) {
                                    items(analysis.weaknesses) { (type, score) ->
                                        EffectivenessChip(type, score.toString(), getTypeColor(type.lowercase()))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text("Resistant Against (Team Defenses)", fontWeight = FontWeight.Bold, color = Color(0xFF2ECC71), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            if (analysis.resistances.isEmpty()) {
                                Text("Your team has no major elemental resistances.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.heightIn(max = 200.dp)
                                ) {
                                    items(analysis.resistances) { (type, score) ->
                                        EffectivenessChip(type, "+$score", getTypeColor(type.lowercase()))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

data class TeamEffectivenessResult(
    val weaknesses: List<Pair<String, Int>>,
    val resistances: List<Pair<String, Int>>
)

fun calculateTeamEffectiveness(team: List<PokemonUiModel>): TeamEffectivenessResult {
    val allTypes = listOf(
        "NORMAL", "FIRE", "WATER", "ELECTRIC", "GRASS", "ICE", "FIGHTING", "POISON", "GROUND",
        "FLYING", "PSYCHIC", "BUG", "ROCK", "GHOST", "DRAGON", "DARK", "STEEL", "FAIRY"
    )

    val teamScores = allTypes.associateWith { 0 }.toMutableMap()

    for (pokemon in team) {
        for (atkType in allTypes) {
            var productMultiplier = 1f
            for (defType in pokemon.types) {
                productMultiplier *= getSingleTypeMatchup(atkType, defType)
            }
            when {
                productMultiplier > 1f -> teamScores[atkType] = teamScores[atkType]!! - 1
                productMultiplier < 1f -> teamScores[atkType] = teamScores[atkType]!! + 1
            }
        }
    }

    val weaknesses = teamScores.filterValues { it < 0 }.toList().sortedBy { it.second }
    val resistances = teamScores.filterValues { it > 0 }.toList().sortedByDescending { it.second }

    return TeamEffectivenessResult(weaknesses, resistances)
}

fun getSingleTypeMatchup(atk: String, def: String): Float {
    val a = atk.uppercase()
    val d = def.uppercase()
    return when (a) {
        "NORMAL" -> if (d == "ROCK" || d == "STEEL") 0.5f else if (d == "GHOST") 0f else 1f
        "FIRE" -> when(d) { "GRASS", "ICE", "BUG", "STEEL" -> 2f; "FIRE", "WATER", "ROCK", "DRAGON" -> 0.5f; else -> 1f }
        "WATER" -> when(d) { "FIRE", "GROUND", "ROCK" -> 2f; "WATER", "GRASS", "DRAGON" -> 0.5f; else -> 1f }
        "ELECTRIC" -> when(d) { "WATER", "FLYING" -> 2f; "ELECTRIC", "GRASS", "DRAGON" -> 0.5f; "GROUND" -> 0f; else -> 1f }
        "GRASS" -> when(d) { "WATER", "GROUND", "ROCK" -> 2f; "FIRE", "GRASS", "POISON", "FLYING", "BUG", "DRAGON", "STEEL" -> 0.5f; else -> 1f }
        "ICE" -> when(d) { "GRASS", "GROUND", "FLYING", "DRAGON" -> 2f; "FIRE", "WATER", "ICE", "STEEL" -> 0.5f; else -> 1f }
        "FIGHTING" -> when(d) { "NORMAL", "ICE", "ROCK", "DARK", "STEEL" -> 2f; "POISON", "FLYING", "PSYCHIC", "BUG", "FAIRY" -> 0.5f; "GHOST" -> 0f; else -> 1f }
        "POISON" -> when(d) { "GRASS", "FAIRY" -> 2f; "POISON", "GROUND", "ROCK", "GHOST" -> 0.5f; "STEEL" -> 0f; else -> 1f }
        "GROUND" -> when(d) { "FIRE", "ELECTRIC", "POISON", "ROCK", "STEEL" -> 2f; "GRASS", "BUG" -> 0.5f; "FLYING" -> 0f; else -> 1f }
        "FLYING" -> when(d) { "GRASS", "FIGHTING", "BUG" -> 2f; "ELECTRIC", "ROCK", "STEEL" -> 0.5f; else -> 1f }
        "PSYCHIC" -> when(d) { "FIGHTING", "POISON" -> 2f; "PSYCHIC", "STEEL" -> 0.5f; "DARK" -> 0f; else -> 1f }
        "BUG" -> when(d) { "GRASS", "PSYCHIC", "DARK" -> 2f; "FIRE", "FIGHTING", "POISON", "FLYING", "GHOST", "STEEL", "FAIRY" -> 0.5f; else -> 1f }
        "ROCK" -> when(d) { "FIRE", "ICE", "FLYING", "BUG" -> 2f; "FIGHTING", "GROUND", "STEEL" -> 0.5f; else -> 1f }
        "GHOST" -> when(d) { "PSYCHIC", "GHOST" -> 2f; "DARK" -> 0.5f; "NORMAL" -> 0f; else -> 1f }
        "DRAGON" -> when(d) { "DRAGON" -> 2f; "STEEL" -> 0.5f; "FAIRY" -> 0f; else -> 1f }
        "DARK" -> when(d) { "PSYCHIC", "GHOST" -> 2f; "FIGHTING", "DARK", "FAIRY" -> 0.5f; else -> 1f }
        "STEEL" -> when(d) { "ICE", "ROCK", "FAIRY" -> 2f; "FIRE", "WATER", "ELECTRIC", "STEEL" -> 0.5f; else -> 1f }
        "FAIRY" -> when(d) { "FIGHTING", "DRAGON", "DARK" -> 2f; "FIRE", "POISON", "STEEL" -> 0.5f; else -> 1f }
        else -> 1f
    }
}

@Composable
fun EffectivenessChip(typeName: String, displayScore: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(color),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(typeName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(displayScore, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun calcMinHp(base: Int) = base * 2 + 110
fun calcMaxHp(base: Int) = base * 2 + 204

fun calcMinStat(base: Int) = ((base * 2 + 5) * 0.9).toInt()
fun calcMaxStat(base: Int) = ((base * 2 + 99) * 1.1).toInt()

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