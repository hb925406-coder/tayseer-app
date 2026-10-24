package com.tayseer.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EvaluationTagType(val defaultArabicLabel: String, val isPositive: Boolean) {
    MEMORIZED("حفظه", true),
    NOT_MEMORIZED("لم يحفظ", false),
    DISTINGUISHED("مميز", true),
    WELL_BEHAVED("مؤدب", true),
    FORGOT_BOOKS("نسيان الكتب", false),
    CUSTOM("ملاحظة مخصصة", true)
}

@Entity(
    tableName = "evaluations",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("studentId"),
        Index("date")
    ]
)
data class EvaluationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val date: String,
    val hijriDate: String,
    val subjectId: Long,
    val teacherName: String,
    val tag: EvaluationTagType,
    val customTagTitle: String = "",
    val score: Int? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
