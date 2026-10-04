package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val password: String,
    val pin: String,
    val isOnline: Boolean = false,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)
