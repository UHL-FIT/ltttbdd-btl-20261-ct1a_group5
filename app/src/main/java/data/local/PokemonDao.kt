package com.example.pokedex.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

// Bảng lưu Dream Team
@Entity(tableName = "dream_team")
data class PokemonEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val mainType: String
)

// Các lệnh thao tác với Database
@Dao
interface PokemonDao {
    @Query("SELECT * FROM dream_team")
    fun getDreamTeam(): Flow<List<PokemonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToTeam(pokemon: PokemonEntity)

    @Delete
    suspend fun removeFromTeam(pokemon: PokemonEntity)

    @Query("SELECT COUNT(*) FROM dream_team")
    suspend fun getTeamCount(): Int
}
