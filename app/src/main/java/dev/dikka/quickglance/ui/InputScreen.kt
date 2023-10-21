package dev.dikka.quickglance.ui

import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dikka.quickglance.Preferences
import dev.dikka.quickglance.ui.components.NumberPicker
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(inputState: MutableState<String>, gotoRead: () -> Unit, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()

    val wpm = remember { mutableIntStateOf(Preferences.wpm) }
    val haptic = LocalContext.current.getSystemService(Vibrator::class.java)

    Surface(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        val clipboardManager = LocalClipboardManager.current

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {
                    haptic.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    clipboardManager.getText()?.let {
                        val textBefore = inputState.value
                        inputState.value = it.text
                        scope.launch {
                            val result = snackbarHostState
                                .showSnackbar(
                                    message = "Text pasted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                            when (result) {
                                SnackbarResult.ActionPerformed -> {
                                    inputState.value = textBefore
                                }

                                else -> {}
                            }
                        }
                    }
                }) {
                    Text("Paste")
                }
                OutlinedTextField(
                    value = inputState.value,
                    placeholder = { Text("Text to read") },
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.4f)
                        .padding(8.dp),
                    onValueChange = {
                        inputState.value = it
                    }
                )
            }
            NumberPicker(state = wpm, min = 100, max = 1000, fontSize = 26.sp, label = {
                Text("WPM", fontSize = 26.sp)
            }, onValueChange = {
                Preferences.wpm = it
            })
            Button(
                onClick = {
                    haptic.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    gotoRead()
                },
                Modifier.size(160.dp, 75.dp)
            ) {
                Text("Read", fontSize = 24.sp)
            }
        }
    }
}
