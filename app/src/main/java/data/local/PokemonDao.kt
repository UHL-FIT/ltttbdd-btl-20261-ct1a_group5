package com.example.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    // ĐỔI TÊN và BỎ "suspend" để trị dứt điểm lỗi KSP
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun savePokemonMark(state: PokemonStateEntity)

    @Query("SELECT id FROM pokemon_state WHERE isFavorite = 1")
    fun getFavoriteIds(): Flow<List<Int>>

    @Query("SELECT id FROM pokemon_state WHERE isCaught = 1")
    fun getCaughtIds(): Flow<List<Int>>

    // ĐỔI TÊN và BỎ "suspend"
    @Query("SELECT * FROM pokemon_state WHERE id = :id")
    fun checkPokemonMark(id: Int): PokemonStateEntity?
}