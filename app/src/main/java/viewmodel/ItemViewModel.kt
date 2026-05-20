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

// Model giao diện cho Item
data class ItemUiModel(
    val id: Int,
    val rawName: String,
    val displayName: String,
    val imageUrl: String,
    val cost: Int,
    val flingPower: String,
    val category: String,
    val shortEffect: String,
    val gameDescription: String,
    val effect: String
)

sealed interface ItemUiState {
    object Loading : ItemUiState
    data class Success(val items: List<ItemUiModel>) : ItemUiState
    data class Error(val message: String) : ItemUiState
}

class ItemViewModel : ViewModel() {
    var uiState: ItemUiState by mutableStateOf(ItemUiState.Loading)
        private set

    init { fetchItems() }

    fun fetchItems() {
        viewModelScope.launch(Dispatchers.IO) {
            uiState = ItemUiState.Loading
            try {
                // Tải danh sách 2050 vật phẩm
                val listResponse = RetrofitClient.apiService.getItems(limit = 2050)
                val currentLoadedList = mutableListOf<ItemUiModel>()

                // Chunk 50 item mỗi lần tải để không lag máy
                val chunks = listResponse.results.chunked(50)
                for (chunk in chunks) {
                    val deferreds = chunk.map { result ->
                        async {
                            try { RetrofitClient.apiService.getItemDetail(result.name) }
                            catch (e: Exception) { null }
                        }
                    }

                    val details = deferreds.awaitAll().filterNotNull()
                    val mapped = details.map { detail ->
                        // Lọc lấy tiếng Anh
                        val engFlavor = detail.flavor_text_entries.find { it.language.name == "en" }?.text?.replace("\n", " ") ?: "No description available."
                        val engEffect = detail.effect_entries.find { it.language.name == "en" }

                        ItemUiModel(
                            id = detail.id,
                            rawName = detail.name,
                            // Viết hoa chữ cái đầu cho đẹp (vd: ability-capsule -> Ability Capsule)
                            displayName = detail.name.split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                            imageUrl = detail.sprites.default ?: "",
                            cost = detail.cost,
                            flingPower = detail.fling_power?.toString() ?: "-",
                            category = detail.category.name.split("-").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                            shortEffect = engEffect?.short_effect?.replace("\n", " ") ?: "No effect available.",
                            gameDescription = engFlavor,
                            effect = engEffect?.effect?.replace("\n", " ") ?: "No detailed effect."
                        )
                    }

                    currentLoadedList.addAll(mapped)
                    uiState = ItemUiState.Success(currentLoadedList.toList())
                }
            } catch (e: Exception) {
                if (uiState !is ItemUiState.Success) {
                    uiState = ItemUiState.Error(e.localizedMessage ?: "Error loading items")
                }
            }
        }
    }
}
