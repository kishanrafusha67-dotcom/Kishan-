package com.example.ui.screens.hazari

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.HazariRecord
import com.example.ui.LabourViewModel
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen
import java.util.Calendar

@Composable
fun HazariScreen(
    viewModel: LabourViewModel
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val onlineUsers by viewModel.onlineUsers.collectAsState()
    val onlineCount by viewModel.onlineUsersCount.collectAsState()
    val hazariList by viewModel.hazariList.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newWorkerName by remember { mutableStateOf("") }
    var newWorkerStatus by remember { mutableStateOf("ONLINE") }
    var newWorkerNotes by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            // HEADER BANNER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
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
                            Text(text = "👥", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "દૈનિક મજૂર હાજરી (Hazari)",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "કોણ ઓનલાઇન છે અને હાજર છે",
                                    fontSize = 12.sp,
                                    color = Color(0xFFBFDBFE)
                                )
                            }
                        }

                        // Share Hazari
                        IconButton(
                            onClick = {
                                val report = buildString {
                                    appendLine("📋 *APMC દૈનિક મજૂર હાજરી રજીસ્ટર*")
                                    appendLine("📅 તારીખ: $selectedDate")
                                    appendLine("🟢 હાલ ઓનલાઇન મજૂર: $onlineCount")
                                    appendLine("━━━━━━━━━━━━━━━━━━━")
                                    hazariList.forEachIndexed { i, h ->
                                        val stEmoji = when (h.status) {
                                            "ONLINE" -> "🟢 હાજર (ઓનલાઇન)"
                                            "COMPLETED" -> "⚪ કામ પૂર્ણ"
                                            "HALF_DAY" -> "🟡 અડધો દિવસ"
                                            else -> "🔴 ગેરહાજર"
                                        }
                                        appendLine("${i + 1}. ${h.workerName} - $stEmoji (સમય: ${h.checkInTime})")
                                    }
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, report)
                                }
                                context.startActivity(Intent.createChooser(intent, "હાજરી રજીસ્ટર શેર કરો"))
                            }
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // MY STATUS TOGGLE (As required: "je majur aavi yo hoy a online thay")
                    currentUser?.let { user ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (user.isOnline) SuccessGreen else Color.Gray)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = user.name,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = if (user.isOnline) "🟢 કામ પર Online છો" else "⚪ હાલ Offline છો",
                                            fontSize = 12.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.toggleMyOnlineStatus() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (user.isOnline) Color(0xFFEF4444) else SuccessGreen
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (user.isOnline) "Check Out" else "Online થાઓ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // DATE SELECTOR & ACTIVE COUNTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        val parts = selectedDate.split("/")
                        val day: Int = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
                        val month: Int = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                        val year: Int = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)

                        DatePickerDialog(context, { _, y: Int, m: Int, d: Int ->
                            val formatted = String.format("%02d/%02d/%04d", d, m + 1, y)
                            viewModel.setDate(formatted)
                        }, year, month, day).show()
                    }
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
                            text = selectedDate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDCFCE7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$onlineCount મજૂર Online",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // LIST OF WORKERS & HAZARI
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Online users quick cards
                item {
                    Text(
                        text = "હાલ કામ પર હાજર / Online મજૂરો ($onlineCount)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                items(onlineUsers) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "👷", fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = user.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "📞 ${user.mobile}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "🟢 Online",
                                    color = Color(0xFF15803D),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Hazari Log
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "$selectedDate ની હાજરી યાદી (${hazariList.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }

                if (hazariList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "આ તારીખે કોઈ વધારાની હાજરી નોંધાયેલ નથી.\nનીચેના '+' બટનથી નવી હાજરી ઉમેરો.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(hazariList) { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = record.workerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "સમય: ${record.checkInTime.ifEmpty { "-" }} ${if (record.notes.isNotEmpty()) "• ${record.notes}" else ""}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (badgeBg, badgeText, statusLabel) = when (record.status) {
                                        "ONLINE" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "હાજર 🟢")
                                        "COMPLETED" -> Triple(Color(0xFFE2E8F0), Color(0xFF475569), "પૂર્ણ ⚪")
                                        "HALF_DAY" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "અડધો દિવસ 🟡")
                                        else -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), "ગેરહાજર 🔴")
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            color = badgeText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteHazari(record.id) },
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

        // FAB to add manual attendance
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp),
            containerColor = PrimaryIndigo,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Hazari")
        }
    }

    // Manual Hazari Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("મજૂર હાજરી ઉમેરો") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newWorkerName,
                        onValueChange = { newWorkerName = it },
                        label = { Text("મજૂરનું નામ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("સ્થિતિ પસંદ કરો:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ONLINE" to "હાજર 🟢",
                            "HALF_DAY" to "અડધો 🟡",
                            "ABSENT" to "ગેરહાજર 🔴"
                        ).forEach { (key, lbl) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (newWorkerStatus == key) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { newWorkerStatus = key }
                            ) {
                                Text(
                                    text = lbl,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (newWorkerStatus == key) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newWorkerNotes,
                        onValueChange = { newWorkerNotes = it },
                        label = { Text("નોંધ (કામ/શેડ નં)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWorkerName.isNotBlank()) {
                            viewModel.addManualHazari(newWorkerName, newWorkerStatus, newWorkerNotes)
                            newWorkerName = ""
                            newWorkerNotes = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("ઉમેરો")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("રદ કરો")
                }
            }
        )
    }
}
