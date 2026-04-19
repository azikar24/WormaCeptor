package com.azikar24.wormaceptor.api.compose

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Records a recomposition event every time the annotated composable recomposes.
 * Data is surfaced in the Recomposition Inspector dashboard.
 *
 * In release builds the tracking backend is absent from the classpath (ships
 * only through `debugImplementation`), so the reflective lookup returns null
 * and this modifier short-circuits to `this` with zero Compose overhead.
 *
 * ```kotlin
 * Card(modifier = Modifier.trackRecomposition("ProductCard")) { ... }
 * ```
 *
 * @param name Human-readable identifier for the composable.
 */
@Suppress("ModifierComposed")
fun Modifier.trackRecomposition(name: String): Modifier {
    val recorder = RecompositionRecorderLookup.recorder ?: return this
    return composed {
        SideEffect { recorder(name) }
        this
    }
}

private object RecompositionRecorderLookup {

    val recorder: ((String) -> Unit)? by lazy { resolveRecorder() }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun resolveRecorder(): ((String) -> Unit)? = try {
        val cls = Class.forName("com.azikar24.wormaceptor.feature.recomposition.RecompositionTracker")
        val instance = cls.getField("INSTANCE").get(null)
        val method = cls.getMethod("record", String::class.java)
        val lambda: (String) -> Unit = { name -> method.invoke(instance, name) }
        lambda
    } catch (t: Throwable) {
        null
    }
}
