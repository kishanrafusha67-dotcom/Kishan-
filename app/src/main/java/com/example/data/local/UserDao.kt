package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE isOnline = 1 ORDER BY lastActiveTimestamp DESC")
    fun getOnlineUsers(): Flow<List<User>>

    @Query("SELECT COUNT(*) FROM users WHERE isOnline = 1")
    fun getOnlineUsersCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET isOnline = :isOnline, lastActiveTimestamp = :timestamp WHERE id = :userId")
    suspend fun updateOnlineStatus(userId: Long, isOnline: Boolean, timestamp: Long = System.currentTimeMillis())
}
