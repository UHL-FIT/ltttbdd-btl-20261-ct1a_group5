package com.example.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "team_table")
data class TeamEntity(
    @PrimaryKey
    val teamName: String,
    val pokemonIds: String // Lưu dạng chuỗi: "25,6,150"
)