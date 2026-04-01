package com.azikar24.wormaceptor.domain.entities.mock

/** Controls when and how a mock rule is applied across multiple requests. */
sealed class MockBehavior {
    /** Always return the mock response for every matching request. */
    data object Always : MockBehavior()

    /** Only mock every [n]th matching request; pass through the rest. */
    data class NthRequest(val n: Int) : MockBehavior()

    /** Mock the first [count] matching requests, then pass through. */
    data class FirstN(val count: Int) : MockBehavior()

    /** Cycle through a list of [responses] for successive matching requests. */
    data class Sequential(val responses: List<MockResponse>) : MockBehavior()

    /** Match the request but pass it through to the network (useful for debugging matchers). */
    data object Passthrough : MockBehavior()
}
