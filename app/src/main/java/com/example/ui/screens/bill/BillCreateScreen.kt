package com.example.ui.screens.bill

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ApmcDirectory
import com.example.data.model.ApmcShop
import com.example.data.model.Bill
import com.example.ui.LabourViewModel
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.HeaderGradientEnd
import com.example.ui.theme.HeaderGradientStart
import com.example.ui.theme.LightCardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryGradientEnd
import com.example.ui.theme.PrimaryGradientStart
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SuccessGreen
import com.example.util.ShareReceiptUtil
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillCreateScreen(
    viewModel: LabourViewModel,
    onNavigateToPocket: () -> Unit,
    onNavigateToHazari: () -> Unit
) {
    val context = LocalContext.current

    val isDark by viewModel.isDarkMode.collectAsState()
    val onlineCount by viewModel.onlineUsersCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val date by viewModel.formDate.collectAsState()
    val billNumber by viewModel.formBillNumber.collectAsState()
    val vakal by viewModel.formVakal.collectAsState()
    val shopNumber by viewModel.formShopNumber.collectAsState()
    val shopName by viewModel.formShopName.collectAsState()
    val shopMobile by viewModel.formShopMobile.collectAsState()

    val kata by viewModel.formKata.collectAsState()
    val bharti by viewModel.formBharti.collectAsState()
    val kilo by viewModel.formKilo.collectAsState()
    val gram by viewModel.formGram.collectAsState()
    val shedNumber by viewModel.formShedNumber.collectAsState()
    val pillarNumber by viewModel.formPillarNumber.collectAsState()
    val totalMudda by viewModel.formTotalMudda.collectAsState()
    val bhav by viewModel.formBhav.collectAsState()

    val totalAmount by viewModel.totalCalculatedAmount.collectAsState()
    val totalWeight by viewModel.totalCalculatedWeight.collectAsState()

    var showShopSheet by remember { mutableStateOf(false) }
    var shopSearchQuery by remember { mutableStateOf("") }
    var savedBillForShare by remember { mutableStateOf<Bill?>(null) }

    // Date picker
    val calendar = remember { Calendar.getInstance() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp)
    ) {
        // TOP BANNER (Gradient with "Bill" and "Pocket" button)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(HeaderGradientStart, HeaderGradientEnd)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bill",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Dark mode toggle
                    IconButton(
                        onClick = { viewModel.toggleDarkMode(!isDark) },
                        modifier = Modifier.testTag("dark_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = Color.White
                        )
                    }

                    // Pocket Button (Matching screenshot 162036)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .clickable { onNavigateToPocket() }
                            .testTag("pocket_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(text = "📊", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pocket",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // ONLINE WORKERS STATUS BAR
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { onNavigateToHazari() }
                .testTag("online_workers_banner"),
            shape = RoundedCornerShape(14.dp),
            color = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isDark) Color(0xFF334155) else Color(0xFFBFDBFE)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$onlineCount મજૂર હાલ કામ પર Online છે",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8)
                    )
                }

                Text(
                    text = "હાજરી જુઓ ❯",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SECTION 1: BILL INFORMATION CARD (Matching screenshot)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("bill_info_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Section Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧾", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bill Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date & Bill Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = "તારીખ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val parts = date.split("/")
                                    val day: Int = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
                                    val month: Int = (parts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                                    val year: Int = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)

                                    DatePickerDialog(context, { _, y: Int, m: Int, d: Int ->
                                        val formatted = String.format("%02d/%02d/%04d", d, m + 1, y)
                                        viewModel.setDate(formatted)
                                    }, year, month, day).show()
                                }
                                .testTag("date_picker_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = date,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Date",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Bill Number
                    Column(modifier = Modifier.weight(0.9f)) {
                        Text(
                            text = "Bill Number",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = billNumber,
                            onValueChange = { viewModel.formBillNumber.value = it },
                            placeholder = { Text("0001") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bill_number_input")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Vakal (વકલ)
                Text(
                    text = "વકલ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = vakal,
                    onValueChange = { viewModel.formVakal.value = it },
                    placeholder = { Text("વકલ / લોટ નંબર") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vakal_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Select Shop dropdown button (દુકાન પસંદ કરો)
                Text(
                    text = "દુકાન પસંદ કરો",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showShopSheet = true }
                        .testTag("select_shop_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (shopName.isNotEmpty()) "$shopNumber - $shopName" else "દુકાન પસંદ કરો (APMC 260 દુકાનો)",
                            fontSize = 15.sp,
                            fontWeight = if (shopName.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                            color = if (shopName.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Shop",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shop Number (Shop Number નાખો)
                Text(
                    text = "Shop Number",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = shopNumber,
                    onValueChange = { viewModel.onShopNumberChange(it) },
                    placeholder = { Text("Shop number નાખો (દા.ત. 1, 2, 3...)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_number_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Shop Name Display Box (matching screenshot: light blue card)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF0F7FF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFBFDBFE)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Shop Name",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (shopName.isNotEmpty()) shopName else "—",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                        )
                    }
                }

                // Shop Mobile Display Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF0F7FF),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0xFF334155) else Color(0xFFBFDBFE)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Shop Mobile",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (shopMobile.isNotEmpty()) shopMobile else "—",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                        )
                    }
                }
            }
        }

        // SECTION 2: GOODS / CALCULATION CARD (માલ / ગણતરી)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("goods_calc_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Section Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📦", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "માલ / ગણતરી",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Table rows matching screenshot 162036
                CalcRow(
                    label = "કટા",
                    value = kata,
                    placeholder = "કટા નાખો",
                    keyboardType = KeyboardType.Number,
                    testTag = "kata_input",
                    onValueChange = {
                        viewModel.formKata.value = it
                        viewModel.recalculateBill()
                    }
                )

                CalcRow(
                    label = "ભરતી",
                    value = bharti,
                    placeholder = "ભરતી (દા.ત. 50/20)",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "bharti_input",
                    onValueChange = {
                        viewModel.formBharti.value = it
                        viewModel.recalculateBill()
                    }
                )

                CalcRow(
                    label = "કિલો",
                    value = kilo,
                    placeholder = "કિલો",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "kilo_input",
                    onValueChange = {
                        viewModel.formKilo.value = it
                        viewModel.recalculateBill()
                    }
                )

                CalcRow(
                    label = "ગ્રામ",
                    value = gram,
                    placeholder = "ગ્રામ",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "gram_input",
                    onValueChange = {
                        viewModel.formGram.value = it
                        viewModel.recalculateBill()
                    }
                )

                CalcRow(
                    label = "શેડ નંબર",
                    value = shedNumber,
                    placeholder = "શેડ નં",
                    keyboardType = KeyboardType.Text,
                    testTag = "shed_number_input",
                    onValueChange = { viewModel.formShedNumber.value = it }
                )

                CalcRow(
                    label = "પીલોર નંબર",
                    value = pillarNumber,
                    placeholder = "પીલોર નં",
                    keyboardType = KeyboardType.Text,
                    testTag = "pillar_number_input",
                    onValueChange = { viewModel.formPillarNumber.value = it }
                )

                CalcRow(
                    label = "કુલ મુદ્દા",
                    value = totalMudda,
                    placeholder = "કુલ મુદ્દા (દા.ત. 10)",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "mudda_input",
                    onValueChange = {
                        viewModel.formTotalMudda.value = it
                        viewModel.recalculateBill()
                    }
                )

                CalcRow(
                    label = "ભાવ",
                    value = bhav,
                    placeholder = "ભાવ (₹)",
                    keyboardType = KeyboardType.Decimal,
                    testTag = "bhav_input",
                    onValueChange = {
                        viewModel.formBhav.value = it
                        viewModel.recalculateBill()
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // TOTAL DISPLAY BAR (Dark bar matching screenshot: Total 0)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("total_display_bar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (totalWeight > 0) {
                                Text(
                                    text = "કુલ વજન: $totalWeight kg",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Text(
                            text = if (totalAmount > 0) "₹ ${String.format("%.2f", totalAmount)}" else "0",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ACTION BUTTONS (Save Bill & Clear)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.saveBill { savedBill ->
                        savedBillForShare = savedBill
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_bill_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💾", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Bill",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = { viewModel.clearForm() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("clear_bill_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔄", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // MODAL BOTTOM SHEET FOR APMC SHOP SELECTION
    if (showShopSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showShopSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "APMC દુકાનો પસંદ કરો (260 દુકાનો)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = shopSearchQuery,
                    onValueChange = { shopSearchQuery = it },
                    placeholder = { Text("દુકાન નંબર અથવા નામ શોધો...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredShops = remember(shopSearchQuery) {
                    ApmcDirectory.search(shopSearchQuery)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(filteredShops) { shop ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.selectShop(shop)
                                    showShopSheet = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PrimaryIndigo.copy(alpha = 0.15f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = shop.shopNumber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = PrimaryIndigo
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = shop.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "📞 ${shop.mobile}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    // POST-SAVE SHARE DIALOG ("bill photo share option")
    savedBillForShare?.let { bill ->
        AlertDialog(
            onDismissRequest = { savedBillForShare = null },
            title = {
                Text(
                    text = "✅ બિલ નં ${bill.billNumber} સાચવ્યું!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("દુકાન: ${bill.shopName} (${bill.shopNumber})")
                    Text("કટા: ${bill.kata} | વજન: ${bill.totalWeight} kg")
                    Text("કુલ રકમ: ₹${bill.totalAmount}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "આ બિલનો ફોટો (Receipt Slip) અથવા વિગત શેર કરો:",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ShareReceiptUtil.shareBillAsPhoto(context, bill)
                        savedBillForShare = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("📸 Bill Photo Share")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        ShareReceiptUtil.shareBillAsText(context, bill)
                        savedBillForShare = null
                    }
                ) {
                    Text("💬 Text Share")
                }
            }
        )
    }
}

@Composable
private fun CalcRow(
    label: String,
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    testTag: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.2f)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1.8f)
                .testTag(testTag)
        )
    }
}
