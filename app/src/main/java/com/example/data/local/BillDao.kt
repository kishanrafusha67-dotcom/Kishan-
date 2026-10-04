package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Bill
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE userId = :userId ORDER BY timestamp DESC")
    fun getBillsByUser(userId: Long): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getBillsByUserAndDate(userId: Long, date: String): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getBillById(id: Long, userId: Long): Bill?

    @Query("SELECT billNumber FROM bills WHERE userId = :userId ORDER BY id DESC LIMIT 1")
    suspend fun getLastBillNumber(userId: Long): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<Bill>): List<Long>

    @Update
    suspend fun updateBill(bill: Bill)

    @Query("DELETE FROM bills WHERE id = :id AND userId = :userId")
    suspend fun deleteBill(id: Long, userId: Long)

    @Query("DELETE FROM bills WHERE userId = :userId")
    suspend fun deleteAllBillsForUser(userId: Long)
}
