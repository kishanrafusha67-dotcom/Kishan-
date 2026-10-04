package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.BackupData
import com.example.data.model.Bill
import com.example.data.model.HazariRecord
import com.example.data.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreManager {

    fun generateBackupJson(
        user: User?,
        bills: List<Bill>,
        hazari: List<HazariRecord>
    ): String {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(now))

        root.put("app", "LabourBillApp")
        root.put("version", 1)
        root.put("exportTimestamp", now)
        root.put("exportDate", dateStr)
        root.put("userName", user?.name ?: "User")
        root.put("userMobile", user?.mobile ?: "")
        root.put("totalBillsCount", bills.size)
        root.put("totalHazariCount", hazari.size)

        // Bills Array
        val billsArray = JSONArray()
        for (b in bills) {
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("userId", b.userId)
            bObj.put("date", b.date)
            bObj.put("billNumber", b.billNumber)
            bObj.put("vakal", b.vakal)
            bObj.put("shopNumber", b.shopNumber)
            bObj.put("shopName", b.shopName)
            bObj.put("shopMobile", b.shopMobile)
            bObj.put("kata", b.kata)
            bObj.put("bharti", b.bharti)
            bObj.put("kilo", b.kilo)
            bObj.put("gram", b.gram)
            bObj.put("shedNumber", b.shedNumber)
            bObj.put("pillarNumber", b.pillarNumber)
            bObj.put("totalMudda", b.totalMudda)
            bObj.put("bhav", b.bhav)
            bObj.put("totalAmount", b.totalAmount)
            bObj.put("totalWeight", b.totalWeight)
            bObj.put("timestamp", b.timestamp)
            billsArray.put(bObj)
        }
        root.put("bills", billsArray)

        // Hazari Array
        val hazariArray = JSONArray()
        for (h in hazari) {
            val hObj = JSONObject()
            hObj.put("id", h.id)
            hObj.put("userId", h.userId)
            hObj.put("workerName", h.workerName)
            hObj.put("workerMobile", h.workerMobile)
            hObj.put("date", h.date)
            hObj.put("status", h.status)
            hObj.put("checkInTime", h.checkInTime)
            hObj.put("checkOutTime", h.checkOutTime)
            hObj.put("wageEarned", h.wageEarned)
            hObj.put("notes", h.notes)
            hObj.put("timestamp", h.timestamp)
            hazariArray.put(hObj)
        }
        root.put("hazari", hazariArray)

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): BackupData? {
        return try {
            val root = JSONObject(jsonString)
            val app = root.optString("app", "LabourBillApp")
            val version = root.optInt("version", 1)
            val exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis())
            val exportDate = root.optString("exportDate", "")
            val userName = root.optString("userName", "")
            val userMobile = root.optString("userMobile", "")

            val billsArray = root.optJSONArray("bills") ?: JSONArray()
            val billsList = mutableListOf<Bill>()
            for (i in 0 until billsArray.length()) {
                val bObj = billsArray.getJSONObject(i)
                billsList.add(
                    Bill(
                        id = bObj.optLong("id", 0L),
                        userId = bObj.optLong("userId", 0L),
                        date = bObj.optString("date", ""),
                        billNumber = bObj.optString("billNumber", ""),
                        vakal = bObj.optString("vakal", ""),
                        shopNumber = bObj.optString("shopNumber", ""),
                        shopName = bObj.optString("shopName", ""),
                        shopMobile = bObj.optString("shopMobile", ""),
                        kata = bObj.optInt("kata", 0),
                        bharti = bObj.optDouble("bharti", 0.0),
                        kilo = bObj.optDouble("kilo", 0.0),
                        gram = bObj.optDouble("gram", 0.0),
                        shedNumber = bObj.optString("shedNumber", ""),
                        pillarNumber = bObj.optString("pillarNumber", ""),
                        totalMudda = bObj.optString("totalMudda", ""),
                        bhav = bObj.optDouble("bhav", 0.0),
                        totalAmount = bObj.optDouble("totalAmount", 0.0),
                        totalWeight = bObj.optDouble("totalWeight", 0.0),
                        timestamp = bObj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }

            val hazariArray = root.optJSONArray("hazari") ?: JSONArray()
            val hazariList = mutableListOf<HazariRecord>()
            for (i in 0 until hazariArray.length()) {
                val hObj = hazariArray.getJSONObject(i)
                hazariList.add(
                    HazariRecord(
                        id = hObj.optLong("id", 0L),
                        userId = hObj.optLong("userId", 0L),
                        workerName = hObj.optString("workerName", ""),
                        workerMobile = hObj.optString("workerMobile", ""),
                        date = hObj.optString("date", ""),
                        status = hObj.optString("status", "ONLINE"),
                        checkInTime = hObj.optString("checkInTime", ""),
                        checkOutTime = hObj.optString("checkOutTime", ""),
                        wageEarned = hObj.optDouble("wageEarned", 0.0),
                        notes = hObj.optString("notes", ""),
                        timestamp = hObj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }

            BackupData(
                app = app,
                version = version,
                exportTimestamp = exportTimestamp,
                exportDate = exportDate,
                userName = userName,
                userMobile = userMobile,
                totalBillsCount = billsList.size,
                totalHazariCount = hazariList.size,
                bills = billsList,
                hazari = hazariList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareBackupFile(context: Context, jsonString: String, fileName: String) {
        try {
            val cachePath = File(context.cacheDir, "backups")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            FileOutputStream(file).use { out ->
                out.write(jsonString.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Labour Bill App - Data Backup ($fileName)")
                putExtra(Intent.EXTRA_TEXT, "Labour Bill App Local JSON Backup File.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "બેકઅપ ફાઇલ શેર કરો"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
