package de.taubsi.aviary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessaging;
import de.taubsi.aviary.model.FlapLog;
import de.taubsi.aviary.model.HeaterLog;
import de.taubsi.aviary.model.Log;
import de.taubsi.aviary.service.FlapLogService;
import de.taubsi.aviary.service.HeaterLogService;
import de.taubsi.aviary.service.LogService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {

    NavigationView navigationView;

    SwipeRefreshLayout swipeRefreshLayout;

    private TextView syncText;

    private TextView heaterText;
    private TextView insideTempText;
    private TextView insideHumidText;
    private TextView outsideTempText;
    private TextView outsideHumidText;
    WebView webview;
    private TextView lampsText;
    private TextView flapText;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_dashboard);
        navigationView.setNavigationItemSelectedListener(this);

        syncText = (TextView) findViewById(R.id.dashboard_sync_text);

        heaterText = (TextView) findViewById(R.id.dashboard_heater_text);
        insideTempText = (TextView) findViewById(R.id.dashboard_inside_temp_text);
        insideHumidText = (TextView) findViewById(R.id.dashboard_inside_humid_text);
        outsideTempText = (TextView) findViewById(R.id.dashboard_outside_temp_text);
        outsideHumidText = (TextView) findViewById(R.id.dashboard_outside_humid_text);

        webview = (WebView) findViewById(R.id.lamp_graphs);

        lampsText = (TextView) findViewById(R.id.dashboard_lamps_text);
        flapText = (TextView) findViewById(R.id.dashboard_flap_text);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            swipeRefreshLayout.setRefreshing(true);
                            refresh(null);
                        }
                    }
            );
        }

        refresh(null);

        FirebaseMessaging.getInstance().subscribeToTopic("error");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean flapSetting = sharedPref.getBoolean(SettingsActivity.KEY_PREF_FLAP_NOTIFICATION, false);
        boolean flapAutoSetting = sharedPref.getBoolean(SettingsActivity.KEY_PREF_FLAP_AUTO_NOTIFICATION, false);
        if(flapSetting){
            if(flapAutoSetting) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic("flap");
                FirebaseMessaging.getInstance().subscribeToTopic("flapAuto");
            }
            else {
                FirebaseMessaging.getInstance().subscribeToTopic("flap");
                FirebaseMessaging.getInstance().subscribeToTopic("flapAuto");
            }
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("flap");
            FirebaseMessaging.getInstance().unsubscribeFromTopic("flapAuto");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(navigationView != null)
            navigationView.setCheckedItem(R.id.nav_dashboard);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent = null;
        switch (id) {
            case R.id.nav_dashboard:
                //intent = new Intent(getApplicationContext(), DashboardActivity.class);
                break;
            case R.id.nav_camera:
                intent = new Intent(getApplicationContext(), CameraActivity.class);
                break;
            case R.id.nav_history:
                intent = new Intent(getApplicationContext(), HistoryActivity.class);
                break;
            case R.id.nav_manual:
                intent = new Intent(getApplicationContext(), ManualActivity.class);
                break;
            case R.id.nav_lampconfig:
                intent = new Intent(getApplicationContext(), LampConfigurationActivity.class);
                break;
            case R.id.nav_config:
                intent = new Intent(getApplicationContext(), ConfigurationActivity.class);
                break;
            case R.id.nav_settings:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (intent != null)
            startActivity(intent);
        return true;
    }

    @Override
    public void onRefresh() {
        refresh(null);
    }

    public void refresh(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RefreshDashboardTask().execute();
        } else {
            syncText.setText(R.string.no_network_connection);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private class RefreshDashboardTask extends AsyncTask<Void, Void, Void> {
        Resources res = getResources();
        Log log = new Log();
        HeaterLog heaterLog = new HeaterLog();
        FlapLog flapLog = new FlapLog();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            syncText.setText(res.getString(R.string.loading));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                log = new LogService(DashboardActivity.this).getLatestLog();
                heaterLog = new HeaterLogService(DashboardActivity.this).getLatestHeaterLog();
                flapLog = new FlapLogService(DashboardActivity.this).getLatestFlapLog();
            } catch (final IOException e) {
                android.util.Log.e("DashboardActivity", e.getLocalizedMessage(), e);
                DashboardActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        syncText.setText(getString(R.string.settings_wrong_message, e.getLocalizedMessage()));
                        new AlertDialog.Builder(DashboardActivity.this)
                                .setTitle(getString(R.string.settings_wrong_title))
                                .setMessage(getString(R.string.settings_wrong_message, e.getLocalizedMessage()))
                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .setIcon(R.drawable.ic_warning_black_24dp)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);
            if(log.isValid()) {
                CharSequence timestamp = DateUtils.getRelativeDateTimeString(getApplicationContext(), log.getTimestamp().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
                syncText.setText(String.format(res.getString(R.string.synced), timestamp));

                insideTempText.setText(String.format(res.getString(R.string.temp), log.getTempIn()));
                insideHumidText.setText(String.format(res.getString(R.string.humid), log.getHumidIn()));
                outsideTempText.setText(String.format(res.getString(R.string.temp), log.getTempOut()));
                outsideHumidText.setText(String.format(res.getString(R.string.humid), log.getHumidOut()));

                webview.setBackgroundColor(0x00000000);
                String content =
                        "<html>\n" +
                                "  <head>\n" +
                                "    <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n" +
                                "    <script type=\"text/javascript\">\n" +
                                "      google.charts.load('current', {'packages':['bar']});\n" +
                                "      google.charts.setOnLoadCallback(drawStuff);\n" +
                                "\n" +
                                "      function drawStuff() {\n" +
                                "        var data = new google.visualization.arrayToDataTable([\n" +
                                "          ['Lampe', 'Prozent'],\n" +
                                "          ['1', " + log.getLamp1() + "],\n" +
                                "          ['2', " + log.getLamp2() + "],\n" +
                                "          ['3', " + log.getLamp3() + "],\n" +
                                "          ['4', " + log.getLamp4() + "],\n" +
                                "          ['5', " + log.getLamp5() + "],\n" +
                                "          ['6', " + log.getLamp6() + "]\n" +
                                "        ]);\n" +
                                "\n" +
                                "        var options = {\n" +
                                "          backgroundColor: { fill:'transparent' },\n" +
                                "          legend: { position: 'none' }, \n" +
                                "          animation: {" +
                                "            duration: 1000,\n" +
                                "            easing: 'linear',\n" +
                                "            startup: true}, \n" +
                                "          axes: {\n" +
                                "            x: {\n" +
                                "              0: { side: 'top', label: 'Lampe'} // Top x-axis.\n" +
                                "            }\n" +
                                "          },\n" +
                                "        };\n" +
                                "\n" +
                                "        var chart = new google.charts.Bar(document.getElementById('top_x_div'));\n" +
                                "        // Convert the Classic options to Material options.\n" +
                                "        chart.draw(data, google.charts.Bar.convertOptions(options));\n" +
                                "      };\n" +
                                "    </script>\n" +
                                "  </head>\n" +
                                "  <body>\n" +
                                "    <div id=\"top_x_div\"></div>\n" +
                                "  </body>\n" +
                                "</html>";

                WebSettings webSettings = webview.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webview.requestFocusFromTouch();
                webview.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);

                String lampsTextString = String.format(res.getString(R.string.lamp), log.getLux(), log.getLuxDimPercent());
                lampsText.setText(lampsTextString);
            }

            if(heaterLog.isValid()) {
                CharSequence heaterTime = DateUtils.formatSameDayTime(heaterLog.getTimestamp().getTime(), new Date().getTime(), SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
                String heaterString = heaterLog.isOn() ? res.getString(R.string.on) : res.getString(R.string.off);
                heaterText.setText(String.format(res.getString(R.string.heater), heaterString, heaterTime));
            }

            if(flapLog.isValid()) {
                String flapOpenString = flapLog.isOpen() ? res.getString(R.string.opened) : res.getString(R.string.closed);
                CharSequence flapTimeString = DateUtils.getRelativeDateTimeString(getApplicationContext(), flapLog.getTimestamp().getTime(), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0);
                String flapTextString = String.format(res.getString(R.string.flap), flapOpenString, flapTimeString);
                flapText.setText(flapTextString);
            }
        }
    }
}
