package com.example.o2scratch.feature.presentation.activation

import app.cash.turbine.test
import com.example.o2scratch.feature.data.networking.ActivationException
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
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ActivationScreenViewModelTest {

    private lateinit var viewModel: ActivationScreenViewModel
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
        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(ScratchCardState.Loading, state.scratchCardState)
            assertFalse(state.showErrorDialog)
        }
    }

    @Test
    fun `state should update when repository card state changes`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.state.test {
            awaitItem() // Initial state

            cardStateFlow.value = ScratchCardState.Scratched("TEST123")
            val state = awaitItem()

            // Then
            assertTrue(state.scratchCardState is ScratchCardState.Scratched)
            assertEquals("TEST123", (state.scratchCardState as ScratchCardState.Scratched).code)
        }
    }

    @Test
    fun `activate action with scratched card should call repository and show success toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched("ABC123"))
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.activateCard("ABC123") } returns Result.success(Unit)

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ActivationScreenViewModel.Action.Activate)
            advanceUntilIdle()

            // Then
            expectNoEvents()

            coVerify { mockRepository.activateCard("ABC123") }
        }
    }

    @Test
    fun `activate action with scratched card should set loading state`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched("ABC123"))
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.activateCard("ABC123") } returns Result.success(Unit)

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState.scratchCardState is ScratchCardState.Scratched)

            viewModel.onAction(ActivationScreenViewModel.Action.Activate)

            val loadingState = awaitItem()
            assertEquals(ScratchCardState.Loading, loadingState.scratchCardState)
        }
    }

    @Test
    fun `activate action with already activated card should show toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Activated("ABC123"))
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ActivationScreenViewModel.Action.Activate)
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is ScreenEvent.Toast)
            assertEquals("Card is already activated", (event as ScreenEvent.Toast).message)

            coVerify(exactly = 0) { mockRepository.activateCard(any()) }
        }
    }

    @Test
    fun `activate action with unscratched card should show error toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ActivationScreenViewModel.Action.Activate)
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is ScreenEvent.Toast)
            assertEquals("You need to scratch the card first", (event as ScreenEvent.Toast).message)

            coVerify(exactly = 0) { mockRepository.activateCard(any()) }
        }
    }

    @Test
    fun `activate action with loading card should show error toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ActivationScreenViewModel.Action.Activate)
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is ScreenEvent.Toast)
            assertEquals("You need to scratch the card first", (event as ScreenEvent.Toast).message)
        }
    }

    @Test
    fun `activate with ActivationException should show error dialog`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched("OLD123"))
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.activateCard("OLD123") } returns Result.failure(
            ActivationException("Version too old")
        )

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.state.test {
            awaitItem() // Initial state

            viewModel.onAction(ActivationScreenViewModel.Action.Activate)
            awaitItem() // Loading state
            advanceUntilIdle()

            val state = awaitItem()

            // Then
            assertTrue(state.showErrorDialog)
        }
    }

    @Test
    fun `activate with generic exception should show error toast`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched("ABC123"))
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.activateCard("ABC123") } returns Result.failure(
            Exception("Network error")
        )

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.events.test {
            viewModel.onAction(ActivationScreenViewModel.Action.Activate)
            advanceUntilIdle()

            // Then
            val event = awaitItem()
            assertTrue(event is ScreenEvent.Toast)
            assertEquals("Could not activate the card. Please try again later.", (event as ScreenEvent.Toast).message)
        }
    }

    @Test
    fun `DismissErrorDialog action should hide error dialog`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Scratched("OLD123"))
        coEvery { mockRepository.cardState } returns cardStateFlow
        coEvery { mockRepository.activateCard("OLD123") } returns Result.failure(
            ActivationException("Version too old")
        )

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When
        viewModel.state.test {
            awaitItem() // Initial state

            viewModel.onAction(ActivationScreenViewModel.Action.Activate)
            awaitItem() // Loading state
            advanceUntilIdle()

            val stateWithDialog = awaitItem()
            assertTrue(stateWithDialog.showErrorDialog)

            viewModel.onAction(ActivationScreenViewModel.Action.DismissErrorDialog)

            val stateWithoutDialog = awaitItem()

            // Then
            assertFalse(stateWithoutDialog.showErrorDialog)
        }
    }

    @Test
    fun `multiple state changes should be observed correctly`() = runTest {
        // Given
        val cardStateFlow = MutableStateFlow<ScratchCardState>(ScratchCardState.Loading)
        coEvery { mockRepository.cardState } returns cardStateFlow

        viewModel = ActivationScreenViewModel(mockRepository)
        advanceUntilIdle()

        // When & Then
        viewModel.state.test {
            val state1 = awaitItem()
            assertEquals(ScratchCardState.Loading, state1.scratchCardState)

            cardStateFlow.value = ScratchCardState.Unscratched
            val state2 = awaitItem()
            assertEquals(ScratchCardState.Unscratched, state2.scratchCardState)

            cardStateFlow.value = ScratchCardState.Scratched("TEST")
            val state3 = awaitItem()
            assertTrue(state3.scratchCardState is ScratchCardState.Scratched)

            cardStateFlow.value = ScratchCardState.Activated("TEST")
            val state4 = awaitItem()
            assertTrue(state4.scratchCardState is ScratchCardState.Activated)
        }
    }
}

