package com.tayseer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tayseer.app.data.entity.AttendanceStatus
import com.tayseer.app.ui.theme.*
import com.tayseer.app.ui.viewmodel.TayseerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: Long,
    viewModel: TayseerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val allStudents by viewModel.allStudents.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val sections by viewModel.sections.collectAsState()

    val student = remember(allStudents, studentId) {
        allStudents.find { it.id == studentId }
    }

    val attendanceRecords by viewModel.getStudentAttendanceFlow(studentId).collectAsState(initial = emptyList())
    val evaluations by viewModel.getStudentEvaluationsFlow(studentId).collectAsState(initial = emptyList())

    val sectionName = sections.find { it.id == student?.sectionId }?.name ?: "القسم"
    val levelName = levels.find { it.id == student?.levelId }?.name ?: "المستوى"

    val presentCount = attendanceRecords.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = attendanceRecords.count { it.status == AttendanceStatus.ABSENT }
    val lateCount = attendanceRecords.count { it.status == AttendanceStatus.LATE }
    val permittedCount = attendanceRecords.count { it.status == AttendanceStatus.PERMITTED }
    val excusedCount = attendanceRecords.count { it.status == AttendanceStatus.EXCUSED_ABSENCE }
    val truantCount = attendanceRecords.count { it.status == AttendanceStatus.TRUANT }
    val totalRecords = attendanceRecords.size
    val rate = if (totalRecords > 0) ((presentCount + permittedCount) * 100 / totalRecords) else 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(student?.fullName ?: "ملف الطالب", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        if (student == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لم يتم العثور على بيانات الطالب")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(SlateBackground)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = student.fullName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("رقم الطالب: ${student.studentNumber} • رمز ولي الأمر: ${student.parentAccessCode}", color = TealPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("القسم: $sectionName • المستوى: $levelName", color = SlateTextSecondary, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("سجل الحضور والغياب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Surface(color = EmeraldLight, shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = "نسبة الحضور: $rate%",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        color = EmeraldAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatBoxSmall("حاضر", presentCount.toString(), EmeraldLight, EmeraldAccent, Modifier.weight(1f))
                                StatBoxSmall("غائب", absentCount.toString(), RedLight, RedDanger, Modifier.weight(1f))
                                StatBoxSmall("متأخر", lateCount.toString(), AmberLight, AmberWarning, Modifier.weight(1f))
                                StatBoxSmall("مستأذن", permittedCount.toString(), BlueLight, BlueInfo, Modifier.weight(1f))
                                StatBoxSmall("بعذر", excusedCount.toString(), Color(0xFFFFEDD5), Color(0xFFEA580C), Modifier.weight(1f))
                                StatBoxSmall("هارب", truantCount.toString(), PurpleLight, PurpleAccent, Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.exportAndShareReport(context, student) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير تقرير PDF ومشاركته مع ولي الأمر", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    Text("التقييمات والسلوكيات المسجلة (${evaluations.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (evaluations.isEmpty()) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Text("لا توجد تقييمات مسجلة بعد لهذا الطالب.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                        }
                    }
                } else {
                    items(evaluations) { ev ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = if (ev.customTagTitle.isNotBlank()) ev.customTagTitle else ev.tag.defaultArabicLabel,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ev.tag.isPositive) EmeraldAccent else RedDanger
                                    )
                                    Text(ev.hijriDate, fontSize = 11.sp, color = Color.Gray)
                                }
                                if (ev.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(ev.notes, fontSize = 13.sp, color = SlateTextPrimary)
                                }
                                Text("المعلم: ${ev.teacherName}", fontSize = 11.sp, color = SlateTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatBoxSmall(label: String, count: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = textColor)
            Text(count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
