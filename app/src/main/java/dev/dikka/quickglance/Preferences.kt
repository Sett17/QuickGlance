package dev.dikka.quickglance

import android.content.Context
import android.content.SharedPreferences

object Preferences {
    private lateinit var prefs: SharedPreferences

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences("quickglance", Context.MODE_PRIVATE)
    }

    private const val PREF_WPM_KEY = "quickglance.wpm"
    var wpm: Int
        get() = prefs.getInt(PREF_WPM_KEY, 300)
        set(value) = prefs.edit().putInt(PREF_WPM_KEY, value).apply()
}