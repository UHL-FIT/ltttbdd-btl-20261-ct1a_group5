package com.example.pokedex.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

// ========================================================
// 1. DATA MODELS: Các "khuôn" hứng JSON từ mạng về
// ========================================================
data class PokemonListResponse(val results: List<PokemonResult>)
data class PokemonResult(val name: String, val url: String)

// --- BASIC POKEMON INFO ---
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlot>,
    val stats: List<StatSlot>,
    val sprites: SpriteResponse,
    val moves: List<MoveSlot>
)
data class TypeSlot(val type: NamedApiResource)
data class StatSlot(val base_stat: Int, val stat: NamedApiResource)
data class SpriteResponse(val front_default: String?, val front_shiny: String?)
data class NamedApiResource(val name: String, val url: String)
data class MoveSlot(val move: NamedApiResource, val version_group_details: List<VersionGroupDetail>)
data class VersionGroupDetail(
    val level_learned_at: Int,
    val move_learn_method: NamedApiResource,
    val version_group: NamedApiResource
)

// --- SPECIES INFO ---
data class PokemonSpeciesResponse(
    val flavor_text_entries: List<FlavorTextEntry>,
    val genera: List<Genus>,
    val evolution_chain: ApiResource,
    val capture_rate: Int,
    val base_happiness: Int,
    val growth_rate: NamedApiResource,
    val egg_groups: List<NamedApiResource>,
    val gender_rate: Int
)
data class FlavorTextEntry(val flavor_text: String, val language: NamedApiResource, val version: NamedApiResource)
data class Genus(val genus: String, val language: NamedApiResource)
data class ApiResource(val url: String)

// --- EVOLUTION CHAIN ---
data class EvolutionChainResponse(val chain: ChainLink)
data class ChainLink(
    val species: NamedApiResource,
    val evolves_to: List<ChainLink>,
    val evolution_details: List<EvolutionDetail>
)
data class EvolutionDetail(val min_level: Int?, val trigger: NamedApiResource, val item: NamedApiResource?)

// --- LOCATION ENCOUNTERS ---
data class LocationEncounterResponse(val location_area: NamedApiResource, val version_details: List<VersionEncounterDetail>)
data class VersionEncounterDetail(val version: NamedApiResource)

// --- TYPE EFFECTIVENESS ---
data class TypeDetailResponse(val damage_relations: DamageRelations)
data class DamageRelations(
    val double_damage_from: List<NamedApiResource>,
    val half_damage_from: List<NamedApiResource>,
    val no_damage_from: List<NamedApiResource>
)

// ==========================================
// --- MỚI THÊM: ITEM INFO (Dành cho Item Dex) ---
// ==========================================
data class ItemListResponse(val results: List<NamedApiResource>)

data class ItemDetailResponse(
    val id: Int,
    val name: String,
    val cost: Int,
    val fling_power: Int?,
    val category: NamedApiResource,
    val effect_entries: List<ItemEffectEntry>,
    val flavor_text_entries: List<ItemFlavorText>,
    val sprites: ItemSprites
)
data class ItemEffectEntry(val effect: String, val short_effect: String, val language: NamedApiResource)
data class ItemFlavorText(val text: String, val language: NamedApiResource)
data class ItemSprites(val default: String?)

// ========================================================
// 2. API INTERFACE: Mở rộng các đường kết nối
// ========================================================
interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(@Query("limit") limit: Int = 386, @Query("offset") offset: Int = 0): PokemonListResponse

    @GET("pokemon/{id}")
    suspend fun getPokemonDetail(@Path("id") id: String): PokemonDetailResponse

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): PokemonSpeciesResponse

    @GET("pokemon/{id}/encounters")
    suspend fun getPokemonLocations(@Path("id") id: Int): List<LocationEncounterResponse>

    @GET("type/{name}")
    suspend fun getTypeDetail(@Path("name") name: String): TypeDetailResponse

    @GET
    suspend fun getEvolutionChain(@Url url: String): EvolutionChainResponse

    // --- MỚI THÊM: 2 API CHO VẬT PHẨM (ITEM) ---
    @GET("item")
    suspend fun getItems(@Query("limit") limit: Int = 2050, @Query("offset") offset: Int = 0): ItemListResponse

    @GET("item/{name}")
    suspend fun getItemDetail(@Path("name") name: String): ItemDetailResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://pokeapi.co/api/v2/"
    val apiService: PokeApiService by lazy {
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(PokeApiService::class.java)
    }
}