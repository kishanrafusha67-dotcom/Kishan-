package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ApmcDirectory
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.model.ApmcShop
import com.example.data.model.BackupData
import com.example.data.model.Bill
import com.example.data.model.HazariRecord
import com.example.data.model.MonthlyDayRecord
import com.example.data.model.MonthlyFinancialSummary
import com.example.data.model.MonthlyWorkerAttendance
import com.example.data.model.User
import com.example.data.repository.LabourRepository
import com.example.util.BackupRestoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LabourViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = LabourRepository(
        database.userDao(),
        database.billDao(),
        database.hazariDao()
    )

    val isDarkMode = MutableStateFlow(sessionManager.isDarkMode)

    private val _currentUserId = MutableStateFlow(sessionManager.currentUserId)
    val currentUserId = _currentUserId.asStateFlow()

    val currentUser: StateFlow<User?> = _currentUserId.flatMapLatest { id ->
        if (id > 0) repository.getUserById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current user's bills (Ensuring "bhadha na data alag alag rava joa")
    val userBills: StateFlow<List<Bill>> = _currentUserId.flatMapLatest { id ->
        if (id > 0) repository.getBillsByUser(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All registered users (for hazari management)
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Online users count & list
    val onlineUsers: StateFlow<List<User>> = repository.getOnlineUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val onlineUsersCount: StateFlow<Int> = repository.getOnlineUsersCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Selected Date for Hazari & Pocket
    val selectedDate = MutableStateFlow(getTodayFormattedDate())

    // Hazari for selected date
    val hazariList: StateFlow<List<HazariRecord>> = selectedDate.flatMapLatest { date ->
        repository.getHazariByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All hazari for monthly attendance aggregation
    val allHazariList: StateFlow<List<HazariRecord>> = repository.getAllHazari()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Month-Year (e.g. "10/2026")
    val selectedMonthYear = MutableStateFlow(getCurrentMonthYear())

    // Monthly Summary aggregating Daily Worker Attendance and Total Bill Amounts
    val monthlySummary: StateFlow<MonthlyFinancialSummary> = combine(
        userBills,
        allHazariList,
        selectedMonthYear
    ) { bills, hazari, my ->
        // Filter bills for selected month (date: dd/MM/yyyy)
        val monthBills = bills.filter { b ->
            val parts = b.date.split("/")
            if (parts.size == 3) "${parts[1]}/${parts[2]}" == my else false
        }

        // Filter hazari for selected month
        val monthHazari = hazari.filter { h ->
            val parts = h.date.split("/")
            if (parts.size == 3) "${parts[1]}/${parts[2]}" == my else false
        }

        val totalAmount = monthBills.sumOf { it.totalAmount }
        val totalBillsCount = monthBills.size
        val totalKata = monthBills.sumOf { it.kata }
        val totalWeight = monthBills.sumOf { it.totalWeight }

        // Worker Attendance Aggregation per worker
        val workersMap = mutableMapOf<String, MutableList<HazariRecord>>()
        monthHazari.forEach { record ->
            workersMap.getOrPut(record.workerName) { mutableListOf() }.add(record)
        }

        val workerAttendanceList = workersMap.map { (name, records) ->
            val presentDays = records.count { it.status == "ONLINE" }
            val halfDays = records.count { it.status == "HALF_DAY" }
            val absentDays = records.count { it.status == "ABSENT" }
            val presentDates = records.filter { it.status == "ONLINE" }.map { it.date }.distinct()
            MonthlyWorkerAttendance(
                workerName = name,
                workerMobile = records.firstOrNull()?.workerMobile ?: "",
                presentDays = presentDays,
                halfDays = halfDays,
                absentDays = absentDays,
                totalRecords = records.size,
                presentDates = presentDates
            )
        }.sortedByDescending { it.presentDays }

        // Daily records in the month
        val datesInMonth = (monthBills.map { it.date } + monthHazari.map { it.date }).distinct()
        val dailyRecords = datesInMonth.map { d ->
            val dBills = monthBills.filter { it.date == d }
            val dHazari = monthHazari.filter { it.date == d }
            val dayNum = d.split("/").getOrNull(0)?.toIntOrNull() ?: 1
            val presentWorkers = dHazari.filter { it.status == "ONLINE" }.map { it.workerName }.distinct()
            MonthlyDayRecord(
                date = d,
                dayOfMonth = dayNum,
                billsCount = dBills.size,
                totalKata = dBills.sumOf { it.kata },
                totalAmount = dBills.sumOf { it.totalAmount },
                totalWeight = dBills.sumOf { it.totalWeight },
                workersPresentCount = presentWorkers.size,
                presentWorkersNames = presentWorkers
            )
        }.sortedByDescending { it.dayOfMonth }

        MonthlyFinancialSummary(
            monthYear = my,
            monthDisplayName = getMonthDisplayName(my),
            totalBillAmount = totalAmount,
            totalBillsCount = totalBillsCount,
            totalKata = totalKata,
            totalWeight = totalWeight,
            activeDaysCount = dailyRecords.size,
            workerAttendanceList = workerAttendanceList,
            dailyRecords = dailyRecords
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlyFinancialSummary(
            monthYear = getCurrentMonthYear(),
            monthDisplayName = getMonthDisplayName(getCurrentMonthYear()),
            totalBillAmount = 0.0,
            totalBillsCount = 0,
            totalKata = 0,
            totalWeight = 0.0,
            activeDaysCount = 0,
            workerAttendanceList = emptyList(),
            dailyRecords = emptyList()
        )
    )

    // Active Bill Form state matching Screenshots
    val formDate = MutableStateFlow(getTodayFormattedDate())
    val formBillNumber = MutableStateFlow("0001")
    val formVakal = MutableStateFlow("")
    val formShopNumber = MutableStateFlow("")
    val formShopName = MutableStateFlow("")
    val formShopMobile = MutableStateFlow("")
    val formKata = MutableStateFlow("")
    val formBharti = MutableStateFlow("")
    val formKilo = MutableStateFlow("")
    val formGram = MutableStateFlow("")
    val formShedNumber = MutableStateFlow("")
    val formPillarNumber = MutableStateFlow("")
    val formTotalMudda = MutableStateFlow("")
    val formBhav = MutableStateFlow("")

    val totalCalculatedAmount = MutableStateFlow(0.0)
    val totalCalculatedWeight = MutableStateFlow(0.0)

    // UI Feedback
    val toastMessage = MutableStateFlow<String?>(null)

    init {
        // Pre-populate initial sample users if empty so testing/demoing works seamlessly
        viewModelScope.launch {
            val users = repository.getUserByMobile("6351175874")
            if (users == null) {
                val demoUser = User(
                    name = "Kishan",
                    mobile = "6351175874",
                    password = "password123",
                    pin = "1234",
                    isOnline = true
                )
                val id = repository.registerUser(demoUser)
                // Add second worker to showcase multiple online workers
                repository.registerUser(
                    User(
                        name = "Ramesh Bhai",
                        mobile = "9825012345",
                        password = "123",
                        pin = "0000",
                        isOnline = true
                    )
                )
                repository.registerUser(
                    User(
                        name = "Hitesh Patel",
                        mobile = "9909012345",
                        password = "123",
                        pin = "1111",
                        isOnline = true
                    )
                )
                if (sessionManager.currentUserId <= 0) {
                    sessionManager.currentUserId = id
                    _currentUserId.value = id
                }
            }
            refreshNextBillNumber()
        }
    }

    fun toggleDarkMode(enable: Boolean) {
        isDarkMode.value = enable
        sessionManager.isDarkMode = enable
    }

    fun setDate(date: String) {
        formDate.value = date
        selectedDate.value = date
    }

    fun onShopNumberChange(number: String) {
        formShopNumber.value = number
        val shop = ApmcDirectory.findByNumber(number)
        if (shop != null) {
            formShopName.value = shop.name
            formShopMobile.value = shop.mobile
        }
    }

    fun selectShop(shop: ApmcShop) {
        formShopNumber.value = shop.shopNumber
        formShopName.value = shop.name
        formShopMobile.value = shop.mobile
    }

    fun recalculateBill() {
        val mudda = formTotalMudda.value.toDoubleOrNull() ?: 0.0
        val kata = formKata.value.toIntOrNull() ?: 0
        val bharti = formBharti.value.toDoubleOrNull() ?: 0.0
        val kilo = formKilo.value.toDoubleOrNull() ?: 0.0
        val gram = formGram.value.toDoubleOrNull() ?: 0.0
        val bhav = formBhav.value.toDoubleOrNull() ?: 0.0

        // Total weight in Kg = (kata * bharti) + kilo + (gram / 1000.0)
        val weight = (kata * bharti) + kilo + (gram / 1000.0)
        totalCalculatedWeight.value = Math.round(weight * 100.0) / 100.0

        // In APMC market trade: કુલ મુદ્દા * ભાવ = Total (e.g. 10 * 4 = 40)
        val amount = when {
            mudda > 0 && bhav > 0 -> mudda * bhav
            kata > 0 && bhav > 0 -> kata * bhav
            weight > 0 && bhav > 0 -> (weight / 20.0) * bhav
            else -> 0.0
        }

        totalCalculatedAmount.value = Math.round(amount * 100.0) / 100.0
    }

    fun previousMonth() {
        val parts = selectedMonthYear.value.split("/")
        var m = parts.getOrNull(0)?.toIntOrNull() ?: 10
        var y = parts.getOrNull(1)?.toIntOrNull() ?: 2026
        m -= 1
        if (m < 1) {
            m = 12
            y -= 1
        }
        selectedMonthYear.value = String.format("%02d/%04d", m, y)
    }

    fun nextMonth() {
        val parts = selectedMonthYear.value.split("/")
        var m = parts.getOrNull(0)?.toIntOrNull() ?: 10
        var y = parts.getOrNull(1)?.toIntOrNull() ?: 2026
        m += 1
        if (m > 12) {
            m = 1
            y += 1
        }
        selectedMonthYear.value = String.format("%02d/%04d", m, y)
    }

    fun setMonthYear(monthYear: String) {
        selectedMonthYear.value = monthYear
    }

    fun saveBill(onSuccess: (Bill) -> Unit) {
        val uId = _currentUserId.value
        if (uId <= 0) {
            toastMessage.value = "મહેરબાની કરીને પહેલા લોગિન કરો"
            return
        }

        recalculateBill()

        val bill = Bill(
            userId = uId,
            date = formDate.value.ifEmpty { getTodayFormattedDate() },
            billNumber = formBillNumber.value.ifEmpty { "0001" },
            vakal = formVakal.value,
            shopNumber = formShopNumber.value,
            shopName = formShopName.value,
            shopMobile = formShopMobile.value,
            kata = formKata.value.toIntOrNull() ?: 0,
            bharti = formBharti.value.toDoubleOrNull() ?: 0.0,
            kilo = formKilo.value.toDoubleOrNull() ?: 0.0,
            gram = formGram.value.toDoubleOrNull() ?: 0.0,
            shedNumber = formShedNumber.value,
            pillarNumber = formPillarNumber.value,
            totalMudda = formTotalMudda.value,
            bhav = formBhav.value.toDoubleOrNull() ?: 0.0,
            totalAmount = totalCalculatedAmount.value,
            totalWeight = totalCalculatedWeight.value
        )

        viewModelScope.launch {
            val id = repository.saveBill(bill)
            val savedBill = bill.copy(id = id)
            toastMessage.value = "બિલ નં ${bill.billNumber} સાચવવામાં આવ્યું!"
            clearForm()
            refreshNextBillNumber()
            onSuccess(savedBill)
        }
    }

    fun clearForm() {
        formVakal.value = ""
        formShopNumber.value = ""
        formShopName.value = ""
        formShopMobile.value = ""
        formKata.value = ""
        formBharti.value = ""
        formKilo.value = ""
        formGram.value = ""
        formShedNumber.value = ""
        formPillarNumber.value = ""
        formTotalMudda.value = ""
        formBhav.value = ""
        totalCalculatedAmount.value = 0.0
        totalCalculatedWeight.value = 0.0
    }

    fun deleteBill(bill: Bill) {
        viewModelScope.launch {
            repository.deleteBill(bill.id, bill.userId)
            toastMessage.value = "બિલ નં ${bill.billNumber} ડિલીટ કર્યું"
        }
    }

    private suspend fun refreshNextBillNumber() {
        val uId = _currentUserId.value
        if (uId > 0) {
            formBillNumber.value = repository.generateNextBillNumber(uId)
        }
    }

    // Auth & Multi-User Separation
    fun login(mobile: String, pinOrPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByMobile(mobile)
            if (user == null) {
                onResult(false, "આ નંબર નોંધાયેલ નથી. કૃપા કરીને Registration કરો.")
                return@launch
            }
            if (user.password == pinOrPass || user.pin == pinOrPass) {
                sessionManager.currentUserId = user.id
                _currentUserId.value = user.id
                repository.setOnlineStatus(user.id, true)
                // Mark today's hazari online
                markTodayHazari(user, "ONLINE")
                refreshNextBillNumber()
                onResult(true, "સ્વાગત છે, ${user.name}!")
            } else {
                onResult(false, "પાસવર્ડ અથવા 4 અંકનો PIN ખોટો છે.")
            }
        }
    }

    fun register(
        name: String,
        mobile: String,
        pass: String,
        pin: String,
        confirmPin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        if (name.isBlank() || mobile.isBlank() || pass.isBlank() || pin.isBlank()) {
            onResult(false, "બધી વિગતો ભરવી જરૂરી છે.")
            return
        }
        if (pin != confirmPin) {
            onResult(false, "PIN અને ફરી લખેલ PIN સરખા નથી.")
            return
        }
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            onResult(false, "PIN બરાબર 4 અંકનો હોવો જોઈએ.")
            return
        }

        viewModelScope.launch {
            val existing = repository.getUserByMobile(mobile)
            if (existing != null) {
                onResult(false, "આ મોબાઇલ નંબર પહેલેથી રજિસ્ટર થયેલ છે. Login કરો.")
                return@launch
            }

            val newUser = User(
                name = name.trim(),
                mobile = mobile.trim(),
                password = pass,
                pin = pin,
                isOnline = true
            )
            val newId = repository.registerUser(newUser)
            sessionManager.currentUserId = newId
            _currentUserId.value = newId
            markTodayHazari(newUser.copy(id = newId), "ONLINE")
            refreshNextBillNumber()
            onResult(true, "Registration સફળ થયું! સ્વાગત છે $name")
        }
    }

    fun logout() {
        val uId = _currentUserId.value
        viewModelScope.launch {
            if (uId > 0) {
                repository.setOnlineStatus(uId, false)
            }
            sessionManager.clearSession()
            _currentUserId.value = -1L
            toastMessage.value = "લોગ આઉટ સફળ થયું"
        }
    }

    fun toggleMyOnlineStatus() {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val newStatus = !user.isOnline
            repository.setOnlineStatus(user.id, newStatus)
            markTodayHazari(user, if (newStatus) "ONLINE" else "COMPLETED")
            toastMessage.value = if (newStatus) "તમે હવે ONLINE છો 🟢" else "તમે હવે OFFLINE છો ⚪"
        }
    }

    private suspend fun markTodayHazari(user: User, status: String) {
        val today = getTodayFormattedDate()
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val existing = repository.getTodayHazariForUser(user.id, today)
        if (existing == null) {
            repository.saveHazari(
                HazariRecord(
                    userId = user.id,
                    workerName = user.name,
                    workerMobile = user.mobile,
                    date = today,
                    status = status,
                    checkInTime = time
                )
            )
        } else {
            repository.updateHazari(
                existing.copy(
                    status = status,
                    checkOutTime = if (status != "ONLINE") time else existing.checkOutTime
                )
            )
        }
    }

    fun addManualHazari(workerName: String, status: String, notes: String) {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repository.saveHazari(
                HazariRecord(
                    userId = _currentUserId.value,
                    workerName = workerName,
                    date = selectedDate.value,
                    status = status,
                    checkInTime = time,
                    notes = notes
                )
            )
            toastMessage.value = "$workerName ની હાજરી નોંધાઈ"
        }
    }

    fun deleteHazari(id: Long) {
        viewModelScope.launch {
            repository.deleteHazari(id)
            toastMessage.value = "હાજરી રેકોર્ડ કાઢી નાખ્યો"
        }
    }

    fun clearToast() {
        toastMessage.value = null
    }

    // Local JSON Backup & Restore
    fun exportLedgerJson(): String {
        val user = currentUser.value
        val bills = userBills.value
        val myHazari = allHazariList.value.filter { it.userId == _currentUserId.value }
        return BackupRestoreManager.generateBackupJson(user, bills, myHazari)
    }

    fun parseBackupData(jsonString: String): BackupData? {
        return BackupRestoreManager.parseBackupJson(jsonString)
    }

    fun importLedgerFromJson(
        jsonString: String,
        replaceExisting: Boolean,
        onResult: (Boolean, String) -> Unit
    ) {
        val uId = _currentUserId.value
        if (uId <= 0) {
            onResult(false, "કૃપા કરીને પહેલા લોગિન કરો.")
            return
        }

        viewModelScope.launch {
            val backupData = BackupRestoreManager.parseBackupJson(jsonString)
            if (backupData == null) {
                onResult(false, "અમાન્ય JSON ફાઇલ છે. કૃપા કરીને સાચી બેકઅપ ફાઇલ પસંદ કરો.")
                return@launch
            }

            if (replaceExisting) {
                repository.deleteAllBillsForUser(uId)
                repository.deleteAllHazariForUser(uId)
            }

            // Map imported records to current user ID
            val mappedBills = backupData.bills.map { b ->
                b.copy(id = 0, userId = uId)
            }
            val mappedHazari = backupData.hazari.map { h ->
                h.copy(id = 0, userId = uId)
            }

            if (mappedBills.isNotEmpty()) {
                repository.saveBills(mappedBills)
            }
            if (mappedHazari.isNotEmpty()) {
                repository.saveHazariList(mappedHazari)
            }

            refreshNextBillNumber()
            toastMessage.value = "${mappedBills.size} બિલ અને ${mappedHazari.size} હાજરી રિસ્ટોર થયા!"
            onResult(
                true,
                "સફળતાપૂર્વક રિસ્ટોર થયું!\n• ${mappedBills.size} બિલ\n• ${mappedHazari.size} હાજરી રેકોર્ડ"
            )
        }
    }

    companion object {
        fun getTodayFormattedDate(): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }

        fun getCurrentMonthYear(): String {
            return SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        }

        fun getMonthDisplayName(monthYear: String): String {
            val parts = monthYear.split("/")
            val month = parts.getOrNull(0)?.toIntOrNull() ?: 10
            val year = parts.getOrNull(1) ?: "2026"
            val monthNames = listOf(
                "જાન્યુઆરી", "ફેબ્રુઆરી", "માર્ચ", "એપ્રિલ", "મે", "જૂન",
                "જુલાઈ", "ઓગસ્ટ", "સપ્ટેમ્બર", "ઓક્ટોબર", "નવેમ્બર", "ડિસેમ્બર"
            )
            val name = monthNames.getOrElse(month - 1) { "મહિનો $month" }
            return "$name $year"
        }
    }
}
