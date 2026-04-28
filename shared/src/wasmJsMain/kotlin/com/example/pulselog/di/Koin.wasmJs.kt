package com.example.pulselog.di

import com.example.pulselog.data.repository.MistakeRepository
import com.example.pulselog.data.repository.MistakeRepositoryWasm
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // Room is not supported on Wasm yet.
    // Provide mocks or empty implementations for now.
    single<MistakeRepository> { MistakeRepositoryWasm() }
}
