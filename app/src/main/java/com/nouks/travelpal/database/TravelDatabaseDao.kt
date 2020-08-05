package com.nouks.travelpal.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nouks.travelpal.database.entities.LocationEntity
import com.nouks.travelpal.database.entities.Travel
import com.nouks.travelpal.database.entities.User

@Dao
interface TravelDatabaseDao {
    // users
    @Insert
    fun insertUser(user: User)

    @Update
    fun updateUser(user: User)

    @Query("SELECT * from users WHERE id = :id")
    fun getUser(id: Long): User?

    // locations
    @Insert
    fun insertLocation(locationEntity: LocationEntity)

    @Update
    fun updateLocation(locationEntity: LocationEntity)

    @Query("SELECT * from locations WHERE id = :id")
    fun getLocationById(id: Long): LocationEntity?

    @Query("SELECT * from locations WHERE name = :name LIMIT 1")
    fun getLocationByName(name: String): LocationEntity?

    // Travel Data
    @Insert
    fun insertTravel(travel: Travel)

    @Update
    fun updateTravel(travel: Travel)

    @Query("SELECT * from travel_data WHERE id = :id")
    fun getTravel(id: Long): Travel?

    @Query("SELECT * from travel_data ORDER BY id DESC LIMIT 1")
    fun getLatestTravel(): Travel?

    @Query("SELECT * from locations ORDER BY id DESC LIMIT 1")
    fun getLatestLocation(): LocationEntity?

}