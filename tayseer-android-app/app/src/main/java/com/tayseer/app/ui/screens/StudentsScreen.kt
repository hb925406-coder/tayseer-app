package com.tayseer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tayseer.app.ui.theme.*
import com.tayseer.app.ui.viewmodel.TayseerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: TayseerViewModel,
    onStudentClick: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val allStudents by viewModel.allStudents.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val sections by viewModel.sections.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevelId by remember { mutableStateOf<Long?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showCsvDialog by remember { mutableStateOf(false) }

    val filteredStudents = remember(allStudents, searchQuery, selectedLevelId) {
        allStudents.filter { st ->
            (selectedLevelId == null || st.levelId == selectedLevelId) &&
            (searchQuery.isBlank() || st.fullName.contains(searchQuery, ignoreCase = true) || st.studentNumber.contains(searchQuery))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الطلاب وقوائم الفصول", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExtendedFloatingActionButton(
                    onClick = { showCsvDialog = true },
                    icon = { Icon(Icons.Default.FileUpload, null) },
                    text = { Text("استيراد CSV") },
                    containerColor = TealLight,
                    contentColor = TealDark
                )
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("إضافة طالب") },
                    containerColor = TealPrimary,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SlateBackground)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("بحث باسم الطالب أو الرقم...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedLevelId == null,
                        onClick = { selectedLevelId = null },
                        label = { Text("الكل (${allStudents.size})") }
                    )
                }
                items(levels) { lvl ->
                    FilterChip(
                        selected = selectedLevelId == lvl.id,
                        onClick = { selectedLevelId = lvl.id },
                        label = { Text(lvl.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TealPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredStudents) { student ->
                    val lvlName = levels.find { it.id == student.levelId }?.name ?: "مستوى"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStudentClick(student.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = student.fullName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "رقم: ${student.studentNumber}  •  $lvlName",
                                    fontSize = 13.sp,
                                    color = SlateTextSecondary
                                )
                                if (student.parentAccessCode.isNotBlank()) {
                                    Text(
                                        text = "رمز ولي الأمر: ${student.parentAccessCode}",
                                        fontSize = 11.sp,
                                        color = BlueInfo,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var number by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var parentPhone by remember { mutableStateOf("") }
        val secId = sections.firstOrNull()?.id ?: 1L
        val lvlId = levels.firstOrNull()?.id ?: 1L

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة طالب جديد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("رقم الطالب (الفريد)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الرباعي للطالب") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("هاتف ولي الأمر") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addStudent(number, name, secId, lvlId, "", parentPhone, { showAddDialog = false }, {})
                }) {
                    Text("حفظ الطالب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
            }
        )
    }

    if (showCsvDialog) {
        var csvText by remember {
            mutableStateOf("101,صالح أحمد بامطرف,771000001,771000002\n102,محمد سعيد باوزير,772000001,772000002")
        }
        val secId = sections.firstOrNull()?.id ?: 1L
        val lvlId = levels.firstOrNull()?.id ?: 1L

        AlertDialog(
            onDismissRequest = { showCsvDialog = false },
            title = { Text("استيراد بيانات الطلاب من CSV", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الصيغة: رقم الطالب,الاسم الكامل,هاتف الطالب,هاتف ولي الأمر", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = csvText,
                        onValueChange = { csvText = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        label = { Text("بيانات CSV") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.importStudentsCsv(csvText, secId, lvlId) { _, _ -> showCsvDialog = false }
                }) {
                    Text("بدء الاستيراد")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCsvDialog = false }) { Text("إلغاء") }
            }
        )
    }
}
