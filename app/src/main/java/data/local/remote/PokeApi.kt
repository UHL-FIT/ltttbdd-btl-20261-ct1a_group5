package com.example.pokedex.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ========================================================
// 1. DATA MODELS: Các "khuôn" để hứng dữ liệu JSON từ mạng về
// ========================================================

// Hứng danh sách tổng quát (Lấy tên và url chứa ID)
data class PokemonListResponse(
    val results: List<PokemonResult>
)

data class PokemonResult(
    val name: String,
    val url: String
)

// Hứng chi tiết một con Pokemon (Để lấy chính xác Hệ - Type của nó)
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val types: List<TypeSlot>
)

data class TypeSlot(
    val type: TypeData
)

data class TypeData(
    val name: String
)

// ========================================================
// 2. API INTERFACE: Định nghĩa các đường dẫn kết nối
// ========================================================
interface PokeApiService {
    // Gọi danh sách 386 con (Gen 1, 2, 3)
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 386,
        @Query("offset") offset: Int = 0
    ): PokemonListResponse

    // Gọi chi tiết từng con (để lấy hệ nguyên tố)
    @GET("pokemon/{name}")
    suspend fun getPokemonDetail(
        @Path("name") name: String
    ): PokemonDetailResponse
}

// ========================================================
// 3. RETROFIT CLIENT: Cỗ máy thực hiện việc gọi mạng
// ========================================================
object RetrofitClient {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    val apiService: PokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }
}