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
 * Widget 4×4 – tocca per aprire l'app e riprodurre "Aiuto" via TTS.
 */
class WidgetAiutoProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_aiuto)

            val speakIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_SPEAK_TEXT, "Aiuto")
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_AIUTO,
                speakIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_aiuto_root, pendingIntent)

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    companion object {
        private const val REQUEST_CODE_AIUTO = 200
    }
}

