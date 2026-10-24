package com.tayseer.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evaluation_tags")
data class EvaluationTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isPositive: Boolean = true,
    val iconName: String = "Star"
)
