package com.example.pokedex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    // --- QUẢN LÝ TRẠNG THÁI FAVORITE / CAUGHT ---

    // ĐÃ THÊM LẠI "suspend"
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePokemonMark(state: PokemonStateEntity)

    // Flow tự xử lý luồng nên không cần suspend
    @Query("SELECT id FROM pokemon_state WHERE isFavorite = 1")
    fun getFavoriteIds(): Flow<List<Int>>

    @Query("SELECT id FROM pokemon_state WHERE isCaught = 1")
    fun getCaughtIds(): Flow<List<Int>>

    // ĐÃ THÊM LẠI "suspend"
    @Query("SELECT * FROM pokemon_state WHERE id = :id")
    suspend fun checkPokemonMark(id: Int): PokemonStateEntity?

    // --- MỚI THÊM: QUẢN LÝ ĐỘI HÌNH TEAM BUILDER ---

    // Lưu thông tin Team (Để demo đơn giản, chúng ta lưu tạm id của 6 pokemon thành chuỗi "1,4,7...")
    // Nếu bạn có Entity riêng cho Team thì đổi lại Type nhé.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTeam(team: TeamEntity)
}