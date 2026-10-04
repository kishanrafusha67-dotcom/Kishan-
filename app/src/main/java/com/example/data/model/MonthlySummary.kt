package com.example.data.model

data class MonthlyWorkerAttendance(
    val workerName: String,
    val workerMobile: String = "",
    val presentDays: Int,
    val halfDays: Int,
    val absentDays: Int,
    val totalRecords: Int,
    val presentDates: List<String>
)

data class MonthlyDayRecord(
    val date: String,
    val dayOfMonth: Int,
    val billsCount: Int,
    val totalKata: Int,
    val totalAmount: Double,
    val totalWeight: Double,
    val workersPresentCount: Int,
    val presentWorkersNames: List<String>
)

data class MonthlyFinancialSummary(
    val monthYear: String, // e.g. "10/2026"
    val monthDisplayName: String, // e.g. "ઓક્ટોબર 2026"
    val totalBillAmount: Double,
    val totalBillsCount: Int,
    val totalKata: Int,
    val totalWeight: Double,
    val activeDaysCount: Int,
    val workerAttendanceList: List<MonthlyWorkerAttendance>,
    val dailyRecords: List<MonthlyDayRecord>
)
