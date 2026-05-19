package com.example.pokedex.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val TypeGrass = Color(0xFF48D0B0)
val TypeFire = Color(0xFFFB6C6C)
val TypeWater = Color(0xFF76BDFE)
val TypeElectric = Color(0xFFFFD86F)
val TypePoison = Color(0xFFA974BC)
val TypeNormal = Color(0xFF929DA3) // Màu mặc định nếu không khớp hệ
val TypeDefault = Color(0xFFC8C8C8)

fun getTypeColor(type: String): Color {
    return when (type.lowercase()) {
        "grass" -> TypeGrass
        "fire" -> TypeFire
        "water" -> TypeWater
        "electric" -> TypeElectric
        "poison" -> TypePoison
        "normal" -> TypeNormal
        else -> TypeDefault
    }
}