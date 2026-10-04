package com.example.ui.screens.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.local.ApmcDirectory
import com.example.data.model.BackupData
import com.example.ui.LabourViewModel
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen
import com.example.util.BackupRestoreManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: LabourViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingBackupData by remember { mutableStateOf<BackupData?>(null) }
    var pendingJsonContent by remember { mutableStateOf<String?>(null) }
    var replaceExistingData by remember { mutableStateOf(false) }

    // Launcher for exporting/saving JSON file to device storage
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            val json = viewModel.exportLedgerJson()
            val success = BackupRestoreManager.writeToUri(context, uri, json)
            if (success) {
                Toast.makeText(context, "બેકઅપ ફાઇલ સફળતાપૂર્વક સાચવવામાં આવી! ✅", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "ફાઇલ સાચવવામાં ક્ષતિ થઈ.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher for selecting/importing JSON file from device storage
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val jsonContent = BackupRestoreManager.readFromUri(context, uri)
            if (jsonContent != null) {
                val parsed = viewModel.parseBackupData(jsonContent)
                if (parsed != null) {
                    pendingBackupData = parsed
                    pendingJsonContent = jsonContent
                    showRestoreConfirmDialog = true
                } else {
                    Toast.makeText(context, "અમાન્ય JSON બેકઅપ ફાઇલ છે.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "ફાઇલ વાંચવામાં નિષ્ફળતા મળી.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
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
                        colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryIndigo,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "👤", fontSize = 28.sp)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = currentUser?.name ?: "મજૂર પ્રોફાઇલ",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "📞 ${currentUser?.mobile ?: "-"}",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // LOCAL JSON BACKUP & RESTORE CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("backup_restore_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💾", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ડેટા બેકઅપ અને રિસ્ટોર (Backup & Restore)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        Text(
                            text = "તમારો ખાતાવહી ડેટા સુરક્ષિત JSON ફાઇલમાં સાચવો",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // EXPORT / BACKUP BUTTONS
                Text(
                    text = "૧. બેકઅપ બનાવો (Export Backup):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save to device storage
                    Button(
                        onClick = {
                            val timeStamp = SimpleDateFormat("dd_MM_yyyy_HHmm", Locale.getDefault()).format(Date())
                            val defaultName = "labour_backup_$timeStamp.json"
                            createDocumentLauncher.launch(defaultName)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_backup_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Save",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("સેવ કરો (JSON)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Share backup file directly
                    OutlinedButton(
                        onClick = {
                            val json = viewModel.exportLedgerJson()
                            val timeStamp = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault()).format(Date())
                            val fileName = "labour_ledger_$timeStamp.json"
                            BackupRestoreManager.shareBackupFile(context, json, fileName)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_backup_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(16.dp),
                                tint = PrimaryIndigo
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("શેર કરો", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // IMPORT / RESTORE BUTTON
                Text(
                    text = "૨. અગાઉનો બેકઅપ પાછો લાવો (Restore Backup):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        openDocumentLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restore_backup_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = "Restore",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "JSON ફાઇલમાંથી રિસ્ટોર કરો",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // GENERAL SETTINGS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "સામાન્ય સેટિંગ્સ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dark mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Dark Mode",
                            tint = PrimaryIndigo
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ડાર્ક મોડ (Dark Mode)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isDark) "રાત્રિ મોડ ચાલુ છે" else "દિવસ મોડ ચાલુ છે",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }
        }

        // APMC DIRECTORY INFO
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APMC ડાયરેક્ટરી વિગત",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Stores",
                        tint = SuccessGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "કુલ ${ApmcDirectory.shops.size} APMC દુકાનો ઉપલબ્ધ છે (ઓફલાઇન)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "બિલ બનાવતી વખતે દુકાન નંબર નાખવાથી નામ અને મોબાઇલ આપોઆપ આવી જશે.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // DATA ISOLATION NOTICE
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Data Security",
                        tint = PrimaryIndigo
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "સુરક્ષિત અને અલગ ડેટા",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "દરેક મજૂરનો પોતાનો ડેટા (બિલ, પોકેટ કમાણી અને હાજરી) સંપૂર્ણ અલગ અને સ્થાનિક ડેટાબેઝમાં સુરક્ષિત રહે છે.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LOGOUT / SWITCH USER BUTTON
        Button(
            onClick = {
                viewModel.logout()
                onLogout()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
                .testTag("logout_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "બીજા મજૂર માટે Login કરો / Log Out",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // RESTORE CONFIRMATION PREVIEW DIALOG
    if (showRestoreConfirmDialog && pendingBackupData != null && pendingJsonContent != null) {
        val data = pendingBackupData!!
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = {
                Text(
                    text = "🔄 બેકઅપ રિસ્ટોર કરો",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "બેકઅપ ફાઇલ વિગત:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• યુઝર: ${data.userName} (${data.userMobile})", fontSize = 13.sp)
                    Text("• તારીખ: ${data.exportDate}", fontSize = 13.sp)
                    Text("• કુલ બિલ: ${data.totalBillsCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    Text("• હાજરી રેકોર્ડ: ${data.totalHazariCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "રિસ્ટોર કરવાની પદ્ધતિ પસંદ કરો:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { replaceExistingData = false }
                    ) {
                        RadioButton(
                            selected = !replaceExistingData,
                            onClick = { replaceExistingData = false }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("હાલના ડેટામાં ઉમેરો (Merge)", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("હાલના બિલ જળવાઈ રહેશે", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { replaceExistingData = true }
                    ) {
                        RadioButton(
                            selected = replaceExistingData,
                            onClick = { replaceExistingData = true }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("નવા ડેટાથી બદલો (Replace)", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFFDC2626))
                            Text("હાલના બિલ હટાવી નવો બેકઅપ મૂકશે", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val content = pendingJsonContent!!
                        viewModel.importLedgerFromJson(content, replaceExistingData) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                        showRestoreConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("હા, રિસ્ટોર કરો")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("રદ કરો")
                }
            }
        )
    }
}
