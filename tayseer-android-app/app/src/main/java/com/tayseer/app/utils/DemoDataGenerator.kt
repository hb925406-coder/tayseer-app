package com.tayseer.app.utils

import com.tayseer.app.data.AppDatabase
import com.tayseer.app.data.entity.*

object DemoDataGenerator {

    suspend fun populateDemoData(database: AppDatabase) {
        val academicDao = database.academicDao()
        val studentDao = database.studentDao()
        val attendanceDao = database.attendanceDao()
        val evaluationDao = database.evaluationDao()
        val userDao = database.userDao()

        val sec1Id = academicDao.insertSection(Section(name = "حلقات القرآن الكريم", description = "تحفيظ القرآن الكريم والعلوم الشرعية"))
        val sec2Id = academicDao.insertSection(Section(name = "التعليم الصباحي", description = "المرحلة الدراسية"))

        val lvl1Id = academicDao.insertLevel(Level(sectionId = sec1Id, name = "حلقة الإمام عاصم (متقدم)"))
        val lvl2Id = academicDao.insertLevel(Level(sectionId = sec1Id, name = "حلقة الإمام نافع (متوسط)"))
        val lvl3Id = academicDao.insertLevel(Level(sectionId = sec2Id, name = "الصف الأول"))

        val sub1Id = academicDao.insertSubject(Subject(levelId = lvl1Id, name = "حفظ القرآن الكريم"))
        val sub2Id = academicDao.insertSubject(Subject(levelId = lvl1Id, name = "التجويد والترتيل"))
        val sub3Id = academicDao.insertSubject(Subject(levelId = lvl2Id, name = "التحفيظ والمراجعة"))

        val teacherSalt = SecurityHelper.generateSalt()
        userDao.insertUser(
            User(
                username = "teacher1",
                passwordHash = SecurityHelper.hashPassword("123456", teacherSalt),
                salt = teacherSalt,
                fullName = "الأستاذ محمد باحميد",
                role = UserRole.TEACHER,
                phone = "770000001",
                assignedSubjectId = sub1Id
            )
        )

        val sampleStudents = listOf(
            Student(studentNumber = "1001", fullName = "أحمد علي بافضل", sectionId = sec1Id, levelId = lvl1Id, parentPhone = "771111111", parentAccessCode = "100123"),
            Student(studentNumber = "1002", fullName = "عمر خالد بن طاهر", sectionId = sec1Id, levelId = lvl1Id, parentPhone = "772222222", parentAccessCode = "100234"),
            Student(studentNumber = "1003", fullName = "سالم محمد العطاس", sectionId = sec1Id, levelId = lvl1Id, parentPhone = "773333333", parentAccessCode = "100345"),
            Student(studentNumber = "1004", fullName = "بلال سعيد الكاف", sectionId = sec1Id, levelId = lvl1Id, parentPhone = "774444444", parentAccessCode = "100456"),
            Student(studentNumber = "1005", fullName = "ياسين عبدالله باوزير", sectionId = sec1Id, levelId = lvl2Id, parentPhone = "775555555", parentAccessCode = "100567"),
            Student(studentNumber = "1006", fullName = "عبدالرحمن صالح الخطيب", sectionId = sec1Id, levelId = lvl2Id, parentPhone = "776666666", parentAccessCode = "100678")
        )
        val studentIds = sampleStudents.map { studentDao.insertStudent(it) }

        val today = HijriCalendarHelper.getTodayIsoDate()
        val hDate = HijriCalendarHelper.getHijriDate(today)

        if (studentIds.size >= 4) {
            attendanceDao.insertRecords(listOf(
                AttendanceRecord(studentId = studentIds[0], date = today, hijriDate = hDate, levelId = lvl1Id, subjectId = sub1Id, status = AttendanceStatus.PRESENT, recordedBy = "أ. محمد"),
                AttendanceRecord(studentId = studentIds[1], date = today, hijriDate = hDate, levelId = lvl1Id, subjectId = sub1Id, status = AttendanceStatus.PRESENT, recordedBy = "أ. محمد"),
                AttendanceRecord(studentId = studentIds[2], date = today, hijriDate = hDate, levelId = lvl1Id, subjectId = sub1Id, status = AttendanceStatus.LATE, reason = "تأخر في المواصلات", recordedBy = "أ. محمد"),
                AttendanceRecord(studentId = studentIds[3], date = today, hijriDate = hDate, levelId = lvl1Id, subjectId = sub1Id, status = AttendanceStatus.EXCUSED_ABSENCE, reason = "ظرف صحي", recordedBy = "أ. محمد")
            ))
        }

        if (studentIds.isNotEmpty()) {
            evaluationDao.insertEvaluation(
                EvaluationRecord(
                    studentId = studentIds[0],
                    date = today,
                    hijriDate = hDate,
                    subjectId = sub1Id,
                    teacherName = "أ. محمد باحميد",
                    tag = EvaluationTagType.DISTINGUISHED,
                    notes = "تلاوة متقنة وأداء ممتاز في سورة البقرة"
                )
            )
            evaluationDao.insertEvaluation(
                EvaluationRecord(
                    studentId = studentIds[1],
                    date = today,
                    hijriDate = hDate,
                    subjectId = sub1Id,
                    teacherName = "أ. محمد باحميد",
                    tag = EvaluationTagType.MEMORIZED,
                    notes = "حفظ ربع الحزب بإتقان"
                )
            )
            evaluationDao.insertEvaluation(
                EvaluationRecord(
                    studentId = studentIds[2],
                    date = today,
                    hijriDate = hDate,
                    subjectId = sub1Id,
                    teacherName = "أ. محمد باحميد",
                    tag = EvaluationTagType.WELL_BEHAVED,
                    notes = "خلق رفيع ومواظب على الأدب"
                )
            )
        }
    }

    suspend fun clearAllData(database: AppDatabase) {
        database.clearAllTables()
    }
}
