package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String,
    val billNumber: String,
    val vakal: String = "",
    val shopNumber: String = "",
    val shopName: String = "",
    val shopMobile: String = "",
    val kata: Int = 0,
    val bharti: Double = 0.0,
    val kilo: Double = 0.0,
    val gram: Double = 0.0,
    val shedNumber: String = "",
    val pillarNumber: String = "",
    val totalMudda: String = "",
    val bhav: Double = 0.0,
    val totalAmount: Double = 0.0,
    val totalWeight: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
