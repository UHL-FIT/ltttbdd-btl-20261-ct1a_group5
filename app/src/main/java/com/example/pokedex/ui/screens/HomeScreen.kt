package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pokedex.ui.components.ExpandableSearchFab
import com.example.pokedex.ui.components.PokemonCard
import com.example.pokedex.ui.components.getTypeColor
import com.example.pokedex.viewmodel.PokemonUiState
import com.example.pokedex.viewmodel.PokemonViewModel

data class PokemonUiModel(
    val id: Int, val name: String, val types: List<String>, val imageUrl: String,
    val hp: Int = 0, val attack: Int = 0, val defense: Int = 0, val spAttack: Int = 0, val spDefense: Int = 0, val speed: Int = 0
)

enum class ActiveSheet { NONE, SORT, VERSIONS, GENS, TYPES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenMenu: () -> Unit,
    onPokemonClick: (Int) -> Unit = {},
    viewModel: PokemonViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentSheet by remember { mutableStateOf(ActiveSheet.NONE) }

    var selectedSortOption by remember { mutableStateOf("ID (# / Number)") }
    var selectedOrder by remember { mutableStateOf("Ascending") }

    var selectedVersion by remember { mutableStateOf("ALL GAME VERSIONS") }
    var selectedGen by remember { mutableStateOf("ALL GENS") }
    var selectedType by remember { mutableStateOf("ALL TYPES") }

