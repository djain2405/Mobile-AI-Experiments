package com.divya.fitnesscoach.ui

import com.divya.fitnesscoach.data.FakeAiReadinessManager
import com.divya.fitnesscoach.data.FakeInsightEngine
import com.divya.fitnesscoach.domain.AiReadinessState
import com.divya.fitnesscoach.domain.FitnessAiReadinessManager
import com.divya.fitnesscoach.domain.GenerateFitnessInsightUseCase
import com.divya.fitnesscoach.domain.ReadinessFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FitnessCoachViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(readinessManager: FitnessAiReadinessManager) = FitnessCoachViewModel(
        generateInsight = GenerateFitnessInsightUseCase(FakeInsightEngine(simulatedDelayMillis = 0L)),
        readinessManager = readinessManager,
        onClear = {}
    )

    @Test
    fun `entering the screen checks and warms up until Ready`() = runTest(dispatcher) {
        val vm = viewModel(FakeAiReadinessManager())
        val states = mutableListOf<AiReadinessState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.readiness.toList(states) }

        advanceUntilIdle()

        assertEquals(
            listOf(AiReadinessState.Checking, AiReadinessState.WarmingUp, AiReadinessState.Ready),
            states
        )
    }

    @Test
    fun `download goes through progress and warm-up before Ready`() = runTest(dispatcher) {
        val vm = viewModel(FakeAiReadinessManager(initialStatus = AiReadinessState.DownloadRequired))
        advanceUntilIdle()
        assertEquals(AiReadinessState.DownloadRequired, vm.readiness.value)

        val states = mutableListOf<AiReadinessState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.readiness.toList(states) }
        vm.downloadModel()
        advanceUntilIdle()

        assertTrue(states.any { it is AiReadinessState.Downloading })
        val warmUpIndex = states.indexOf(AiReadinessState.WarmingUp)
        assertTrue("WarmingUp must come after downloading", warmUpIndex > 0)
        assertEquals(AiReadinessState.Ready, states.last())
    }

    @Test
    fun `failed download offers a retry path`() = runTest(dispatcher) {
        val manager = FakeAiReadinessManager(
            initialStatus = AiReadinessState.DownloadRequired,
            downloadSucceeds = false
        )
        val vm = viewModel(manager)
        advanceUntilIdle()

        vm.downloadModel()
        advanceUntilIdle()
        assertEquals(AiReadinessState.Failed(ReadinessFailure.DownloadFailed), vm.readiness.value)

        vm.retryReadiness()
        advanceUntilIdle()
        assertEquals(AiReadinessState.DownloadRequired, vm.readiness.value)
    }

    @Test
    fun `failed status check becomes Ready on retry when the runtime recovers`() = runTest(dispatcher) {
        val manager = FakeAiReadinessManager(
            initialStatus = AiReadinessState.Failed(ReadinessFailure.StatusCheckFailed)
        )
        val vm = viewModel(manager)
        advanceUntilIdle()
        assertEquals(AiReadinessState.Failed(ReadinessFailure.StatusCheckFailed), vm.readiness.value)

        manager.currentStatus = AiReadinessState.Ready
        vm.retryReadiness()
        advanceUntilIdle()

        assertEquals(AiReadinessState.Ready, vm.readiness.value)
    }

    @Test
    fun `generate is ignored until the runtime is ready`() = runTest(dispatcher) {
        val vm = viewModel(FakeAiReadinessManager(initialStatus = AiReadinessState.DownloadRequired))
        advanceUntilIdle()

        vm.requestInsight()
        advanceUntilIdle()

        assertEquals(FitnessCoachUiState.Idle, vm.uiState.value)
    }

    @Test
    fun `generate produces an insight once Ready`() = runTest(dispatcher) {
        val vm = viewModel(FakeAiReadinessManager())
        advanceUntilIdle()

        vm.requestInsight()
        advanceUntilIdle()

        assertEquals(
            FitnessCoachUiState.Insight(FakeInsightEngine.defaultSuccess.insight),
            vm.uiState.value
        )
    }

    @Test
    fun `only Ready and Unavailable allow an insight request`() {
        val allowed = listOf(
            AiReadinessState.Checking,
            AiReadinessState.DownloadRequired,
            AiReadinessState.Downloading(0L),
            AiReadinessState.WarmingUp,
            AiReadinessState.Ready,
            AiReadinessState.Unavailable,
            AiReadinessState.Failed(ReadinessFailure.Unknown)
        ).filter { it.allowsInsightRequest() }

        assertEquals(listOf(AiReadinessState.Ready, AiReadinessState.Unavailable), allowed)
    }
}
