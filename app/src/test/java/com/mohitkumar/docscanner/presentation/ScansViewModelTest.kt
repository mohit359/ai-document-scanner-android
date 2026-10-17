package com.mohitkumar.docscanner.presentation

import app.cash.turbine.test
import com.mohitkumar.docscanner.domain.model.Scan
import com.mohitkumar.docscanner.domain.repository.ScanRepository
import com.mohitkumar.docscanner.domain.usecase.DeleteScanUseCase
import com.mohitkumar.docscanner.domain.usecase.ObserveScansUseCase
import com.mohitkumar.docscanner.domain.usecase.SaveScanUseCase
import com.mohitkumar.docscanner.domain.usecase.SummarizeScanUseCase
import com.mohitkumar.docscanner.presentation.scans.ScansViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class ScansViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private val scans = MutableStateFlow(listOf(Scan(1, "Invoice total 500", null, 0L)))
    private val repository = mockk<ScanRepository>(relaxed = true) {
        every { observeScans() } returns scans
    }

    private fun viewModel() = ScansViewModel(
        ObserveScansUseCase(repository),
        SaveScanUseCase(repository),
        SummarizeScanUseCase(repository),
        DeleteScanUseCase(repository),
    )

    @Test
    fun `exposes scans from repository`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            var item = awaitItem()
            if (item.scans.isEmpty()) item = awaitItem() // skip the initial empty state
            assertEquals(1, item.scans.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `blank recognised text is not saved and a message is shown`() = runTest {
        val vm = viewModel()
        vm.uiState.test {
            vm.onTextRecognized("   ")
            advanceUntilIdle()
            val last = expectMostRecentItem()
            assertNotNull(last.message)
            coVerify(exactly = 0) { repository.saveScan(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed summary shows an error message and clears loading`() = runTest {
        coEvery { repository.summarize(1) } returns Result.failure(RuntimeException("timeout"))
        val vm = viewModel()
        vm.uiState.test {
            vm.summarize(1)
            advanceUntilIdle()
            val last = expectMostRecentItem()
            assertEquals(true, last.summarizingIds.isEmpty())
            assertEquals("Summary failed: timeout", last.message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
