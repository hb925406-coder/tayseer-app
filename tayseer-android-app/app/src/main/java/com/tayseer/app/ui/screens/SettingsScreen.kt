package com.tayseer.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.tayseer.app.ui.viewmodel.AuthViewModel
import com.tayseer.app.ui.viewmodel.TayseerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    tayseerViewModel: TayseerViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isHijriPrimary by tayseerViewModel.isHijriPrimary.collectAsState()
    val hijriOffset by tayseerViewModel.hijriOffsetDays.collectAsState()

    var showClearDataConfirm by remember { mutableStateOf(false) }
    var showLoadDemoConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات وإدارة النظام", fontWeight = FontWeight.Bold) },
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
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("إعدادات التقويم والتاريخ", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("التقويم الهجري كافتراضي في الواجهات")
                            Switch(
                                checked = isHijriPrimary,
                                onCheckedChange = { tayseerViewModel.setCalendarPrimary(it) }
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        Text("ضبط التاريخ الهجري (لرؤية الهلال):", fontSize = 13.sp, color = SlateTextSecondary)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            FilterChip(
                                selected = hijriOffset == -1,
                                onClick = { tayseerViewModel.setHijriOffset(-1) },
                                label = { Text("-1 يوم") }
                            )
                            FilterChip(
                                selected = hijriOffset == 0,
                                onClick = { tayseerViewModel.setHijriOffset(0) },
                                label = { Text("تلقائي (0)") }
                            )
                            FilterChip(
                                selected = hijriOffset == 1,
                                onClick = { tayseerViewModel.setHijriOffset(1) },
                                label = { Text("+1 يوم") }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("البيانات التجريبية والاختبار", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("يمكنك تحميل عينة من الحلقات والطلاب وسجلات الحضور والتقييمات لتجربة التطبيق فوراً، أو مسحها في أي وقت.", fontSize = 13.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { showLoadDemoConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TealLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تحميل بيانات تجريبية", color = TealDark, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showClearDataConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RedLight),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("مسح كافة البيانات", color = RedDanger, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("عن تطبيق تيسير", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("الإصدار: 1.0.0 (نسخة أصلية Native 100%)", fontSize = 13.sp, color = SlateTextPrimary)
                        Text("التخزين: محلي بالكامل على الهاتف بواسطة Room SQLite", fontSize = 13.sp, color = SlateTextSecondary)
                        Text("الخصوصية: يعمل بدون إنترنت تماماً ولا يتم رفع أي بيانات خارج هاتفك.", fontSize = 13.sp, color = EmeraldAccent)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDanger)
                ) {
                    Icon(Icons.Default.ExitToApp, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تسجيل الخروج من الحساب")
                }
            }
        }
    }

    if (showLoadDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showLoadDemoConfirm = false },
            title = { Text("تحميل البيانات التجريبية") },
            text = { Text("هل تريد إضافة حلقات وطلاب وسجلات حضور تجريبية لتجربة التطبيق؟") },
            confirmButton = {
                Button(onClick = {
                    tayseerViewModel.loadDemoData()
                    showLoadDemoConfirm = false
                }) { Text("تأكيد التحميل") }
            },
            dismissButton = {
                TextButton(onClick = { showLoadDemoConfirm = false }) { Text("إلغاء") }
            }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("تأكيد مسح البيانات", color = RedDanger) },
            text = { Text("تحذير: سيتم حذف جميع الأقسام والحلقات والطلاب وسجلات الحضور والتقييمات نهائياً من قاعدة البيانات المحلية.") },
            confirmButton = {
                Button(
                    onClick = {
                        tayseerViewModel.clearAllData()
                        showClearDataConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                ) { Text("مسح نهائي") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) { Text("إلغاء") }
            }
        )
    }
}
