package com.mohitkumar.docscanner.presentation.scans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohitkumar.docscanner.domain.model.Scan
import com.mohitkumar.docscanner.domain.usecase.DeleteScanUseCase
import com.mohitkumar.docscanner.domain.usecase.ObserveScansUseCase
import com.mohitkumar.docscanner.domain.usecase.SaveScanUseCase
import com.mohitkumar.docscanner.domain.usecase.SummarizeScanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScansUiState(
    val scans: List<Scan> = emptyList(),
    val summarizingIds: Set<Long> = emptySet(),
    val message: String? = null,
)

@HiltViewModel
class ScansViewModel @Inject constructor(
    observeScans: ObserveScansUseCase,
    private val saveScan: SaveScanUseCase,
    private val summarizeScan: SummarizeScanUseCase,
    private val deleteScan: DeleteScanUseCase,
) : ViewModel() {

    private data class Transient(val summarizingIds: Set<Long> = emptySet(), val message: String? = null)

    private val transient = MutableStateFlow(Transient())

    val uiState: StateFlow<ScansUiState> =
        combine(observeScans(), transient) { scans, t ->
            ScansUiState(scans, t.summarizingIds, t.message)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScansUiState())

    fun onTextRecognized(text: String) {
        viewModelScope.launch {
            val id = saveScan(text)
            if (id == null) transient.update { it.copy(message = "No text found. Try again with better light.") }
        }
    }

    fun summarize(scanId: Long) {
        viewModelScope.launch {
            transient.update { it.copy(summarizingIds = it.summarizingIds + scanId) }
            val result = summarizeScan(scanId)
            transient.update {
                it.copy(
                    summarizingIds = it.summarizingIds - scanId,
                    message = result.exceptionOrNull()?.let { e -> "Summary failed: ${e.message ?: "network error"}" },
                )
            }
        }
    }

    fun delete(scanId: Long) {
        viewModelScope.launch { deleteScan(scanId) }
    }

    fun messageShown() = transient.update { it.copy(message = null) }
}
