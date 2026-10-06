package com.azikar24.wormaceptor.feature.preferences.vm

import com.azikar24.wormaceptor.common.presentation.SearchDebounce
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SearchDebounceMillisTest {

    @Test
    fun `blank query loads immediately`() {
        searchDebounceMillis("") shouldBe 0L
        searchDebounceMillis("  ") shouldBe 0L
    }

    @Test
    fun `typed query is debounced`() {
        searchDebounceMillis("theme") shouldBe SearchDebounce.DEFAULT
    }
}
