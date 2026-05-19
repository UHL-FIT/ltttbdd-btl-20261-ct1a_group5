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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // Thêm import ViewModel
import coil.compose.AsyncImage // Thêm import Coil để tải ảnh
import com.example.pokedex.viewmodel.PokemonUiState
import com.example.pokedex.viewmodel.PokemonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamBuilderScreen(
    onOpenMenu: () -> Unit,
    // CẮM "BỘ NÃO" VÀO ĐÂY:
    viewModel: PokemonViewModel = viewModel()
) {
    // Trạng thái kiểm tra xem đang ở màn hình Chỉnh sửa hay Chọn Pokemon
    var isSelectingPokemon by remember { mutableStateOf(false) }

    // Trạng thái dữ liệu đội hình
    var teamName by remember { mutableStateOf("") }
    var selectedTeam by remember { mutableStateOf(listOf<PokemonUiModel>()) }

    // Trạng thái thanh tìm kiếm
    var searchQuery by remember { mutableStateOf("") }

    // 1. LẤY DỮ LIỆU THẬT TỪ VIEWMODEL
    val uiState = viewModel.uiState
    val allPokemon = if (uiState is PokemonUiState.Success) uiState.pokemonList else emptyList()

    // 2. LOGIC TÌM KIẾM (Lọc danh sách theo tên hoặc ID)
    val filteredPokemon = allPokemon.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.id.toString() == searchQuery
    }

    if (isSelectingPokemon) {
        // ==========================================
        // MÀN HÌNH 2: CHỌN POKÉMON (Đã có data thật)
        // ==========================================
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
                // Thanh tìm kiếm (Đã hoạt động thật)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search name and number") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                // Danh sách Pokemon để chọn
                LazyColumn {
                    items(filteredPokemon) { pokemon ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Thêm vào đội hình nếu chưa đủ 6 con và chưa có con này
                                    if (selectedTeam.size < 6 && !selectedTeam.contains(pokemon)) {
                                        selectedTeam = selectedTeam + pokemon
                                    }
                                    isSelectingPokemon = false
                                    searchQuery = "" // Reset tìm kiếm khi chọn xong
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ẢNH THẬT TỪ MẠNG
                            AsyncImage(
                                model = pokemon.imageUrl,
                                contentDescription = pokemon.name,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0F0F0)) // Nền xám nhạt cho ảnh
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            // Thông tin
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

                            // Icons bên phải
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Fav", tint = Color.Gray)
                            Spacer(modifier = Modifier.width(16.dp))
                            // Hiển thị nút check xanh nếu đã có trong đội hình
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
        // ==========================================
        // MÀN HÌNH 1: TEAM EDITOR CHÍNH
        // ==========================================
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
                // Nhập tên đội
                Text("TEAM NAME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    placeholder = { Text("Team Name") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Thanh công cụ POKEMON PARTY
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("POKÉMON PARTY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row {
                        Button(onClick = { /* Xóa Pokemon (Sẽ code sau) */ }, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(32.dp)) {
                            Text("CLEAR", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { isSelectingPokemon = true }, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(32.dp)) {
                            Text("ADD", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Khung hiển thị các Pokemon đã chọn
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0)).padding(16.dp).defaultMinSize(minHeight = 120.dp)
                ) {
                    if (selectedTeam.isEmpty()) {
                        Text("No Pokémon added yet. Click ADD.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        // Hiển thị danh sách Pokemon ĐÃ CHỌN (có Ảnh thật)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            selectedTeam.forEach { pokemon ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                                    // ẢNH THẬT
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

                    // --- TEAM STATS (Dữ liệu giả lập - Chờ chọc API Stats) ---
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
                            StatRow("HP", 85, Color.Red, 300, 394)
                            StatRow("Attack", 103, Color.Yellow, 215, 366)
                            StatRow("Defense", 99, Color.Green, 335, 513)
                            StatRow("Sp. Attack", 76, Color.DarkGray, 83, 205)
                            StatRow("Sp. Defense", 87, Color(0xFFD2691E), 87, 210)
                            StatRow("Speed", 70, Color.Blue, 54, 170)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- TYPE EFFECTIVENESS (Dữ liệu giả lập - Chờ chọc API) ---
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