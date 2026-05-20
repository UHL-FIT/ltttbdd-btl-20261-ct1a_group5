package com.example.pokedex.viewmodel

import android.app.Application // Thêm Import Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel // Đổi từ ViewModel sang AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.local.PokemonDatabase
import com.example.pokedex.data.local.PokemonStateEntity
import com.example.pokedex.data.remote.*
import com.example.pokedex.ui.components.getTypeColor
import com.example.pokedex.ui.screens.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

// Trạng thái chung
sealed interface PokemonUiState {
    object Loading : PokemonUiState
    data class Success(val pokemonList: List<PokemonUiModel>) : PokemonUiState
    data class Error(val message: String) : PokemonUiState
}

// Trạng thái cho Detail Screen
sealed interface PokemonDetailState {
    object Idle : PokemonDetailState
    object Loading : PokemonDetailState
    data class Success(val pokemon: DetailedPokemonUiModel) : PokemonDetailState
    data class Error(val message: String) : PokemonDetailState
}

// CHÚ Ý: Kế thừa AndroidViewModel thay vì ViewModel để kết nối Database
class PokemonViewModel(application: Application) : AndroidViewModel(application) {

    // Khởi tạo Database và kết nối với DAO
    private val dao = PokemonDatabase.getDatabase(application).pokemonDao()

    // --- HOME SCREEN LOGIC ---
    var uiState: PokemonUiState by mutableStateOf(PokemonUiState.Loading)
        private set

    // ==========================================
    // QUẢN LÝ TRẠNG THÁI FAVORITE & CAUGHT TỪ DATABASE
    // ==========================================
    var favoriteIds by mutableStateOf(setOf<Int>())
        private set

    var caughtIds by mutableStateOf(setOf<Int>())
        private set

    init {
        // Tải danh sách Pokemon từ mạng
        getGen1To3Pokemon()

        // LẮNG NGHE DATABASE: Tự động cập nhật danh sách Yêu thích mỗi khi Database thay đổi
        viewModelScope.launch {
            dao.getFavoriteIds().collect { list ->
                favoriteIds = list.toSet()
            }
        }

        // LẮNG NGHE DATABASE: Tự động cập nhật danh sách Đã Bắt mỗi khi Database thay đổi
        viewModelScope.launch {
            dao.getCaughtIds().collect { list ->
                caughtIds = list.toSet()
            }
        }
    }

    // --- HÀM LƯU DATABASE KHI BẤM NÚT TRÊN UI ---
    fun toggleFavorite(id: Int) {
        // Đẩy lệnh đọc/ghi Database vào luồng chạy ngầm (IO) để không làm lag giao diện
        viewModelScope.launch(Dispatchers.IO) {
            // Kiểm tra xem con này đã có trong DB chưa, chưa có thì tạo mới
            val currentState = dao.checkPokemonMark(id) ?: PokemonStateEntity(id)
            // Đảo ngược trạng thái hiện tại (Đang true thành false, đang false thành true)
            val newState = currentState.copy(isFavorite = !currentState.isFavorite)
            // Lưu đè lại vào DB
            dao.savePokemonMark(newState)
        }
    }

