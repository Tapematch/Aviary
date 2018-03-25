package de.taubsi.aviary.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import de.taubsi.aviary.R;
import de.taubsi.aviary.SettingsActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

class HttpService {

    Context context;
    SharedPreferences sharedPref;
    String apiUrl;
    String apiKey;

    HttpService(Context context) throws IOException {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        apiUrl = sharedPref.getString(SettingsActivity.KEY_PREF_API_URL, null);
        apiKey = sharedPref.getString(SettingsActivity.KEY_PREF_API_KEY, null);
        if(apiUrl==null || apiUrl.isEmpty() || apiKey==null || apiKey.isEmpty())
            throw new IOException(context.getString(R.string.settings_not_set_message));
    }

    InputStream get(String urlString) throws IOException {
        URL url = new URL(apiUrl + urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("AviaryAPI-Key", apiKey);
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.connect();
        if(conn.getResponseCode() == 200) {
            return conn.getInputStream();
        } else if(conn.getResponseCode() == 404) {
            throw new IOException("API-Url nicht korrekt!");
        }  else if(conn.getResponseCode() == 401) {
            throw new IOException("API-Key nicht korrekt!");
        } else {
            throw new IOException(conn.getResponseMessage());
        }
    }

    String post(String urlString, String json) throws IOException {
        URL url = new URL(apiUrl + urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("AviaryAPI-Key", apiKey);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.connect();

        OutputStream os = conn.getOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

        osw.write(json);
        osw.flush();
        osw.close();

        if(conn.getResponseCode() == 200){
            return null;
        } else if(conn.getResponseCode() == 404) {
            throw new IOException("API-Url nicht korrekt!");
        }  else if(conn.getResponseCode() == 401) {
            throw new IOException("API-Key nicht korrekt!");
        } else {
            throw new IOException(conn.getResponseMessage());
        }
    }

    String delete(String urlString) throws IOException {
        URL url = new URL(apiUrl + urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("AviaryAPI-Key", apiKey);
        conn.setRequestMethod("DELETE");
        conn.connect();

        if(conn.getResponseCode() == 201){
            return null;
        } else if(conn.getResponseCode() == 404) {
            throw new IOException("API-Url nicht korrekt!");
        }  else if(conn.getResponseCode() == 401) {
            throw new IOException("API-Key nicht korrekt!");
        } else {
            throw new IOException(conn.getResponseMessage());
        }
    }
}
