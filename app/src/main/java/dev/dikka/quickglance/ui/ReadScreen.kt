@file:OptIn(ExperimentalComposeUiApi::class)

package dev.dikka.quickglance.ui

import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dikka.quickglance.*
import dev.dikka.quickglance.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReadScreen(content: String, snackbarHostState: SnackbarHostState) {
    Surface(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxHeight(0.95f)
    ) {
        val ctx = LocalContext.current
        val words = remember { mutableStateListOf<ProcessedWord>() }
        val durations = remember { mutableStateListOf<Double>() }
        var isLoaded by remember { mutableStateOf(false) }

        val maxWidth = dpToPx(LocalConfiguration.current.screenWidthDp.dp, LocalContext.current)

        val maxFontSizeSp = remember { mutableFloatStateOf(0f) }

        val haptic = LocalContext.current.getSystemService(Vibrator::class.java)

        LaunchedEffect(true) {
            words.addAll(splitAndProcessText(content))
            durations.addAll(calculateWordDurations(words, Preferences.wpm, .5))

            val longestWord = words.maxBy { it.word.length }

            maxFontSizeSp.floatValue = pxToSp(maxFontSizeForWidth(longestWord, maxWidth, context = ctx), ctx).value

            isLoaded = true
        }

        val currentIndex = remember { mutableIntStateOf(0) }
        val isPlaying = remember { mutableStateOf(false) }


        var leftPressed by remember { mutableStateOf(false) }
        var rightPressed by remember { mutableStateOf(false) }
        val leftClick = {
            if (currentIndex.intValue > 0) {
                haptic.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                currentIndex.intValue -= 1
            } else {
                leftPressed = false
            }
            isPlaying.value = false
        }
        val rightClick = {
            if (currentIndex.intValue < words.size - 1) {
                haptic.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                currentIndex.intValue++
            } else {
                rightPressed = false
            }
            isPlaying.value = false
        }

        val maxDelayMillis = 200L
        val minDelayMillis = 30L
        val delayDecayFactor = 0.15f
        LaunchedEffect(leftPressed) {
            var currentDelayMillis = maxDelayMillis

            delay(550)

            while (leftPressed) {
                leftClick()
                delay(currentDelayMillis)
                currentDelayMillis =
                    (currentDelayMillis - (currentDelayMillis * delayDecayFactor))
                        .toLong().coerceAtLeast(minDelayMillis)
            }
        }

        LaunchedEffect(rightPressed) {
            var currentDelayMillis = maxDelayMillis

            delay(550)

            while (rightPressed) {
                rightClick()
                delay(currentDelayMillis)
                currentDelayMillis =
                    (currentDelayMillis - (currentDelayMillis * delayDecayFactor))
                        .toLong().coerceAtLeast(minDelayMillis)
            }
        }

//        AnimatedContent(targetState = isLoaded.value, label = "") {
//            if (it) {
                LaunchedEffect(isPlaying.value) {
                    if (isPlaying.value) {
                        var index = currentIndex.intValue
                        while (index < words.size && isActive) {
                            delay((durations[index]).toLong())
                            if (isPlaying.value) {
                                if (index < words.size - 1) {
                                    currentIndex.intValue++
                                    index++
                                } else {
                                    isPlaying.value = false
                                    break
                                }
                            }
                        }
                    }
                }

                Column(
                    Modifier,
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier)

                    Box {
                        if (isLoaded && currentIndex.intValue <= words.size) {
                            words[currentIndex.intValue].Render(
                                fontSize = maxFontSizeSp.floatValue.sp
                            )
                        }
                        Spacer(Modifier.height(100.dp))
                    }

                    Row(
                        Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {},
                            Modifier
                                .size(100.dp, 75.dp)
                                .pointerInteropFilter {
                                    if (it.action == MotionEvent.ACTION_DOWN) {
                                        if (currentIndex.intValue > 0) {
                                            currentIndex.intValue -= 1
                                        } else {
                                            currentIndex.intValue = words.size - 1
                                        }

                                        leftPressed = true
                                    } else {
                                        leftPressed = false
                                    }
                                    true
                                }
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowLeft,
                                contentDescription = "Pause",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Button(
                            onClick = {
                                if (currentIndex.intValue == words.size - 1) {
                                    currentIndex.intValue = 0
                                }
                                haptic.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                                isPlaying.value = !isPlaying.value
                            }, Modifier.size(125.dp, 75.dp)
                        ) {
                            AnimatedContent(targetState = isPlaying.value, label = "") {
                                if (it) {
                                    Icon(
                                        painter = painterResource(R.drawable.round_pause_24),
                                        contentDescription = "Pause",
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.PlayArrow,
                                        contentDescription = "Play",
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                        Button(
                            onClick = {},
                            Modifier
                                .size(100.dp, 75.dp)
                                .pointerInteropFilter {
                                    if (it.action == MotionEvent.ACTION_DOWN) {
                                        if (currentIndex.intValue == words.size - 1) {
                                            currentIndex.intValue = 0
                                        } else {
                                            currentIndex.intValue += 1
                                        }

                                        rightPressed = true
                                    } else {
                                        rightPressed = false
                                    }
                                    true
                                }
                        ) {
                            Icon(
                                Icons.Rounded.KeyboardArrowRight,
                                contentDescription = "Pause",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
//            } else {
//                Column(
//                    verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text("Analyzing Text...")
//                }
//            }
//
//        }


    }
}
