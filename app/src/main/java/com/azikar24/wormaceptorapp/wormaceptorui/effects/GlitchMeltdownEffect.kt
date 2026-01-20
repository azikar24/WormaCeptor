/*
 * Copyright AziKar24 2024.
 */

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

    // Pseudo-random function
    float hash(float n) {
        return fract(sin(n) * 43758.5453123);
    }

    float hash2(float2 p) {
        return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
    }

    // Noise function
    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f);

        float a = hash2(i);
        float b = hash2(i + float2(1.0, 0.0));
        float c = hash2(i + float2(0.0, 1.0));
        float d = hash2(i + float2(1.0, 1.0));

        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    }

    half4 main(float2 coord) {
        float2 uv = coord / resolution;
        float intensity = progress;

        // RGB split / chromatic aberration (increases with progress)
        float rgbSplit = intensity * 0.03;
        float2 redOffset = float2(rgbSplit * sin(time * 10.0 + uv.y * 20.0), 0.0);
        float2 blueOffset = float2(-rgbSplit * cos(time * 8.0 + uv.y * 15.0), rgbSplit * 0.5);

        // Screen tearing - horizontal block shifts
        float tearStrength = intensity * intensity * 0.1;
        float blockY = floor(uv.y * (10.0 + intensity * 20.0));
        float tearOffset = 0.0;
        if (hash(blockY + floor(time * 30.0)) > (1.0 - intensity * 0.5)) {
            tearOffset = (hash(blockY * 2.0 + time) - 0.5) * tearStrength;
        }

        // Melt/drip effect (increases dramatically in later stages)
        float meltAmount = intensity * intensity * 0.15;
        float meltNoise = noise(float2(uv.x * 10.0, time * 2.0));
        float meltOffset = meltAmount * meltNoise * (1.0 - uv.y);

        // Apply distortions to UV
        float2 distortedUV = uv;
        distortedUV.x += tearOffset;
        distortedUV.y += meltOffset;

        // Clamp UVs
        distortedUV = clamp(distortedUV, float2(0.0), float2(1.0));
        float2 redUV = clamp(distortedUV + redOffset, float2(0.0), float2(1.0));
        float2 blueUV = clamp(distortedUV + blueOffset, float2(0.0), float2(1.0));

        // Sample content with RGB separation
        half4 colR = content.eval(redUV * resolution);
        half4 colG = content.eval(distortedUV * resolution);
        half4 colB = content.eval(blueUV * resolution);

        half4 color = half4(colR.r, colG.g, colB.b, colG.a);

        // Static noise overlay
        float noiseVal = hash2(uv * resolution + float2(time * 100.0, time * 50.0));
        float noiseIntensity = intensity * 0.3;
        color.rgb = mix(color.rgb, half3(noiseVal), noiseIntensity * step(0.7, noiseVal + intensity * 0.3));

        // Scan lines
        float scanLine = sin(coord.y * 2.0 + time * 50.0) * 0.5 + 0.5;
        scanLine = pow(scanLine, 2.0);
        float scanIntensity = intensity * 0.15;
        color.rgb *= (1.0 - scanIntensity * scanLine);

        // Color corruption - desaturate and darken
        float darkShift = intensity * 0.4;
        float luminance = dot(color.rgb, half3(0.299, 0.587, 0.114));
        color.rgb = mix(color.rgb, half3(luminance), darkShift);
        color.rgb *= (1.0 - darkShift * 0.3);

        // Block glitches
        float blockSize = 20.0 + intensity * 30.0;
        float2 blockCoord = floor(coord / blockSize);
        float blockRand = hash2(blockCoord + floor(time * 20.0));
        if (blockRand > (1.0 - intensity * 0.15) && intensity > 0.4) {
            float glitchType = hash(blockRand * 100.0);
            if (glitchType < 0.33) {
                color.rgb = half3(0.0, 0.0, 0.0);
            } else if (glitchType < 0.66) {
                color.rgb = 1.0 - color.rgb;
            } else {
                color.rgb = half3(noiseVal);
            }
        }

        // Final flash at the end (progress > 0.9)
        if (intensity > 0.9) {
            float flashIntensity = (intensity - 0.9) / 0.1;
            half3 flashColor = mix(half3(0.0, 0.0, 0.0), half3(1.0, 1.0, 1.0), flashIntensity);
            color.rgb = mix(color.rgb, flashColor, flashIntensity * flashIntensity);
        }

        return color;
    }
"""

@Composable
fun GlitchMeltdownEffect(
    isActive: Boolean,
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
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
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
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
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shake",
    )

    val rotationWobble by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rotation",
    )

    val noiseKey by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "noise_key",
    )

    val shakeIntensity = progress * 20f
    val rotationIntensity = progress * 3f
    val scaleVariation = 1f + (progress * 0.05f * shakeOffset)

    val darkOverlayAlpha = (progress * 0.6f).coerceIn(0f, 0.6f)
    val flashAlpha = if (progress > 0.9f) ((progress - 0.9f) / 0.1f).coerceIn(0f, 1f) else 0f

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    x = (shakeOffset * shakeIntensity).dp,
                    y = (shakeOffset * shakeIntensity * 0.7f).dp,
                )
                .rotate(rotationWobble * rotationIntensity)
                .scale(scaleVariation),
        ) {
            content()
        }

        if (progress > 0.2f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scanLineCount = 100
                val scanLineAlpha = ((progress - 0.2f) / 0.8f * 0.3f).coerceIn(0f, 0.3f)
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

        if (progress > 0.4f) {
            val random = remember(noiseKey.toInt()) { Random(noiseKey.toInt()) }
            Canvas(modifier = Modifier.fillMaxSize()) {
                val noiseAlpha = ((progress - 0.4f) / 0.6f * 0.4f).coerceIn(0f, 0.4f)
                val pixelSize = 4f
                val noiseCount = ((size.width / pixelSize) * (size.height / pixelSize) * 0.02f).toInt()

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

        if (progress > 0.3f) {
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