    fun toggleCaught(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = dao.checkPokemonMark(id) ?: PokemonStateEntity(id)
            val newState = currentState.copy(isCaught = !currentState.isCaught)
            dao.savePokemonMark(newState)
        }
    }

    // ===============================================================
    // CÁC HÀM LẤY DỮ LIỆU TỪ POKE-API (GIỮ NGUYÊN)
    // ===============================================================

    fun getGen1To3Pokemon() {
        viewModelScope.launch {
            uiState = PokemonUiState.Loading
            try {
                val listResponse = RetrofitClient.apiService.getPokemonList(limit = 1025)
                val currentLoadedList = mutableListOf<PokemonUiModel>()

                val chunks = listResponse.results.chunked(50)
                for (chunk in chunks) {
                    val deferreds = chunk.map { result ->
                        async {
                            try { RetrofitClient.apiService.getPokemonDetail(result.name) }
                            catch (e: Exception) { null }
                        }
                    }

                    val detailsList = deferreds.awaitAll().filterNotNull()
                    val mappedChunk = detailsList.map { detail ->
                        fun getStat(s: String) = detail.stats.find { it.stat.name == s }?.base_stat ?: 0
                        PokemonUiModel(
                            id = detail.id,
                            name = detail.name.replaceFirstChar { it.uppercase() },
                            types = detail.types.map { it.type.name },
                            imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${detail.id}.png",
                            hp = getStat("hp"), attack = getStat("attack"), defense = getStat("defense"),
                            spAttack = getStat("special-attack"), spDefense = getStat("special-defense"), speed = getStat("speed")
                        )
                    }.sortedBy { it.id }

                    currentLoadedList.addAll(mappedChunk)
                    uiState = PokemonUiState.Success(currentLoadedList.toList())
                }
            } catch (e: Exception) {
                if (uiState !is PokemonUiState.Success) {
                    uiState = PokemonUiState.Error(e.localizedMessage ?: "Error")
                }
            }
        }
    }

    // --- QUẢN LÝ DỮ LIỆU CHI TIẾT "DATADEX STYLE" ---
    var detailState: PokemonDetailState by mutableStateOf(PokemonDetailState.Idle)
        private set

    fun getPokemonDetailComplete(id: Int) {
        viewModelScope.launch {
            detailState = PokemonDetailState.Loading
            try {
                val basicDeferred = async { RetrofitClient.apiService.getPokemonDetail(id.toString()) }
                val speciesDeferred = async { RetrofitClient.apiService.getPokemonSpecies(id) }
                val locationsDeferred = async { RetrofitClient.apiService.getPokemonLocations(id) }

                val basicRes = basicDeferred.await()
                val speciesRes = speciesDeferred.await()

                val typeDeferreds = basicRes.types.map {
                    async { RetrofitClient.apiService.getTypeDetail(it.type.name) }
                }
                val evolutionDeferred = async { RetrofitClient.apiService.getEvolutionChain(speciesRes.evolution_chain.url) }

                val locationsRes = locationsDeferred.await()
                val evolutionRes = evolutionDeferred.await()
                val typesResList = typeDeferreds.awaitAll()

                val descMap = speciesRes.flavor_text_entries.filter { it.language.name == "en" }.associate { it.version.name.uppercase() to it.flavor_text.replace("\n", " ") }
                val generaName = speciesRes.genera.find { it.language.name == "en" }?.genus ?: "Pokemon"

                val levelMoves = basicRes.moves.flatMap { moveSlot ->
                    moveSlot.version_group_details.filter { it.move_learn_method.name == "level-up" }.map {
                        val mockTypes = listOf("NORMAL", "GRASS", "FIRE", "WATER", "PSYCHIC")
                        val mockClasses = listOf("PHYSICAL", "SPECIAL", "STATUS")
                        MoveUiItem(
                            name = moveSlot.move.name.replace("-", " ").uppercase(),
                            level = it.level_learned_at,
                            method = it.move_learn_method.name,
                            type = mockTypes.random(),
                            power = listOf("40", "60", "90", "-").random(),
                            damageClass = mockClasses.random()
                        )
                    }
                }.sortedBy { it.level }.distinctBy { it.name }

                val evoItems = mutableListOf<EvolutionUiItem>()
                parseEvoChainRecursive(evolutionRes.chain, evoItems)

                val locationMap = mutableMapOf<String, MutableList<String>>()
                locationsRes.forEach { encounter ->
                    encounter.version_details.forEach { versionDetail ->
                        val versionName = versionDetail.version.name.uppercase()
                        if (locationMap[versionName] == null) locationMap[versionName] = mutableListOf()
                        locationMap[versionName]?.add(encounter.location_area.name.replace("-", " ").uppercase())
                    }
                }

                val allTypes = listOf("normal", "fighting", "flying", "poison", "ground", "rock", "bug", "ghost", "steel", "fire", "water", "grass", "electric", "psychic", "ice", "dragon", "dark", "fairy")
                val damageMultiplier = mutableMapOf<String, Float>()
                allTypes.forEach { damageMultiplier[it] = 1f }

                typesResList.forEach { typeRes ->
                    typeRes.damage_relations.double_damage_from.forEach { damageMultiplier[it.name] = damageMultiplier[it.name]!! * 2f }
                    typeRes.damage_relations.half_damage_from.forEach { damageMultiplier[it.name] = damageMultiplier[it.name]!! * 0.5f }
                    typeRes.damage_relations.no_damage_from.forEach { damageMultiplier[it.name] = damageMultiplier[it.name]!! * 0f }
                }

                fun formatMult(v: Float) = if (v == 0.5f) "x ½" else if (v == 0.25f) "x ¼" else if (v == 0f) "x 0" else "x ${v.toInt()}"

                val weakItems = damageMultiplier.filter { it.value > 1f }.map { DamageUiItem(it.key.uppercase(), formatMult(it.value), getTypeColor(it.key)) }
                val resistantItems = damageMultiplier.filter { it.value < 1f && it.value > 0f }.map { DamageUiItem(it.key.uppercase(), formatMult(it.value), getTypeColor(it.key)) }
                val normalItems = damageMultiplier.filter { it.value == 1f }.map { DamageUiItem(it.key.uppercase(), "", getTypeColor(it.key)) }

                val nextId = if (id < 1025) id + 1 else 1

                val detailedPokemon = DetailedPokemonUiModel(
                    basic = PokemonUiModel(
                        id = basicRes.id, name = basicRes.name.replaceFirstChar { it.uppercase() }, types = basicRes.types.map { it.type.name },
                        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${basicRes.id}.png",
                        hp = basicRes.stats[0].base_stat, attack = basicRes.stats[1].base_stat, defense = basicRes.stats[2].base_stat,
                        spAttack = basicRes.stats[3].base_stat, spDefense = basicRes.stats[4].base_stat, speed = basicRes.stats[5].base_stat
                    ),
                    speciesName = generaName, height = basicRes.height / 10f, weight = basicRes.weight / 10f, descriptions = descMap,
                    frontDefaultUrl = basicRes.sprites.front_default, frontShinyUrl = basicRes.sprites.front_shiny,
                    levelUpMoves = levelMoves, evolutionChain = evoItems, locations = locationMap,
                    weakAgainst = weakItems, resistantAgainst = resistantItems, normalDamageFrom = normalItems,
                    catchRate = speciesRes.capture_rate, baseFriendship = speciesRes.base_happiness,
                    growthRate = speciesRes.growth_rate.name.replace("-", " ").uppercase(),
                    eggGroups = speciesRes.egg_groups.map { it.name.replace("-", " ").uppercase() }, genderRate = speciesRes.gender_rate,
                    nextPokemonId = nextId, nextPokemonName = "Pokémon #$nextId", nextPokemonImageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$nextId.png"
                )

                detailState = PokemonDetailState.Success(detailedPokemon)
            } catch (e: Exception) {
                detailState = PokemonDetailState.Error(e.localizedMessage ?: "Failed to load")
            }
        }
    }

    private fun parseEvoChainRecursive(chainLink: ChainLink, items: MutableList<EvolutionUiItem>) {
        val speciesUrl = chainLink.species.url
        val idString = speciesUrl.trimEnd('/').substringAfterLast('/')
        val pokemonId = idString.toIntOrNull() ?: 0

        val triggerDesc = chainLink.evolution_details.firstOrNull()?.let {
            when (it.trigger.name) {
                "level-up" -> "Level ${it.min_level ?: "up"}"
                "use-item" -> "${it.item?.name?.replace("-", " ")?.uppercase()}"
                "trade" -> "Trade"
                else -> ""
            }
        } ?: ""

        items.add(
            EvolutionUiItem(
                id = pokemonId,
                name = chainLink.species.name.replaceFirstChar { it.uppercase() },
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokemonId.png",
                triggerDesc = triggerDesc
            )
        )

        chainLink.evolves_to.forEach { nextStage -> parseEvoChainRecursive(nextStage, items) }
    }
}