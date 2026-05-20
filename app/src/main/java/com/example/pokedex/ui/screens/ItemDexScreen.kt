package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.pokedex.viewmodel.ItemUiModel
import com.example.pokedex.viewmodel.ItemUiState
import com.example.pokedex.viewmodel.ItemViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDexScreen(
    onOpenMenu: () -> Unit,
    viewModel: ItemViewModel = viewModel()
) {
    val uiState = viewModel.uiState
    var selectedItem by remember { mutableStateOf<ItemUiModel?>(null) }

    // --- BIẾN TRẠNG THÁI CHO TÌM KIẾM ---
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Hiển thị thanh Search nếu đang bấm tìm kiếm
            if (isSearchActive) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { },
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Enter item name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = {
                        IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    colors = SearchBarDefaults.colors(containerColor = Color.White)
                ) {}
            } else {
                TopAppBar(
                    title = { Text("Item Dex", color = Color(0xFF334F6A)) },
                    navigationIcon = {
                        IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, tint = Color(0xFF7B8E9C), contentDescription = "Menu") }
                    },
                    // --- ĐÃ XÓA NÚT SETTINGS Ở GÓC PHẢI ---
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6F8))
                )
            }
        },
        // --- BỔ SUNG BONG BÓNG (FAB) TÌM KIẾM DƯỚI GÓC PHẢI ---
        floatingActionButton = {
            if (!isSearchActive) {
                FloatingActionButton(
                    onClick = { isSearchActive = true },
                    containerColor = Color(0xFF5A7E9A), // Màu xanh xám đồng bộ với Header của Item
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search Items")
                }
            }
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is ItemUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF334F6A))
                }
                is ItemUiState.Error -> {
                    Text("Error loading items", modifier = Modifier.align(Alignment.Center), color = Color.Red)
                }
                is ItemUiState.Success -> {
                    var filteredItems = uiState.items

                    // --- LOGIC LỌC: CHỈ TÌM THEO ITEM NAME ---
                    if (searchQuery.isNotEmpty()) {
                        filteredItems = filteredItems.filter { item ->
                            item.displayName.contains(searchQuery, ignoreCase = true) ||
                                    item.rawName.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Empty", modifier = Modifier.size(60.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No items found...", color = Color.Gray, fontSize = 16.sp)
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredItems) { item ->
                                ItemCard(item = item) { selectedItem = item }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET CHI TIẾT ITEM (Giữ nguyên) ---
    if (selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedItem = null },
            containerColor = Color(0xFFF4F6F8),
            dragHandle = null
        ) {
            ItemDetailSheetContent(item = selectedItem!!)
        }
    }
}

@Composable
fun ItemCard(item: ItemUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334F6A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.shortEffect,
                    fontSize = 14.sp,
                    color = Color(0xFF8B9CB6),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true).build(),
                contentDescription = item.displayName,
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ItemDetailSheetContent(item: ItemUiModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF5A7E9A))
                .padding(top = 24.dp, bottom = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(item.displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Item", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
            }
            AsyncImage(
                model = item.imageUrl, contentDescription = null,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).size(40.dp)
            )
        }

        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailInfoItem("Category", item.category, Modifier.weight(1f))
                DetailInfoItem("Bag pocket", "${item.category} Pocket", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DetailInfoItem("Cost", if(item.cost > 0) "${item.cost} Pokédollar" else "-", Modifier.weight(1f))
                DetailInfoItem("Fling power", item.flingPower, Modifier.weight(1f))
                DetailInfoItem("Fling effect", "-", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GAME DESCRIPTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7E9A))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(item.gameDescription, fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EFFECT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A7E9A))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(item.effect, fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DetailInfoItem(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
        Text(title, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp), textAlign = TextAlign.Center)
    }
}