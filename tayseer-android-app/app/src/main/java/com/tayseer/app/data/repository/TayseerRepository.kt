package com.tayseer.app.data.repository

import com.tayseer.app.data.AppDatabase
import com.tayseer.app.data.entity.*
import com.tayseer.app.utils.DemoDataGenerator
import com.tayseer.app.utils.SecurityHelper
import kotlinx.coroutines.flow.Flow

class TayseerRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val academicDao = database.academicDao()
    private val studentDao = database.studentDao()
    private val attendanceDao = database.attendanceDao()
    private val evaluationDao = database.evaluationDao()
    private val settingsDao = database.settingsDao()

    suspend fun getUserCount(): Int = userDao.getUserCount()

    suspend fun createInitialAdmin(password: String): Long {
        val salt = SecurityHelper.generateSalt()
        val hash = SecurityHelper.hashPassword(password, salt)
        return userDao.insertUser(
            User(
                username = "admin",
                passwordHash = hash,
                salt = salt,
                fullName = "مدير النظام",
                role = UserRole.ADMIN
            )
        )
    }

    suspend fun login(username: String, password: String): User? {
        val user = userDao.getUserByUsername(username.trim()) ?: return null
        val isValid = SecurityHelper.verifyPassword(password, user.salt, user.passwordHash)
        return if (isValid) user else null
    }

    suspend fun loginParentByCode(accessCode: String): Student? {
        return studentDao.getStudentByAccessCode(accessCode.trim())
    }

    fun getAllTeachers(): Flow<List<User>> = userDao.getUsersByRole(UserRole.TEACHER)

    suspend fun addTeacher(username: String, password: String, fullName: String, phone: String, subjectId: Long?): Long {
        val salt = SecurityHelper.generateSalt()
        val hash = SecurityHelper.hashPassword(password, salt)
        return userDao.insertUser(
            User(
                username = username.trim(),
                passwordHash = hash,
                salt = salt,
                fullName = fullName.trim(),
                role = UserRole.TEACHER,
                phone = phone.trim(),
                assignedSubjectId = subjectId
            )
        )
    }

    suspend fun changePassword(user: User, newPassword: String) {
        val newSalt = SecurityHelper.generateSalt()
        val newHash = SecurityHelper.hashPassword(newPassword, newSalt)
        userDao.updateUser(user.copy(passwordHash = newHash, salt = newSalt))
    }

    fun getAllSections(): Flow<List<Section>> = academicDao.getAllSections()
    suspend fun insertSection(section: Section): Long = academicDao.insertSection(section)
    suspend fun deleteSection(section: Section) = academicDao.deleteSection(section)

    fun getAllLevels(): Flow<List<Level>> = academicDao.getAllLevels()
    fun getLevelsBySection(sectionId: Long): Flow<List<Level>> = academicDao.getLevelsBySection(sectionId)
    suspend fun insertLevel(level: Level): Long = academicDao.insertLevel(level)
    suspend fun deleteLevel(level: Level) = academicDao.deleteLevel(level)

    fun getAllSubjects(): Flow<List<Subject>> = academicDao.getAllSubjects()
    fun getSubjectsByLevel(levelId: Long): Flow<List<Subject>> = academicDao.getSubjectsByLevel(levelId)
    suspend fun insertSubject(subject: Subject): Long = academicDao.insertSubject(subject)
    suspend fun deleteSubject(subject: Subject) = academicDao.deleteSubject(subject)

    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()
    fun getStudentsByLevel(levelId: Long): Flow<List<Student>> = studentDao.getStudentsByLevel(levelId)
    fun getStudentsBySection(sectionId: Long): Flow<List<Student>> = studentDao.getStudentsBySection(sectionId)
    fun countTotalStudents(): Flow<Int> = studentDao.countTotalStudents()
    suspend fun getStudentById(id: Long): Student? = studentDao.getStudentById(id)
    suspend fun getStudentByNumber(number: String): Student? = studentDao.getStudentByNumber(number)
    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)
    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    suspend fun importStudentsFromCsv(
        csvText: String,
        sectionId: Long,
        levelId: Long
    ): Pair<Int, List<String>> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        var successCount = 0
        val errors = mutableListOf<String>()

        for ((index, line) in lines.withIndex()) {
            val parts = line.split(",").map { it.trim() }
            if (parts.size < 2) {
                errors.add("سطر ${index + 1}: صيغة غير مكتملة")
                continue
            }
            val num = parts[0]
            val name = parts[1]
            val phone = if (parts.size > 2) parts[2] else ""
            val parentPhone = if (parts.size > 3) parts[3] else ""

            if (studentDao.getStudentByNumber(num) != null) {
                errors.add("سطر ${index + 1}: رقم الطالب ($num) مكرر مسجل بالفعل")
                continue
            }

            try {
                studentDao.insertStudent(
                    Student(
                        studentNumber = num,
                        fullName = name,
                        sectionId = sectionId,
                        levelId = levelId,
                        phone = phone,
                        parentPhone = parentPhone,
                        parentAccessCode = SecurityHelper.generateAccessCode()
                    )
                )
                successCount++
            } catch (e: Exception) {
                errors.add("سطر ${index + 1}: خطأ أثناء الحفظ (${e.localizedMessage})")
            }
        }
        return Pair(successCount, errors)
    }

    fun getAttendanceForDateAndLevel(date: String, levelId: Long, subjectId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDateAndLevel(date, levelId, subjectId)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDate(date)

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForStudent(studentId)

    fun getAttendanceForStudentBetween(studentId: Long, start: String, end: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForStudentBetween(studentId, start, end)

    suspend fun recordAttendance(record: AttendanceRecord): Long =
        attendanceDao.insertOrUpdateRecord(record)

    suspend fun recordAttendanceBatch(records: List<AttendanceRecord>): List<Long> =
        attendanceDao.insertRecords(records)

    fun getEvaluationsForStudent(studentId: Long): Flow<List<EvaluationRecord>> =
        evaluationDao.getEvaluationsForStudent(studentId)

    fun getEvaluationsForStudentBetween(studentId: Long, start: String, end: String): Flow<List<EvaluationRecord>> =
        evaluationDao.getEvaluationsForStudentBetween(studentId, start, end)

    fun getRecentEvaluations(limit: Int = 10): Flow<List<EvaluationRecord>> =
        evaluationDao.getRecentEvaluations(limit)

    suspend fun addEvaluation(evaluation: EvaluationRecord): Long =
        evaluationDao.insertEvaluation(evaluation)

    suspend fun deleteEvaluation(evaluation: EvaluationRecord) =
        evaluationDao.deleteEvaluation(evaluation)

    fun getAllCustomTags(): Flow<List<EvaluationTag>> = evaluationDao.getAllEvaluationTags()
    suspend fun addCustomTag(tag: EvaluationTag): Long = evaluationDao.insertTag(tag)
    suspend fun deleteCustomTag(tag: EvaluationTag) = evaluationDao.deleteTag(tag)

    suspend fun getSetting(key: String): String? = settingsDao.getSettingValue(key)
    suspend fun setSetting(key: String, value: String) = settingsDao.setSetting(AppSetting(key, value))

    suspend fun loadDemoData() {
        DemoDataGenerator.populateDemoData(database)
    }

    suspend fun clearAllData() {
        DemoDataGenerator.clearAllData(database)
    }
}
