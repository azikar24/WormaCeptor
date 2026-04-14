package com.azikar24.wormaceptor.common.presentation

/** Marker interface for all feature navigators. */
interface FeatureNavigator

/** No-op navigator for single-screen features that require no navigation. */
object NoOpNavigator : FeatureNavigator
