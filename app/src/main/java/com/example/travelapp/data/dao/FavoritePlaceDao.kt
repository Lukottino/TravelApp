package com.example.travelapp.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.travelapp.data.model.FavoritePlace

@Dao
interface FavoritePlaceDao {

    @Insert
    suspend fun insertFavorite(place: FavoritePlace): Long

    @Delete
    suspend fun deleteFavorite(place: FavoritePlace)

    @Query("SELECT * FROM favorite_place ORDER BY name ASC")
    fun getAllFavorites(): LiveData<List<FavoritePlace>>
}
