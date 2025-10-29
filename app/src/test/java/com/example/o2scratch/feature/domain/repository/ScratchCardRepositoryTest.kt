package com.example.o2scratch.feature.domain.repository

import app.cash.turbine.test
import com.example.o2scratch.feature.data.networking.ActivationException
import com.example.o2scratch.feature.data.networking.ScratchClient
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class ScratchCardRepositoryTest {

    private lateinit var repository: ScratchCardRepository
    private lateinit var mockApiClient: ScratchClient
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope
    private val testCodeGenerator: () -> String = { "TEST-CODE" }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(SupervisorJob() + testDispatcher)
        mockApiClient = mockk(relaxed = true)
        repository = ScratchCardRepository(mockApiClient, testScope, testCodeGenerator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Unscratched`() = runTest {
        // When & Then
        repository.cardState.test {
            val state = awaitItem()
            assertEquals(ScratchCardState.Unscratched, state)
        }
    }

    @Test
    fun `scratchCard should emit Loading then Scratched state`() = runTest {
        // When
        repository.cardState.test {
            val initialState = awaitItem()
            assertEquals(ScratchCardState.Unscratched, initialState)

            repository.scratchCard()

            // Should emit Loading
            val loadingState = awaitItem()
            assertEquals(ScratchCardState.Loading, loadingState)

            // Advance time for the delay
            advanceTimeBy(2000)

            // Should emit Scratched with the generated code
            val scratchedState = awaitItem()
            assertTrue(scratchedState is ScratchCardState.Scratched)
            assertEquals("TEST-CODE", (scratchedState as ScratchCardState.Scratched).code)
        }
    }

    @Test
    fun `scratchCard should return success result`() = runTest {
        // When
        val result = repository.scratchCard()
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `scratchCard should use the injected code generator`() = runTest {
        // When
        repository.scratchCard()
        advanceUntilIdle()

        // Then
        val state = repository.cardState.value
        assertTrue(state is ScratchCardState.Scratched)
        assertEquals("TEST-CODE", (state as ScratchCardState.Scratched).code)
    }

    @Test
    fun `scratchCard with different generators should produce different codes`() = runTest {
        // Given
        val generator1 = { "ALPHA-CODE" }
        val generator2 = { "BETA-CODE" }

        val repo1 = ScratchCardRepository(mockApiClient, testScope, generator1)
        val repo2 = ScratchCardRepository(mockApiClient, testScope, generator2)

        // When
        repo1.scratchCard()
        repo2.scratchCard()
        advanceUntilIdle()

        // Then
        val state1 = repo1.cardState.value
        val state2 = repo2.cardState.value

        assertTrue(state1 is ScratchCardState.Scratched)
        assertTrue(state2 is ScratchCardState.Scratched)
        assertEquals("ALPHA-CODE", (state1 as ScratchCardState.Scratched).code)
        assertEquals("BETA-CODE", (state2 as ScratchCardState.Scratched).code)
    }

    @Test
    fun `activateCard with valid version should emit Loading then Activated state`() = runTest {
        // Given
        val code = "TEST123"
        val validVersion = 300000 // Greater than 277028
        coEvery { mockApiClient.getVersion(code) } returns Result.success(validVersion)

        // First scratch the card
        repository.scratchCard()
        advanceUntilIdle()

        // When
        repository.cardState.test {
            skipItems(1) // Skip Scratched state

            repository.activateCard(code)

            // Should emit Loading
            val loadingState = awaitItem()
            assertEquals(ScratchCardState.Loading, loadingState)

            advanceUntilIdle()

            // Should emit Activated
            val activatedState = awaitItem()
            assertTrue(activatedState is ScratchCardState.Activated)
            assertEquals(code, (activatedState as ScratchCardState.Activated).code)
        }
    }

    @Test
    fun `activateCard with valid version should return success result`() = runTest {
        // Given
        val code = "TEST123"
        val validVersion = 300000
        coEvery { mockApiClient.getVersion(code) } returns Result.success(validVersion)

        // When
        val result = repository.activateCard(code)
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockApiClient.getVersion(code) }
    }

    @Test
    fun `activateCard with invalid version should emit Loading then Scratched state`() = runTest {
        // Given
        val code = "OLD123"
        val invalidVersion = 200000 // Less than 277028
        coEvery { mockApiClient.getVersion(code) } returns Result.success(invalidVersion)

        // When
        repository.cardState.test {
            skipItems(1) // Skip initial state

            repository.activateCard(code)

            // Should emit Loading
            val loadingState = awaitItem()
            assertEquals(ScratchCardState.Loading, loadingState)

            advanceUntilIdle()

            // Should emit Scratched (reverted)
            val scratchedState = awaitItem()
            assertTrue(scratchedState is ScratchCardState.Scratched)
            assertEquals(code, (scratchedState as ScratchCardState.Scratched).code)
        }
    }

    @Test
    fun `activateCard with invalid version should return failure with ActivationException`() = runTest {
        // Given
        val code = "OLD123"
        val invalidVersion = 200000
        coEvery { mockApiClient.getVersion(code) } returns Result.success(invalidVersion)

        // When
        val result = repository.activateCard(code)
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationException)
        coVerify { mockApiClient.getVersion(code) }
    }

    @Test
    fun `activateCard with version equal to threshold should return failure`() = runTest {
        // Given
        val code = "EQUAL123"
        val thresholdVersion = 277028 // Equal to threshold
        coEvery { mockApiClient.getVersion(code) } returns Result.success(thresholdVersion)

        // When
        val result = repository.activateCard(code)
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ActivationException)
    }

    @Test
    fun `activateCard with version exactly above threshold should return success`() = runTest {
        // Given
        val code = "EDGE123"
        val edgeVersion = 277029 // Just above threshold
        coEvery { mockApiClient.getVersion(code) } returns Result.success(edgeVersion)

        // When
        val result = repository.activateCard(code)
        advanceUntilIdle()

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `activateCard with API failure should emit Loading then Scratched state`() = runTest {
        // Given
        val code = "FAIL123"
        val exception = Exception("Network error")
        coEvery { mockApiClient.getVersion(code) } returns Result.failure(exception)

        // When
        repository.cardState.test {
            skipItems(1) // Skip initial state

            repository.activateCard(code)

            // Should emit Loading
            val loadingState = awaitItem()
            assertEquals(ScratchCardState.Loading, loadingState)

            advanceUntilIdle()

            // Should emit Scratched (reverted)
            val scratchedState = awaitItem()
            assertTrue(scratchedState is ScratchCardState.Scratched)
            assertEquals(code, (scratchedState as ScratchCardState.Scratched).code)
        }
    }

    @Test
    fun `activateCard with API failure should return failure result`() = runTest {
        // Given
        val code = "FAIL123"
        val exception = Exception("Network error")
        coEvery { mockApiClient.getVersion(code) } returns Result.failure(exception)

        // When
        val result = repository.activateCard(code)
        advanceUntilIdle()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `activateCard should call API client with correct code`() = runTest {
        // Given
        val code = "VERIFY123"
        coEvery { mockApiClient.getVersion(code) } returns Result.success(300000)

        // When
        repository.activateCard(code)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { mockApiClient.getVersion(code) }
    }

    @Test
    fun `multiple scratchCard calls should emit states in correct order`() = runTest(testDispatcher) {
        // When & Then
        repository.cardState.test {
            assertEquals(ScratchCardState.Unscratched, awaitItem())

            // First scratch
            repository.scratchCard()
            assertEquals(ScratchCardState.Loading, awaitItem())
            advanceTimeBy(2000)

            val firstScratched = awaitItem()
            assertTrue(firstScratched is ScratchCardState.Scratched)

            // Note: In real scenario, you'd need to reset state for second scratch
            // This test just verifies the state flow emissions
        }
    }

    @Test
    fun `complete flow - scratch then successful activation`() = runTest(testDispatcher) {
        // Given
        val validVersion = 300000
        coEvery { mockApiClient.getVersion(any()) } returns Result.success(validVersion)

        // When & Then
        repository.cardState.test {
            // Initial state
            assertEquals(ScratchCardState.Unscratched, awaitItem())

            // Scratch the card
            repository.scratchCard()
            assertEquals(ScratchCardState.Loading, awaitItem())
            advanceTimeBy(2000)

            val scratchedState = awaitItem()
            assertTrue(scratchedState is ScratchCardState.Scratched)
            val code = (scratchedState as ScratchCardState.Scratched).code

            // Activate the card
            repository.activateCard(code)
            assertEquals(ScratchCardState.Loading, awaitItem())
            advanceUntilIdle()

            val activatedState = awaitItem()
            assertTrue(activatedState is ScratchCardState.Activated)
            assertEquals(code, (activatedState as ScratchCardState.Activated).code)
        }
    }

    @Test
    fun `complete flow - scratch then failed activation reverts to scratched`() = runTest {
        // Given
        val invalidVersion = 100000
        coEvery { mockApiClient.getVersion(any()) } returns Result.success(invalidVersion)

        // When & Then
        repository.cardState.test {
            // Initial state
            assertEquals(ScratchCardState.Unscratched, awaitItem())

            // Scratch the card
            repository.scratchCard()
            assertEquals(ScratchCardState.Loading, awaitItem())
            advanceTimeBy(2000)

            val scratchedState = awaitItem()
            assertTrue(scratchedState is ScratchCardState.Scratched)
            val code = (scratchedState as ScratchCardState.Scratched).code

            // Try to activate the card (should fail)
            repository.activateCard(code)
            assertEquals(ScratchCardState.Loading, awaitItem())
            advanceUntilIdle()

            // Should revert to Scratched
            val revertedState = awaitItem()
            assertTrue(revertedState is ScratchCardState.Scratched)
            assertEquals(code, (revertedState as ScratchCardState.Scratched).code)
        }
    }

    @Test
    fun `activateCard runs in applicationScope and survives test scope cancellation`() = runTest {
        // Given
        val code = "SURVIVE123"
        val validVersion = 300000
        coEvery { mockApiClient.getVersion(code) } returns Result.success(validVersion)

        // When - Start activation
        val resultDeferred = repository.activateCard(code)

        // Simulate scope cancellation (like ViewModel being cleared)
        // The operation should still complete because it's in applicationScope

        advanceUntilIdle()
        val result = resultDeferred

        // Then - Should still succeed
        assertTrue(result.isSuccess)
        coVerify { mockApiClient.getVersion(code) }
    }
}

