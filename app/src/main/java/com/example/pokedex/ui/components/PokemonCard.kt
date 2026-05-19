package com.example.pokedex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // Thư viện thần thánh giúp tải ảnh

@Composable
fun PokemonCard(
    id: Int,
    name: String,
    types: List<String>,
    imageUrl: String
) {
    // Tự động đổi màu nền thẻ bài dựa vào hệ (Type) của Pokémon
    val bgColor = getTypeColor(types.firstOrNull() ?: "normal")

    Box(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(12.dp)
    ) {
        // --- CỘT TRÁI: ID, TÊN, HỆ ---
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            // ID format dạng #001
            Text(
                text = "#${id.toString().padStart(3, '0')}",
                color = Color.Black.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            // Tên Pokémon
            Text(
                text = name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Các nút hiển thị hệ (Types)
            types.forEach { type ->
                Text(
                    text = type.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // --- GÓC PHẢI DƯỚI: ẢNH POKÉMON TỪ MẠNG ---
        AsyncImage(
            model = imageUrl,
            contentDescription = name,
            modifier = Modifier
                .size(85.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 8.dp, y = 8.dp), // Đẩy ảnh xích xuống góc cho có hiệu ứng 3D
            contentScale = ContentScale.Fit
        )
    }
}

// Hàm phụ trợ giúp tự động chọn màu nền theo hệ
fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "grass" -> Color(0xFF48D0B0)
        "fire" -> Color(0xFFFB6C6C)
        "water" -> Color(0xFF76BDFE)
        "electric" -> Color(0xFFFFCE4B)
        "poison" -> Color(0xFFA33EA1)
        "bug" -> Color(0xFFA6B91A)
        "normal" -> Color(0xFFA8A77A)
        "psychic" -> Color(0xFFF95587)
        "ground" -> Color(0xFFE2BF65)
        "rock" -> Color(0xFFB6A136)
        "fighting" -> Color(0xFFC22E28)
        "ghost" -> Color(0xFF735797)
        "ice" -> Color(0xFF96D9D6)
        "dragon" -> Color(0xFF6F35FC)
        else -> Color(0xFFB0BEC5)
    }
}