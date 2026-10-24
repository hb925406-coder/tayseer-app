package com.tayseer.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tayseer.app.data.entity.*
import com.tayseer.app.data.repository.TayseerRepository
import com.tayseer.app.utils.HijriCalendarHelper
import com.tayseer.app.utils.PdfReportGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TayseerViewModel(private val repository: TayseerRepository) : ViewModel() {

    private val _isHijriPrimary = MutableStateFlow(true)
    val isHijriPrimary: StateFlow<Boolean> = _isHijriPrimary.asStateFlow()

    private val _hijriOffsetDays = MutableStateFlow(0)
    val hijriOffsetDays: StateFlow<Int> = _hijriOffsetDays.asStateFlow()

    val sections: StateFlow<List<Section>> = repository.getAllSections()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val levels: StateFlow<List<Level>> = repository.getAllLevels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudentsCount: StateFlow<Int> = repository.countTotalStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentEvaluations: StateFlow<List<EvaluationRecord>> = repository.getRecentEvaluations(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(HijriCalendarHelper.getTodayIsoDate())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedLevelId = MutableStateFlow<Long?>(null)
    val selectedLevelId: StateFlow<Long?> = _selectedLevelId.asStateFlow()

    private val _selectedSubjectId = MutableStateFlow<Long?>(null)
    val selectedSubjectId: StateFlow<Long?> = _selectedSubjectId.asStateFlow()

    private val _currentAttendanceMap = MutableStateFlow<Map<Long, AttendanceRecord>>(emptyMap())
    val currentAttendanceMap: StateFlow<Map<Long, AttendanceRecord>> = _currentAttendanceMap.asStateFlow()

    val todayAttendanceRecords: StateFlow<List<AttendanceRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val calendarPref = repository.getSetting("default_calendar")
            if (calendarPref != null) {
                _isHijriPrimary.value = (calendarPref == "HIJRI")
            }
            val offsetPref = repository.getSetting("hijri_offset")
            if (offsetPref != null) {
                _hijriOffsetDays.value = offsetPref.toIntOrNull() ?: 0
            }
        }
    }

    fun setDate(date: String) {
        _selectedDate.value = date
        loadAttendanceForSelection()
    }

    fun selectLevel(levelId: Long?) {
        _selectedLevelId.value = levelId
        loadAttendanceForSelection()
    }

    fun selectSubject(subjectId: Long?) {
        _selectedSubjectId.value = subjectId
        loadAttendanceForSelection()
    }

    fun setCalendarPrimary(isHijri: Boolean) {
        _isHijriPrimary.value = isHijri
        viewModelScope.launch {
            repository.setSetting("default_calendar", if (isHijri) "HIJRI" else "GREGORIAN")
        }
    }

    fun setHijriOffset(offset: Int) {
        _hijriOffsetDays.value = offset
        viewModelScope.launch {
            repository.setSetting("hijri_offset", offset.toString())
        }
    }

    private fun loadAttendanceForSelection() {
        val date = _selectedDate.value
        val levelId = _selectedLevelId.value ?: return
        val subjectId = _selectedSubjectId.value ?: 0L

        viewModelScope.launch {
            repository.getAttendanceForDateAndLevel(date, levelId, subjectId).collect { list ->
                _currentAttendanceMap.value = list.associateBy { it.studentId }
            }
        }
    }

    fun updateStudentAttendanceStatus(
        studentId: Long,
        levelId: Long,
        subjectId: Long,
        status: AttendanceStatus,
        reason: String = "",
        notes: String = "",
        recorderName: String = ""
    ) {
        val date = _selectedDate.value
        val hDate = HijriCalendarHelper.getHijriDate(date, _hijriOffsetDays.value)
        val existing = _currentAttendanceMap.value[studentId]

        val record = existing?.copy(
            status = status,
            reason = reason,
            notes = notes,
            recordedBy = recorderName,
            timestamp = System.currentTimeMillis()
        ) ?: AttendanceRecord(
            studentId = studentId,
            date = date,
            hijriDate = hDate,
            levelId = levelId,
            subjectId = subjectId,
            status = status,
            reason = reason,
            notes = notes,
            recordedBy = recorderName
        )

        viewModelScope.launch {
            repository.recordAttendance(record)
            _currentAttendanceMap.value = _currentAttendanceMap.value + (studentId to record)
        }
    }

    fun saveAllAttendance(levelId: Long, subjectId: Long, recorderName: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val hDate = HijriCalendarHelper.getHijriDate(date, _hijriOffsetDays.value)

            val students = allStudents.value.filter { it.levelId == levelId }
            val recordsToInsert = students.map { student ->
                _currentAttendanceMap.value[student.id] ?: AttendanceRecord(
                    studentId = student.id,
                    date = date,
                    hijriDate = hDate,
                    levelId = levelId,
                    subjectId = subjectId,
                    status = AttendanceStatus.PRESENT,
                    recordedBy = recorderName
                )
            }
            repository.recordAttendanceBatch(recordsToInsert)
            _toastMessage.value = "تم حفظ سجل الحضور لجميع الطلاب بنجاح"
            loadAttendanceForSelection()
        }
    }

    fun recordEvaluation(
        studentId: Long,
        subjectId: Long,
        teacherName: String,
        tag: EvaluationTagType,
        customTitle: String = "",
        notes: String = "",
        score: Int? = null,
        onSuccess: () -> Unit
    ) {
        val date = _selectedDate.value
        val hDate = HijriCalendarHelper.getHijriDate(date, _hijriOffsetDays.value)

        val eval = EvaluationRecord(
            studentId = studentId,
            date = date,
            hijriDate = hDate,
            subjectId = subjectId,
            teacherName = teacherName,
            tag = tag,
            customTagTitle = customTitle,
            notes = notes,
            score = score
        )

        viewModelScope.launch {
            repository.addEvaluation(eval)
            _toastMessage.value = "تم حفظ تقييم الطالب بنجاح"
            onSuccess()
        }
    }

    fun addStudent(
        studentNumber: String,
        fullName: String,
        sectionId: Long,
        levelId: Long,
        phone: String,
        parentPhone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (studentNumber.isBlank() || fullName.isBlank()) {
            onError("يرجى إدخال رقم الطالب والاسم الرباعي")
            return
        }

        viewModelScope.launch {
            val existing = repository.getStudentByNumber(studentNumber.trim())
            if (existing != null) {
                onError("رقم الطالب ($studentNumber) مسجل مسبقاً لطالب آخر!")
                return@launch
            }

            try {
                repository.insertStudent(
                    Student(
                        studentNumber = studentNumber.trim(),
                        fullName = fullName.trim(),
                        sectionId = sectionId,
                        levelId = levelId,
                        phone = phone.trim(),
                        parentPhone = parentPhone.trim(),
                        parentAccessCode = com.tayseer.app.utils.SecurityHelper.generateAccessCode()
                    )
                )
                _toastMessage.value = "تمت إضافة الطالب بنجاح"
                onSuccess()
            } catch (e: Exception) {
                onError("حدث خطأ أثناء حفظ الطالب: ${e.localizedMessage}")
            }
        }
    }

    fun importStudentsCsv(
        csvText: String,
        sectionId: Long,
        levelId: Long,
        onResult: (Int, List<String>) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.importStudentsFromCsv(csvText, sectionId, levelId)
            _toastMessage.value = "تم استيراد ${result.first} طالب بنجاح"
            onResult(result.first, result.second)
        }
    }

    fun getStudentAttendanceFlow(studentId: Long): Flow<List<AttendanceRecord>> =
        repository.getAttendanceForStudent(studentId)

    fun getStudentEvaluationsFlow(studentId: Long): Flow<List<EvaluationRecord>> =
        repository.getEvaluationsForStudent(studentId)

    fun exportAndShareReport(context: Context, student: Student) {
        viewModelScope.launch {
            val sectionName = sections.value.find { it.id == student.sectionId }?.name ?: "القسم العام"
            val levelName = levels.value.find { it.id == student.levelId }?.name ?: "المستوى"
            val attendance = repository.getAttendanceForStudent(student.id).first()
            val evals = repository.getEvaluationsForStudent(student.id).first()

            val file = PdfReportGenerator.generateAndShareStudentReport(
                context = context,
                student = student,
                sectionName = sectionName,
                levelName = levelName,
                attendanceRecords = attendance,
                evaluations = evals
            )
            if (file != null) {
                PdfReportGenerator.sharePdfFile(context, file)
            } else {
                _toastMessage.value = "فشل إنشاء تقرير PDF"
            }
        }
    }

    fun loadDemoData() {
        viewModelScope.launch {
            try {
                repository.loadDemoData()
                _toastMessage.value = "تم تحميل البيانات التجريبية بنجاح"
            } catch (e: Exception) {
                _toastMessage.value = "فشل تحميل البيانات التجريبية: ${e.localizedMessage}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                repository.clearAllData()
                _toastMessage.value = "تم مسح جميع البيانات بنجاح"
            } catch (e: Exception) {
                _toastMessage.value = "فشل مسح البيانات: ${e.localizedMessage}"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}

class TayseerViewModelFactory(private val repository: TayseerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TayseerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TayseerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
