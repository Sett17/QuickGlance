package dev.dikka.quickglance.ui

import android.content.Context
import android.util.TypedValue
import android.view.textservice.TextInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


fun dpToPx(dp: Dp, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.value, context.resources.displayMetrics)
}

fun pxToDp(px: Float, context: Context): Dp {
    return (px / context.resources.displayMetrics.density).dp
}

fun spToPx(sp: TextUnit, context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.value, context.resources.displayMetrics)
}

fun pxToSp(px: Float, context: Context): TextUnit {
    return (px / context.resources.displayMetrics.density).sp
}