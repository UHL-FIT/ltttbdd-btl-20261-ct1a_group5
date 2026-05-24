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

// Model giao diện cho 1 Kỹ năng
data class MoveUiModel(
    val id: Int,
    val name: String,
    val power: String,
    val accuracy: String,
    val pp: String,
    val type: String,          // Hệ (Normal, Fire...)
    val category: String,      // Physical, Special, Status
    val gameDescription: String,
    val effect: String,
    val generation: String     // Gen 1, Gen 2...
)

sealed interface MoveUiState {
    object Loading : MoveUiState
    data class Success(val moves: List<MoveUiModel>) : MoveUiState
    data class Error(val message: String) : MoveUiState
}

class MoveViewModel : ViewModel() {
    var uiState: MoveUiState by mutableStateOf(MoveUiState.Loading)
        private set

    // --- BIẾN QUẢN LÝ BỘ LỌC (FILTERS) ---
    var selectedGen by mutableStateOf("ALL GENS")
    var selectedType by mutableStateOf("ALL TYPES")
    var selectedCategory by mutableStateOf("ALL CAT.")

    // Biến lưu trữ toàn bộ dữ liệu gốc để tiện việc lọc
    private val allMovesList = mutableListOf<MoveUiModel>()

    init { fetchMoves() }

    private fun fetchMoves() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState = MoveUiState.Loading
            try {
                // Tải danh sách khoảng 900+ moves
                val listResponse = RetrofitClient.apiService.getMoves(limit = 930)

                val chunks = listResponse.results.chunked(50)
                for (chunk in chunks) {
                    val deferreds = chunk.map { result ->
                        async {
                            try { RetrofitClient.apiService.getMoveDetail(result.name) }
                            catch (e: Exception) { null }
                        }
                    }

                    val details = deferreds.awaitAll().filterNotNull()
                    val mapped = details.map { detail ->
                        val engFlavor = detail.flavor_text_entries.find { it.language.name == "en" }?.flavor_text?.replace("\n", " ") ?: "No description available."
                        val engEffect = detail.effect_entries.find { it.language.name == "en" }

                        MoveUiModel(
                            id = detail.id,
                            name = detail.name.split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                            power = detail.power?.toString() ?: "-",
                            accuracy = detail.accuracy?.toString() ?: "-",
                            pp = detail.pp?.toString() ?: "-",
                            type = detail.type.name.uppercase(),
                            category = detail.damage_class.name.uppercase(), // PHYSICAL, SPECIAL, STATUS
                            gameDescription = engFlavor,
                            effect = engEffect?.effect?.replace("\n", " ") ?: "No detailed effect.",
                            generation = mapGeneration(detail.generation.name)
                        )
                    }

                    allMovesList.addAll(mapped)
                    // Cập nhật giao diện ngay lập tức với dữ liệu đã lọc
                    applyFilters()
                }
            } catch (e: Exception) {
                if (uiState !is MoveUiState.Success) {
                    uiState = MoveUiState.Error(e.localizedMessage ?: "Error loading moves")
                }
            }
        }
    }

    // --- HÀM ÁP DỤNG BỘ LỌC ---
    fun applyFilters() {
        var filtered = allMovesList.toList()

        if (selectedGen != "ALL GENS") {
            filtered = filtered.filter { it.generation.equals(selectedGen, ignoreCase = true) }
        }
        if (selectedType != "ALL TYPES") {
            filtered = filtered.filter { it.type.equals(selectedType, ignoreCase = true) }
        }
        if (selectedCategory != "ALL CAT.") {
            filtered = filtered.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }

        uiState = MoveUiState.Success(filtered)
    }

    // Hàm chuyển đổi "generation-i" thành "GEN 1"
    private fun mapGeneration(genString: String): String {
        return when (genString) {
            "generation-i" -> "GEN 1"
            "generation-ii" -> "GEN 2"
            "generation-iii" -> "GEN 3"
            "generation-iv" -> "GEN 4"
            "generation-v" -> "GEN 5"
            "generation-vi" -> "GEN 6"
            "generation-vii" -> "GEN 7"
            "generation-viii" -> "GEN 8"
            "generation-ix" -> "GEN 9"
            else -> "UNKNOWN GEN"
        }
    }
}
