package com.tayseer.app.ui.screens

import androidx.compose.foundation.background
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
import com.tayseer.app.data.entity.EvaluationTagType
import com.tayseer.app.data.entity.Student
import com.tayseer.app.ui.theme.*
import com.tayseer.app.ui.viewmodel.TayseerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationScreen(
    viewModel: TayseerViewModel,
    onNavigateBack: () -> Unit
) {
    val levels by viewModel.levels.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val recentEvaluations by viewModel.recentEvaluations.collectAsState()

    var selectedLevelId by remember(levels) { mutableStateOf(levels.firstOrNull()?.id) }
    val filteredStudents = remember(allStudents, selectedLevelId) {
        allStudents.filter { it.levelId == selectedLevelId }
    }

    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedTag by remember { mutableStateOf(EvaluationTagType.MEMORIZED) }
    var customTagText by remember { mutableStateOf("") }
    var evaluationNotes by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("الأستاذ") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقييم الطلاب والسلوكيات", fontWeight = FontWeight.Bold) },
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
                Text("المستوى / الحلقة:", fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(levels) { lvl ->
                        FilterChip(
                            selected = selectedLevelId == lvl.id,
                            onClick = { selectedLevelId = lvl.id; selectedStudent = null },
                            label = { Text(lvl.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Text("اختر الطالب:", fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(filteredStudents) { st ->
                        FilterChip(
                            selected = selectedStudent?.id == st.id,
                            onClick = { selectedStudent = st },
                            label = { Text(st.fullName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldAccent,
                                selectedLabelColor = Color.White
                            )
                        )
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
                        Text(
                            text = if (selectedStudent != null) "تقييم الطالب: ${selectedStudent!!.fullName}" else "يرجى تحديد طالب أولاً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TealPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("الصفة / نوع التقييم:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))

                        val tags = listOf(
                            Pair(EvaluationTagType.MEMORIZED, "حفظه 📖"),
                            Pair(EvaluationTagType.NOT_MEMORIZED, "لم يحفظ ⚠️"),
                            Pair(EvaluationTagType.DISTINGUISHED, "مميز ⭐"),
                            Pair(EvaluationTagType.WELL_BEHAVED, "مؤدب 🤝"),
                            Pair(EvaluationTagType.FORGOT_BOOKS, "نسيان الكتب 📚"),
                            Pair(EvaluationTagType.CUSTOM, "ملاحظة مخصصة ✍️")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                tags.take(3).forEach { (tag, label) ->
                                    TagChipButton(
                                        label = label,
                                        isSelected = selectedTag == tag,
                                        isPositive = tag.isPositive,
                                        modifier = Modifier.weight(1f)
                                    ) { selectedTag = tag }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                tags.drop(3).forEach { (tag, label) ->
                                    TagChipButton(
                                        label = label,
                                        isSelected = selectedTag == tag,
                                        isPositive = tag.isPositive,
                                        modifier = Modifier.weight(1f)
                                    ) { selectedTag = tag }
                                }
                            }
                        }

                        if (selectedTag == EvaluationTagType.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customTagText,
                                onValueChange = { customTagText = it },
                                label = { Text("عنوان التقييم المخصص") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = evaluationNotes,
                            onValueChange = { evaluationNotes = it },
                            label = { Text("الملاحظات / مقدار الحفظ أو السلوك") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = { teacherName = it },
                            label = { Text("اسم المعلم الموثق") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (selectedStudent != null) {
                                    viewModel.recordEvaluation(
                                        studentId = selectedStudent!!.id,
                                        subjectId = 0L,
                                        teacherName = teacherName,
                                        tag = selectedTag,
                                        customTitle = customTagText,
                                        notes = evaluationNotes
                                    ) {
                                        evaluationNotes = ""
                                        customTagText = ""
                                    }
                                }
                            },
                            enabled = selectedStudent != null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("حفظ تقييم الطالب", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text("سجل التقييمات السابقة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(recentEvaluations) { eval ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (eval.customTagTitle.isNotBlank()) eval.customTagTitle else eval.tag.defaultArabicLabel,
                                fontWeight = FontWeight.Bold,
                                color = if (eval.tag.isPositive) EmeraldAccent else RedDanger
                            )
                            Text(eval.hijriDate, fontSize = 11.sp, color = Color.Gray)
                        }
                        if (eval.notes.isNotBlank()) {
                            Text(eval.notes, style = MaterialTheme.typography.bodyMedium, color = SlateTextPrimary)
                        }
                        Text("بواسطة: ${eval.teacherName}", fontSize = 11.sp, color = SlateTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun TagChipButton(
    label: String,
    isSelected: Boolean,
    isPositive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val activeColor = if (isPositive) EmeraldAccent else RedDanger
    Surface(
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeColor else activeColor.copy(alpha = 0.12f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else activeColor
            )
        }
    }
}
