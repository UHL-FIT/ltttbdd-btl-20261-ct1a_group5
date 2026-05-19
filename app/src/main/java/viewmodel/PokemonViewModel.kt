package com.example.pokedex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.remote.RetrofitClient
import com.example.pokedex.ui.screens.PokemonUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

// Trạng thái của màn hình: Đang tải, Tải thành công, hoặc Lỗi mạng
sealed interface PokemonUiState {
    object Loading : PokemonUiState
    data class Success(val pokemonList: List<PokemonUiModel>) : PokemonUiState
    data class Error(val message: String) : PokemonUiState
}

class PokemonViewModel : ViewModel() {

    // Biến lưu giữ trạng thái UI để HomeScreen quan sát
    var uiState: PokemonUiState by mutableStateOf(PokemonUiState.Loading)
        private set

    init {
        // Tự động gọi lấy dữ liệu ngay khi App vừa mở
        getGen1To3Pokemon()
    }

    fun getGen1To3Pokemon() {
        viewModelScope.launch {
            uiState = PokemonUiState.Loading
            try {
                // 1. Gọi API lấy nhanh danh sách tên của 386 con (Gen 1, 2, 3)
                val listResponse = RetrofitClient.apiService.getPokemonList(limit = 386)

                // 2. Kỹ thuật đỉnh cao: Chạy song song (Async) để lấy chi tiết Hệ (Types) của từng con cùng một lúc
                val deferredDetails = listResponse.results.map { result ->
                    async {
                        try {
                            RetrofitClient.apiService.getPokemonDetail(result.name)
                        } catch (e: Exception) {
                            null // Nếu 1 con bị lỗi thì bỏ qua để không sập cả danh sách
                        }
                    }
                }

                // Đợi tất cả các tiến trình chạy song song hoàn thành
                val detailsList = deferredDetails.awaitAll().filterNotNull()

                // 3. Ánh xạ dữ liệu thô từ API thành Model UI gọn gàng của bạn
                val mappedList = detailsList.map { detail ->
                    // Sử dụng link ảnh Artwork chính thức chất lượng cao từ server GitHub của PokéAPI
                    val officialImageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${detail.id}.png"

                    val typeNames = detail.types.map { it.type.name }

                    PokemonUiModel(
                        id = detail.id,
                        name = detail.name.replaceFirstChar { it.uppercase() }, // Viết hoa chữ cái đầu cho đẹp
                        types = typeNames,
                        imageUrl = officialImageUrl
                    )
                }.sortedBy { it.id } // Sắp xếp chuẩn chỉ tăng dần từ #001 trở đi

                // Đẩy dữ liệu thật lên giao diện
                uiState = PokemonUiState.Success(mappedList)

            } catch (e: Exception) {
                // Nếu mất mạng hoặc lỗi server, báo lỗi ra màn hình
                uiState = PokemonUiState.Error(e.localizedMessage ?: "Network Error Connection")
            }
        }
    }
}