package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Import để dùng ViewModel
import com.example.pokedex.ui.components.ExpandableSearchFab
import com.example.pokedex.ui.components.PokemonCard
import com.example.pokedex.viewmodel.PokemonUiState
import com.example.pokedex.viewmodel.PokemonViewModel

// Model dữ liệu
data class PokemonUiModel(val id: Int, val name: String, val types: kotlin.collections.List<String>, val imageUrl: String)

// ENUM quản lý các loại Bottom Sheet
enum class ActiveSheet { NONE, SORT, VERSIONS, GENS, TYPES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenMenu: () -> Unit,
    // CẮM "BỘ NÃO" VÀO ĐÂY:
    viewModel: PokemonViewModel = viewModel()
) {
    // Lấy trạng thái hiện tại từ ViewModel (Đang tải, Lỗi, hoặc Thành công)
    val uiState = viewModel.uiState

    // --- TRẠNG THÁI UI ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentSheet by remember { mutableStateOf(ActiveSheet.NONE) }
    var selectedSortOption by remember { mutableStateOf("ID (# / Number)") }
    var selectedOrder by remember { mutableStateOf("Ascending") }

    // ĐÃ XÓA DUMMY DATA Ở ĐÂY

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Enter name or number...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                ) {}
            } else {
                TopAppBar(
                    title = { Text("PokéDex") },
                    navigationIcon = {
                        IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                    },
                    actions = {
                        IconButton(onClick = { /* Mở Dream Team */ }) {
                            Icon(Icons.Default.Star, contentDescription = "Favourite", tint = Color.Gray)
                        }
                        IconButton(onClick = { /* Chọn nhiều */ }) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Multi Select", tint = Color.Gray)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            ExpandableSearchFab(
                onSearchClick = { isSearchActive = true },
                onSortClick = { currentSheet = ActiveSheet.SORT }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // --- THANH FILTER NGANG BÊN TRÊN ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTopButton("ALL GAME VERSIONS", modifier = Modifier.weight(1f)) { currentSheet = ActiveSheet.VERSIONS }
                FilterTopButton("ALL GENS", modifier = Modifier.weight(1f)) { currentSheet = ActiveSheet.GENS }
                FilterTopButton("ALL TYPES", modifier = Modifier.weight(1f)) { currentSheet = ActiveSheet.TYPES }
            }

            // --- NỘI DUNG DANH SÁCH (THÔNG MINH THEO MẠNG) ---
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (uiState) {
                    // Trạng thái 1: Đang xoay chờ tải mạng
                    is PokemonUiState.Loading -> {
                        CircularProgressIndicator(
                            color = Color(0xFFE3350D),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    // Trạng thái 2: Lỗi mạng
                    is PokemonUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Connection Error!", color = Color.Red, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.getGen1To3Pokemon() }) {
                                Text("Retry")
                            }
                        }
                    }
                    // Trạng thái 3: Thành công, hiển thị lưới dữ liệu thật
                    is PokemonUiState.Success -> {
                        val pokemonList = uiState.pokemonList

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pokemonList.size) { index ->
                                val pokemon = pokemonList[index]
                                PokemonCard(
                                    id = pokemon.id,
                                    name = pokemon.name,
                                    types = pokemon.types,
                                    imageUrl = pokemon.imageUrl // Link ảnh thật được đẩy vào đây
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // QUẢN LÝ CÁC BOTTOM SHEET (GIỮ NGUYÊN HOÀN TOÀN TỪ CỦA BẠN)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (selectedOrder == "Ascending"), onClick = { selectedOrder = "Ascending" })
                                Text("Ascending", fontSize = 16.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (selectedOrder == "Descending"), onClick = { selectedOrder = "Descending" })
                                Text("Descending", fontSize = 16.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = { currentSheet = ActiveSheet.NONE }, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334F6A)), shape = RoundedCornerShape(8.dp)) {
                            Text("APPLY", color = Color.White, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                ActiveSheet.VERSIONS -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Select game version", fontSize = 20.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        BigFilterButton("ALL GAME VERSIONS", Color(0xFFD6DFE8), Color.Gray)

                        Text("Gen 1", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BigFilterButton("Red", Color(0xFFE3350D), Color.White, Modifier.weight(1f))
                            BigFilterButton("Blue", Color(0xFF3165BC), Color.White, Modifier.weight(1f))
                        }
                        BigFilterButton("Yellow", Color(0xFFF6D830), Color.White)

                        Text("Gen 2", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BigFilterButton("Gold", Color(0xFFDAA520), Color.White, Modifier.weight(1f))
                            BigFilterButton("Silver", Color(0xFFC0C0C0), Color.White, Modifier.weight(1f))
                        }
                        BigFilterButton("Crystal", Color(0xFF4DD0E1), Color.White)

                        Text("Gen 3", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BigFilterButton("Ruby", Color(0xFFA00000), Color.White, Modifier.weight(1f))
                            BigFilterButton("Sapphire", Color(0xFF0000A0), Color.White, Modifier.weight(1f))
                        }
                        BigFilterButton("Emerald", Color(0xFF008000), Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BigFilterButton("FireRed", Color(0xFFFF4500), Color.White, Modifier.weight(1f))
                            BigFilterButton("LeafGreen", Color(0xFF32CD32), Color.White, Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                ActiveSheet.GENS -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Select generation", fontSize = 20.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        BigFilterButton("ALL GENS", Color(0xFFD6DFE8), Color.Gray)
                        BigFilterButton("GEN 1", Color(0xFF8B9CB6), Color.White)
                        BigFilterButton("GEN 2", Color(0xFF8B9CB6), Color.White)
                        BigFilterButton("GEN 3", Color(0xFF8B9CB6), Color.White)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                ActiveSheet.TYPES -> {
                    Column(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Select type", fontSize = 20.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                        BigFilterButton("ALL TYPES", Color(0xFFD6DFE8), Color.Gray)
                        BigFilterButton("NORMAL", Color(0xFFA8A77A), Color.White)
                        BigFilterButton("FIGHTING", Color(0xFFC22E28), Color.White)
                        BigFilterButton("FLYING", Color(0xFFA98FF3), Color.White)
                        BigFilterButton("POISON", Color(0xFFA33EA1), Color.White)
                        BigFilterButton("GROUND", Color(0xFFE2BF65), Color.White)
                        BigFilterButton("ROCK", Color(0xFFB6A136), Color.White)
                        BigFilterButton("BUG", Color(0xFFA6B91A), Color.White)
                        BigFilterButton("GHOST", Color(0xFF735797), Color.White)
                        BigFilterButton("STEEL", Color(0xFFB7B7CE), Color.White)
                        BigFilterButton("FIRE", Color(0xFFEE8130), Color.White)
                        BigFilterButton("WATER", Color(0xFF6390F0), Color.White)
                        BigFilterButton("GRASS", Color(0xFF7AC74C), Color.White)
                        BigFilterButton("ELECTRIC", Color(0xFFF7D02C), Color.White)
                        BigFilterButton("PSYCHIC", Color(0xFFF95587), Color.White)
                        BigFilterButton("ICE", Color(0xFF96D9D6), Color.White)
                        BigFilterButton("DRAGON", Color(0xFF6F35FC), Color.White)
                        BigFilterButton("DARK", Color(0xFF705746), Color.White)
                        BigFilterButton("FAIRY", Color(0xFFD685AD), Color.White)
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                else -> {}
            }
        }
    }
}

// --- CÁC HÀM UI PHỤ TRỢ (HELPER COMPOSABLES) ---

@Composable
fun FilterTopButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE5E9EC))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A7F9C))
    }
}

@Composable
fun BigFilterButton(text: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier.fillMaxWidth()) {
    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable { /* Xử lý sự kiện sau */ }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}