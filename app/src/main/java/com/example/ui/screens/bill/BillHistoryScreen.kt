package com.example.ui.screens.bill

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import com.example.data.model.Bill
import com.example.ui.LabourViewModel
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryIndigo
import com.example.util.ShareReceiptUtil

@Composable
fun BillHistoryScreen(
    viewModel: LabourViewModel
) {
    val context = LocalContext.current
    val bills by viewModel.userBills.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedBillForSlip by remember { mutableStateOf<Bill?>(null) }

    val filteredBills = remember(bills, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) bills
        else bills.filter {
            it.billNumber.lowercase().contains(q) ||
            it.shopName.lowercase().contains(q) ||
            it.shopNumber.lowercase().contains(q) ||
            it.date.contains(q) ||
            it.vakal.lowercase().contains(q)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        colors = listOf(Color(0xFF4338CA), Color(0xFF6366F1))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "બિલ યાદી (Bill History)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "કુલ ${bills.size} બિલ સાચવેલ છે",
                        fontSize = 12.sp,
                        color = Color(0xFFE0E7FF)
                    )
                }

                Text(text = "📋", fontSize = 28.sp)
            }
        }

        // SEARCH BAR
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("બિલ નંબર, દુકાન અથવા તારીખ શોધો...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("bill_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredBills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🧾", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "કોઈ બિલ મળ્યું નથી" else "હજુ સુધી કોઈ બિલ બનાવ્યું નથી",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            .padding(vertical = 6.dp)
                            .clickable { selectedBillForSlip = bill },
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
                                        modifier = Modifier.size(36.dp)
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
                                            text = bill.shopName.ifEmpty { "દુકાન નં. ${bill.shopNumber}" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "કટા: ${bill.kata} • વજન: ${bill.totalWeight} kg • ભાવ: ₹${bill.bhav}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row {
                                    // Bill Photo Share button
                                    Button(
                                        onClick = { ShareReceiptUtil.shareBillAsPhoto(context, bill) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PrimaryIndigo
                                        ),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                            horizontal = 10.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = "📸 Photo",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Text share
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

    // DETAILED RECEIPT SLIP POPUP
    selectedBillForSlip?.let { bill ->
        AlertDialog(
            onDismissRequest = { selectedBillForSlip = null },
            title = {
                Text(
                    text = "📄 બિલ પાવતી (Slip)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text("બિલ નં: ${bill.billNumber} | તારીખ: ${bill.date}", fontWeight = FontWeight.SemiBold)
                    Text("દુકાન: ${bill.shopName} (${bill.shopNumber})")
                    if (bill.shopMobile.isNotEmpty()) Text("ફોન: ${bill.shopMobile}")
                    if (bill.vakal.isNotEmpty()) Text("વકલ: ${bill.vakal}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("કટા (બારદાન): ${bill.kata}")
                    Text("ભરતી: ${bill.bharti} kg")
                    Text("કિલો/ગ્રામ: ${bill.kilo} kg ${bill.gram} gm")
                    if (bill.shedNumber.isNotEmpty()) Text("શેડ નં: ${bill.shedNumber} • પીલોર નં: ${bill.pillarNumber}")
                    Text("ભાવ: ₹${bill.bhav}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("કુલ વજન: ${bill.totalWeight} kg", fontWeight = FontWeight.Bold)
                    Text("કુલ રકમ: ₹${bill.totalAmount}", fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, fontSize = 18.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ShareReceiptUtil.shareBillAsPhoto(context, bill)
                        selectedBillForSlip = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("📸 Photo Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBillForSlip = null }) {
                    Text("બંધ કરો")
                }
            }
        )
    }
}
