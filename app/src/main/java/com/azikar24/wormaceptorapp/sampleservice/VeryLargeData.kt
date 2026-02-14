package com.azikar24.wormaceptorapp.sampleservice

class VeryLargeData {
    private val data: String

    init {
        val stringBuilder = StringBuilder(1000000) // 100 * 10_000
        repeat(999) {
            stringBuilder.append(
                "The quick brown fox jumps over the lazy dog over and over again many times,100 word sentence formed.",
            )
        }
        data = stringBuilder.toString()
    }
}
