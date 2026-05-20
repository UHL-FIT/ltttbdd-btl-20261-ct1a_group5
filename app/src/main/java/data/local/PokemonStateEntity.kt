package com.example.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tạo một bảng tên là "pokemon_state" trong Database
@Entity(tableName = "pokemon_state")
data class PokemonStateEntity(
    @PrimaryKey val id: Int, // Khóa chính chính là ID của Pokémon (Ví dụ: 1 cho Bulbasaur)
    val isFavorite: Boolean = false,
    val isCaught: Boolean = false
)