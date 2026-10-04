package com.example.ui.screens.pocket

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Bill
import com.example.ui.LabourViewModel
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryIndigo
import com.example.util.ShareReceiptUtil
import java.util.Calendar

@Composable
fun PocketScreen(
    viewModel: LabourViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allBills by viewModel.userBills.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var activeTab by rememberSaveable { mutableIntStateOf(0) } // 0: Daily Work, 1: Monthly Summary
    var viewAllDates by remember { mutableStateOf(false) }

    // Filter bills
    val filteredBills = remember(allBills, selectedDate, viewAllDates) {
        if (viewAllDates) allBills else allBills.filter { it.date == selectedDate }
    }

    val totalBillsCount = filteredBills.size
    val totalKata = filteredBills.sumOf { it.kata }
    val totalWeight = filteredBills.sumOf { it.totalWeight }
    val totalMarketAmount = filteredBills.sumOf { it.totalAmount }

    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 90.dp)
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0F766E), Color(0xFF0D9488), Color(0xFF14B8A6))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📊", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "મારું પોકેટ (Pocket Ledger)",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "દૈનિક કામ અને માસિક નાણાકીય હિસાબ",
                                fontSize = 12.sp,
                                color = Color(0xFFCCFBF1)
                            )
                        }
                    }

                    // Share Summary Button
                    IconButton(
                        onClick = {
                            val report = buildString {
                                appendLine("📊 *APMC મજૂરી પોકેટ કામ રિપોર્ટ*")
                                appendLine("📅 તારીખ: ${if (viewAllDates) "બધા દિવસો" else selectedDate}")
                                appendLine("━━━━━━━━━━━━━━━━━━━")
                                appendLine("• કુલ બિલ: $totalBillsCount")
                                appendLine("• કુલ કટા: $totalKata")
                                appendLine("• કુલ વજન: ${String.format("%.2f", totalWeight)} kg")
                                appendLine("• કુલ રકમ: ₹${String.format("%.2f", totalMarketAmount)}")
                                appendLine("━━━━━━━━━━━━━━━━━━━")
                                filteredBills.forEachIndexed { i, b ->
                                    appendLine("${i + 1}. બિલ નં ${b.billNumber} | ${b.shopName} (${b.kata} કટા)")
                                }
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(intent, "પોકેટ રિપોર્ટ શેર કરો"))
                        },
                        modifier = Modifier.testTag("share_pocket_report")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Report",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // VIEW MODE SELECTOR (Daily Work vs Monthly Financial Summary)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (activeTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                shadowElevation = if (activeTab == 0) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeTab = 0 }
                    .testTag("tab_daily_work")
            ) {
                Text(
                    text = "📅 દૈનિક કામ (Daily)",
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeTab == 0) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (activeTab == 1) PrimaryIndigo else Color.Transparent,
                shadowElevation = if (activeTab == 1) 2.dp else 0.dp,
                modifier = Modifier
                    .weight(1f)
                    .clickable { activeTab = 1 }
                    .testTag("tab_monthly_summary")
            ) {
                Text(
                    text = "🗓️ માસિક હિસાબ (Monthly)",
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                    color = if (activeTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (activeTab == 1) {
            // MONTHLY SUMMARY VIEW (aggregates daily worker attendance and total bill amounts per month)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                MonthlySummaryView(viewModel = viewModel)
            }
        } else {
            // DAILY WORK VIEW
            Column(modifier = Modifier.fillMaxSize()) {
                // Big Pocket Work Summary Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "કુલ કામ (Total Bags Handled)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$totalKata કટા",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryIndigo
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "કુલ બિલ રકમ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹ ${String.format("%.2f", totalMarketAmount)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // DATE SELECTOR & FILTER CHIPS
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable {
                                val parts = selectedDate.split("/")
                                val day: Int = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
                                val month: Int = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                                val year: Int = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)

                                DatePickerDialog(context, { _, y: Int, m: Int, d: Int ->
                                    val formatted = String.format("%02d/%02d/%04d", d, m + 1, y)
                                    viewModel.setDate(formatted)
                                    viewAllDates = false
                                }, year, month, day).show()
                            }
                            .testTag("pocket_date_picker")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = "Date",
                                modifier = Modifier.size(16.dp),
                                tint = PrimaryIndigo
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewAllDates) "તારીખ: બધા" else selectedDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!viewAllDates) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { viewAllDates = false }
                        ) {
                            Text(
                                text = "પસંદ કરેલ",
                                color = if (!viewAllDates) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (viewAllDates) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { viewAllDates = true }
                        ) {
                            Text(
                                text = "કુલ બધું",
                                color = if (viewAllDates) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // METRICS 3-COLUMN SUMMARY
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "કુલ કટા",
                        value = "$totalKata",
                        subtitle = "Bags Handled",
                        icon = "📦",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "કુલ વજન",
                        value = "${String.format("%.1f", totalWeight)}",
                        subtitle = "Kg Weight",
                        icon = "⚖️",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "કુલ બિલ",
                        value = "$totalBillsCount",
                        subtitle = "Bills Done",
                        icon = "🧾",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // LIST OF BILLS / WORK LOG
                Text(
                    text = "કામની વિગત (Bills Done)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (filteredBills.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🌾", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "આ તારીખે કોઈ બિલ બનાવ્યું નથી",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(filteredBills) { bill ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = PrimaryIndigo.copy(alpha = 0.15f),
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = bill.shopNumber.ifEmpty { "1" },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = PrimaryIndigo
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = bill.shopName.ifEmpty { "APMC દુકાન" },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = "બિલ નં: ${bill.billNumber} • ${bill.date}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Text(
                                            text = "₹${bill.totalAmount}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PrimaryBlue
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${bill.kata} કટા • ${bill.totalWeight} kg • મુદ્દા: ${bill.totalMudda.ifEmpty { "-" }} • ભાવ: ₹${bill.bhav}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Row {
                                            // Share photo
                                            IconButton(
                                                onClick = { ShareReceiptUtil.shareBillAsPhoto(context, bill) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Text(text = "📸", fontSize = 16.sp)
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            // Share text
                                            IconButton(
                                                onClick = { ShareReceiptUtil.shareBillAsText(context, bill) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Share,
                                                    contentDescription = "Share Text",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = PrimaryIndigo
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            // Delete
                                            IconButton(
                                                onClick = { viewModel.deleteBill(bill) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFFEF4444)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
