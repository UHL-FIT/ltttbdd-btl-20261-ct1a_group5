package com.example.pokedex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
// --- MỚI: Thêm import để dùng crossfade ---
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

@Composable
fun PokemonCard(
    id: Int,
    name: String,
    types: List<String>,
    imageUrl: String,
    isFavorite: Boolean,
    isCaught: Boolean,
    onFavoriteClick: () -> Unit,
    onCaughtClick: () -> Unit,
    onClick: () -> Unit = {}
) {
    val bgColor = getTypeColor(types.firstOrNull() ?: "normal")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            // --- CỘT TRÁI (CHỮ & HỆ) ---
            Column(
                modifier = Modifier
                    .weight(1f) // Chiếm phần không gian còn lại
                    .padding(start = 20.dp, top = 16.dp, bottom = 16.dp)
            ) {
                // ROW chứa ID và các Icon đánh dấu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${id.toString().padStart(3, '0')}",
                        color = Color.Black.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1, // Không cho rớt dòng
                        // --- 1. SỬA CHÍNH: Ép ID lùi lại để dành chỗ cố định cho Icon ---
                        modifier = Modifier.weight(1f)
                    )

                    // NÚT YÊU THÍCH (STAR)
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFFCE4B) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onFavoriteClick() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // NÚT ĐÃ BẮT (CAUGHT)
                    Icon(
                        imageVector = if (isCaught) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = "Caught",
                        tint = if (isCaught) Color(0xFF48D0B0) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { onCaughtClick() }
                    )
                }

                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    types.forEach { type ->
                        Text(
                            text = type.uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // --- CỘT PHẢI: ẢNH ---
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomEnd
            ) {

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true) // Bật hiệu ứng mờ dần ( crossfade ) cực kỳ đẹp mắt khi ảnh nạp xong
                        .build(),
                    contentDescription = name,
                    modifier = Modifier
                        .size(120.dp) //
                        .offset(x = 8.dp, y = 8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

// Giữ nguyên getTypeColor ở dưới...
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
        "アイス" -> Color(0xFF96D9D6)
        "dragon" -> Color(0xFF6F35FC)
        "dark" -> Color(0xFF705848)
        "steel" -> Color(0xFFB8B8D0)
        "fairy" -> Color(0xFFEE99AC)
        "flying" -> Color(0xFFA890F0)
        else -> Color(0xFFB0BEC5)
    }
}