package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HazariRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface HazariDao {
    @Query("SELECT * FROM hazari_records WHERE date = :date ORDER BY timestamp DESC")
    fun getHazariByDate(date: String): Flow<List<HazariRecord>>

    @Query("SELECT * FROM hazari_records ORDER BY timestamp DESC")
    fun getAllHazari(): Flow<List<HazariRecord>>

    @Query("SELECT * FROM hazari_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHazariByUser(userId: Long): Flow<List<HazariRecord>>

    @Query("SELECT * FROM hazari_records WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getTodayHazariForUser(userId: Long, date: String): HazariRecord?

    @Query("SELECT COUNT(*) FROM hazari_records WHERE date = :date AND status = 'ONLINE'")
    fun getActiveOnlineHazariCount(date: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazari(record: HazariRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazariList(records: List<HazariRecord>): List<Long>

    @Update
    suspend fun updateHazari(record: HazariRecord)

    @Query("DELETE FROM hazari_records WHERE id = :id")
    suspend fun deleteHazari(id: Long)

    @Query("DELETE FROM hazari_records WHERE userId = :userId")
    suspend fun deleteAllHazariForUser(userId: Long)
}
