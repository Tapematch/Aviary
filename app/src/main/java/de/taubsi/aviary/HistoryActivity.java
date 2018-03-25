package de.taubsi.aviary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import de.taubsi.aviary.model.FlapLog;
import de.taubsi.aviary.model.HeaterLog;
import de.taubsi.aviary.model.Log;
import de.taubsi.aviary.service.FlapLogService;
import de.taubsi.aviary.service.HeaterLogService;
import de.taubsi.aviary.service.LogService;
import de.taubsi.aviary.util.FlapLogTimestampComparator;
import de.taubsi.aviary.util.HeaterLogTimestampComparator;

import java.io.IOException;
import java.util.*;

public class HistoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener  {

    NavigationView navigationView;

    SwipeRefreshLayout swipeRefreshLayout;

    WebView tempView;
    WebView humidView;
    WebView lampView;
    WebView lightView;
    WebView heaterView;
    WebView flapView;

    int minutes = 0;
    int hours = 0;
    int days = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_history);
        navigationView.setNavigationItemSelectedListener(this);

        tempView = (WebView) findViewById(R.id.history_temp);
        humidView = (WebView) findViewById(R.id.history_humid);
        lampView = (WebView) findViewById(R.id.history_lamps);
        lightView = (WebView) findViewById(R.id.history_light);
        heaterView = (WebView) findViewById(R.id.history_heater);
        flapView = (WebView) findViewById(R.id.history_flap);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            refresh(null);
                        }
                    }
            );
        }

        final EditText editText = (EditText) findViewById(R.id.editText);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 switch((String) parent.getItemAtPosition(position)){
                     case "Minuten":
                         minutes = Integer.parseInt(editText.getText().toString());
                         hours = 0;
                         days = 0;
                         break;
                     case "Stunden":
                         minutes = 0;
                         hours = Integer.parseInt(editText.getText().toString());
                         days = 0;
                         break;
                     case "Tage":
                         minutes = 0;
                         hours = 0;
                         days = Integer.parseInt(editText.getText().toString());
                         break;
                 }
                 refresh(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        refresh(null);
    }

    public void refresh(View view) {
        android.util.Log.v("### REFRESH", minutes + " - " + hours + " - " + days);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RefreshHistoryTask().execute();
        } else {
        }
    }

    @Override
    public void onRefresh() {
        refresh(null);
    }

    private class RefreshHistoryTask extends AsyncTask<Void, Void, Void> {
        Resources res = getResources();
        List<Log> logs = new ArrayList<>();
        List<HeaterLog> heaterLogs = new ArrayList<>();
        List<FlapLog> flapLogs = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                logs = new LogService(HistoryActivity.this).listLog(minutes, hours, days);
                heaterLogs = new HeaterLogService(HistoryActivity.this).listHeaterLog(minutes, hours, days);
                flapLogs = new FlapLogService(HistoryActivity.this).listFlapLog(minutes, hours, days);

                Collections.sort(heaterLogs, new HeaterLogTimestampComparator());
                Collections.sort(flapLogs, new FlapLogTimestampComparator());
            } catch (final IOException e) {
                android.util.Log.e("DashboardActivity", e.getLocalizedMessage(), e);
                HistoryActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        new AlertDialog.Builder(HistoryActivity.this)
                                .setTitle(getString(R.string.settings_wrong_title))
                                .setMessage(getString(R.string.settings_wrong_message, e.getLocalizedMessage()))
                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(HistoryActivity.this, SettingsActivity.class);
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            setTempView();
            setHumidView();
            setLampView();
            setLightView();
            setHeaterView();
            setFlapView();

            swipeRefreshLayout.setRefreshing(false);
        }

        private void setTempView() {
            tempView.setBackgroundColor(0x00000000);
            String content = "<html><p>keine Daten vorhanden</p></html>";
            if(logs.size()>0) {
                content =
                        "<html>" +
                                "<head>" +
                                "  <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                                "    <script type=\"text/javascript\">" +
                                "      google.charts.load('current', {'packages':['line']});" +
                                "      google.charts.setOnLoadCallback(drawChart);" +
                                "" +
                                "    function drawChart() {" +
                                "" +
                                "      var data = new google.visualization.DataTable();" +
                                "      data.addColumn('date', 'Zeit');" +
                                "      data.addColumn('number', 'Innen');" +
                                "      data.addColumn('number', 'Außen');" +
                                "" +
                                "      data.addRows([";

                for (Log log : logs) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(log.getTimestamp());
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int minute = cal.get(Calendar.MINUTE);
                    int seconds = cal.get(Calendar.SECOND);
                    int millisecond = cal.get(Calendar.MILLISECOND);
                    String data = "[new Date(" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", " + seconds + ", " + millisecond + "), " + log.getTempIn() + ", " + log.getTempOut() + "],";
                    content = content + data;
                }

                content = content.substring(0, content.length() - 1);
                content = content +
                        "      ]);" +
                        "" +
                        "      var options = {" +
                        "        backgroundColor: { fill:'transparent' }," +
                        "        axes: {" +
                        "          x: {" +
                        "            0: {side: 'top'}" +
                        "          }" +
                        "        }" +
                        "      };" +
                        "" +
                        "      var chart = new google.charts.Line(document.getElementById('line_top_x'));" +
                        "" +
                        "      chart.draw(data, options);" +
                        "    }" +
                        "  </script>" +
                        "</head>" +
                        "<body>" +
                        "  <div id=\"line_top_x\"></div>" +
                        "</body>" +
                        "</html>";
            }
            WebSettings webSettings = tempView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            tempView.requestFocusFromTouch();
            tempView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        }

        private void setHumidView() {
            humidView.setBackgroundColor(0x00000000);

            String content = "<html><p>keine Daten vorhanden</p></html>";
            if(logs.size()>0) {
                content =
                        "<html>" +
                                "<head>" +
                                "  <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                                "    <script type=\"text/javascript\">" +
                                "      google.charts.load('current', {'packages':['line']});" +
                                "      google.charts.setOnLoadCallback(drawChart);" +
                                "" +
                                "    function drawChart() {" +
                                "" +
                                "      var data = new google.visualization.DataTable();" +
                                "      data.addColumn('date', 'Zeit');" +
                                "      data.addColumn('number', 'Innen');" +
                                "      data.addColumn('number', 'Außen');" +
                                "" +
                                "      data.addRows([";

                for (Log log : logs) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(log.getTimestamp());
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int minute = cal.get(Calendar.MINUTE);
                    int seconds = cal.get(Calendar.SECOND);
                    int millisecond = cal.get(Calendar.MILLISECOND);
                    String data = "[new Date(" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", " + seconds + ", " + millisecond + "), " + log.getHumidIn() + ", " + log.getHumidOut() + "],";
                    content = content + data;
                }

                content = content.substring(0, content.length() - 1);
                content = content +
                        "      ]);" +
                        "" +
                        "      var options = {" +
                        "        backgroundColor: { fill:'transparent' }," +
                        "        axes: {" +
                        "          x: {" +
                        "            0: {side: 'top'}" +
                        "          }" +
                        "        }" +
                        "      };" +
                        "" +
                        "      var chart = new google.charts.Line(document.getElementById('line_top_x'));" +
                        "" +
                        "      chart.draw(data, options);" +
                        "    }" +
                        "  </script>" +
                        "</head>" +
                        "<body>" +
                        "  <div id=\"line_top_x\"></div>" +
                        "</body>" +
                        "</html>";
            }
            WebSettings webSettings = humidView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            humidView.requestFocusFromTouch();
            humidView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        }

        private void setLampView() {
            lampView.setBackgroundColor(0x00000000);
            String content = "<html><p>keine Daten vorhanden</p></html>";
            if(logs.size()>0) {
                content =
                        "<html>" +
                                "<head>" +
                                "  <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                                "    <script type=\"text/javascript\">" +
                                "      google.charts.load('current', {'packages':['line']});" +
                                "      google.charts.setOnLoadCallback(drawChart);" +
                                "" +
                                "    function drawChart() {" +
                                "" +
                                "      var data = new google.visualization.DataTable();" +
                                "      data.addColumn('date', 'Zeit');" +
                                "      data.addColumn('number', '1');" +
                                "      data.addColumn('number', '2');" +
                                "      data.addColumn('number', '3');" +
                                "      data.addColumn('number', '4');" +
                                "      data.addColumn('number', '5');" +
                                "      data.addColumn('number', '6');" +
                                "" +
                                "      data.addRows([";

                for (Log log : logs) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(log.getTimestamp());
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int minute = cal.get(Calendar.MINUTE);
                    int seconds = cal.get(Calendar.SECOND);
                    int millisecond = cal.get(Calendar.MILLISECOND);
                    String data = "[new Date(" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", " + seconds + ", " + millisecond + "), " + log.getLamp1() + ", " + log.getLamp2() + ", " + log.getLamp3() + ", " + log.getLamp4() + ", " + log.getLamp5() + ", " + log.getLamp6() + "],";
                    content = content + data;
                }

                content = content.substring(0, content.length() - 1);
                content = content +
                        "      ]);" +
                        "" +
                        "      var options = {" +
                        "        backgroundColor: { fill:'transparent' }," +
                        "        axes: {" +
                        "          x: {" +
                        "            0: {side: 'top'}" +
                        "          }" +
                        "        }" +
                        "      };" +
                        "" +
                        "      var chart = new google.charts.Line(document.getElementById('line_top_x'));" +
                        "" +
                        "      chart.draw(data, options);" +
                        "    }" +
                        "  </script>" +
                        "</head>" +
                        "<body>" +
                        "  <div id=\"line_top_x\"></div>" +
                        "</body>" +
                        "</html>";
            }
            WebSettings webSettings = lampView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            lampView.requestFocusFromTouch();
            lampView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        }

        private void setLightView() {
            lightView.setBackgroundColor(0x00000000);
            String content = "<html><p>keine Daten vorhanden</p></html>";
            if(logs.size()>0) {
                content =
                        "<html>" +
                                "<head>" +
                                "  <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                                "    <script type=\"text/javascript\">" +
                                "      google.charts.load('current', {'packages':['line']});" +
                                "      google.charts.setOnLoadCallback(drawChart);" +
                                "" +
                                "    function drawChart() {" +
                                "" +
                                "      var data = new google.visualization.DataTable();" +
                                "      data.addColumn('date', 'Zeit');" +
                                "      data.addColumn('number', 'Helligkeit');" +
                                "      data.addColumn('number', 'Anpassung');" +
                                "" +
                                "      data.addRows([";

                for (Log log : logs) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(log.getTimestamp());
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int hour = cal.get(Calendar.HOUR_OF_DAY);
                    int minute = cal.get(Calendar.MINUTE);
                    int seconds = cal.get(Calendar.SECOND);
                    int millisecond = cal.get(Calendar.MILLISECOND);
                    String data = "[new Date(" + year + ", " + month + ", " + day + ", " + hour + ", " + minute + ", " + seconds + ", " + millisecond + "), " + log.getLux() + ", " + log.getLuxDimPercent() + "],";
                    content = content + data;
                }

                content = content.substring(0, content.length() - 1);
                content = content +
                        "      ]);" +
                        "" +
                        "      var options = {" +
                        "        backgroundColor: { fill:'transparent' }," +
                        "        series: {\n" +
                        "          // Gives each series an axis name that matches the Y-axis below.\n" +
                        "          0: {axis: 'Helligkeit'},\n" +
                        "          1: {axis: 'Anpassung'}\n" +
                        "        }," +
                        "        axes: {" +
                        "          x: {" +
                        "            0: {side: 'top'}" +
                        "          }" +
                        "        }" +
                        "      };" +
                        "" +
                        "      var chart = new google.charts.Line(document.getElementById('line_top_x'));" +
                        "" +
                        "      chart.draw(data, options);" +
                        "    }" +
                        "  </script>" +
                        "</head>" +
                        "<body>" +
                        "  <div id=\"line_top_x\"></div>" +
                        "</body>" +
                        "</html>";
            }
            WebSettings webSettings = lightView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            lightView.requestFocusFromTouch();
            lightView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        }

        private void setHeaterView() {
            heaterView.setBackgroundColor(0x00000000);
            String content = "<html><p>keine Daten vorhanden</p></html>";
            if(heaterLogs.size()>0) {
                content =
                        "<html>" +
                                "<head>" +
                                "  <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                                "    <script type=\"text/javascript\">" +
                                "     google.charts.load('current', {'packages':['timeline']});\n" +
                                "      google.charts.setOnLoadCallback(drawChart);" +
                                "      function drawChart() {" +
                                "        var container = document.getElementById('timeline');" +
                                "        var chart = new google.visualization.Timeline(container);" +
                                "        var dataTable = new google.visualization.DataTable();" +
                                "" +
                                "        dataTable.addColumn({ type: 'string', id: 'Heater' });" +
                                "    dataTable.addColumn({ type: 'string', id: 'State' });" +
                                "        dataTable.addColumn({ type: 'date', id: 'Start' });" +
                                "        dataTable.addColumn({ type: 'date', id: 'End' });" +
                                "        dataTable.addRows([";

                for (int i = 0; i < heaterLogs.size(); i++) {
                    HeaterLog startLog = heaterLogs.get(i);

                    Calendar startcal = Calendar.getInstance();
                    startcal.setTime(startLog.getTimestamp());
                    int startyear = startcal.get(Calendar.YEAR);
                    int startmonth = startcal.get(Calendar.MONTH);
                    int startday = startcal.get(Calendar.DAY_OF_MONTH);
                    int starthour = startcal.get(Calendar.HOUR_OF_DAY);
                    int startminute = startcal.get(Calendar.MINUTE);
                    int startseconds = startcal.get(Calendar.SECOND);
                    int startmillisecond = startcal.get(Calendar.MILLISECOND);
                    String state = startLog.isOn() ? "An" : "Aus";

                    Calendar endcal = Calendar.getInstance();
                    if (heaterLogs.size() > i + 1)
                        endcal.setTime(heaterLogs.get(i + 1).getTimestamp());
                    else
                        endcal.setTime(new Date());

                    int endyear = endcal.get(Calendar.YEAR);
                    int endmonth = endcal.get(Calendar.MONTH);
                    int endday = endcal.get(Calendar.DAY_OF_MONTH);
                    int endhour = endcal.get(Calendar.HOUR_OF_DAY);
                    int endminute = endcal.get(Calendar.MINUTE);
                    int endseconds = endcal.get(Calendar.SECOND);
                    int endmillisecond = endcal.get(Calendar.MILLISECOND);

                    String data = "['Heater', '" + state + "', new Date(" + startyear + ", " + startmonth + ", " + startday + ", " + starthour + ", " + startminute + ", " + startseconds + ", " + startmillisecond + "), new Date(" + endyear + ", " + endmonth + ", " + endday + ", " + endhour + ", " + endminute + ", " + endseconds + ", " + endmillisecond + ")],";
                    android.util.Log.v("### DATA", data);
                    content = content + data;
                }

                content = content.substring(0, content.length() - 1);
                content = content +
                        "          ]);" +
                        "" +
                        "var options = {\n" +
                        "      timeline: { showRowLabels: false },\n" +
                        "      height: 100" +
                        "    };" +
                        "    " +
                        "        chart.draw(dataTable, options);" +
                        "      } " +
                        "  </script>" +
                        "</head>" +
                        "<body>" +
                        "  <div id=\"timeline\"></div>" +
                        "</body>" +
                        "</html>";
            }
            WebSettings webSettings = heaterView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            heaterView.requestFocusFromTouch();
            heaterView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        }

        private void setFlapView() {
            flapView.setBackgroundColor(0x00000000);
            String content = "<html><p>keine Daten vorhanden</p></html>";
            if(flapLogs.size()>0) {
                content =
                        "<html>" +
                                "<head>" +
                                "  <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>" +
                                "    <script type=\"text/javascript\">" +
                                "     google.charts.load('current', {'packages':['timeline']});\n" +
                                "      google.charts.setOnLoadCallback(drawChart);" +
                                "      function drawChart() {" +
                                "        var container = document.getElementById('timeline');" +
                                "        var chart = new google.visualization.Timeline(container);" +
                                "        var dataTable = new google.visualization.DataTable();" +
                                "" +
                                "        dataTable.addColumn({ type: 'string', id: 'Flap' });" +
                                "    dataTable.addColumn({ type: 'string', id: 'State' });" +
                                "        dataTable.addColumn({ type: 'date', id: 'Start' });" +
                                "        dataTable.addColumn({ type: 'date', id: 'End' });" +
                                "        dataTable.addRows([";

                for (int i = 0; i < flapLogs.size(); i++) {
                    FlapLog startLog = flapLogs.get(i);

                    Calendar startcal = Calendar.getInstance();
                    startcal.setTime(startLog.getTimestamp());
                    int startyear = startcal.get(Calendar.YEAR);
                    int startmonth = startcal.get(Calendar.MONTH);
                    int startday = startcal.get(Calendar.DAY_OF_MONTH);
                    int starthour = startcal.get(Calendar.HOUR_OF_DAY);
                    int startminute = startcal.get(Calendar.MINUTE);
                    int startseconds = startcal.get(Calendar.SECOND);
                    int startmillisecond = startcal.get(Calendar.MILLISECOND);
                    String state = startLog.isOpen() ? "Offen" : "Geschlossen";

                    Calendar endcal = Calendar.getInstance();
                    if (flapLogs.size() > i + 1)
                        endcal.setTime(flapLogs.get(i + 1).getTimestamp());
                    else
                        endcal.setTime(new Date());

                    int endyear = endcal.get(Calendar.YEAR);
                    int endmonth = endcal.get(Calendar.MONTH);
                    int endday = endcal.get(Calendar.DAY_OF_MONTH);
                    int endhour = endcal.get(Calendar.HOUR_OF_DAY);
                    int endminute = endcal.get(Calendar.MINUTE);
                    int endseconds = endcal.get(Calendar.SECOND);
                    int endmillisecond = endcal.get(Calendar.MILLISECOND);

                    String data = "['Flap', '" + state + "', new Date(" + startyear + ", " + startmonth + ", " + startday + ", " + starthour + ", " + startminute + ", " + startseconds + ", " + startmillisecond + "), new Date(" + endyear + ", " + endmonth + ", " + endday + ", " + endhour + ", " + endminute + ", " + endseconds + ", " + endmillisecond + ")],";
                    android.util.Log.v("### DATA", data);
                    content = content + data;
                }

                content = content.substring(0, content.length() - 1);
                content = content +
                        "          ]);" +
                        "" +
                        "var options = {\n" +
                        "      timeline: { showRowLabels: false },\n" +
                        "      height: 100" +
                        "    };" +
                        "    " +
                        "        chart.draw(dataTable, options);" +
                        "      } " +
                        "  </script>" +
                        "</head>" +
                        "<body>" +
                        "  <div id=\"timeline\"></div>" +
                        "</body>" +
                        "</html>";
            }
            WebSettings webSettings = flapView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            flapView.requestFocusFromTouch();
            flapView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(navigationView != null)
            navigationView.setCheckedItem(R.id.nav_history);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        Intent intent = null;
        switch (id) {
            case R.id.nav_dashboard:
                intent = new Intent(getApplicationContext(), DashboardActivity.class);
                break;
            case R.id.nav_camera:
                intent = new Intent(getApplicationContext(), CameraActivity.class);
                break;
            case R.id.nav_history:
                //intent = new Intent(getApplicationContext(), HistoryActivity.class);
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
}
