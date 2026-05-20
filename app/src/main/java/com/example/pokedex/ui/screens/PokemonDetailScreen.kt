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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokedex.ui.components.getTypeColor
import com.example.pokedex.viewmodel.PokemonDetailState
import com.example.pokedex.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    onBack: () -> Unit,
    onNavigateToPokemon: (Int) -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: PokemonViewModel
) {
    LaunchedEffect(pokemonId) { viewModel.getPokemonDetailComplete(pokemonId) }
    val detailState = viewModel.detailState

    var selectedTab by remember { mutableStateOf(0) }

    when (detailState) {
        is PokemonDetailState.Idle, is PokemonDetailState.Loading -> {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFE3350D))
            }
        }
        is PokemonDetailState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onBack) { Text("Go Back (Error)") }
            }
        }
        is PokemonDetailState.Success -> {
            val pokemon = detailState.pokemon
            val basic = pokemon.basic
            val bgColor = getTypeColor(basic.types.firstOrNull() ?: "normal")

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("") },
                        navigationIcon = {
                            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                    )
                },
                bottomBar = {
                    NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Info, contentDescription = "Info") },
                            label = { Text("Info") },
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = bgColor)
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.List, contentDescription = "Moves") },
                            label = { Text("Moves") },
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = bgColor)
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Star, contentDescription = "More") },
                            label = { Text("More") },
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = bgColor)
                        )
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().background(bgColor).padding(padding)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 24.dp)) {
                        Column(modifier = Modifier.align(Alignment.TopStart)) {
                            Text(text = basic.name, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = pokemon.speciesName, fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row {
                                basic.types.forEach { type ->
                                    Text(
                                        text = type.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White,
                                        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = 0.2f)).padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }
                        Text(text = "#${basic.id.toString().padStart(3, '0')}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.TopEnd))
                        AsyncImage(
                            model = basic.imageUrl, contentDescription = basic.name, contentScale = ContentScale.Fit,
                            modifier = Modifier.size(160.dp).align(Alignment.BottomEnd).offset(y = 20.dp)
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)).background(Color.White)) {
                        when (selectedTab) {
                            0 -> TabInfo(pokemon, bgColor)
                            1 -> TabMoves(pokemon.levelUpMoves, bgColor)
                            2 -> TabMore(pokemon, bgColor, onNavigateToPokemon, onNavigateHome)
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// CÁC HÀM UI CHO TỪNG TAB RIÊNG BIỆT
// ------------------------------------------------------------------

@Composable
fun TabInfo(pokemon: DetailedPokemonUiModel, bgColor: Color) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))
        SectionTitle("Species", bgColor)
        Text(text = pokemon.descriptions.values.firstOrNull() ?: "No description available.", fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            InfoCard("Height", "${pokemon.height} m")
            InfoCard("Weight", "${pokemon.weight} kg")
        }
        Spacer(modifier = Modifier.height(32.dp))

        SectionTitle("Base Stats", bgColor)
        val basic = pokemon.basic
        DetailStatRow("HP", basic.hp, Color(0xFFFF5959), calcMinHp(basic.hp), calcMaxHp(basic.hp))
        DetailStatRow("Attack", basic.attack, Color(0xFFF5AC78), calcMinStat(basic.attack), calcMaxStat(basic.attack))
        DetailStatRow("Defense", basic.defense, Color(0xFFFAE078), calcMinStat(basic.defense), calcMaxStat(basic.defense))
        DetailStatRow("Sp. Atk", basic.spAttack, Color(0xFF9DB7F5), calcMinStat(basic.spAttack), calcMaxStat(basic.spAttack))
        DetailStatRow("Sp. Def", basic.spDefense, Color(0xFFA7DB8D), calcMinStat(basic.spDefense), calcMaxStat(basic.spDefense))
        DetailStatRow("Speed", basic.speed, Color(0xFFFA92B2), calcMinStat(basic.speed), calcMaxStat(basic.speed))
        Spacer(modifier = Modifier.height(32.dp))

        SectionTitle("Sprites", bgColor)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(model = pokemon.frontDefaultUrl, contentDescription = "Normal", modifier = Modifier.size(100.dp))
                Text("Normal", color = Color.Gray, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(model = pokemon.frontShinyUrl, contentDescription = "Shiny", modifier = Modifier.size(100.dp))
                Text("Shiny", color = Color.Gray, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TabMoves(moves: List<MoveUiItem>, bgColor: Color) {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Level", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(40.dp))
            Text("Move", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("Power", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
            Text("Acc.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = Color.LightGray)

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)) {
            items(moves) { move ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(move.level.toString(), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(40.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(move.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = bgColor)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(move.type, fontSize = 10.sp, color = Color.White, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(getTypeColor(move.type.lowercase())).padding(horizontal = 6.dp, vertical = 2.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(move.damageClass, fontSize = 10.sp, color = Color.White, modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.DarkGray).padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text(move.power, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                    Text(move.accuracy, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
                }
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun TabMore(pokemon: DetailedPokemonUiModel, bgColor: Color, onNext: (Int) -> Unit, onHome: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.height(48.dp))

        // --- CẬP NHẬT LOGIC EVOLUTION MỚI ---
        SectionTitle("Evolution Chain", bgColor)
        if (pokemon.evolutionChain.size < 2) {
            Text("This Pokémon does not evolve.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp))
        } else {
            // Dùng hàm windowed(2, 1) để bắt cặp tiến hóa. VD: [A, B, C] -> [A, B] và [B, C]
            pokemon.evolutionChain.windowed(size = 2, step = 1, partialWindows = false).forEachIndexed { index, pair ->
                val stageA = pair[0]
                val stageB = pair[1]

                EvolutionPairItem(stageA, stageB, onNext)

                if (index < pokemon.evolutionChain.size - 2) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // --- DAMAGE TAKEN ---
        SectionTitle("Damage Taken", bgColor)
        Text("Weak against...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
        pokemon.weakAgainst.chunked(3).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item -> EffectivenessBadge(item.typeName, item.multiplier, item.color, Modifier.weight(1f)) }
                if (rowItems.size < 3) repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Resistant against...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
        pokemon.resistantAgainst.chunked(3).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item -> EffectivenessBadge(item.typeName, item.multiplier, item.color, Modifier.weight(1f)) }
                if (rowItems.size < 3) repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text("Normal damage from...", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
        pokemon.normalDamageFrom.chunked(3).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item -> EffectivenessBadge(item.typeName, item.multiplier, item.color, Modifier.weight(1f)) }
                if (rowItems.size < 3) repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- NAVIGATION MENU ---
        SectionTitle("Navigation", bgColor)
        Button(onClick = onHome, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray), shape = RoundedCornerShape(8.dp)) {
            Icon(Icons.Default.Home, contentDescription = "Home", tint = bgColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("HOME", color = bgColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8F4F0)).clickable { onNext(pokemon.nextPokemonId) }.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Next Pokémon", color = Color.Gray, fontSize = 12.sp)
                    Text(pokemon.nextPokemonName, color = bgColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                AsyncImage(model = pokemon.nextPokemonImageUrl, contentDescription = "Next", modifier = Modifier.size(60.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = bgColor)
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

// --- COMPONENTS PHỤ TRỢ ---
@Composable fun SectionTitle(title: String, color: Color) { Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), textAlign = TextAlign.Center) }
@Composable fun InfoCard(title: String, value: String) { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5F5F5)).padding(12.dp).widthIn(min = 90.dp)) { Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray, textAlign = TextAlign.Center); Spacer(modifier = Modifier.height(4.dp)); Text(text = title, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center) } }
@Composable fun DetailStatRow(name: String, value: Int, color: Color, min: Int, max: Int) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth()) { Text(name, modifier = Modifier.width(65.dp), fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium); Text(value.toString().padStart(3, '0'), modifier = Modifier.width(35.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold); LinearProgressIndicator(progress = { value / 255f }, modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)), color = color, trackColor = color.copy(alpha = 0.2f)); Spacer(modifier = Modifier.width(12.dp)); Text(min.toString(), modifier = Modifier.width(35.dp), fontSize = 13.sp, color = Color.Gray); Text(max.toString(), modifier = Modifier.width(35.dp), fontSize = 13.sp, color = Color.Gray) } }
@Composable fun EffectivenessBadge(typeName: String, multiplier: String, bgColor: Color, modifier: Modifier = Modifier) { Row(modifier = modifier.clip(RoundedCornerShape(8.dp)).background(bgColor), verticalAlignment = Alignment.CenterVertically) { Text(typeName, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(vertical = 6.dp), textAlign = TextAlign.Center); if (multiplier.isNotEmpty()) Text(multiplier, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp)) } }

// --- COMPOSABLE VẼ CẶP TIẾN HÓA MỚI HOÀN TOÀN ---
@Composable
fun EvolutionPairItem(stageA: MoveUiItem, stageB: MoveUiItem, onNext: (Int) -> Unit) {
    // Trick nhỏ: Model hiện tại lưu evolution dưới dạng EvolutionUiItem.
    // Tôi tạo Composable nhận vào đúng kiểu dữ liệu đó.
}

@Composable
fun EvolutionPairItem(stageA: EvolutionUiItem, stageB: EvolutionUiItem, onNext: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- STAGE A (Ép 30% chiều rộng) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(0.3f).clickable { onNext(stageA.id) }
        ) {
            AsyncImage(model = stageA.imageUrl, contentDescription = stageA.name, modifier = Modifier.size(80.dp), contentScale = ContentScale.Fit)
            Text(
                text = stageA.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp)
            )
        }

        // --- MŨI TÊN VÀ ĐIỀU KIỆN (Ép 40% chiều rộng) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(0.4f)
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Evolves", tint = Color.LightGray, modifier = Modifier.size(28.dp))
            if (stageB.triggerDesc.isNotEmpty()) {
                Text(
                    text = stageB.triggerDesc.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray,
                    textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // --- STAGE B (Ép 30% chiều rộng) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(0.3f).clickable { onNext(stageB.id) }
        ) {
            AsyncImage(model = stageB.imageUrl, contentDescription = stageB.name, modifier = Modifier.size(80.dp), contentScale = ContentScale.Fit)
            Text(
                text = stageB.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}