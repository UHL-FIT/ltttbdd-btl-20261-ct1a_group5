package com.example.pokedex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.random.Random

// --- DATA MODELS ---
data class LocationUiModel(val id: Int, val name: String, val region: String, val pokemonCount: Int)

data class EncounterUiModel(
    val pokemonName: String,
    val imageUrl: String,
    val levelRange: String,
    val rate: String,
    val rateColor: Long, // Xanh lá (Dễ gặp), Vàng (Trung bình), Đỏ (Khó gặp)
    val versions: List<String>
)

data class EncounterGroup(
    val methodTitle: String,
    val methodDescription: String,
    val icon: String? = null,
    val encounters: List<EncounterUiModel>
)

class LocationViewModel : ViewModel() {

    var selectedVersion by mutableStateOf("Scarlet")
        private set

    fun setVersion(version: String) { selectedVersion = version }

    // --- DANH SÁCH BIOME CỐ ĐỊNH ---
    val locationList = listOf(
        LocationUiModel(1, "Bamboo", "Paldea", Random.nextInt(15, 40)),
        LocationUiModel(2, "Beach", "Paldea", Random.nextInt(20, 50)),
        LocationUiModel(3, "Canyon", "Blueberry Academy", Random.nextInt(50, 90)),
        LocationUiModel(4, "Cave", "Paldea", Random.nextInt(30, 60)),
        LocationUiModel(5, "Coastal", "Blueberry Academy", Random.nextInt(60, 100)),
        LocationUiModel(6, "Desert", "Paldea", Random.nextInt(15, 30)),
        LocationUiModel(7, "Flower", "Paldea", Random.nextInt(10, 25)),
        LocationUiModel(8, "Forest", "Paldea", Random.nextInt(30, 60)),
        LocationUiModel(9, "Lake", "Paldea", Random.nextInt(20, 45)),
        LocationUiModel(10, "Mountain", "Paldea", Random.nextInt(40, 75))
    )

    // --- CỖ MÁY TẠO DỮ LIỆU NGẪU NHIÊN CHO TỪNG BIOME ---
    fun getEncountersForLocation(locationId: Int): List<EncounterGroup> {
        val groups = mutableListOf<EncounterGroup>()

        // 1. Tạo nhóm Overworld (Xuất hiện ban ngày)
        groups.add(
            EncounterGroup(
                methodTitle = "Overworld",
                methodDescription = "Visible Pokémon in route, grass, ocean etc...",
                encounters = generateRandomEncounters(count = Random.nextInt(4, 8))
            )
        )

        // 2. Tạo nhóm Nighttime (Chỉ xuất hiện ban đêm)
        if (Random.nextBoolean()) { // 50% cơ hội Biome này có Pokémon ăn đêm
            groups.add(
                EncounterGroup(
                    methodTitle = "Nighttime only",
                    methodDescription = "Pokémon that only appear when the sun goes down.",
                    icon = "moon",
                    encounters = generateRandomEncounters(count = Random.nextInt(1, 4))
                )
            )
        }

        // 3. Tạo nhóm Fishing (Câu cá)
        if (locationId in listOf(2, 5, 9)) { // Chỉ Beach, Coastal, Lake mới có câu cá
            groups.add(
                EncounterGroup(
                    methodTitle = "Surfing / Fishing",
                    methodDescription = "Pokémon found in the water or by using a rod.",
                    icon = "water",
                    encounters = generateRandomEncounters(count = Random.nextInt(2, 5))
                )
            )
        }

        return groups
    }

    // Hàm tiện ích tự động bốc Pokémon và random tỉ lệ
    private fun generateRandomEncounters(count: Int): List<EncounterUiModel> {
        val mockPokemonList = listOf(
            Pair("Pikachu", 25), Pair("Charizard", 6), Pair("Bulbasaur", 1), Pair("Squirtle", 7),
            Pair("Eevee", 133), Pair("Snorlax", 143), Pair("Gengar", 143), Pair("Dragonite", 149),
            Pair("Lucario", 149), Pair("Garchomp", 445), Pair("Togekiss", 468), Pair("Sylveon", 468),
            Pair("Oranguru", 765), Pair("Lokix", 924), Pair("Scyther", 123), Pair("Breloom", 286)
        )

        val encounters = mutableListOf<EncounterUiModel>()

        // Xáo trộn danh sách và lấy ra số lượng cần thiết
        val shuffledPokemon = mockPokemonList.shuffled().take(count)

        for (pokemon in shuffledPokemon) {
            val rate = Random.nextInt(1, 65) // Random tỉ lệ từ 1% đến 65%

            // Tự động gán màu: >= 40% Xanh, >= 15% Vàng, còn lại Đỏ
            val rateColor = when {
                rate >= 40 -> 0xFF2ECC71 // Màu Xanh lá
                rate >= 15 -> 0xFFF1C40F // Màu Vàng
                else -> 0xFFE74C3C       // Màu Đỏ
            }

            val minLevel = Random.nextInt(5, 40)
            val maxLevel = minLevel + Random.nextInt(5, 20)

            // Random version: Có con chỉ có ở Scarlet, có con Violet, có con cả 2
            val versions = when (Random.nextInt(3)) {
                0 -> listOf("Scarlet")
                1 -> listOf("Violet")
                else -> listOf("Scarlet", "Violet")
            }

            encounters.add(
                EncounterUiModel(
                    pokemonName = pokemon.first,
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${pokemon.second}.png",
                    levelRange = "Level $minLevel-$maxLevel",
                    rate = "$rate%",
                    rateColor = rateColor,
                    versions = versions
                )
            )
        }

        // Sắp xếp lại theo tỉ lệ xuất hiện giảm dần (Con nào dễ gặp xếp trên)
        return encounters.sortedByDescending { it.rate.replace("%", "").toInt() }
    }
}