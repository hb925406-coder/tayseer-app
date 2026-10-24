package com.tayseer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tayseer.app.data.entity.AttendanceStatus
import com.tayseer.app.ui.theme.*
import com.tayseer.app.ui.viewmodel.TayseerViewModel
import com.tayseer.app.utils.HijriCalendarHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TayseerViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToEvaluation: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val totalStudents by viewModel.totalStudentsCount.collectAsState()
    val todayAttendance by viewModel.todayAttendanceRecords.collectAsState()
    val recentEvaluations by viewModel.recentEvaluations.collectAsState()
    val isHijriPrimary by viewModel.isHijriPrimary.collectAsState()
    val hijriOffset by viewModel.hijriOffsetDays.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    val presentCount = todayAttendance.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = todayAttendance.count { it.status == AttendanceStatus.ABSENT }
    val lateCount = todayAttendance.count { it.status == AttendanceStatus.LATE }
    val permittedCount = todayAttendance.count { it.status == AttendanceStatus.PERMITTED }
    val excusedCount = todayAttendance.count { it.status == AttendanceStatus.EXCUSED_ABSENCE }

    val dateDisplay = HijriCalendarHelper.getCombinedDateDisplay(selectedDate, hijriOffset, isHijriPrimary)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("الرئيسية • تطبيق تيسير", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(dateDisplay, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
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
                Text(
                    text = "إحصائيات الحضور لليوم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "إجمالي الطلاب",
                        value = totalStudents.toString(),
                        icon = Icons.Default.Groups,
                        containerColor = TealLight,
                        contentColor = TealDark,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "الحاضرون اليوم",
                        value = presentCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        containerColor = EmeraldLight,
                        contentColor = Color(0xFF065F46),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "الغائبون",
                        value = absentCount.toString(),
                        icon = Icons.Default.Cancel,
                        containerColor = RedLight,
                        contentColor = RedDanger,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "متأخر / مستأذن",
                        value = (lateCount + permittedCount + excusedCount).toString(),
                        icon = Icons.Default.AccessTime,
                        containerColor = AmberLight,
                        contentColor = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "الإجراءات السريعة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionButton(
                        title = "تسجيل الحضور",
                        icon = Icons.Default.FactCheck,
                        color = TealPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAttendance
                    )
                    QuickActionButton(
                        title = "تقييم طالب",
                        icon = Icons.Default.Star,
                        color = EmeraldAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToEvaluation
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionButton(
                        title = "إدارة الطلاب",
                        icon = Icons.Default.PersonAdd,
                        color = BlueInfo,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStudents
                    )
                    QuickActionButton(
                        title = "الإعدادات والنسخ",
                        icon = Icons.Default.Settings,
                        color = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSettings
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أحدث التقييمات المسجلة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    TextButton(onClick = onNavigateToEvaluation) {
                        Text("عرض الكل", color = TealPrimary)
                    }
                }
            }

            if (recentEvaluations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "لا توجد تقييمات مسجلة اليوم. استخدم الأزرار أعلاه لتسجيل أول تقييم.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(recentEvaluations) { eval ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (eval.customTagTitle.isNotBlank()) eval.customTagTitle else eval.tag.defaultArabicLabel,
                                    fontWeight = FontWeight.Bold,
                                    color = if (eval.tag.isPositive) EmeraldAccent else RedDanger,
                                    fontSize = 15.sp
                                )
                                if (eval.notes.isNotBlank()) {
                                    Text(
                                        text = eval.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateTextSecondary
                                    )
                                }
                                Text(
                                    text = "المعلم: ${eval.teacherName} • ${eval.hijriDate}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 13.sp, color = contentColor, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = color)
                }
            }
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SlateTextPrimary)
        }
    }
}