    // --- THÊM 2 BIẾN TRẠNG THÁI ĐỂ QUẢN LÝ BỘ LỌC YÊU THÍCH VÀ ĐÃ BẮT ---
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showCaughtOnly by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery, onQueryChange = { searchQuery = it }, onSearch = { }, active = false, onActiveChange = {},
                    placeholder = { Text("Enter name or number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { IconButton(onClick = { isSearchActive = false; searchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = "Close") } },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                ) {}
            } else {
                TopAppBar(
                    title = { Text("PokéDex") },
                    navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu") } },
                    actions = {
                        // --- NÚT BẬT/TẮT LỌC POKÉMON YÊU THÍCH (THẢ TIM) ---
                        IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Favourite",
                                tint = if (showFavoritesOnly) Color(0xFFFFCE4B) else Color.LightGray
                            )
                        }
                        // --- NÚT BẬT/TẮT LỌC POKÉMON ĐÃ BẮT ---
                        IconButton(onClick = { showCaughtOnly = !showCaughtOnly }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Caught",
                                tint = if (showCaughtOnly) Color(0xFF48D0B0) else Color.LightGray
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = { ExpandableSearchFab(onSearchClick = { isSearchActive = true }, onSortClick = { currentSheet = ActiveSheet.SORT }) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            val isLightVersionText = selectedVersion.contains("White") || selectedVersion == "Yellow" || selectedVersion == "Let's Go Pikachu"
            val versionBg = if (selectedVersion == "ALL GAME VERSIONS") Color(0xFFE5E9EC) else getVersionColor(selectedVersion)
            val versionTxt = if (selectedVersion == "ALL GAME VERSIONS" || isLightVersionText) Color(0xFF6A7F9C) else Color.White

            val genBg = if (selectedGen == "ALL GENS") Color(0xFFE5E9EC) else Color(0xFF334F6A)
            val genTxt = if (selectedGen == "ALL GENS") Color(0xFF6A7F9C) else Color.White

            val typeBg = if (selectedType == "ALL TYPES") Color(0xFFE5E9EC) else getTypeColor(selectedType)
            val typeTxt = if (selectedType == "ALL TYPES") Color(0xFF6A7F9C) else Color.White

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterTopButton(selectedVersion, versionBg, versionTxt, modifier = Modifier.weight(1f)) { currentSheet = ActiveSheet.VERSIONS }
                FilterTopButton(selectedGen, genBg, genTxt, modifier = Modifier.weight(1f)) { currentSheet = ActiveSheet.GENS }
                FilterTopButton(selectedType, typeBg, typeTxt, modifier = Modifier.weight(1f)) { currentSheet = ActiveSheet.TYPES }
            }

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (uiState) {
                    is PokemonUiState.Loading -> { CircularProgressIndicator(color = Color(0xFFE3350D), modifier = Modifier.align(Alignment.Center)) }
                    is PokemonUiState.Error -> {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Connection Error!", color = Color.Red, fontWeight = FontWeight.Bold)
                            Button(onClick = { viewModel.getGen1To3Pokemon() }) { Text("Retry") }
                        }
                    }
                    is PokemonUiState.Success -> {
                        var filteredList = uiState.pokemonList

                        // --- 1. LỌC THEO TRẠNG THÁI CAUGHT VÀ FAVORITE TRƯỚC ---
                        if (showFavoritesOnly) {
                            filteredList = filteredList.filter { viewModel.favoriteIds.contains(it.id) }
                        }
                        if (showCaughtOnly) {
                            filteredList = filteredList.filter { viewModel.caughtIds.contains(it.id) }
                        }

                        // Các luồng lọc cũ
                        if (searchQuery.isNotEmpty()) { filteredList = filteredList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.toString() == searchQuery } }

                        if (selectedGen != "ALL GENS") {
                            val genRange = when (selectedGen) {
                                "GEN 1" -> 1..151
                                "GEN 2" -> 152..251
                                "GEN 3" -> 252..386
                                "GEN 4" -> 387..493
                                "GEN 5" -> 494..649
                                "GEN 6" -> 650..721
                                "GEN 7" -> 722..809
                                "GEN 8" -> 810..905
                                "GEN 9" -> 906..1025
                                else -> 1..9999
                            }
                            filteredList = filteredList.filter { it.id in genRange }
                        }

                        if (selectedVersion != "ALL GAME VERSIONS") {
                            val versionMaxId = when (selectedVersion) {
                                "Red", "Blue", "Yellow" -> 151
                                "Gold", "Silver", "Crystal" -> 251
                                "Ruby", "Sapphire", "Emerald", "FireRed", "LeafGreen" -> 386
                                "Diamond", "Pearl", "Platinum", "HeartGold", "SoulSilver" -> 493
                                "Black", "White", "Black 2", "White 2" -> 649
                                "X", "Y", "Omega Ruby", "Alpha Sapphire" -> 721
                                "Sun", "Moon", "Ultra Sun", "Ultra Moon", "Let's Go Pikachu", "Let's Go Eevee" -> 809
                                "Sword", "Shield", "Brilliant Diamond", "Shining Pearl", "Legends: Arceus" -> 905
                                "Scarlet", "Violet" -> 1025
                                else -> 1025
                            }
                            filteredList = filteredList.filter { it.id <= versionMaxId }
                        }

                        if (selectedType != "ALL TYPES") { filteredList = filteredList.filter { pokemon -> pokemon.types.any { it.equals(selectedType, ignoreCase = true) } } }

                        filteredList = when (selectedSortOption) {
                            "Alphabet (A-Z)" -> filteredList.sortedBy { it.name }
                            "Total" -> filteredList.sortedBy { it.hp + it.attack + it.defense + it.spAttack + it.spDefense + it.speed }
                            "HP" -> filteredList.sortedBy { it.hp }
                            "Attack" -> filteredList.sortedBy { it.attack }
                            "Defense" -> filteredList.sortedBy { it.defense }
                            "Sp. Attack" -> filteredList.sortedBy { it.spAttack }
                            "Sp. Defense" -> filteredList.sortedBy { it.spDefense }
                            "Speed" -> filteredList.sortedBy { it.speed }
                            else -> filteredList.sortedBy { it.id }
                        }

                        if (selectedOrder == "Descending") { filteredList = filteredList.reversed() }

                        // --- 2. HIỂN THỊ DANH SÁCH HOẶC GIAO DIỆN TRỐNG ---
                        if (filteredList.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "Empty", modifier = Modifier.size(60.dp), tint = Color.LightGray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No Pokémon found...", color = Color.Gray, fontSize = 16.sp)
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredList.size) { index ->
                                    val pokemon = filteredList[index]
                                    PokemonCard(
                                        id = pokemon.id,
                                        name = pokemon.name,
                                        types = pokemon.types,
                                        imageUrl = pokemon.imageUrl,
                                        // Gắn dữ liệu và sự kiện từ ViewModel vào Card
                                        isFavorite = viewModel.favoriteIds.contains(pokemon.id),
                                        isCaught = viewModel.caughtIds.contains(pokemon.id),
                                        onFavoriteClick = { viewModel.toggleFavorite(pokemon.id) },
                                        onCaughtClick = { viewModel.toggleCaught(pokemon.id) },
                                        onClick = { onPokemonClick(pokemon.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // QUẢN LÝ BOTTOM SHEETS
    // =========================================================
    if (currentSheet != ActiveSheet.NONE) {
        ModalBottomSheet(onDismissRequest = { currentSheet = ActiveSheet.NONE }) {
            when (currentSheet) {
                ActiveSheet.SORT -> {
                    Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Text("Sort by...", fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally), color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        val sortOptions = listOf("ID (# / Number)", "Alphabet (A-Z)", "Total", "HP", "Attack", "Defense", "Sp. Attack", "Sp. Defense", "Speed")
                        sortOptions.forEach { option ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                RadioButton(selected = (selectedSortOption == option), onClick = { selectedSortOption = option }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF334F6A)))
                                Text(text = option, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Order", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = (selectedOrder == "Ascending"), onClick = { selectedOrder = "Ascending" }); Text("Ascending", fontSize = 16.sp) }
                            Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = (selectedOrder == "Descending"), onClick = { selectedOrder = "Descending" }); Text("Descending", fontSize = 16.sp) }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { currentSheet = ActiveSheet.NONE }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334F6A)), shape = RoundedCornerShape(8.dp)) { Text("APPLY", color = Color.White, fontSize = 16.sp) }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                ActiveSheet.VERSIONS -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Select game version", fontSize = 20.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))

                        BigFilterButton("ALL GAME VERSIONS", Color(0xFFD6DFE8), Color.Gray, Modifier.fillMaxWidth()) {
                            selectedVersion = "ALL GAME VERSIONS"; currentSheet = ActiveSheet.NONE
                        }

                        fun getTextColor(version: String): Color {
                            val isLightVersionText = version.contains("White") || version == "Yellow" || version == "Let's Go Pikachu"
                            return if (isLightVersionText) Color.DarkGray else Color.White
                        }

                        @Composable
                        fun VersionItem(versionName: String) {
                            BigFilterButton(
                                versionName,
                                getVersionColor(versionName),
                                getTextColor(versionName),
                                Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            ) {
                                selectedVersion = versionName; currentSheet = ActiveSheet.NONE
                            }
                        }

                        Text("Gen 1", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Red")
                        VersionItem("Blue")
                        VersionItem("Yellow")

                        Text("Gen 2", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Gold")
                        VersionItem("Silver")
                        VersionItem("Crystal")

                        Text("Gen 3", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Ruby")
                        VersionItem("Sapphire")
                        VersionItem("Emerald")
                        VersionItem("FireRed")
                        VersionItem("LeafGreen")

                        Text("Gen 4", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Diamond")
                        VersionItem("Pearl")
                        VersionItem("Platinum")
                        VersionItem("HeartGold")
                        VersionItem("SoulSilver")

                        Text("Gen 5", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Black")
                        VersionItem("White")
                        VersionItem("Black 2")
                        VersionItem("White 2")

                        Text("Gen 6", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("X")
                        VersionItem("Y")
                        VersionItem("Omega Ruby")
                        VersionItem("Alpha Sapphire")

                        Text("Gen 7", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Sun")
                        VersionItem("Moon")
                        VersionItem("Ultra Sun")
                        VersionItem("Ultra Moon")
                        VersionItem("Let's Go Pikachu")
                        VersionItem("Let's Go Eevee")

                        Text("Gen 8", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Sword")
                        VersionItem("Shield")
                        VersionItem("Brilliant Diamond")
                        VersionItem("Shining Pearl")
                        VersionItem("Legends: Arceus")

                        Text("Gen 9", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        VersionItem("Scarlet")
                        VersionItem("Violet")

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                ActiveSheet.GENS -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Select generation", fontSize = 20.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        BigFilterButton("ALL GENS", Color(0xFFD6DFE8), Color.Gray, Modifier.fillMaxWidth()) { selectedGen = "ALL GENS"; currentSheet = ActiveSheet.NONE }

                        val gens = listOf("GEN 1", "GEN 2", "GEN 3", "GEN 4", "GEN 5", "GEN 6", "GEN 7", "GEN 8", "GEN 9")
                        gens.chunked(2).forEach { rowGens ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                rowGens.forEach { gen ->
                                    BigFilterButton(gen, Color(0xFF8B9CB6), Color.White, Modifier.weight(1f)) { selectedGen = gen; currentSheet = ActiveSheet.NONE }
                                }
                                if (rowGens.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                ActiveSheet.TYPES -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Select type", fontSize = 20.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        BigFilterButton("ALL TYPES", Color(0xFFD6DFE8), Color.Gray, Modifier.fillMaxWidth()) { selectedType = "ALL TYPES"; currentSheet = ActiveSheet.NONE }
                        val types = listOf("NORMAL", "FIGHTING", "FLYING", "POISON", "GROUND", "ROCK", "BUG", "GHOST", "STEEL", "FIRE", "WATER", "GRASS", "ELECTRIC", "PSYCHIC", "ICE", "DRAGON", "DARK", "FAIRY")
                        types.chunked(2).forEach { rowTypes ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                                rowTypes.forEach { type ->
                                    BigFilterButton(type, getTypeColor(type), Color.White, Modifier.weight(1f)) { selectedType = type; currentSheet = ActiveSheet.NONE }
                                }
                                if (rowTypes.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                else -> {}
            }
        }
    }
}

// --- CÁC HÀM UI PHỤ TRỢ ---

fun getVersionColor(version: String): Color {
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
        "Violet" -> Color(0xFF8A2BE2)
        "Legends: Arceus" -> Color(0xFF2A595C)
        "Let's Go Pikachu" -> Color(0xFFF4D23C)
        "Let's Go Eevee" -> Color(0xFFC39F63)
        else -> Color(0xFF334F6A)
    }
}

@Composable
fun FilterTopButton(text: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(bgColor).clickable { onClick() }.padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) { Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor, maxLines = 1) }
}

@Composable
fun BigFilterButton(text: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit = {}) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(8.dp)).background(bgColor).clickable { onClick() }.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor) }
}