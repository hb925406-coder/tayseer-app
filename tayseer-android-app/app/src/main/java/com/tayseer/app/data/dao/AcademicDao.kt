package com.tayseer.app.data.dao

import androidx.room.*
import com.tayseer.app.data.entity.Level
import com.tayseer.app.data.entity.Section
import com.tayseer.app.data.entity.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicDao {
    @Query("SELECT * FROM sections ORDER BY name ASC")
    fun getAllSections(): Flow<List<Section>>

    @Query("SELECT * FROM sections WHERE id = :id LIMIT 1")
    suspend fun getSectionById(id: Long): Section?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: Section): Long

    @Update
    suspend fun updateSection(section: Section)

    @Delete
    suspend fun deleteSection(section: Section)

    @Query("SELECT * FROM levels ORDER BY name ASC")
    fun getAllLevels(): Flow<List<Level>>

    @Query("SELECT * FROM levels WHERE sectionId = :sectionId ORDER BY name ASC")
    fun getLevelsBySection(sectionId: Long): Flow<List<Level>>

    @Query("SELECT * FROM levels WHERE id = :id LIMIT 1")
    suspend fun getLevelById(id: Long): Level?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: Level): Long

    @Update
    suspend fun updateLevel(level: Level)

    @Delete
    suspend fun deleteLevel(level: Level)

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE levelId = :levelId ORDER BY name ASC")
    fun getSubjectsByLevel(levelId: Long): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE teacherId = :teacherId ORDER BY name ASC")
    fun getSubjectsByTeacher(teacherId: Long): Flow<List<Subject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)
}
