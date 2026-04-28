package com.example.pulselog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    override val username: String,
    override val password: String,
    override val pin: String,
    override val role: String
) : com.example.pulselog.data.model.User
