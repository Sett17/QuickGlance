package dev.dikka.quickglance

import android.content.Context
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.pow

fun stringWidth(str: String, fontSize: Float): Float {
    val p = Paint()
    p.textSize = fontSize
    return p.measureText(str)
}

fun maxFontSizeForWidth(word: ProcessedWord, maxWidthPx: Float, epsilon: Float = .91f, context: Context): Float {
    // we need a fontSize that will make beginning (the longer part) + center fit into half the maxWidth

    val part = word.word.substring(0, word.highlightPosition + 1)

    var lowerBound = 0f
    var upperBound = maxWidthPx

    val halfMaxWidth = maxWidthPx / 2

    while ((upperBound - lowerBound) / 2 > epsilon) {
        val midpoint = (upperBound + lowerBound) / 2
        if (stringWidth(part, midpoint) > halfMaxWidth) {
            upperBound = midpoint
        } else {
            lowerBound = midpoint
        }
    }

    return lowerBound
}

data class ProcessedWord(val word: String, val highlightPosition: Int)

@Composable
fun ProcessedWord.Render(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
) {
    val before = buildAnnotatedString {
        append(word.substring(0, highlightPosition))
    }
    val highlight = buildAnnotatedString {
        withStyle(
            SpanStyle(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.inversePrimary,
                        MaterialTheme.colorScheme.primary,
                    )
                )
            )
        ) {
            append(word[highlightPosition])
        }
    }
    val after = buildAnnotatedString {
        append(word.substring(highlightPosition + 1))
    }

    Layout(
        content = {
            Text(text = before, fontSize = fontSize)
            Text(
                text = highlight, fontSize = fontSize, modifier = Modifier
            )
            Text(text = after, fontSize = fontSize)
        }
    ) { measurables, constraints ->

        val beforePlaceable = measurables[0].measure(constraints)
        val highlightPlaceable = measurables[1].measure(constraints)
        val afterPlaceable = measurables[2].measure(constraints)

        val height = maxOf(beforePlaceable.height, highlightPlaceable.height, afterPlaceable.height)

        val highlightX = (constraints.maxWidth - highlightPlaceable.width) / 2
        val beforeX = highlightX - beforePlaceable.width
        val afterX = highlightX + highlightPlaceable.width

        layout(constraints.maxWidth, height) {
            beforePlaceable.place(beforeX, 0)
            highlightPlaceable.place(highlightX, 0)
            afterPlaceable.place(afterX, 0)
        }
    }
}

fun processWord(input: String): ProcessedWord {
    val divisor = 2
    val charOffset = 1
    val codePoints = input.codePoints().toArray()
    val center = (Math.round(codePoints.size / divisor.toDouble()) - charOffset).coerceAtLeast(0).toInt()
    return ProcessedWord(input, center)
}


