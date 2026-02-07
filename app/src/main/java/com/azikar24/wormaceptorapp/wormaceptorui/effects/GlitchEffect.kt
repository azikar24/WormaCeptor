package com.azikar24.wormaceptorapp.wormaceptorui.effects

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private const val GLITCH_SHADER = """
    uniform shader content;
    uniform float2 resolution;
    uniform float progress;
    uniform float time;

    // Saturate/clamp helper
    float sat(float t) {
        return clamp(t, 0.0, 1.0);
    }

    float2 sat2(float2 t) {
        return clamp(t, float2(0.0), float2(1.0));
    }

    // Remaps interval [a;b] to [0;1]
    float remap(float t, float a, float b) {
        return sat((t - a) / (b - a));
    }

    // Triangle wave: t=[0;0.5;1], y=[0;1;0]
    float linterp(float t) {
        return sat(1.0 - abs(2.0 * t - 1.0));
    }

    // Chromatic spectrum offset
    float3 spectrum_offset(float t) {
        float lo = step(t, 0.5);
        float hi = 1.0 - lo;
        float w = linterp(remap(t, 1.0 / 6.0, 5.0 / 6.0));
        float neg_w = 1.0 - w;
        float3 ret = float3(lo, 1.0, hi) * float3(neg_w, w, neg_w);
        return pow(ret, float3(1.0 / 2.2));
    }

    // Pseudo-random [0;1]
    float rand(float2 n) {
        return fract(sin(dot(n, float2(12.9898, 78.233))) * 43758.5453);
    }

    // Pseudo-random [-1;1]
    float srand(float2 n) {
        return rand(n) * 2.0 - 1.0;
    }

    // Truncate to discrete levels
    float mytrunc(float x, float num_levels) {
        return floor(x * num_levels) / num_levels;
    }

    float2 mytrunc2(float2 x, float num_levels) {
        return floor(x * num_levels) / num_levels;
    }

    half4 main(float2 coord) {
        float2 uv = coord / resolution;

        float modTime = mod(time * 100.0, 32.0) / 110.0;
        float GLITCH = clamp(progress, 0.0, 1.0);

        float gnm = sat(GLITCH);
        float rnd0 = rand(mytrunc2(float2(modTime, modTime), 6.0));
        float r0 = sat((1.0 - gnm) * 0.7 + rnd0);
        float rnd1 = rand(float2(mytrunc(uv.x, 10.0 * r0), modTime));
        float r1 = 0.5 - 0.5 * gnm + rnd1;
        r1 = 1.0 - max(0.0, (r1 < 1.0) ? r1 : 0.9999999);
        float rnd2 = rand(float2(mytrunc(uv.y, 40.0 * r1), modTime));
        float r2 = sat(rnd2);
        float rnd3 = rand(float2(mytrunc(uv.y, 10.0 * r0), modTime));
        float r3 = (1.0 - sat(rnd3 + 0.8)) - 0.1;
        float pxrnd = rand(uv + modTime);

        float ofs = 0.05 * r2 * GLITCH * (rnd0 > 0.5 ? 1.0 : -1.0);
        ofs += 0.5 * pxrnd * ofs;

        float2 distortedUV = uv;
        distortedUV.y += 0.1 * r3 * GLITCH;

        const int NUM_SAMPLES = 20;
        const float RCP_NUM_SAMPLES_F = 1.0 / float(NUM_SAMPLES);

        float4 sum = float4(0.0);
        float3 wsum = float3(0.0);

        for (int i = 0; i < NUM_SAMPLES; ++i) {
            float t = float(i) * RCP_NUM_SAMPLES_F;
            float2 sampleUV = distortedUV;
            sampleUV.x = sat(sampleUV.x + ofs * t);

            half4 sampleCol = content.eval(sampleUV * resolution);
            float3 s = spectrum_offset(t);
            sum.rgb += float3(sampleCol.rgb) * s;
            sum.a += float(sampleCol.a);
            wsum += s;
        }

        sum.rgb /= wsum;
        sum.a *= RCP_NUM_SAMPLES_F;

        return half4(half3(sum.rgb), half(sum.a));
    }
"""

@Composable
fun GlitchEffect(isActive: Boolean, progress: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (!isActive) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        GlitchMeltdownEffectAGSL(
            progress = progress,
            modifier = modifier,
            content = content,
        )
    } else {
        GlitchMeltdownEffectFallback(
            progress = progress,
            modifier = modifier,
            content = content,
        )
    }
}

