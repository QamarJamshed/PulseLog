package com.example.pulselog.data.repository

import com.example.pulselog.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MistakeRepositoryWasm : MistakeRepository {
    private val users = mutableListOf<UserImpl>()
    private val mistakes = mutableListOf<MistakeImpl>()
    private val pages = mutableListOf<PageOptionImpl>()
    private val reasons = mutableListOf<ReasonOptionImpl>()
    private val designations = mutableListOf<DesignationOptionImpl>()

    init {
        // Add default admin for testing
        users.add(UserImpl(id = 1, username = "admin", role = "Admin", pin = "12345", password = "password123"))
    }

    override suspend fun getUserByUsername(username: String): User? = users.find { it.username == username }
    
    override suspend fun insertUser(
        username: String,
        role: String,
        pin: String,
        password: String
    ): Any? {
        val newUser = UserImpl(id = users.size.toLong() + 1, username = username, role = role, pin = pin, password = password)
        users.add(newUser)
        return newUser
    }

    override suspend fun updateUser(
        id: Long,
        username: String,
        role: String,
        pin: String
    ): Any? {
        val index = users.indexOfFirst { it.id == id }
        if (index != -1) {
            val oldUser = users[index]
            users[index] = UserImpl(id = id, username = username, role = role, pin = pin, password = oldUser.password)
        }
        return Unit
    }
    
    override suspend fun deleteUser(user: User): Any? {
        users.removeAll { it.id == user.id }
        return Unit
    }
    
    override fun getAllUsers(): Flow<List<User>> = flowOf(users)

    override suspend fun insertMistake(
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
        id: Long
    ): Any? {
        val newId = if (id == 0L) mistakes.size.toLong() + 1 else id
        val newMistake = MistakeImpl(
            id = newId,
            agentId = agentId,
            clientName = clientName,
            amount = amount,
            pageName = pageName,
            reason = reason,
            designation = designation,
            shift = shift,
            date = date,
            notes = notes,
            isDraft = isDraft,
            batchId = batchId,
            piId = piId,
            type = type
        )
        
        val index = mistakes.indexOfFirst { it.id == newId }
        if (index != -1) {
            mistakes[index] = newMistake
        } else {
            mistakes.add(newMistake)
        }
        return newMistake
    }

    override suspend fun updateMistake(mistake: Mistake): Any? {
        val index = mistakes.indexOfFirst { it.id == mistake.id }
        if (index != -1) {
            mistakes[index] = MistakeImpl(
                id = mistake.id,
                agentId = mistake.agentId,
                clientName = mistake.clientName,
                amount = mistake.amount,
                pageName = mistake.pageName,
                reason = mistake.reason,
                designation = mistake.designation,
                shift = mistake.shift,
                date = mistake.date,
                notes = mistake.notes,
                isDraft = mistake.isDraft,
                batchId = mistake.batchId,
                piId = mistake.piId,
                type = mistake.type
            )
        }
        return Unit
    }
    
    override suspend fun deleteMistake(mistake: Mistake): Any? {
        mistakes.removeAll { it.id == mistake.id }
        return Unit
    }
    
    override fun getAllMistakes(): Flow<List<Mistake>> = flowOf(mistakes)
    override fun getMistakesByAgent(agentId: Long): Flow<List<Mistake>> = flowOf(mistakes.filter { it.agentId == agentId })
    override suspend fun getLastNonDraftMistake(): Mistake? = mistakes.lastOrNull { !it.isDraft }

    override fun getAllPages(): Flow<List<PageOption>> = flowOf(pages)
    override suspend fun insertPage(name: String): Any? {
        val newPage = PageOptionImpl(id = pages.size + 1, name = name)
        pages.add(newPage)
        return newPage
    }
    
    override fun getAllReasons(): Flow<List<ReasonOption>> = flowOf(reasons)
    override suspend fun insertReason(name: String): Any? {
        val newReason = ReasonOptionImpl(id = reasons.size + 1, name = name)
        reasons.add(newReason)
        return newReason
    }
    
    override fun getAllDesignations(): Flow<List<DesignationOption>> = flowOf(designations)
    override suspend fun insertDesignation(name: String): Any? {
        val newDesignation = DesignationOptionImpl(id = designations.size + 1, name = name)
        designations.add(newDesignation)
        return newDesignation
    }
}

private data class UserImpl(
    override val id: Long,
    override val username: String,
    override val password: String,
    override val pin: String,
    override val role: String
) : User

private data class MistakeImpl(
    override val id: Long,
    override val agentId: Long,
    override val clientName: String,
    override val amount: Double,
    override val pageName: String,
    override val reason: String,
    override val designation: String,
    override val shift: String,
    override val date: Long,
    override val notes: String?,
    override val isDraft: Boolean,
    override val batchId: String?,
    override val piId: String?,
    override val type: String
) : Mistake

private data class PageOptionImpl(
    override val id: Int,
    override val name: String
) : PageOption

private data class ReasonOptionImpl(
    override val id: Int,
    override val name: String
) : ReasonOption

private data class DesignationOptionImpl(
    override val id: Int,
    override val name: String
) : DesignationOption
