package com.example.pulselog.data.local.dao

import androidx.room.*
import com.example.pulselog.data.local.entity.Mistake
import kotlinx.coroutines.flow.Flow

@Dao
interface MistakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMistake(mistake: Mistake): Long

    @Update
    suspend fun updateMistake(mistake: Mistake): Int

    @Delete
    suspend fun deleteMistake(mistake: Mistake): Int

    @Query("SELECT * FROM mistakes ORDER BY date DESC")
    fun getAllMistakes(): Flow<List<Mistake>>

    @Query("SELECT * FROM mistakes WHERE agentId = :agentId ORDER BY date DESC")
    fun getMistakesByAgent(agentId: Long): Flow<List<Mistake>>

    @Query("SELECT * FROM mistakes WHERE isDraft = 0 ORDER BY date DESC LIMIT 1")
    suspend fun getLastNonDraftMistake(): Mistake?
}
