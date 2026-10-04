package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hazari_records")
data class HazariRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val workerName: String,
    val workerMobile: String = "",
    val date: String,
    val status: String = "ONLINE", // "ONLINE", "COMPLETED", "ABSENT", "HALF_DAY"
    val checkInTime: String = "",
    val checkOutTime: String = "",
    val wageEarned: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
