package com.example.o2scratch.di

import com.example.o2scratch.feature.data.networking.ScratchClient
import com.example.o2scratch.feature.domain.repository.ScratchCardRepository
import com.example.o2scratch.feature.presentation.activation.ActivationScreenViewModel
import com.example.o2scratch.feature.presentation.main.MainScreenViewModel
import com.example.o2scratch.feature.presentation.scratch.ScratchScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // ApiClient
    single { ScratchClient() }

    // Repository
    single { ScratchCardRepository(get()) }

    // ViewModels
    viewModel { MainScreenViewModel(get()) }
    viewModel { ScratchScreenViewModel(get()) }
    viewModel { ActivationScreenViewModel(get()) }

}

