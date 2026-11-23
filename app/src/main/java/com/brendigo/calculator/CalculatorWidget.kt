package com.brendigo.calculator

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class CalculatorWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val intent=Intent(context,MainActivity::class.java).apply {
                action=Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pending=PendingIntent.getActivity(context,id,intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val views=RemoteViews(context.packageName,R.layout.calculator_widget_view)
            views.setOnClickPendingIntent(R.id.widget_root,pending)
            manager.updateAppWidget(id,views)
        }
    }
}
