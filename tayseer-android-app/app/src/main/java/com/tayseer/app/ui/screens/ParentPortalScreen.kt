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
import com.tayseer.app.ui.viewmodel.AuthViewModel
import com.tayseer.app.ui.viewmodel.TayseerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentPortalScreen(
    studentId: Long,
    authViewModel: AuthViewModel,
    tayseerViewModel: TayseerViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val allStudents by tayseerViewModel.allStudents.collectAsState()
    val student = allStudents.find { it.id == studentId }

    val attendanceRecords by tayseerViewModel.getStudentAttendanceFlow(studentId).collectAsState(initial = emptyList())
    val evaluations by tayseerViewModel.getStudentEvaluationsFlow(studentId).collectAsState(initial = emptyList())

    val presentCount = attendanceRecords.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = attendanceRecords.count { it.status == AttendanceStatus.ABSENT }
    val lateCount = attendanceRecords.count { it.status == AttendanceStatus.LATE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بوابة ولي الأمر • تيسير", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "خروج", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TealPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
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
                        Text("مرحباً بك يا ولي أمر الطالب:", color = SlateTextSecondary, fontSize = 13.sp)
                        Text(
                            text = student?.fullName ?: "الطالب",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                        Text("رقم القيد: ${student?.studentNumber}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("أيام الحضور", presentCount.toString(), Icons.Default.Check, EmeraldLight, EmeraldAccent, Modifier.weight(1f))
                    StatCard("أيام الغياب", absentCount.toString(), Icons.Default.Cancel, RedLight, RedDanger, Modifier.weight(1f))
                    StatCard("مرات التأخر", lateCount.toString(), Icons.Default.AccessTime, AmberLight, AmberWarning, Modifier.weight(1f))
                }
            }

            item {
                if (student != null) {
                    Button(
                        onClick = { tayseerViewModel.exportAndShareReport(context, student) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.Download, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تحميل تقرير المتابعة كملف PDF")
                    }
                }
            }

            item {
                Text("ملاحظات المعلمين والتقييمات الأخيرة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            if (evaluations.isEmpty()) {
                item {
                    Text("لا توجد تقييمات مسجلة حتى الآن.", color = Color.Gray)
                }
            } else {
                items(evaluations) { ev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
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
