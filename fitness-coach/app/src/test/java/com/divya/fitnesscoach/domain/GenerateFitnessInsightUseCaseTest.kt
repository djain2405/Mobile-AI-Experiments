package com.divya.fitnesscoach.domain

import com.divya.fitnesscoach.data.FakeInsightEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * This is exactly the kind of test that isn't possible without the FitnessInsightEngine
 * architecture seam, there's no real Gemini Nano available in a JVM unit test, ever.
 * The fake makes this test possible at all, and none of it needs ML Kit on the classpath.
 */
class GenerateFitnessInsightUseCaseTest {

    @Test
    fun `returns success insight from the engine`() = runTest {
        val engine = FakeInsightEngine(response = FitnessInsightResult.Success("Nice work this week."))
        val useCase = GenerateFitnessInsightUseCase(engine)

        val result = useCase("Workouts: Mon 30min, Wed 25min")

        assertEquals(FitnessInsightResult.Success("Nice work this week."), result)
    }

    @Test
    fun `propagates unavailable from the engine`() = runTest {
        val engine = FakeInsightEngine(response = FitnessInsightResult.Unavailable)
        val useCase = GenerateFitnessInsightUseCase(engine)

        val result = useCase("Workouts: Mon 30min")

        assertEquals(FitnessInsightResult.Unavailable, result)
    }

    @Test
    fun `returns InvalidInput for a blank activity summary, without throwing`() = runTest {
        val engine = FakeInsightEngine()
        val useCase = GenerateFitnessInsightUseCase(engine)

        val result = useCase("   ")

        assertEquals(FitnessInsightResult.InvalidInput, result)
    }

    @Test
    fun `returns InvalidInput for an over-length activity summary`() = runTest {
        val engine = FakeInsightEngine()
        val useCase = GenerateFitnessInsightUseCase(engine)

        val tooLong = "a".repeat(1_001)
        val result = useCase(tooLong)

        assertEquals(FitnessInsightResult.InvalidInput, result)
    }

    @Test
    fun `maps a failure reason through to the result`() = runTest {
        val engine = FakeInsightEngine(
            response = FitnessInsightResult.Failed(FitnessInsightFailure.DeviceUnsupported)
        )
        val useCase = GenerateFitnessInsightUseCase(engine)

        val result = useCase("Workouts: Mon 30min")

        assertEquals(FitnessInsightResult.Failed(FitnessInsightFailure.DeviceUnsupported), result)
    }
}
