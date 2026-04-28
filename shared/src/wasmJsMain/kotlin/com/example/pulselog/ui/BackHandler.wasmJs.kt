package com.example.pulselog.ui

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op or browser-specific back handling logic for Wasm
}
