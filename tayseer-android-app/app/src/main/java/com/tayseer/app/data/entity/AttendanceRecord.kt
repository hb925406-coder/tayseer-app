package com.tayseer.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AttendanceStatus(val arabicLabel: String) {
    PRESENT("حاضر"),
    ABSENT("غائب"),
    PERMITTED("مستأذن"),
    LATE("متأخر"),
    EXCUSED_ABSENCE("غائب بعذر"),
    TRUANT("هارب")
}

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "date", "subjectId"], unique = true),
        Index("studentId"),
        Index("date"),
        Index("levelId")
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String,
    val hijriDate: String,
    val levelId: Long,
    val subjectId: Long,
    val status: AttendanceStatus,
    val reason: String = "",
    val notes: String = "",
    val recordedBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
