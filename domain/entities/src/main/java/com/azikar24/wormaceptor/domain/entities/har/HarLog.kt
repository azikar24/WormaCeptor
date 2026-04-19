package com.azikar24.wormaceptor.domain.entities.har

/**
 * Top-level HAR document wrapper.
 *
 * @see <a href="http://www.softwareishard.com/blog/har-12-spec/">HAR 1.2 Spec</a>
 */
data class HarLog(
    val version: String = "1.2",
    val creator: HarCreator,
    val entries: List<HarEntry>,
)
