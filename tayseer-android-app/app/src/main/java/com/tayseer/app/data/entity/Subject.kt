package com.tayseer.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = Level::class,
            parentColumns = ["id"],
            childColumns = ["levelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("levelId")]
)
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val levelId: Long,
    val name: String,
    val teacherId: Long? = null
)
