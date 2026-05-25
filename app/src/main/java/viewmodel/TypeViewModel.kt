package com.example.pokedex.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.remote.DamageRelations
import com.example.pokedex.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

// 18 Hệ cơ bản của Pokemon
val ALL_POKEMON_TYPES = listOf(
    "NORMAL", "FIGHTING", "FLYING", "POISON", "GROUND", "ROCK", "BUG", "GHOST", "STEEL",
    "FIRE", "WATER", "GRASS", "ELECTRIC", "PSYCHIC", "ICE", "DRAGON", "DARK", "FAIRY"
)

class TypeViewModel : ViewModel() {
    var isLoading by mutableStateOf(true)
        private set

    // Lưu trữ dữ liệu API của 18 hệ
    private val typeDataMap = mutableMapOf<String, DamageRelations>()

    // Biến trạng thái UI do người dùng chọn
    var primaryType by mutableStateOf<String?>(null)
    var secondaryType by mutableStateOf<String?>(null)

    init {
        fetchAllTypes()
    }

    private fun fetchAllTypes() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                // Tải đồng loạt 18 hệ để tiết kiệm thời gian
                val deferreds = ALL_POKEMON_TYPES.map { type ->
                    async {
                        val response = RetrofitClient.apiService.getTypeDetail(type.lowercase())
                        type to response.damage_relations
                    }
                }
                deferreds.awaitAll().forEach { (typeName, relations) ->
                    typeDataMap[typeName] = relations
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    // --- HÀM TÍNH TOÁN SÁT THƯƠNG NHẬN VÀO (PHÒNG THỦ) ---
    fun calculateDamageTaken(): Map<String, Float> {
        val result = mutableMapOf<String, Float>()
        ALL_POKEMON_TYPES.forEach { result[it] = 1f } // Mặc định tất cả x1

        val type1 = primaryType
        var type2 = secondaryType
        if (type1 == type2) type2 = null // Tránh bị nhân đôi nếu chọn 2 hệ giống nhau

        if (type1 != null) applyDefensiveMultipliers(type1, result)
        if (type2 != null) applyDefensiveMultipliers(type2, result)

        return result
    }

    private fun applyDefensiveMultipliers(type: String, map: MutableMap<String, Float>) {
        val relations = typeDataMap[type] ?: return
        relations.double_damage_from.forEach { map[it.name.uppercase()] = map[it.name.uppercase()]!! * 2f }
        relations.half_damage_from.forEach { map[it.name.uppercase()] = map[it.name.uppercase()]!! * 0.5f }
        relations.no_damage_from.forEach { map[it.name.uppercase()] = map[it.name.uppercase()]!! * 0f }
    }

    // --- HÀM TÍNH TOÁN SÁT THƯƠNG GÂY RA (TẤN CÔNG) ---
    fun getDamageDealt(type: String): Map<String, Float> {
        val result = mutableMapOf<String, Float>()
        ALL_POKEMON_TYPES.forEach { result[it] = 1f }

        val relations = typeDataMap[type] ?: return result
        relations.double_damage_to.forEach { result[it.name.uppercase()] = 2f }
        relations.half_damage_to.forEach { result[it.name.uppercase()] = 0.5f }
        relations.no_damage_to.forEach { result[it.name.uppercase()] = 0f }

        return result
    }
}