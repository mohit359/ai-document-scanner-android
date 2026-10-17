package com.mohitkumar.docscanner.domain.usecase

import com.mohitkumar.docscanner.domain.model.Scan
import com.mohitkumar.docscanner.domain.repository.ScanRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveScansUseCase @Inject constructor(private val repository: ScanRepository) {
    operator fun invoke(): Flow<List<Scan>> = repository.observeScans()
}

class SaveScanUseCase @Inject constructor(private val repository: ScanRepository) {
    /** Returns null when the recognised text is blank (nothing worth saving). */
    suspend operator fun invoke(rawText: String): Long? {
        val cleaned = rawText.trim()
        return if (cleaned.isEmpty()) null else repository.saveScan(cleaned)
    }
}

class SummarizeScanUseCase @Inject constructor(private val repository: ScanRepository) {
    suspend operator fun invoke(scanId: Long): Result<Unit> = repository.summarize(scanId)
}

class DeleteScanUseCase @Inject constructor(private val repository: ScanRepository) {
    suspend operator fun invoke(scanId: Long) = repository.delete(scanId)
}
