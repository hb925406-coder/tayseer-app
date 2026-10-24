package com.tayseer.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,    // الإدارة
    TEACHER,  // المعلم
    PARENT    // ولي الأمر
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val fullName: String,
    val role: UserRole,
    val phone: String? = null,
    val assignedSubjectId: Long? = null,
    val childStudentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
