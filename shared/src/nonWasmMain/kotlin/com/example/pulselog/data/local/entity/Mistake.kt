package com.example.pulselog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mistakes")
data class Mistake(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    override val agentId: Long,
    override val clientName: String,
    override val amount: Double,
    override val pageName: String,
    override val reason: String,
    override val designation: String,
    override val shift: String,
    override val date: Long,
    override val notes: String? = null,
    override val isDraft: Boolean = false,
    override val batchId: String? = null,
    override val piId: String? = null,
    override val type: String = "Deposit" // Deposit or Redeem
) : com.example.pulselog.data.model.Mistake