val a = 9.8
val b = 0.077
fun calculateWordDurations(words: List<ProcessedWord>, wpm: Int, epsilon: Double): List<Double> {
    val n = words.size
    val minDuration = 15.0 // Assuming this is the minimum duration for a word
    Log.d("DIKKA", "n: $n, wpm: $wpm, epsilon: $epsilon, minDuration: $minDuration")

    // 1. Calculate Raw Weights
    val rawWeights = words.map {
//        it.word.length +
        a * E.pow(b * (it.word.length)) +
                if (',' in it.word || '.' in it.word || '!' in it.word || '?' in it.word) {
                    3.0
                } else {
                    0.0
                }

    }

    // 2. Normalize Weights
    val totalWeight = rawWeights.sum()
    val normalizedWeights = rawWeights.map { it / totalWeight }

    Log.d(
        "jo", "${
            words.map { it.word }.zip(normalizedWeights)
        }"
    )

    // 3. Calculate Preliminary Durations
    val totalDuration = n.toDouble() / wpm * 60 * 1000
    val durations = normalizedWeights.map { it * totalDuration }.toMutableList()
    Log.d("DIKKA", "totalDuration: $totalDuration")

    // 4. Apply Minimum Duration Constraint and Recalculate
    var iteration = 0
    val maxIterations = 10
    while (iteration < maxIterations) {
        var deficitDuration = totalDuration
        for (i in durations.indices) {
            if (durations[i] < minDuration) {
                durations[i] = minDuration
            }
            deficitDuration -= durations[i]
        }

        val totalWeightForDeficit =
            normalizedWeights.withIndex().filter { durations[it.index] > minDuration }.sumOf { it.value }
        for (i in durations.indices) {
            if (durations[i] > minDuration) {
                durations[i] += normalizedWeights[i] / totalWeightForDeficit * deficitDuration
            }
        }

        val currentWpm = n.toDouble() / (durations.sum() / 60 / 1000)
        Log.d("DIKKA", "IT: $iteration, currentWpm: $currentWpm, abs(currentWpm - wpm): ${abs(currentWpm - wpm)}")
        if (abs(currentWpm - wpm) <= epsilon) {
            break
        }
        iteration++
    }

    // 5. Handle Edge Case
    if (iteration == maxIterations) {
        val realWpm = n.toDouble() / (durations.sum() / 60 / 1000)
        Log.d("DIKKA", "The exact target WPM is not achievable with the given constraints. Real WPM: $realWpm")
    }

    Log.d("DIKKA", "DONE")

    return durations
}

fun splitAndProcessText(text: String): List<ProcessedWord> {
    val wordPattern = """[a-zA-Z0-9]+[.!?()]?|[.!?()]""".toRegex()
    val words = wordPattern.findAll(text).map { it.value }.toList()
    return words.map { processWord(it) }
}


/*
### Algorithm:

#### 1\. **Calculate Raw Weights:**

For each word iii in the list:

wi\=log⁡(length(wordi)+1)w\_i = \\log(\\text{length}(word\_i) + 1)wi​\=log(length(wordi​)+1)

#### 2\. **Normalize Weights:**

total\_weight\=∑i\=1nwi\\text{total\\\_weight} = \\sum\_{i=1}^{n} w\_itotal\_weight\=i\=1∑n​wi​

Then, for each word iii:

wi\=witotal\_weightw\_i = \\frac{w\_i}{\\text{total\\\_weight}}wi​\=total\_weightwi​​

#### 3\. **Calculate Preliminary Durations:**

total\_duration\=ntarget WPM×60×1000\\text{total\\\_duration} = \\frac{n}{\\text{target WPM}} \\times 60 \\times 1000total\_duration\=target WPMn​×60×1000

Then, for each word iii:

durationi\=wi×total\_duration\\text{duration}\_i = w\_i \\times \\text{total\\\_duration}durationi​\=wi​×total\_duration

#### 4\. **Apply Minimum Duration Constraint and Recalculate:**

- Set a tolerance level, ϵ\\epsilonϵ, for the acceptable deviation from the target WPM.
- Initialize a variable, `iteration`, to 0.
- Repeat the following until the deviation from the target WPM is within ϵ\\epsilonϵ or `iteration` exceeds a set maximum:
    - For each word iii:
        - If durationi<min\_duration\\text{duration}\_i < \\text{min\\\_duration}durationi​<min\_duration, set durationi\=min\_duration\\text{duration}\_i = \\text{min\\\_duration}durationi​\=min\_duration.
    - Calculate the deficit duration:

        deficit\_duration\=total\_duration−∑i\=1ndurationi\\text{deficit\\\_duration} = \\text{total\\\_duration} - \\sum\_{i=1}^{n} \\text{duration}\_ideficit\_duration\=total\_duration−i\=1∑n​durationi​

    - Distribute the `deficit_duration` proportionally among words with durationi\>min\_duration\\text{duration}\_i > \\text{min\\\_duration}durationi​\>min\_duration.
    - Increment `iteration`.

#### 5\. **Handle Edge Case:**

If the deviation from the target WPM is not within ϵ\\epsilonϵ after the maximum iterations:

- Notify the user that the exact target WPM is not achievable with the given constraints, and provide the 'real' WPM.
 */