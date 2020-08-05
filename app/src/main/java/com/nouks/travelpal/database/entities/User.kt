package com.nouks.travelpal.database.entities

import android.provider.ContactsContract
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    @ColumnInfo(name = "phone")
    var phoneNumber: String,
    @ColumnInfo(name = "email")
    var email: String,
    @ColumnInfo(name = "roles")
    var roles: String = "USER",
    @ColumnInfo(name = "token")
    var token: String,
    @ColumnInfo(name = "expiresAt")
    var tokenExpAt: Long = 0L,
    @ColumnInfo(name = "refresh_token")
    var refreshToken: String
)