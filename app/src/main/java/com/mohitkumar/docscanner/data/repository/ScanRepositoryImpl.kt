package com.mohitkumar.docscanner.data.repository

import com.mohitkumar.docscanner.data.local.ScanDao
import com.mohitkumar.docscanner.data.local.ScanEntity
import com.mohitkumar.docscanner.data.local.toDomain
import com.mohitkumar.docscanner.data.remote.SummaryApi
import com.mohitkumar.docscanner.data.remote.SummaryRequest
import com.mohitkumar.docscanner.domain.model.Scan
import com.mohitkumar.docscanner.domain.repository.ScanRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ScanRepositoryImpl @Inject constructor(
    private val dao: ScanDao,
    private val api: SummaryApi,
) : ScanRepository {

    override fun observeScans(): Flow<List<Scan>> =
        dao.observeAll().map { list -> list.map(ScanEntity::toDomain) }

    override suspend fun saveScan(text: String): Long =
        dao.insert(ScanEntity(text = text, createdAt = System.currentTimeMillis()))

    override suspend fun summarize(scanId: Long): Result<Unit> {
        val scan = dao.getById(scanId) ?: return Result.failure(IllegalArgumentException("Scan not found"))
        return try {
            val response = api.summarize(SummaryRequest(scan.text.take(MAX_CHARS)))
            dao.updateSummary(scanId, response.summary)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(scanId: Long) = dao.delete(scanId)

    private companion object {
        const val MAX_CHARS = 8_000
    }
}
