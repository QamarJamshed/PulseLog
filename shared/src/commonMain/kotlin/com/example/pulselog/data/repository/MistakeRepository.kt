package com.example.pulselog.data.repository

import com.example.pulselog.data.model.*
import kotlinx.coroutines.flow.Flow

interface MistakeRepository {
    // User Methods
    suspend fun getUserByUsername(username: String): User?
    suspend fun insertUser(
        username: String,
        role: String,
        pin: String,
        password: String
    ): Any?
    suspend fun updateUser(
        id: Long,
        username: String,
        role: String,
        pin: String
    ): Any?
    suspend fun deleteUser(user: User): Any?
    fun getAllUsers(): Flow<List<User>>

    // Mistake Methods
    suspend fun insertMistake(
        agentId: Long,
        clientName: String,
        amount: Double,
        pageName: String,
        reason: String,
        designation: String,
        shift: String,
        date: Long,
        notes: String?,
        isDraft: Boolean,
        batchId: String?,
        piId: String?,
        type: String,
        id: Long = 0
    ): Any?
    suspend fun updateMistake(mistake: Mistake): Any?
    suspend fun deleteMistake(mistake: Mistake): Any?
    fun getAllMistakes(): Flow<List<Mistake>>
    fun getMistakesByAgent(agentId: Long): Flow<List<Mistake>>
    suspend fun getLastNonDraftMistake(): Mistake?

    // Dropdown Methods
    fun getAllPages(): Flow<List<PageOption>>
    suspend fun insertPage(name: String): Any?
    fun getAllReasons(): Flow<List<ReasonOption>>
    suspend fun insertReason(name: String): Any?
    fun getAllDesignations(): Flow<List<DesignationOption>>
    suspend fun insertDesignation(name: String): Any?
}
