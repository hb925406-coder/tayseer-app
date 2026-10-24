package com.tayseer.app.data

import androidx.room.TypeConverter
import com.tayseer.app.data.entity.AttendanceStatus
import com.tayseer.app.data.entity.EvaluationTagType
import com.tayseer.app.data.entity.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.TEACHER
    }

    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = try {
        AttendanceStatus.valueOf(value)
    } catch (e: Exception) {
        AttendanceStatus.PRESENT
    }

    @TypeConverter
    fun fromEvaluationTagType(value: EvaluationTagType): String = value.name

    @TypeConverter
    fun toEvaluationTagType(value: String): EvaluationTagType = try {
        EvaluationTagType.valueOf(value)
    } catch (e: Exception) {
        EvaluationTagType.CUSTOM
    }
}
