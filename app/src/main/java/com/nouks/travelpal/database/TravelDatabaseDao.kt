package com.nouks.travelpal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nouks.travelpal.database.entities.*

@Dao
interface TravelDatabaseDao {
    // users
    @Insert
    fun insertUser(user: User): Long?

    @Update
    fun updateUser(user: User)

    @Query("SELECT * from users WHERE id = :id")
    fun getUser(id: Long): User?

    @Query("SELECT * FROM users ORDER BY id LIMIT 1")
    fun getCurrentUser(): User?

    // locations
    @Insert
    fun insertLocation(locationEntity: LocationEntity): Long?

    @Update
    fun updateLocation(locationEntity: LocationEntity)

    @Query("SELECT * from locations WHERE id = :id")
    fun getLocationById(id: Long): LocationEntity?

    @Query("SELECT * from locations WHERE name = :name LIMIT 1")
    fun getLocationByName(name: String): LocationEntity?

    // Travel Data
    @Insert
    fun insertTravel(travel: Travel): Long?

    @Update
    fun updateTravel(travel: Travel)

    @Query("SELECT * from travel_data WHERE id = :id")
    fun getTravel(id: Long): Travel?

    @Query("SELECT * from travel_data ORDER BY id DESC LIMIT 1")
    fun getLatestTravel(): Travel?

    @Query("SELECT * from locations ORDER BY id DESC LIMIT 1")
    fun getLatestLocation(): LocationEntity?

    @Query("SELECT * FROM app_states WHERE name = :name LIMIT 1")
    fun getAutoCompleteUsageState(name: String): AppState?

    @Insert
    fun setAutoCompleteUsageState(appState: AppState)

    @Update
    fun updateAutoCompleteUsageState(appState: AppState)

}