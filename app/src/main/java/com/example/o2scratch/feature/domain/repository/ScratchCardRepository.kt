package com.example.o2scratch.feature.domain.repository

import android.util.Log
import com.example.o2scratch.feature.data.networking.ActivationException
import com.example.o2scratch.feature.data.networking.ScratchClient
import com.example.o2scratch.feature.presentation.common.ScratchCardState
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScratchCardRepository(
    private val apiClient: ScratchClient,
    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val generateUniqueCode: () -> String = { UUID.randomUUID().toString() }
) {

    private val _cardState = MutableStateFlow<ScratchCardState>(ScratchCardState.Unscratched)
    val cardState: StateFlow<ScratchCardState> = _cardState.asStateFlow()

    suspend fun scratchCard(): Result<Unit> {
        _cardState.value = ScratchCardState.Loading
        Log.d(ScratchCardRepository::class.toString(), "Starting scratch operation")

        delay(2000) // Simulate heavy operation (2 seconds)

        val code = generateUniqueCode()
        Log.d(ScratchCardRepository::class.toString(), "Generated code: $code")
        _cardState.value = ScratchCardState.Scratched(code)
        return Result.success(Unit)
    }

    suspend fun activateCard(code: String): Result<Unit> {
        return applicationScope.async {
            _cardState.value = ScratchCardState.Loading
            Log.d(ScratchCardRepository::class.toString(), "Starting activation for code: $code")
            //            delay(2000) // delay for testing purposes

            apiClient.getVersion(code)
                .fold(
                    onSuccess = { version ->
                        validateVersion(version, code)
                    },
                    onFailure = { exception ->
                        Log.e(ScratchCardRepository::class.toString(), "Activation failed", exception)
                        _cardState.value = ScratchCardState.Scratched(code)
                        Result.failure(exception)
                    }
                )
        }.await()
    }

    private fun validateVersion(version: Int, code: String): Result<Unit> {
        return if (version > ACTIVATION_THRESHOLD) {
            Log.d(ScratchCardRepository::class.toString(), "Activation successful")
            _cardState.value = ScratchCardState.Activated(code)
            Result.success(Unit)
        } else {
            Log.d(ScratchCardRepository::class.toString(), "Version ${version} is not greater than $ACTIVATION_THRESHOLD")
            _cardState.value = ScratchCardState.Scratched(code)
            Result.failure(ActivationException("Version ${version} is not greater than $ACTIVATION_THRESHOLD"))
        }
    }

    companion object Companion {
        private const val ACTIVATION_THRESHOLD = 277028
    }
}