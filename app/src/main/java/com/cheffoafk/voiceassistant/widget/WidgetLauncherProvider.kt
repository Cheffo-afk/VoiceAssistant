package com.cheffoafk.voiceassistant.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cheffoafk.voiceassistant.MainActivity
import com.cheffoafk.voiceassistant.R

/**
 * Widget 6×6 – header con icona e nome app, poi due bottoni:
 *   • "Tocca per aprire" → apre la schermata principale
 *   • "Aiuto" (rosso)   → apre l'app e riproduce "Aiuto" via TTS
 */
class WidgetLauncherProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_launcher)

            // --- Bottone "Tocca per aprire" ---
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_OPEN,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_open, openPendingIntent)

            // --- Bottone "Aiuto" (rosso) ---
            val aiutoIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_SPEAK_TEXT, "Aiuto")
            }
            val aiutoPendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_AIUTO,
                aiutoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_aiuto, aiutoPendingIntent)

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    companion object {
        private const val REQUEST_CODE_OPEN  = 100
        private const val REQUEST_CODE_AIUTO = 101
    }
}
