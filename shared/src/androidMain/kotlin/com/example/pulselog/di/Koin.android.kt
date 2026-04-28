package com.example.pulselog.di

import com.example.pulselog.data.local.getDatabaseBuilder
import com.example.pulselog.data.local.getRoomDatabase
import com.example.pulselog.data.repository.MistakeRepository
import com.example.pulselog.data.repository.MistakeRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { getDatabaseBuilder(get()) }
    single { getRoomDatabase(get()) }
    single { get<com.example.pulselog.data.local.AppDatabase>().userDao() }
    single { get<com.example.pulselog.data.local.AppDatabase>().mistakeDao() }
    single { get<com.example.pulselog.data.local.AppDatabase>().dropdownDao() }
    single<MistakeRepository> { MistakeRepositoryImpl(get(), get(), get()) }
}
