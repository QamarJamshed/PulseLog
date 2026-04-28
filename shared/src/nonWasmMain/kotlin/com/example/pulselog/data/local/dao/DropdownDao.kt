package com.example.pulselog.data.local.dao

import androidx.room.*
import com.example.pulselog.data.local.entity.DesignationOption
import com.example.pulselog.data.local.entity.PageOption
import com.example.pulselog.data.local.entity.ReasonOption
import kotlinx.coroutines.flow.Flow

@Dao
interface DropdownDao {
    @Query("SELECT * FROM page_options")
    fun getAllPages(): Flow<List<PageOption>>

    @Insert
    suspend fun insertPage(page: PageOption): Long

    @Query("SELECT * FROM reason_options")
    fun getAllReasons(): Flow<List<ReasonOption>>

    @Insert
    suspend fun insertReason(reason: ReasonOption): Long

    @Query("SELECT * FROM designation_options")
    fun getAllDesignations(): Flow<List<DesignationOption>>

    @Insert
    suspend fun insertDesignation(designation: DesignationOption): Long
}
