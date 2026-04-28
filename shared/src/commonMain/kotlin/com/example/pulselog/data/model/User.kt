package com.example.pulselog.data.model

interface User {
    val id: Long
    val username: String
    val password: String
    val pin: String
    val role: String
}
