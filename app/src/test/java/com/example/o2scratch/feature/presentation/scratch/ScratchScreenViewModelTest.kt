package com.example.o2scratch.feature.presentation.scratch

import app.cash.turbine.test
import com.example.o2scratch.feature.domain.repository.ScratchCardRepository
import com.example.o2scratch.feature.presentation.base.ScreenEvent
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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
class ScratchScreenViewModelTest {

    private lateinit var viewModel: ScratchScreenViewModel
    private lateinit var mockRepository: ScratchCardRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        // When
        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(ScratchCardState.Loading, state)
        }
    }

    @Test
    fun `state should update when repository card state changes`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.state.test {
            awaitItem() // Initial state

            cardStateFlow.value = ScratchCardState.Unscratched
            val state = awaitItem()

            // Then
            assertEquals(ScratchCardState.Unscratched, state)
        }
    }

    @Test
    fun `scratch action with unscratched card should call repository scratchCard`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.scratchCard() } returns Result.success(Unit)

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
        advanceUntilIdle()

        // Then
        coVerify { mockRepository.scratchCard() }
    }

    @Test
    fun `scratch action with scratched card should show already scratched toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched("ABC123"))
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is ScreenEvent.Toast)
            assertEquals("Card is already scratched", (event as ScreenEvent.Toast).message)

            coVerify(exactly = 0) { mockRepository.scratchCard() }
        }
    }

    @Test
    fun `scratch action with activated card should show already activated toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Activated("ABC123"))
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is ScreenEvent.Toast)
            assertEquals("Card is already activated", (event as ScreenEvent.Toast).message)

            coVerify(exactly = 0) { mockRepository.scratchCard() }
        }
    }

    @Test
    fun `scratch action with loading card should show error toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is ScreenEvent.Toast)
            assertEquals("Could not scratch the card. Please try again later.", (event as ScreenEvent.Toast).message)

            coVerify(exactly = 0) { mockRepository.scratchCard() }
        }
    }

    @Test
    fun `state should transition from Unscratched to Scratched after scratching`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.scratchCard() } answers {
            cardStateFlow.value = ScratchCardState.Scratched("TEST123")
            Result.success(Unit)
        }

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(ScratchCardState.Unscratched, initialState)

            viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
            advanceUntilIdle()

            val scratchedState = awaitItem()

            // Then
            assertTrue(scratchedState is ScratchCardState.Scratched)
            assertEquals("TEST123", (scratchedState as ScratchCardState.Scratched).code)
        }
    }

    @Test
    fun `multiple scratch attempts on scratched card should keep showing toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched("ABC123"))
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When & Then
        viewModel.events.test {
            // First attempt
            viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
            advanceUntilIdle()
            val event1 = awaitItem()
            assertTrue(event1 is ScreenEvent.Toast)
            assertEquals("Card is already scratched", (event1 as ScreenEvent.Toast).message)

            // Second attempt
            viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
            advanceUntilIdle()
            val event2 = awaitItem()
            assertTrue(event2 is ScreenEvent.Toast)
            assertEquals("Card is already scratched", (event2 as ScreenEvent.Toast).message)

            coVerify(exactly = 0) { mockRepository.scratchCard() }
        }
    }

    @Test
    fun `state changes through all lifecycle stages should be observed correctly`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When & Then
        viewModel.state.test {
            // Initial Loading state
            val state1 = awaitItem()
            assertEquals(ScratchCardState.Loading, state1)

            // Transition to Unscratched
            cardStateFlow.value = ScratchCardState.Unscratched
            val state2 = awaitItem()
            assertEquals(ScratchCardState.Unscratched, state2)

            // Transition to Scratched
            cardStateFlow.value = ScratchCardState.Scratched("CODE123")
            val state3 = awaitItem()
            assertTrue(state3 is ScratchCardState.Scratched)
            assertEquals("CODE123", (state3 as ScratchCardState.Scratched).code)

            // Transition to Activated
            cardStateFlow.value = ScratchCardState.Activated("CODE123")
            val state4 = awaitItem()
            assertTrue(state4 is ScratchCardState.Activated)
            assertEquals("CODE123", (state4 as ScratchCardState.Activated).code)
        }
    }

    @Test
    fun `repository scratchCard should only be called for unscratched state`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.scratchCard() } returns Result.success(Unit)

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When - Scratch when Unscratched
        viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
        advanceUntilIdle()

        // Then - Should be called once
        coVerify(exactly = 1) { mockRepository.scratchCard() }

        // When - Update state to Scratched
        cardStateFlow.value = ScratchCardState.Scratched("ABC123")
        advanceUntilIdle()

        // When - Try to scratch again
        viewModel.onAction(ScratchScreenViewModel.Action.Scratch)
        advanceUntilIdle()

        // Then - Should still be called only once (not called again)
        coVerify(exactly = 1) { mockRepository.scratchCard() }
    }

    @Test
    fun `consecutive state updates from repository should all be observed`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ScratchScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When & Then
        viewModel.state.test {
            awaitItem() // Loading

            cardStateFlow.value = ScratchCardState.Unscratched
            assertEquals(ScratchCardState.Unscratched, awaitItem())

            cardStateFlow.value = ScratchCardState.Loading
            assertEquals(ScratchCardState.Loading, awaitItem())

            cardStateFlow.value = ScratchCardState.Scratched("TEST")
            val scratchedState = awaitItem()
            assertTrue(scratchedState is ScratchCardState.Scratched)
        }
    }
}

