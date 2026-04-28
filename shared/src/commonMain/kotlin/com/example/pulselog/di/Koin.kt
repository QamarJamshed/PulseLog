package com.example.pulselog.di

import com.example.pulselog.data.repository.MistakeRepository
import com.example.pulselog.ui.auth.AuthViewModel
import com.example.pulselog.ui.mistake.MistakeViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

fun commonModule() = module {
    single { AuthViewModel(get()) }
    factory { MistakeViewModel(get()) }
}

fun initKoin(appDeclaration: org.koin.dsl.KoinAppDeclaration = {}) = org.koin.core.context.startKoin {
    appDeclaration()
    modules(commonModule(), platformModule())
}

expect fun platformModule(): Module
