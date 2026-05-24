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

// Model UI cho Nature
data class NatureUiModel(
    val id: Int,
    val name: String,
    val increasedStat: String,
    val decreasedStat: String,
    val likesFlavor: String,
    val dislikesFlavor: String
)

sealed interface NatureUiState {
    object Loading : NatureUiState
    data class Success(val natures: List<NatureUiModel>) : NatureUiState
    data class Error(val message: String) : NatureUiState
}

class NatureViewModel : ViewModel() {
    var uiState: NatureUiState by mutableStateOf(NatureUiState.Loading)
        private set

    // Biến cho thanh tìm kiếm Bong bóng
    var searchQuery by mutableStateOf("")
    var isSearchActive by mutableStateOf(false)

    private val allNaturesList = mutableListOf<NatureUiModel>()

    init { fetchNatures() }

    private fun fetchNatures() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState = NatureUiState.Loading
            try {
                // Chỉ có 25 Nature trong toàn bộ game
                val listResponse = RetrofitClient.apiService.getNatures(limit = 30)

                val deferreds = listResponse.results.map { result ->
                    async { RetrofitClient.apiService.getNatureDetail(result.name) }
                }

                val details = deferreds.awaitAll()
                val mapped = details.map { detail ->
                    NatureUiModel(
                        id = detail.id,
                        name = detail.name.replaceFirstChar { it.uppercase() },
                        increasedStat = formatStat(detail.increased_stat?.name),
                        decreasedStat = formatStat(detail.decreased_stat?.name),
                        likesFlavor = formatFlavor(detail.likes_flavor?.name, "Likes", "flavor"),
                        dislikesFlavor = formatFlavor(detail.hates_flavor?.name, "Dislikes", "flavor")
                    )
                }

                allNaturesList.addAll(mapped.sortedBy { it.name })
                applyFilters()
            } catch (e: Exception) {
                uiState = NatureUiState.Error(e.localizedMessage ?: "Error loading natures")
            }
        }
    }

    fun applyFilters() {
        val filtered = if (searchQuery.isNotEmpty()) {
            allNaturesList.filter { it.name.contains(searchQuery, ignoreCase = true) }
        } else {
            allNaturesList
        }
        uiState = NatureUiState.Success(filtered)
    }

    // Các hàm làm đẹp chữ
    private fun formatStat(stat: String?): String {
        if (stat == null) return "-"
        return when (stat) {
            "special-attack" -> "Sp. Attack"
            "special-defense" -> "Sp. Defense"
            else -> stat.replaceFirstChar { it.uppercase() }
        }
    }

    private fun formatFlavor(flavor: String?, prefix: String, suffix: String): String {
        if (flavor == null) return "-"
        return "$prefix ${flavor.replaceFirstChar { it.uppercase() }} $suffix"
    }
}