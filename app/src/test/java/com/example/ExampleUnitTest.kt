package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun `test mudda and bhav calculation - kul mudda 10 bhav 4 total 40`() {
    val mudda = 10.0
    val bhav = 4.0
    val total = mudda * bhav
    assertEquals(40.0, total, 0.001)
  }

  @Test
  fun `test monthly display name format`() {
    val display = com.example.ui.LabourViewModel.getMonthDisplayName("10/2026")
    assertEquals("ઓક્ટોબર 2026", display)
  }
}
