package com.example.data.model

data class BackupData(
    val app: String = "LabourBillApp",
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val exportDate: String,
    val userName: String,
    val userMobile: String,
    val totalBillsCount: Int,
    val totalHazariCount: Int,
    val bills: List<Bill>,
    val hazari: List<HazariRecord>
)
