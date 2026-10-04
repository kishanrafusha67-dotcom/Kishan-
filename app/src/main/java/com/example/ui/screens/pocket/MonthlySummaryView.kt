package com.example.ui.screens.pocket

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.model.MonthlyDayRecord
import com.example.data.model.MonthlyFinancialSummary
import com.example.data.model.MonthlyWorkerAttendance
import com.example.ui.LabourViewModel
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen

@Composable
fun MonthlySummaryView(
    viewModel: LabourViewModel
) {
    val context = LocalContext.current
    val summary by viewModel.monthlySummary.collectAsState()
    val selectedMonthYear by viewModel.selectedMonthYear.collectAsState()

    var showWorkerSection by remember { mutableStateOf(true) }
    var showDailyBreakdown by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // MONTH SELECTOR & NAVIGATOR
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.previousMonth() },
                    modifier = Modifier.testTag("prev_month_btn")
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "અગાઉનો મહિનો",
                        tint = PrimaryIndigo
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.monthDisplayName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "નાણાકીય અને હાજરી સારાંશ ($selectedMonthYear)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.testTag("next_month_btn")
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "આગળનો મહિનો",
                            tint = PrimaryIndigo
                        )
                    }

                    // Share Monthly Report
                    IconButton(
                        onClick = { shareMonthlyReport(context, summary) },
                        modifier = Modifier.testTag("share_monthly_report_btn")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Monthly Report",
                            tint = PrimaryBlue
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MONTHLY FINANCIAL HIGHLIGHT HERO CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF4F46E5))
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "માસિક કુલ બિલ રકમ (Total Bill Amount)",
                            fontSize = 12.sp,
                            color = Color(0xFFBFDBFE),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "₹ ${String.format("%.2f", summary.totalBillAmount)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${summary.activeDaysCount} દિવસ કામ",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MonthlyStatMini(
                        label = "કુલ બિલ",
                        value = "${summary.totalBillsCount}",
                        icon = "🧾"
                    )
                    MonthlyStatMini(
                        label = "કુલ કટા",
                        value = "${summary.totalKata}",
                        icon = "📦"
                    )
                    MonthlyStatMini(
                        label = "કુલ વજન",
                        value = "${String.format("%.1f", summary.totalWeight)} kg",
                        icon = "⚖️"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: WORKER ATTENDANCE AGGREGATION
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showWorkerSection = !showWorkerSection },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "👥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "દૈનિક મજૂર હાજરી સંકલન (Attendance)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "મહિના દરમિયાન કોણ કેટલા દિવસ હાજર રહ્યું",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showWorkerSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Workers",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = showWorkerSection) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        if (summary.workerAttendanceList.isEmpty()) {
                            Text(
                                text = "આ મહિનામાં કોઈ હાજરી રેકોર્ડ નોંધાયેલ નથી.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            summary.workerAttendanceList.forEach { worker ->
                                WorkerMonthlyAttendanceRow(worker = worker)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SECTION 2: DAILY BREAKDOWN WITHIN MONTH
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDailyBreakdown = !showDailyBreakdown },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "દિવસ મુજબ હિસાબ (Daily Breakdown)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "તારીખવાર બિલ રકમ અને હાજર મજૂરો",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showDailyBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Daily",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = showDailyBreakdown) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        if (summary.dailyRecords.isEmpty()) {
                            Text(
                                text = "આ મહિનામાં કોઈ દૈનિક એન્ટ્રી નથી.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            summary.dailyRecords.forEach { dayRecord ->
                                DayBreakdownRow(dayRecord = dayRecord)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun MonthlyStatMini(
    label: String,
    value: String,
    icon: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFFCCFBF1)
            )
        }
    }
}

@Composable
private fun WorkerMonthlyAttendanceRow(
    worker: MonthlyWorkerAttendance
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "👷", fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = worker.workerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (worker.workerMobile.isNotEmpty()) {
                            Text(
                                text = "📞 ${worker.workerMobile}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "${worker.presentDays} દિવસ હાજર",
                            color = Color(0xFF15803D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    if (worker.halfDays > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "${worker.halfDays} અડધો",
                                color = Color(0xFFB45309),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayBreakdownRow(
    dayRecord: MonthlyDayRecord
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryIndigo.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${dayRecord.dayOfMonth}",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = dayRecord.date,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${dayRecord.billsCount} બિલ • ${dayRecord.totalKata} કટા • ${String.format("%.1f", dayRecord.totalWeight)} kg",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹ ${String.format("%.2f", dayRecord.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryBlue
                    )
                    if (dayRecord.workersPresentCount > 0) {
                        Text(
                            text = "🟢 ${dayRecord.workersPresentCount} મજૂર હાજર",
                            fontSize = 10.sp,
                            color = SuccessGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (dayRecord.presentWorkersNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "હાજર: ${dayRecord.presentWorkersNames.joinToString(", ")}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun shareMonthlyReport(context: Context, summary: MonthlyFinancialSummary) {
    val report = buildString {
        appendLine("📊 *APMC માસિક નાણાકીય અને હાજરી હિસાબ*")
        appendLine("📅 મહિનો: ${summary.monthDisplayName} (${summary.monthYear})")
        appendLine("━━━━━━━━━━━━━━━━━━━")
        appendLine("💰 *કુલ બિલ રકમ: ₹${String.format("%.2f", summary.totalBillAmount)}*")
        appendLine("🧾 કુલ બિલ સંખ્યા: ${summary.totalBillsCount}")
        appendLine("📦 કુલ કટા (બારદાન): ${summary.totalKata}")
        appendLine("⚖️ કુલ વજન: ${String.format("%.2f", summary.totalWeight)} kg")
        appendLine("🗓️ કામના સક્રિય દિવસો: ${summary.activeDaysCount}")
        appendLine("━━━━━━━━━━━━━━━━━━━")
        appendLine("👥 *મજૂર હાજરી સંકલન:*")
        if (summary.workerAttendanceList.isEmpty()) {
            appendLine("કોઈ હાજરી નોંધાયેલ નથી.")
        } else {
            summary.workerAttendanceList.forEachIndexed { i, w ->
                appendLine("${i + 1}. ${w.workerName} -> ${w.presentDays} દિવસ હાજર ${if (w.halfDays > 0) "(${w.halfDays} અડધો)" else ""}")
            }
        }
        appendLine("━━━━━━━━━━━━━━━━━━━")
        appendLine("📅 *દિવસ મુજબ બિલ રકમ:*")
        summary.dailyRecords.take(15).forEach { d ->
            appendLine("• ${d.date}: ₹${String.format("%.2f", d.totalAmount)} (${d.billsCount} બિલ, ${d.totalKata} કટા)")
        }
        appendLine("━━━━━━━━━━━━━━━━━━━")
        appendLine("Generated by Labour Bill Application")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "APMC માસિક હિસાબ: ${summary.monthDisplayName}")
        putExtra(Intent.EXTRA_TEXT, report)
    }
    context.startActivity(Intent.createChooser(intent, "માસિક હિસાબ શેર કરો"))
}
