package com.example.pokedex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

// --- MODELS UI ---
data class AbilityUiModel(
    val id: Int,
    val name: String,
    val shortEffect: String,
    val effect: String,
    val inDepthEffect: String,
    val gameDescription: String,
    val generation: String,
    val pokemonList: List<AbilityPokemonUiModel> // Chứa danh sách Pokemon
)

data class AbilityPokemonUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val types: List<String> // Dùng để tô màu Background thẻ bài
)

sealed interface AbilityUiState {
    object Loading : AbilityUiState
    data class Success(val abilities: List<AbilityUiModel>) : AbilityUiState
    data class Error(val message: String) : AbilityUiState
}

class AbilityViewModel : ViewModel() {
    var uiState: AbilityUiState by mutableStateOf(AbilityUiState.Loading)
        private set

    // --- BIẾN TRẠNG THÁI CHO TÌM KIẾM VÀ LỌC ---
    var selectedGen by mutableStateOf("ALL GENS")
    var searchQuery by mutableStateOf("")
    var isSearchActive by mutableStateOf(false)

    private val allAbilitiesList = mutableListOf<AbilityUiModel>()

    init { fetchAbilities() }

    private fun fetchAbilities() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState = AbilityUiState.Loading
            try {
                // Tải danh sách khoảng 300+ Abilities
                val listResponse = RetrofitClient.apiService.getAbilities(limit = 350)
                val chunks = listResponse.results.chunked(50)

                for (chunk in chunks) {
                    val deferreds = chunk.map { result ->
                        async {
                            try { RetrofitClient.apiService.getAbilityDetail(result.name) }
                            catch (e: Exception) { null }
                        }
                    }

                    val details = deferreds.awaitAll().filterNotNull()
                    val mapped = details.map { detail ->
                        val engFlavor = detail.flavor_text_entries.find { it.language.name == "en" }?.flavor_text?.replace("\n", " ") ?: "No description available."
                        val engEffect = detail.effect_entries.find { it.language.name == "en" }

                        // Lấy tối đa 15 Pokemon đại diện cho mượt máy
                        val pokemons = detail.pokemon.take(15).map { poke ->
                            val pokeId = poke.pokemon.url.trimEnd('/').substringAfterLast("/").toIntOrNull() ?: 1
                            val formattedName = poke.pokemon.name.split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

                            // Mock Types để UI thẻ bài nằm ngang có màu rực rỡ giống Concept
                            val mockTypes = listOf("GRASS", "FIRE", "WATER", "BUG", "NORMAL", "POISON", "ELECTRIC", "GROUND", "FAIRY", "FIGHTING", "PSYCHIC", "ROCK", "GHOST", "ICE", "DRAGON", "DARK", "STEEL", "FLYING")
                            val randomType1 = mockTypes.random()
                            val randomType2 = mockTypes.random()
                            val types = if (randomType1 != randomType2) listOf(randomType1, randomType2) else listOf(randomType1)

                            AbilityPokemonUiModel(
                                id = pokeId,
                                name = formattedName,
                                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png",
                                types = types
                            )
                        }

                        AbilityUiModel(
                            id = detail.id,
                            name = detail.name.split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                            shortEffect = engEffect?.short_effect?.replace("\n", " ") ?: "No short effect.",
                            effect = engEffect?.effect?.replace("\n", " ") ?: "No effect available.",
                            inDepthEffect = engEffect?.effect?.replace("\n", " ") ?: "No in-depth effect.",
                            gameDescription = engFlavor,
                            generation = mapGen(detail.generation.name),
                            pokemonList = pokemons
                        )
                    }

                    allAbilitiesList.addAll(mapped)
                    applyFilters() // Cập nhật màn hình liên tục
                }
            } catch (e: Exception) {
                if (uiState !is AbilityUiState.Success) {
                    uiState = AbilityUiState.Error(e.localizedMessage ?: "Error")
                }
            }
        }
    }

    // --- HÀM XỬ LÝ LỌC VÀ TÌM KIẾM ---
    fun applyFilters() {
        var filtered = allAbilitiesList.toList()

        if (selectedGen != "ALL GENS") {
            filtered = filtered.filter { it.generation.equals(selectedGen, ignoreCase = true) }
        }

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

        // Sắp xếp A-Z
        uiState = AbilityUiState.Success(filtered.sortedBy { it.name })
    }

    private fun mapGen(gen: String): String {
        return when (gen) {
            "generation-iii" -> "GEN 3"
            "generation-iv" -> "GEN 4"
            "generation-v" -> "GEN 5"
            "generation-vi" -> "GEN 6"
            "generation-vii" -> "GEN 7"
            "generation-viii" -> "GEN 8"
            "generation-ix" -> "GEN 9"
            else -> "ALL GENS" // Đặc tính (Ability) bắt đầu có từ Gen 3
        }
    }
}