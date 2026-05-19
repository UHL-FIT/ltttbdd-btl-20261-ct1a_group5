package com.example.pokedex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppDrawer(
    currentRoute: String = "pokedex",
    closeDrawer: () -> Unit,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(300.dp)
    ) {
        // 1. PHẦN HEADER (Giữ nguyên phong cách PokéDex của bạn)
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PokéDex",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE3350D)
                )
            }

            // Khung thông tin Trainer (Xanh đậm)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF334F6A))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Welcome, trainer", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("PROFILE", fontSize = 10.sp, color = Color.White) }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.height(28.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) { Text("SIGN IN", fontSize = 10.sp, color = Color.White) }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. DANH SÁCH MENU (Bọc trong verticalScroll đề phòng màn hình nhỏ)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Nhóm 1: Các loại từ điển bách khoa (Dex)
            DrawerMenuItem(
                icon = Icons.Default.CatchingPokemon, // Icon quả cầu Pokéball trong thư viện mở rộng
                label = "Pokédex",
                isSelected = currentRoute == "pokedex",
                onClick = { onNavigate("pokedex"); closeDrawer() }
            )
            DrawerMenuItem(
                icon = Icons.Default.FlashOn, // Thay cho biểu tượng tia sét/kiếm của Move Dex
                label = "Move Dex",
                isSelected = currentRoute == "moves",
                onClick = { onNavigate("moves"); closeDrawer() }
            )
            DrawerMenuItem(
                icon = Icons.Default.AutoAwesome, // Biểu tượng lấp lánh cho Ability Dex
                label = "Ability Dex",
                isSelected = currentRoute == "abilities",
                onClick = { onNavigate("abilities"); closeDrawer() }
            )
            DrawerMenuItem(
                icon = Icons.Default.Backpack, // Biểu tượng túi đồ cho Item Dex
                label = "Item Dex",
                isSelected = currentRoute == "items",
                onClick = { onNavigate("items"); closeDrawer() }
            )
            DrawerMenuItem(
                icon = Icons.Default.LocationOn, // Biểu tượng ghim bản đồ cho Location Dex
                label = "Location Dex",
                isSelected = currentRoute == "locations",
                onClick = { onNavigate("locations"); closeDrawer() }
            )
            DrawerMenuItem(
                icon = Icons.Default.Label, // Biểu tượng thẻ tag cho Type Dex
                label = "Type Dex",
                isSelected = currentRoute == "types",
                onClick = { onNavigate("types"); closeDrawer() }
            )
            DrawerMenuItem(
                icon = Icons.Default.Eco, // Biểu tượng chiếc lá thiên nhiên cho Nature Dex
                label = "Nature Dex",
                isSelected = currentRoute == "natures",
                onClick = { onNavigate("natures"); closeDrawer() }
            )

            // Dấu gạch ngang phân tách nhóm như bản gốc
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), thickness = 1.dp, color = Color.LightGray)

            // Nhóm 2: Tính năng xây dựng đội hình
            DrawerMenuItem(
                icon = Icons.Default.Groups, // Biểu tượng nhóm người cho Team Builder
                label = "Team Builder",
                isSelected = currentRoute == "team",
                onClick = { onNavigate("team"); closeDrawer() }
            )

            // Dấu gạch ngang phân tách nhóm
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp), thickness = 1.dp, color = Color.LightGray)

            // Nhóm 3: Hệ thống và Hỗ trợ
            DrawerMenuItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentRoute == "settings",
                onClick = { onNavigate("settings"); closeDrawer() }
            )
            DrawerMenuItem(
                icon = Icons.Default.Email, // Biểu tượng hòm thư cho Help & Feedback
                label = "Help & Feedback",
                isSelected = currentRoute == "help",
                onClick = { onNavigate("help"); closeDrawer() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tintColor = if (isSelected) Color(0xFFE3350D) else Color(0xFF334F6A)

    NavigationDrawerItem(
        label = { Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = tintColor) },
        icon = { Icon(imageVector = icon, contentDescription = label, tint = tintColor) },
        selected = isSelected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFF5F5F5), // Tạo màu nền xám nhẹ khi mục đó được chọn
            unselectedContainerColor = Color.Transparent
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}