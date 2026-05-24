package com.example.pokedex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random

data class LocationUiModel(val id: Int, val name: String, val region: String, val pokemonCount: Int)

data class EncounterUiModel(
    val pokemonName: String, val imageUrl: String, val levelRange: String,
    val rate: String, val typeColor: Long, val versions: List<String>
)

data class EncounterGroup(
    val methodTitle: String, val methodDescription: String,
    val icon: String? = null, val encounters: List<EncounterUiModel>
)

class LocationViewModel : ViewModel() {

    // --- BIẾN TRẠNG THÁI CHO GAME VERSION ---
    var selectedVersion by mutableStateOf("Scarlet")
        private set

    fun setVersion(version: String) { selectedVersion = version }

    // Logic thông minh: Suy ra vùng đất dựa trên phiên bản Game
    fun getRegionForVersion(version: String): String {
        return when (version) {
            "Red", "Blue", "Yellow", "FireRed", "LeafGreen", "Let's Go Pikachu", "Let's Go Eevee" -> "Kanto"
            "Gold", "Silver", "Crystal", "HeartGold", "SoulSilver" -> "Johto"
            "Ruby", "Sapphire", "Emerald", "Omega Ruby", "Alpha Sapphire" -> "Hoenn"
            "Diamond", "Pearl", "Platinum", "Brilliant Diamond", "Shining Pearl", "Legends: Arceus" -> "Sinnoh"
            "Black", "White", "Black 2", "White 2" -> "Unova"
            "X", "Y" -> "Kalos"
            "Sun", "Moon", "Ultra Sun", "Ultra Moon" -> "Alola"
            "Sword", "Shield" -> "Galar"
            "Scarlet", "Violet" -> "Paldea"
            else -> "Paldea" // Mặc định
        }
    }

    // --- DANH SÁCH ĐỊA ĐIỂM (TỪ KANTO ĐẾN PALDEA) ---
    val allLocations = listOf(
        // Kanto
        LocationUiModel(101, "Pallet Town", "Kanto", Random.nextInt(5, 10)),
        LocationUiModel(102, "Viridian Forest", "Kanto", Random.nextInt(15, 30)),
        LocationUiModel(103, "Mt. Moon", "Kanto", Random.nextInt(20, 45)),

        // Johto
        LocationUiModel(201, "Route 29", "Johto", Random.nextInt(10, 20)),
        LocationUiModel(202, "Ilex Forest", "Johto", Random.nextInt(15, 30)),
        LocationUiModel(203, "Lake of Rage", "Johto", Random.nextInt(25, 50)),

        // Hoenn
        LocationUiModel(301, "Petalburg Woods", "Hoenn", Random.nextInt(10, 25)),
        LocationUiModel(302, "Mt. Chimney", "Hoenn", Random.nextInt(20, 40)),
        LocationUiModel(303, "Shoal Cave", "Hoenn", Random.nextInt(15, 35)),

        // Sinnoh
        LocationUiModel(401, "Mt. Coronet", "Sinnoh", Random.nextInt(40, 80)),
        LocationUiModel(402, "Great Marsh", "Sinnoh", Random.nextInt(20, 45)),

        // Unova
        LocationUiModel(501, "Pinwheel Forest", "Unova", Random.nextInt(20, 40)),
        LocationUiModel(502, "Desert Resort", "Unova", Random.nextInt(25, 50)),

        // Kalos
        LocationUiModel(601, "Santalune Forest", "Kalos", Random.nextInt(15, 35)),
        LocationUiModel(602, "Terminus Cave", "Kalos", Random.nextInt(30, 60)),

        // Alola
        LocationUiModel(701, "Melemele Meadow", "Alola", Random.nextInt(15, 30)),
        LocationUiModel(702, "Mount Lanakila", "Alola", Random.nextInt(30, 55)),

        // Galar
        LocationUiModel(801, "Slumbering Weald", "Galar", Random.nextInt(10, 25)),
        LocationUiModel(802, "Wild Area", "Galar", Random.nextInt(60, 120)),

        // Paldea
        LocationUiModel(1, "Bamboo", "Paldea", Random.nextInt(15, 40)),
        LocationUiModel(2, "Beach", "Paldea", Random.nextInt(20, 50)),
        LocationUiModel(3, "Canyon", "Paldea", Random.nextInt(50, 90)),
        LocationUiModel(4, "Cave", "Paldea", Random.nextInt(30, 60)),
        LocationUiModel(5, "Coastal", "Paldea", Random.nextInt(60, 100)),
        LocationUiModel(6, "Desert", "Paldea", Random.nextInt(15, 30))
    )

    fun getEncountersForLocation(locationId: Int): List<EncounterGroup> {
        val groups = mutableListOf<EncounterGroup>()
        groups.add(EncounterGroup("Overworld", "Visible Pokémon in route, grass, ocean etc...", null, generateRandomEncounters(Random.nextInt(4, 8))))
        if (Random.nextBoolean()) groups.add(EncounterGroup("Nighttime only", "Pokémon that only appear when the sun goes down.", "moon", generateRandomEncounters(Random.nextInt(1, 4))))
        if (Random.nextBoolean()) groups.add(EncounterGroup("Surfing / Fishing", "Pokémon found in the water or by using a rod.", "water", generateRandomEncounters(Random.nextInt(2, 5))))
        return groups
    }

    private fun generateRandomEncounters(count: Int): List<EncounterUiModel> {
        val mockPokemonList = listOf(
            Triple("Pikachu", 25, 0xFFFFCE4B), Triple("Charizard", 6, 0xFFFB6C6C), Triple("Bulbasaur", 1, 0xFF48D0B0),
            Triple("Squirtle", 7, 0xFF76BDFE), Triple("Gengar", 94, 0xFF735797), Triple("Lucario", 448, 0xFFC22E28),
            Triple("Garchomp", 445, 0xFF6F35FC), Triple("Sylveon", 700, 0xFFEE99AC), Triple("Scyther", 123, 0xFFA6B91A),
            Triple("Umbreon", 197, 0xFF705848), Triple("Metagross", 444, 0xFFB8B8D0), Triple("Glaceon", 471, 0xFF96D9D6)
        )
        val encounters = mutableListOf<EncounterUiModel>()
        val shuffledPokemon = mockPokemonList.shuffled().take(count)

        for (pokemon in shuffledPokemon) {
            val rate = Random.nextInt(1, 65)
            encounters.add(
                EncounterUiModel(
                    pokemonName = pokemon.first,
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.second}.png",
                    levelRange = "Level ${Random.nextInt(5, 40)}-${Random.nextInt(45, 60)}",
                    rate = "$rate%", typeColor = pokemon.third.toLong(),
                    versions = listOf(selectedVersion) // Bắt đúng version đang chọn
                )
            )
        }
        return encounters.sortedByDescending { it.rate.replace("%", "").toInt() }
    }
}