@Composable
private fun GlitchMeltdownEffectAGSL(progress: Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Box(modifier = modifier) { content() }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glitch_time")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = GlitchConstants.Animation.GLITCH_TIME_TARGET,
        animationSpec = infiniteRepeatable(
            animation = tween(GlitchConstants.Animation.GLITCH_TIME_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "time",
    )

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val shader = remember { RuntimeShader(GLITCH_SHADER) }

    Box(
        modifier = modifier.graphicsLayer {
            shader.setFloatUniform("resolution", screenWidthPx, screenHeightPx)
            shader.setFloatUniform("progress", progress)
            shader.setFloatUniform("time", time)
            renderEffect = RenderEffect
                .createRuntimeShaderEffect(shader, "content")
                .asComposeRenderEffect()
        },
    ) {
        content()
    }
}

@Composable
private fun GlitchMeltdownEffectFallback(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fallback_glitch")

    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(GlitchConstants.Animation.SHAKE_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shake",
    )

    val rotationWobble by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(GlitchConstants.Animation.ROTATION_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rotation",
    )

    val noiseKey by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = GlitchConstants.Animation.NOISE_KEY_TARGET,
        animationSpec = infiniteRepeatable(
            animation = tween(GlitchConstants.Animation.NOISE_KEY_DURATION_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "noise_key",
    )

    val shakeIntensity = progress * GlitchConstants.Intensity.MAX_SHAKE_DP
    val rotationIntensity = progress * GlitchConstants.Intensity.MAX_ROTATION_DEGREES
    val scaleVariation = 1f + progress * GlitchConstants.Intensity.SCALE_VARIATION * shakeOffset

    val darkOverlayAlpha = (progress * GlitchConstants.Alpha.MAX_DARK_OVERLAY).coerceIn(
        0f,
        GlitchConstants.Alpha.MAX_DARK_OVERLAY,
    )
    val flashAlpha = if (progress > GlitchConstants.Alpha.FLASH_THRESHOLD) {
        ((progress - GlitchConstants.Alpha.FLASH_THRESHOLD) / GlitchConstants.Alpha.FLASH_RANGE).coerceIn(0f, 1f)
    } else {
        0f
    }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    x = (shakeOffset * shakeIntensity).dp,
                    y = (shakeOffset * shakeIntensity * GlitchConstants.Intensity.Y_SHAKE_FACTOR).dp,
                )
                .rotate(rotationWobble * rotationIntensity)
                .scale(scaleVariation),
        ) {
            content()
        }

        if (progress > GlitchConstants.ProgressThreshold.SCANLINES) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanLineCount = GlitchConstants.Scanlines.COUNT
                val scanlineProgress = progress - GlitchConstants.ProgressThreshold.SCANLINES
                val scanLineAlpha = (
                    scanlineProgress / GlitchConstants.Scanlines.FADE_IN_RANGE *
                        GlitchConstants.Scanlines.MAX_ALPHA
                    ).coerceIn(0f, GlitchConstants.Scanlines.MAX_ALPHA)
                for (i in 0 until scanLineCount) {
                    val y = (i.toFloat() / scanLineCount) * size.height
                    if (i % 2 == 0) {
                        drawRect(
                            color = Color.Black.copy(alpha = scanLineAlpha),
                            topLeft = Offset(0f, y),
                            size = Size(size.width, size.height / scanLineCount / 2),
                        )
                    }
                }
            }
        }

        if (progress > GlitchConstants.ProgressThreshold.NOISE) {
            val random = remember(noiseKey.toInt()) { Random(noiseKey.toInt()) }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val noiseProgress = progress - GlitchConstants.ProgressThreshold.NOISE
                val noiseAlpha = (
                    noiseProgress / GlitchConstants.Noise.FADE_IN_RANGE *
                        GlitchConstants.Noise.MAX_ALPHA
                    ).coerceIn(0f, GlitchConstants.Noise.MAX_ALPHA)
                val pixelSize = GlitchConstants.Noise.PIXEL_SIZE
                val pixelArea = (size.width / pixelSize) * (size.height / pixelSize)
                val noiseCount = (pixelArea * GlitchConstants.Noise.DENSITY).toInt()

                repeat(noiseCount) {
                    val x = random.nextFloat() * size.width
                    val y = random.nextFloat() * size.height
                    val brightness = random.nextFloat()
                    drawRect(
                        color = Color(brightness, brightness, brightness, noiseAlpha),
                        topLeft = Offset(x, y),
                        size = Size(pixelSize, pixelSize),
                    )
                }
            }
        }

        if (progress > GlitchConstants.ProgressThreshold.DARK_OVERLAY) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = darkOverlayAlpha)),
            )
        }

        if (flashAlpha > 0f) {
            val flashColor = if (flashAlpha > 0.5f) Color.White else Color.Black
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(flashColor.copy(alpha = flashAlpha)),
            )
        }
    }
}
