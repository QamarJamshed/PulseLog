package com.example.pulselog.data.repository

import com.example.pulselog.data.local.dao.DropdownDao
import com.example.pulselog.data.local.dao.MistakeDao
import com.example.pulselog.data.local.dao.UserDao
import com.example.pulselog.data.local.entity.*
import com.example.pulselog.data.model.User as UserInterface
import com.example.pulselog.data.model.Mistake as MistakeInterface
import com.example.pulselog.data.model.PageOption as PageOptionInterface
import com.example.pulselog.data.model.ReasonOption as ReasonOptionInterface
import com.example.pulselog.data.model.DesignationOption as DesignationOptionInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull

class MistakeRepositoryImpl(
    private val userDao: UserDao,
    private val mistakeDao: MistakeDao,
    private val dropdownDao: DropdownDao
) : MistakeRepository {
    // User Methods
    override suspend fun insertUser(
        username: String,
        role: String,
        pin: String,
        password: String
    ) = userDao.insertUser(User(username = username, role = role, pin = pin, password = password))

    override suspend fun updateUser(
        id: Long,
        username: String,
        role: String,
        pin: String
    ) {
        val user = userDao.getAllUsers().map { users -> users.find { it.id == id } }.firstOrNull()
        if (user != null) {
            userDao.updateUser(User(
                id = user.id,
                username = username,
                role = role,
                pin = pin,
                password = user.password
            ))
        }
    }
    override suspend fun deleteUser(user: UserInterface) = userDao.deleteUser(user as User)
    override suspend fun getUserByUsername(username: String): UserInterface? = userDao.getUserByUsername(username)
    override fun getAllUsers(): Flow<List<UserInterface>> = userDao.getAllUsers().map { it }

    // Mistake Methods
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
    ) = mistakeDao.insertMistake(
        Mistake(
            id = id,
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
    )

    override suspend fun updateMistake(mistake: MistakeInterface) = mistakeDao.updateMistake(mistake as Mistake)
    override suspend fun deleteMistake(mistake: MistakeInterface) = mistakeDao.deleteMistake(mistake as Mistake)
    override fun getAllMistakes(): Flow<List<MistakeInterface>> = mistakeDao.getAllMistakes().map { it }
    override fun getMistakesByAgent(agentId: Long): Flow<List<MistakeInterface>> = mistakeDao.getMistakesByAgent(agentId).map { it }
    override suspend fun getLastNonDraftMistake(): MistakeInterface? = mistakeDao.getLastNonDraftMistake()

    // Dropdown Methods
    override fun getAllPages(): Flow<List<PageOptionInterface>> = dropdownDao.getAllPages().map { it }
    override suspend fun insertPage(name: String) = dropdownDao.insertPage(PageOption(name = name))
    override fun getAllReasons(): Flow<List<ReasonOptionInterface>> = dropdownDao.getAllReasons().map { it }
    override suspend fun insertReason(name: String) = dropdownDao.insertReason(ReasonOption(name = name))
    override fun getAllDesignations(): Flow<List<DesignationOptionInterface>> = dropdownDao.getAllDesignations().map { it }
    override suspend fun insertDesignation(name: String) = dropdownDao.insertDesignation(DesignationOption(name = name))
}
