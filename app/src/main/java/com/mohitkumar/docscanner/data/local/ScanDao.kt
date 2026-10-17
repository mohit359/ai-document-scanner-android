package com.mohitkumar.docscanner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scans ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ScanEntity>>

    @Query("SELECT * FROM scans WHERE id = :id")
    suspend fun getById(id: Long): ScanEntity?

    @Insert
    suspend fun insert(entity: ScanEntity): Long

    @Query("UPDATE scans SET summary = :summary WHERE id = :id")
    suspend fun updateSummary(id: Long, summary: String)

    @Query("DELETE FROM scans WHERE id = :id")
    suspend fun delete(id: Long)
}
