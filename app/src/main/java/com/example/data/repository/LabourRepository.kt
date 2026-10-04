package com.example.data.repository

import com.example.data.local.ApmcDirectory
import com.example.data.local.BillDao
import com.example.data.local.HazariDao
import com.example.data.local.UserDao
import com.example.data.model.ApmcShop
import com.example.data.model.Bill
import com.example.data.model.HazariRecord
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class LabourRepository(
    private val userDao: UserDao,
    private val billDao: BillDao,
    private val hazariDao: HazariDao
) {
    // User / Auth
    suspend fun getUserByMobile(mobile: String): User? = userDao.getUserByMobile(mobile.trim())

    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)

    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    fun getOnlineUsers(): Flow<List<User>> = userDao.getOnlineUsers()

    fun getOnlineUsersCount(): Flow<Int> = userDao.getOnlineUsersCount()

    suspend fun registerUser(user: User): Long = userDao.insertUser(user)

    suspend fun updateUser(user: User) = userDao.updateUser(user)

    suspend fun setOnlineStatus(userId: Long, isOnline: Boolean) {
        userDao.updateOnlineStatus(userId, isOnline, System.currentTimeMillis())
    }

    // Bills
    fun getBillsByUser(userId: Long): Flow<List<Bill>> = billDao.getBillsByUser(userId)

    fun getBillsByUserAndDate(userId: Long, date: String): Flow<List<Bill>> =
        billDao.getBillsByUserAndDate(userId, date)

    suspend fun getBillById(id: Long, userId: Long): Bill? = billDao.getBillById(id, userId)

    suspend fun saveBill(bill: Bill): Long = billDao.insertBill(bill)

    suspend fun saveBills(bills: List<Bill>): List<Long> = billDao.insertBills(bills)

    suspend fun deleteBill(id: Long, userId: Long) = billDao.deleteBill(id, userId)

    suspend fun deleteAllBillsForUser(userId: Long) = billDao.deleteAllBillsForUser(userId)

    suspend fun generateNextBillNumber(userId: Long): String {
        val lastNumber = billDao.getLastBillNumber(userId) ?: return "0001"
        val numericPart = lastNumber.filter { it.isDigit() }.toIntOrNull() ?: 0
        return String.format("%04d", numericPart + 1)
    }

    // Hazari
    fun getHazariByDate(date: String): Flow<List<HazariRecord>> = hazariDao.getHazariByDate(date)

    fun getAllHazari(): Flow<List<HazariRecord>> = hazariDao.getAllHazari()

    fun getHazariByUser(userId: Long): Flow<List<HazariRecord>> = hazariDao.getHazariByUser(userId)

    suspend fun getTodayHazariForUser(userId: Long, date: String): HazariRecord? =
        hazariDao.getTodayHazariForUser(userId, date)

    fun getActiveOnlineHazariCount(date: String): Flow<Int> =
        hazariDao.getActiveOnlineHazariCount(date)

    suspend fun saveHazari(record: HazariRecord): Long = hazariDao.insertHazari(record)

    suspend fun saveHazariList(records: List<HazariRecord>): List<Long> = hazariDao.insertHazariList(records)

    suspend fun updateHazari(record: HazariRecord) = hazariDao.updateHazari(record)

    suspend fun deleteHazari(id: Long) = hazariDao.deleteHazari(id)

    suspend fun deleteAllHazariForUser(userId: Long) = hazariDao.deleteAllHazariForUser(userId)

    // APMC Directory
    fun getShopByNumber(number: String): ApmcShop? = ApmcDirectory.findByNumber(number)

    fun searchShops(query: String): List<ApmcShop> = ApmcDirectory.search(query)
}
