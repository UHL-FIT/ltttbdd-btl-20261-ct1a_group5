package com.example.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    // --- QUẢN LÝ TRẠNG THÁI FAVORITE / CAUGHT ---


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePokemonMark(state: PokemonStateEntity)

    // Flow tự xử lý luồng nên không cần suspend
    @Query("SELECT id FROM pokemon_state WHERE isFavorite = 1")
    fun getFavoriteIds(): Flow<List<Int>>

    @Query("SELECT id FROM pokemon_state WHERE isCaught = 1")
    fun getCaughtIds(): Flow<List<Int>>


    @Query("SELECT * FROM pokemon_state WHERE id = :id")
    suspend fun checkPokemonMark(id: Int): PokemonStateEntity?





    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTeam(team: TeamEntity)
}