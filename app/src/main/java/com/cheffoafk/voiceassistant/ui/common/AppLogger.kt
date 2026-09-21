package com.cheffoafk.voiceassistant.ui.common

import android.util.Log

/**
 * Shorthand per il pattern `runCatching { ... }.onFailure { AppLogger.error(...) }`.
 *
 * Uso:
 * ```
 * runCatching { synthesizer.speak(text) }
 *     .logOnFailure(TAG, "Errore TTS")
 * ```
 */
fun <T> Result<T>.logOnFailure(tag: String, message: String): Result<T> =
    onFailure { AppLogger.error(tag, message, it) }

object AppLogger {
    fun error(tag: String, message: String, throwable: Throwable? = null) {
        try {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        } catch (_: Throwable) {
            // Nei test JVM android.util.Log non e disponibile: fallback su stderr.
            if (throwable != null) {
                System.err.println("[$tag] $message: ${throwable.message}")
            } else {
                System.err.println("[$tag] $message")
            }
        }
    }
}

