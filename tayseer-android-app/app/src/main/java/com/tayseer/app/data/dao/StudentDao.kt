package com.tayseer.app.data.dao

import androidx.room.*
import com.tayseer.app.data.entity.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE isActive = 1 ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE levelId = :levelId AND isActive = 1 ORDER BY fullName ASC")
    fun getStudentsByLevel(levelId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE sectionId = :sectionId AND isActive = 1 ORDER BY fullName ASC")
    fun getStudentsBySection(sectionId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE studentNumber = :number LIMIT 1")
    suspend fun getStudentByNumber(number: String): Student?

    @Query("SELECT * FROM students WHERE parentAccessCode = :code LIMIT 1")
    suspend fun getStudentByAccessCode(code: String): Student?

    @Query("SELECT COUNT(*) FROM students WHERE isActive = 1")
    fun countTotalStudents(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>): List<Long>

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)
}
