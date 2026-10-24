package com.tayseer.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = Section::class,
            parentColumns = ["id"],
            childColumns = ["sectionId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Level::class,
            parentColumns = ["id"],
            childColumns = ["levelId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["studentNumber"], unique = true),
        Index("sectionId"),
        Index("levelId")
    ]
)
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentNumber: String,
    val fullName: String,
    val sectionId: Long,
    val levelId: Long,
    val phone: String = "",
    val parentPhone: String = "",
    val parentAccessCode: String = "",
    val birthDate: String = "",
    val notes: String = "",
    val isActive: Boolean = true
)
