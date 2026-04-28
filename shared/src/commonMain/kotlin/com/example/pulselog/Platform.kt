package com.example.pulselog

enum class PlatformType {
    ANDROID, WASM
}

expect val platform: PlatformType
