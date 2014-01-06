package com.example.amdrates;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Random;
import java.util.concurrent.ExecutionException;

public class WidgetUpdateService extends Service {
    private static final String LOG = "com.example.amdrates.WidgetUpdateService";

    private int widgetId = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG, "called");
        String bank = intent.getStringExtra(AmdRatesWidgetProvider.RATE_BANK);
        String currency = intent.getStringExtra(AmdRatesWidgetProvider.RATE_CURRENCY);
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);

        new RetrieveRateTask().execute(bank, currency);

        stopSelf();

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class RetrieveRateTask extends AsyncTask<String, Void, Rate> {
        private String bank;
        private String currency;

        @Override
        protected Rate doInBackground(String... arguments) {
            bank = arguments[0];
            currency = arguments[1];

            Rate rate = null;
            try {
                rate = AmdRatesHelper.getRate(bank, currency);
            } catch (AmdRatesHelper.ApiException e) {
                Log.e(LOG, "Couldn't contact API", e);
            } catch (AmdRatesHelper.ParseException e) {
                Log.e(LOG, "Couldn't parse API response", e);
            }

            return rate;
        }

        @Override
        protected void onPostExecute(Rate rate) {
            Context context = getApplicationContext();
            RemoteViews views = null;
            if (rate != null) {
                views = new RemoteViews(context.getPackageName(), R.layout.amdrates_widget_layout);

                views.setTextViewText(R.id.widget_sell, Double.toString(rate.sell));
                views.setTextViewText(R.id.widget_buy, Double.toString(rate.buy));
                views.setTextViewText(R.id.widget_bank, bank);
                views.setTextViewText(R.id.widget_currency, currency);

                Intent clickIntent = new Intent(context, AmdRatesConfigure.class);
                clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId /* for uniqueness */, clickIntent, 0 );
                views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            } else {
                views = new RemoteViews(context.getPackageName(), R.layout.amdrates_widget_layout);
                views.setTextViewText(R.id.widget_bank, context.getString(R.string.widget_error));
            }

            //ComponentName thisWidget = new ComponentName(context, AmdRatesWidgetProvider.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(widgetId, views);

        }
    }
}
