package com.example.pokedex.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
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
import com.example.pokedex.viewmodel.NatureUiModel
import com.example.pokedex.viewmodel.NatureUiState
import com.example.pokedex.viewmodel.NatureViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NatureDexScreen(
    onOpenMenu: () -> Unit,
    viewModel: NatureViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            if (viewModel.isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.searchQuery = it; viewModel.applyFilters() },
                            placeholder = { Text("Search nature...", color = Color.Gray, fontSize = 18.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true, modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.isSearchActive = false; viewModel.searchQuery = ""; viewModel.applyFilters() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF334F6A))
                        }
                    },
                    actions = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery = ""; viewModel.applyFilters() }) { Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray) }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            } else {
                TopAppBar(
                    title = { Text("Nature Dex", color = Color(0xFF334F6A)) },
                    navigationIcon = { IconButton(onClick = onOpenMenu) { Icon(Icons.Default.Menu, tint = Color(0xFF7B8E9C), contentDescription = "Menu") } },
                    actions = { IconButton(onClick = { }) { Icon(Icons.Default.Settings, tint = Color(0xFF7B8E9C), contentDescription = "Settings") } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F6F8))
                )
            }
        },
        floatingActionButton = {
            if (!viewModel.isSearchActive) {
                FloatingActionButton(onClick = { viewModel.isSearchActive = true }, containerColor = Color(0xFF334F6A), contentColor = Color.White, shape = CircleShape) {
                    Icon(Icons.Default.FilterAlt, contentDescription = "Search")
                }
            }
        },
        containerColor = Color(0xFFF4F6F8)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is NatureUiState.Loading -> { CircularProgressIndicator(color = Color(0xFF334F6A), modifier = Modifier.align(Alignment.Center)) }
                is NatureUiState.Error -> { Text("Error loading natures", color = Color.Red, modifier = Modifier.align(Alignment.Center)) }
                is NatureUiState.Success -> {
                    val natures = uiState.natures
                    if (natures.isEmpty()) {
                        Text("No natures found...", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(natures) { nature -> NatureCard(nature) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NatureCard(nature: NatureUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Header: Tên Nature
            Text(
                text = nature.name, fontSize = 20.sp, color = Color(0xFF334F6A),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), textAlign = TextAlign.Center
            )

            HorizontalDivider(color = Color(0xFFF0F0F0))

            // Row 1: Hương vị (Flavors)
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(nature.likesFlavor, fontSize = 14.sp, color = Color.Gray)
                }
                Box(modifier = Modifier.weight(1f).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(nature.dislikesFlavor, fontSize = 14.sp, color = Color.Gray)
                }
            }

            // Row 2: Chỉ số Tăng / Giảm
            Row(modifier = Modifier.fillMaxWidth()) {
                // Tăng (Đỏ)
                Row(
                    modifier = Modifier.weight(1f).background(Color(0xFFF05A5A)).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.KeyboardDoubleArrowUp, contentDescription = "Up", tint = Color.White, modifier = Modifier.size(20.dp))
                    Text(nature.increasedStat, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(20.dp)) // Cân bằng khoảng cách
                }

                // Giảm (Xanh)
                Row(
                    modifier = Modifier.weight(1f).background(Color(0xFF4A90E2)).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(20.dp)) // Cân bằng khoảng cách
                    Text(nature.decreasedStat, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Icon(Icons.Default.KeyboardDoubleArrowDown, contentDescription = "Down", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}