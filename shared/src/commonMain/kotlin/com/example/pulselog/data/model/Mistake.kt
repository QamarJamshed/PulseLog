package com.example.pulselog.data.model

interface Mistake {
    val id: Long
    val agentId: Long
    val clientName: String
    val amount: Double
    val pageName: String
    val reason: String
    val designation: String
    val shift: String
    val date: Long
    val notes: String?
    val isDraft: Boolean
    val batchId: String?
    val piId: String?
    val type: String
}
