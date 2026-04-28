package com.example.pulselog.ui.mistake

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.example.pulselog.data.model.*
import com.example.pulselog.data.repository.MistakeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class MistakeViewModel(private val repository: MistakeRepository) : ScreenModel {

    val allMistakes: StateFlow<List<Mistake>> = repository.getAllMistakes()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pages: StateFlow<List<PageOption>> = repository.getAllPages()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reasons: StateFlow<List<ReasonOption>> = repository.getAllReasons()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val designations: StateFlow<List<DesignationOption>> = repository.getAllDesignations()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveMistake(
        clientName: String,
        amount: Double,
        pageName: String,
        reason: String,
        type: String,
        designation: String,
        notes: String?,
        agentId: Long,
        piId: String? = null,
        isDraft: Boolean = false,
        batchId: String? = null,
        manualShift: String? = null,
        onSuccess: () -> Unit
    ) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val shift = manualShift ?: calculateShift(currentTime)

        screenModelScope.launch {
            repository.insertMistake(
                agentId = agentId,
                clientName = clientName,
                amount = amount,
                pageName = pageName,
                reason = reason,
                designation = designation,
                shift = shift,
                date = currentTime,
                notes = notes,
                isDraft = isDraft,
                batchId = batchId,
                piId = piId,
                type = type
            )
            onSuccess()
        }
    }

    suspend fun getLastNonDraftMistake(): Mistake? {
        return repository.getLastNonDraftMistake()
    }

    fun addPage(name: String) {
        screenModelScope.launch {
            repository.insertPage(name)
        }
    }

    fun addReason(name: String) {
        screenModelScope.launch {
            repository.insertReason(name)
        }
    }

    fun addDesignation(name: String) {
        screenModelScope.launch {
            repository.insertDesignation(name)
        }
    }

    fun saveUser(username: String, role: String, pin: String, pass: String) {
        screenModelScope.launch {
            repository.insertUser(username, role, pin, pass)
        }
    }

    fun updateUser(id: Long, username: String, role: String, pin: String) {
        screenModelScope.launch {
            repository.updateUser(id, username, role, pin)
        }
    }

    fun deleteUser(user: User) {
        screenModelScope.launch {
            repository.deleteUser(user)
        }
    }

    fun deleteMistake(mistake: Mistake, onSuccess: () -> Unit = {}) {
        screenModelScope.launch {
            repository.deleteMistake(mistake)
            onSuccess()
        }
    }

    fun updateMistake(
        id: Long,
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
        onSuccess: () -> Unit
    ) {
        screenModelScope.launch {
            repository.insertMistake(
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
            onSuccess()
        }
    }

    fun calculateShift(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = localDateTime.hour
        return when (hour) {
            in 0..7 -> "Night"
            in 8..15 -> "Morning"
            else -> "Evening"
        }
    }
}
