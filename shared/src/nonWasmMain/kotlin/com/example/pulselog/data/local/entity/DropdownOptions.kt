package com.example.pulselog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "page_options")
data class PageOption(
    @PrimaryKey(autoGenerate = true)
    override val id: Int = 0,
    override val name: String
) : com.example.pulselog.data.model.PageOption

@Entity(tableName = "reason_options")
data class ReasonOption(
    @PrimaryKey(autoGenerate = true)
    override val id: Int = 0,
    override val name: String
) : com.example.pulselog.data.model.ReasonOption

@Entity(tableName = "designation_options")
data class DesignationOption(
    @PrimaryKey(autoGenerate = true)
    override val id: Int = 0,
    override val name: String
) : com.example.pulselog.data.model.DesignationOption
