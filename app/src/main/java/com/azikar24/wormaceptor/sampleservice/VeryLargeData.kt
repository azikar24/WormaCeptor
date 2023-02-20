/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.sampleservice

class VeryLargeData {
    val data: String

    init {
        val stringBuilder = StringBuilder(1000000) // 100 * 10_000
        for (i in 1..9999) {
            stringBuilder.append("The quick brown fox jumps over the lazy dog over and over again many times,100 word sentence formed.")
        }
        data = stringBuilder.toString()
    }
}
