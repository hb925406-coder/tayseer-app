package com.tayseer.app.data.dao

import androidx.room.*
import com.tayseer.app.data.entity.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date AND levelId = :levelId AND subjectId = :subjectId")
    fun getAttendanceForDateAndLevel(date: String, levelId: Long, subjectId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getAttendanceForStudentBetween(studentId: Long, startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId AND date = :date AND subjectId = :subjectId LIMIT 1")
    suspend fun getExistingRecord(studentId: Long, date: String, subjectId: Long): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecord>): List<Long>

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()
}
