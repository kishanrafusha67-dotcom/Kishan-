package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Labour Bill", appName)
  }

  @Test
  fun `test apmc directory lookup`() {
    val shop = com.example.data.local.ApmcDirectory.findByNumber("1")
    assertEquals("અજય ટ્રેડર્સ", shop?.name)
    assertEquals("9824519205", shop?.mobile)
  }

  @Test
  fun `test backup json generation and parse roundtrip`() {
    val sampleUser = com.example.data.model.User(
      id = 1,
      name = "Kishan",
      mobile = "6351175874",
      password = "123",
      pin = "1234"
    )
    val sampleBill = com.example.data.model.Bill(
      id = 10,
      userId = 1,
      date = "04/10/2026",
      billNumber = "0001",
      shopNumber = "1",
      shopName = "અજય ટ્રેડર્સ",
      shopMobile = "9824519205",
      kata = 20,
      totalMudda = "10",
      bhav = 4.0,
      totalAmount = 40.0
    )
    val sampleHazari = com.example.data.model.HazariRecord(
      id = 5,
      userId = 1,
      workerName = "Kishan",
      date = "04/10/2026",
      status = "ONLINE"
    )

    val jsonString = com.example.util.BackupRestoreManager.generateBackupJson(
      sampleUser,
      listOf(sampleBill),
      listOf(sampleHazari)
    )

    assertNotNull(jsonString)
    assertTrue(jsonString.contains("LabourBillApp"))

    val parsed = com.example.util.BackupRestoreManager.parseBackupJson(jsonString)
    assertNotNull(parsed)
    assertEquals("Kishan", parsed?.userName)
    assertEquals(1, parsed?.totalBillsCount)
    assertEquals("0001", parsed?.bills?.first()?.billNumber)
    assertEquals(40.0, parsed?.bills?.first()?.totalAmount ?: 0.0, 0.001)
    assertEquals(1, parsed?.totalHazariCount)
    assertEquals("ONLINE", parsed?.hazari?.first()?.status)
  }
}
