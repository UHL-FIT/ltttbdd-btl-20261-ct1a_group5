package com.example.pokedex.ui.screens

import androidx.compose.ui.graphics.Color

data class DetailedPokemonUiModel(
    val basic: PokemonUiModel,
    val speciesName: String,
    val height: Float,
    val weight: Float,
    val descriptions: Map<String, String>,
    val frontDefaultUrl: String?,
    val frontShinyUrl: String?,

    // Moves
    val levelUpMoves: List<MoveUiItem>,

    val evolutionChain: List<EvolutionUiItem>,
    val locations: Map<String, List<String>>,

    // Damage Math
    val weakAgainst: List<DamageUiItem>,
    val resistantAgainst: List<DamageUiItem>,
    val normalDamageFrom: List<DamageUiItem>,

    // Training & Breeding
    val catchRate: Int,
    val baseFriendship: Int,
    val growthRate: String,
    val eggGroups: List<String>,
    val genderRate: Int,

    // Navigation
    val nextPokemonId: Int,
    val nextPokemonName: String,
    val nextPokemonImageUrl: String
)

data class MoveUiItem(val name: String, val level: Int, val method: String, val type: String = "NORMAL", val power: String = "-", val accuracy: String = "100", val damageClass: String = "PHYSICAL")
data class EvolutionUiItem(val id: Int, val name: String, val imageUrl: String, val triggerDesc: String)
data class DamageUiItem(val typeName: String, val multiplier: String, val color: Color)