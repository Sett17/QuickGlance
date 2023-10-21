package dev.dikka.quickglance.ui.components

import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dikka.quickglance.stringWidth
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NumberPicker(
    state: MutableIntState,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    min: Int = 0,
    max: Int = 100,
    step: Int = 5,
    enabled: Boolean = true,
    onValueChange: (Int) -> Unit = {},
    fontSize: TextUnit = 36.sp,
) {
    state.intValue = state.intValue.coerceIn(min, max)
    var upPressed by remember { mutableStateOf(false) }
    var downPressed by remember { mutableStateOf(false) }

    val haptic = LocalContext.current.getSystemService(Vibrator::class.java)

    val upOnClick = upOnClick@{
        if (state.intValue + step > max || !enabled) return@upOnClick
        haptic.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        state.intValue += step
        onValueChange(state.intValue)
    }
    val downOnClick = downOnClick@{
        if (state.intValue - step < min || !enabled) return@downOnClick
        haptic.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        state.intValue -= step
        onValueChange(state.intValue)
    }

    BoxWithConstraints(
        modifier
    ) {
        Column(
            Modifier,
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = upOnClick,
                Modifier
                    .padding(0.dp)
                    .size((fontSize.value * 1.5).dp)
                    .pointerInteropFilter {
                        upPressed = when (it.action) {
                            MotionEvent.ACTION_DOWN -> true
                            else -> false
                        }
                        true
                    },
                enabled = enabled
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    "Up",
                    Modifier
                        .size((fontSize.value * 1.5).dp)
                )
            }
            BasicTextField("${state.intValue}",
                modifier = Modifier.width(
                    stringWidth("$max", fontSize.value).dp + 5.dp
                ),
                enabled = enabled,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                maxLines = 1,
                onValueChange = { str ->
                    val i = str.toIntOrNull() ?: return@BasicTextField
                    state.intValue = i.coerceIn(min, max)
                    onValueChange(state.intValue)
                })
            IconButton(
                onClick = downOnClick,
                Modifier
                    .padding(0.dp)
                    .size((fontSize.value * 1.5).dp)
                    .pointerInteropFilter {
                        downPressed = when (it.action) {
                            MotionEvent.ACTION_DOWN -> true
                            else -> false
                        }
                        true
                    },
                enabled = enabled
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    "Down",
                    Modifier
                        .size((fontSize.value * 1.5).dp),
                )
            }
            label()
        }
    }

    val maxDelayMillis = 200L
    val minDelayMillis = 20L
    val delayDecayFactor = 0.15f

    LaunchedEffect(upPressed, enabled) {
        var currentDelayMillis = maxDelayMillis

        while (enabled && upPressed) {
            upOnClick()
            delay(currentDelayMillis)
            currentDelayMillis =
                (currentDelayMillis - (currentDelayMillis * delayDecayFactor))
                    .toLong().coerceAtLeast(minDelayMillis)
        }
    }

    LaunchedEffect(downPressed, enabled) {
        var currentDelayMillis = maxDelayMillis

        while (enabled && downPressed) {
            downOnClick()
            delay(currentDelayMillis)
            currentDelayMillis =
                (currentDelayMillis - (currentDelayMillis * delayDecayFactor))
                    .toLong().coerceAtLeast(minDelayMillis)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NumberPickerPreview() {
    val v = remember { mutableIntStateOf(69) }
    NumberPicker(v, max = 50)
}
