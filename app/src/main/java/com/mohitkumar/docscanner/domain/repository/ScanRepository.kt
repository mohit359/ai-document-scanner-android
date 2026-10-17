package com.mohitkumar.docscanner.domain.repository

import com.mohitkumar.docscanner.domain.model.Scan
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    fun observeScans(): Flow<List<Scan>>
    suspend fun saveScan(text: String): Long
    /** Calls the AI summary service and stores the result. */
    suspend fun summarize(scanId: Long): Result<Unit>
    suspend fun delete(scanId: Long)
}
