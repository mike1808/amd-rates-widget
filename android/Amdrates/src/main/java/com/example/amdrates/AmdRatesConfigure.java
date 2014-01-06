package com.example.amdrates;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Spinner;

import java.util.ArrayList;

public class AmdRatesConfigure extends Activity {

    private Spinner bankSpinner, currencySpinner;
    private Button submitBtn;

    private int mAppWidgetId = 0;

    private String selectedBank = "VTB";
    private String selectedCurrency = "USD";

    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.configure);

        mContext = this;
        bankSpinner = (Spinner)findViewById(R.id.bank_spinner);
        currencySpinner = (Spinner)findViewById(R.id.currency_spinner);
        submitBtn = (Button)findViewById(R.id.btn_submit);

        new DownloadBanksTask().execute();

        submitBtn.setOnClickListener(submitBtnOnClickListener);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        bankSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedBank = (String) adapterView.getItemAtPosition(i);
                Log.v("Selected Bank", selectedBank);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCurrency = (String) adapterView.getItemAtPosition(i);
                Log.v("Selected Currency", selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private View.OnClickListener submitBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AmdRatesWidgetProvider.updateWidget(getBaseContext(), mAppWidgetId,
                    selectedBank, selectedCurrency);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    protected class DownloadBanksTask extends AsyncTask<Void, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> banks = null;

            try {
                banks = AmdRatesHelper.getBanks();
            } catch (AmdRatesHelper.ApiException e) {
                e.printStackTrace();
                return null;
            } catch (AmdRatesHelper.ParseException e) {
                e.printStackTrace();
                return null;
            }

            return banks;
        }

        @Override
        protected void onPostExecute(ArrayList<String> banks) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(mContext,
                  android.R.layout.simple_spinner_item,  banks);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            bankSpinner.setAdapter(dataAdapter);

            bankSpinner.setSelection(0);
        }
    }
}
