package com.example.amdrates;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AmdRatesWidgetProvider extends AppWidgetProvider {
    public static final String RATE_BANK = "RATE_BANK";
    public static final String RATE_CURRENCY = "RATE_CURRENCY";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        updateAllWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidget(Context context, int widgetId, String bank, String currency) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(widgetId + "bank", bank);
        editor.putString(widgetId + "currency", currency);
        editor.commit();

        Intent intent = new Intent(context.getApplicationContext(), WidgetUpdateService.class);
        intent.putExtra(RATE_BANK, bank);
        intent.putExtra(RATE_CURRENCY, currency);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        context.startService(intent);
    }


    private void updateAllWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for(int widgetId : appWidgetIds) {
            String bank = prefs.getString(widgetId + "bank", "VTB");
            String currency = prefs.getString(widgetId + "currency", "USD");

            Intent intent = new Intent(context.getApplicationContext(), WidgetUpdateService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.putExtra(RATE_BANK, bank);
            intent.putExtra(RATE_CURRENCY, currency);

            context.startService(intent);
        }
    }

}
