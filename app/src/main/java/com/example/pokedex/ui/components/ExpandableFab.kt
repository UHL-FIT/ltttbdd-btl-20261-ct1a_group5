package com.example.pokedex.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpandableSearchFab(
    onSearchClick: () -> Unit,
    onSortClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "fab_rotation")

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimatedVisibility(visible = expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- NÚT SẮP XẾP (Đã gộp chung Lọc) ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sort & Filter",
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallFloatingActionButton(
                        onClick = { expanded = false; onSortClick() },
                        containerColor = Color(0xFFEAF1F8)
                    ) {
                        Icon(Icons.Default.List, contentDescription = "Sort")
                    }
                }

                // --- NÚT TÌM KIẾM ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Search all",
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SmallFloatingActionButton(
                        onClick = { expanded = false; onSearchClick() },
                        containerColor = Color(0xFFEAF1F8)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = Color(0xFF334F6A),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Search,
                contentDescription = "Expand",
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}