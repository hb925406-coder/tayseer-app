package com.tayseer.app.data.dao

import androidx.room.*
import com.tayseer.app.data.entity.EvaluationRecord
import com.tayseer.app.data.entity.EvaluationTag
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationDao {
    @Query("SELECT * FROM evaluations WHERE studentId = :studentId ORDER BY date DESC, timestamp DESC")
    fun getEvaluationsForStudent(studentId: Long): Flow<List<EvaluationRecord>>

    @Query("SELECT * FROM evaluations WHERE studentId = :studentId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, timestamp DESC")
    fun getEvaluationsForStudentBetween(studentId: Long, startDate: String, endDate: String): Flow<List<EvaluationRecord>>

    @Query("SELECT * FROM evaluations ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvaluations(limit: Int = 10): Flow<List<EvaluationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: EvaluationRecord): Long

    @Delete
    suspend fun deleteEvaluation(evaluation: EvaluationRecord)

    @Query("SELECT * FROM evaluation_tags ORDER BY name ASC")
    fun getAllEvaluationTags(): Flow<List<EvaluationTag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: EvaluationTag): Long

    @Delete
    suspend fun deleteTag(tag: EvaluationTag)

    @Query("DELETE FROM evaluations")
    suspend fun deleteAll()
}
