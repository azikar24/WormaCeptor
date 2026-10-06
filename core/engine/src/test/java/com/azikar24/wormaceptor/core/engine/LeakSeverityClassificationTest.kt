package com.azikar24.wormaceptor.core.engine

import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class LeakSeverityClassificationTest {

    private val shortRetention = 5_000L

    @Test
    fun `first activity leak is high`() {
        classifyLeakSeverity(LeakedObjectKind.ACTIVITY, shortRetention, isRecurring = false) shouldBe
            LeakSeverity.HIGH
    }

    @Test
    fun `recurring activity leak is critical`() {
        classifyLeakSeverity(LeakedObjectKind.ACTIVITY, shortRetention, isRecurring = true) shouldBe
            LeakSeverity.CRITICAL
    }

    @Test
    fun `long retained activity is critical`() {
        classifyLeakSeverity(LeakedObjectKind.ACTIVITY, LongRetentionMs, isRecurring = false) shouldBe
            LeakSeverity.CRITICAL
    }

    @Test
    fun `fragment and view leaks are medium`() {
        classifyLeakSeverity(LeakedObjectKind.FRAGMENT, shortRetention, isRecurring = false) shouldBe
            LeakSeverity.MEDIUM
        classifyLeakSeverity(LeakedObjectKind.VIEW, shortRetention, isRecurring = false) shouldBe
            LeakSeverity.MEDIUM
    }

    @Test
    fun `recurring fragment leak is high`() {
        classifyLeakSeverity(LeakedObjectKind.FRAGMENT, shortRetention, isRecurring = true) shouldBe
            LeakSeverity.HIGH
    }

    @Test
    fun `other objects are low, escalating to medium`() {
        classifyLeakSeverity(LeakedObjectKind.OTHER, shortRetention, isRecurring = false) shouldBe
            LeakSeverity.LOW
        classifyLeakSeverity(LeakedObjectKind.OTHER, LongRetentionMs, isRecurring = false) shouldBe
            LeakSeverity.MEDIUM
    }
}
