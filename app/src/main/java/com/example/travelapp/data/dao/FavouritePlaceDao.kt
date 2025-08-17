package com.example.travelapp.data.dao

import androidx.room.*
import com.example.travelapp.data.model.FavoritePlace
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePlaceDao {
    @Query("SELECT * FROM FavoritePlace")
    fun getAllFavorites(): Flow<List<FavoritePlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoritePlace)

    @Delete
    suspend fun deleteFavorite(favorite: FavoritePlace)
}
