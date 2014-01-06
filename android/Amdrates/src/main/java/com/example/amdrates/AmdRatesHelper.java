package com.example.amdrates;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AmdRatesHelper {
    private static final String RATES_API_URL = "http://amd-rates.herokuapp.com/api/v1/rates/%s";
    private static final String BANKS_API_URL = "http://amd-rates.herokuapp.com/api/v1/banks";
    private static final int HTTP_STATUS_OK = 200;

    private static byte[] sBuffer = new byte[512];

    public static class ApiException extends Exception {
        public ApiException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ApiException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static class ParseException extends Exception {
        public ParseException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }
    }

    public static Rate getRate(String bank, String currency) throws ApiException, ParseException {
        Rate rate = null;

        String content = getUrlContent(String.format(RATES_API_URL, bank));
        try {
            JSONObject response = new JSONObject(content);
            JSONObject currencyRate = response.getJSONObject(currency);
            JSONObject sellRate = currencyRate.getJSONObject("sell");
            JSONObject buyRate = currencyRate.getJSONObject("buy");

            rate = new Rate(sellRate.getDouble("rate"), buyRate.getDouble("rate"));
        } catch (JSONException e) {
            throw new ParseException("Problem parsing API response", e);
        }

        return rate;
    }

    public static ArrayList<String> getBanks() throws ApiException, ParseException {
        ArrayList<String> banks = new ArrayList<String>();

        String content = getUrlContent(BANKS_API_URL);
        try {
            JSONObject response = new JSONObject(content);
            JSONArray banksArray = response.getJSONArray("banks");

            if (banksArray != null) {
                for(int i = 0; i < banksArray.length(); i++) {
                    banks.add(banksArray.get(i).toString());
                }
            }

        } catch (JSONException e) {
            throw new ParseException("Problem parsing API response", e);
        }

        return banks;
    }

    protected static synchronized String getUrlContent(String url) throws ApiException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        try {
            HttpResponse response = client.execute(request);

            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HTTP_STATUS_OK) {
                throw new ApiException("Invalid response from server: " +
                        status.toString());
            }

            // Pull content stream from response
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();

            ByteArrayOutputStream content = new ByteArrayOutputStream();

            // Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }

            return new String(content.toByteArray());
        } catch (IOException e) {
            throw new ApiException("Problem communicating with API", e);
        }
    }
}
