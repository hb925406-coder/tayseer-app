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
import com.tayseer.app.data.entity.AttendanceRecord
import com.tayseer.app.data.entity.AttendanceStatus
import com.tayseer.app.data.entity.Student
import com.tayseer.app.ui.theme.*
import com.tayseer.app.ui.viewmodel.TayseerViewModel
import com.tayseer.app.utils.HijriCalendarHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: TayseerViewModel,
    onNavigateBack: () -> Unit
) {
    val levels by viewModel.levels.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val selectedLevelId by viewModel.selectedLevelId.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentAttendance by viewModel.currentAttendanceMap.collectAsState()
    val hijriOffset by viewModel.hijriOffsetDays.collectAsState()
    val isHijriPrimary by viewModel.isHijriPrimary.collectAsState()

    var activeLevelId by remember(levels) {
        mutableStateOf(selectedLevelId ?: levels.firstOrNull()?.id)
    }

    LaunchedEffect(activeLevelId) {
        viewModel.selectLevel(activeLevelId)
    }

    val studentsInLevel = remember(allStudents, activeLevelId) {
        allStudents.filter { it.levelId == activeLevelId }
    }

    var showNoteDialogForStudent by remember { mutableStateOf<Student?>(null) }
    var noteStatus by remember { mutableStateOf(AttendanceStatus.ABSENT) }
    var noteReason by remember { mutableStateOf("") }
    var noteDetails by remember { mutableStateOf("") }

    val dateFormatted = HijriCalendarHelper.getCombinedDateDisplay(selectedDate, hijriOffset, isHijriPrimary)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تسجيل الحضور اليومي", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (activeLevelId != null) {
                                viewModel.saveAllAttendance(activeLevelId!!, 0L, "المشرف")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حفظ الكل")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SlateBackground)
        ) {
            Surface(
                color = TealLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التاريخ: $dateFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TealDark
                    )
                }
            }

            if (levels.isNotEmpty()) {
                Text(
                    text = "اختر المستوى أو الحلقة:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(levels) { level ->
                        FilterChip(
                            selected = activeLevelId == level.id,
                            onClick = { activeLevelId = level.id },
                            label = { Text(level.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            if (studentsInLevel.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا يوجد طلاب مضافون في هذا المستوى حتى الآن.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(studentsInLevel) { student ->
                        val record = currentAttendance[student.id]
                        val currentStatus = record?.status ?: AttendanceStatus.PRESENT

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = student.fullName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = SlateTextPrimary
                                        )
                                        Text(
                                            text = "رقم الطالب: ${student.studentNumber}",
                                            fontSize = 12.sp,
                                            color = SlateTextSecondary
                                        )
                                    }

                                    if (!record?.reason.isNullOrBlank()) {
                                        Surface(
                                            color = AmberLight,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "السبب: ${record?.reason}",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                fontSize = 11.sp,
                                                color = AmberWarning
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    AttendanceStatusButton(
                                        label = "حاضر",
                                        isSelected = currentStatus == AttendanceStatus.PRESENT,
                                        color = EmeraldAccent,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        viewModel.updateStudentAttendanceStatus(
                                            studentId = student.id,
                                            levelId = student.levelId,
                                            subjectId = 0L,
                                            status = AttendanceStatus.PRESENT,
                                            recorderName = "المشرف"
                                        )
                                    }

                                    AttendanceStatusButton(
                                        label = "غائب",
                                        isSelected = currentStatus == AttendanceStatus.ABSENT,
                                        color = RedDanger,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        viewModel.updateStudentAttendanceStatus(
                                            studentId = student.id,
                                            levelId = student.levelId,
                                            subjectId = 0L,
                                            status = AttendanceStatus.ABSENT,
                                            recorderName = "المشرف"
                                        )
                                    }

                                    AttendanceStatusButton(
                                        label = "متأخر",
                                        isSelected = currentStatus == AttendanceStatus.LATE,
                                        color = AmberWarning,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        noteStatus = AttendanceStatus.LATE
                                        showNoteDialogForStudent = student
                                    }

                                    AttendanceStatusButton(
                                        label = "مستأذن",
                                        isSelected = currentStatus == AttendanceStatus.PERMITTED,
                                        color = BlueInfo,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        noteStatus = AttendanceStatus.PERMITTED
                                        showNoteDialogForStudent = student
                                    }

                                    AttendanceStatusButton(
                                        label = "بعذر",
                                        isSelected = currentStatus == AttendanceStatus.EXCUSED_ABSENCE,
                                        color = Color(0xFFEA580C),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        noteStatus = AttendanceStatus.EXCUSED_ABSENCE
                                        showNoteDialogForStudent = student
                                    }

                                    AttendanceStatusButton(
                                        label = "هارب",
                                        isSelected = currentStatus == AttendanceStatus.TRUANT,
                                        color = PurpleAccent,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        noteStatus = AttendanceStatus.TRUANT
                                        showNoteDialogForStudent = student
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoteDialogForStudent != null) {
        val student = showNoteDialogForStudent!!
        AlertDialog(
            onDismissRequest = { showNoteDialogForStudent = null },
            title = { Text("توثيق حالة ${noteStatus.arabicLabel} - ${student.fullName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteReason,
                        onValueChange = { noteReason = it },
                        label = { Text("السبب (مثال: عذر صحي، تأخر مواصلات)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteDetails,
                        onValueChange = { noteDetails = it },
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateStudentAttendanceStatus(
                        studentId = student.id,
                        levelId = student.levelId,
                        subjectId = 0L,
                        status = noteStatus,
                        reason = noteReason,
                        notes = noteDetails,
                        recorderName = "المشرف"
                    )
                    noteReason = ""
                    noteDetails = ""
                    showNoteDialogForStudent = null
                }) {
                    Text("حفظ الحالة والسبب")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialogForStudent = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun AttendanceStatusButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color else color.copy(alpha = 0.12f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else color
            )
        }
    }
}
