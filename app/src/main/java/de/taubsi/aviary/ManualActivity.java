package de.taubsi.aviary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.taubsi.aviary.model.FlapLog;
import de.taubsi.aviary.model.HeaterLog;
import de.taubsi.aviary.model.Log;
import de.taubsi.aviary.service.FlapLogService;
import de.taubsi.aviary.service.HeaterLogService;
import de.taubsi.aviary.service.LogService;
import de.taubsi.aviary.service.ManualService;
import de.taubsi.aviary.util.OnLampSeekbarChangeListener;

import java.io.IOException;

public class ManualActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Switch switch1;
    boolean manual = false;

    NavigationView navigationView;
    ScrollView contentLayout;

    SeekBar lamp1SeekBar;
    SeekBar lamp2SeekBar;
    SeekBar lamp3SeekBar;
    SeekBar lamp4SeekBar;
    SeekBar lamp5SeekBar;
    SeekBar lamp6SeekBar;

    TextView lamp1Text;
    TextView lamp2Text;
    TextView lamp3Text;
    TextView lamp4Text;
    TextView lamp5Text;
    TextView lamp6Text;

    Switch heaterSwitch;
    ToggleButton flapButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_manual);
        navigationView.setNavigationItemSelectedListener(this);

        contentLayout = (ScrollView) findViewById(R.id.manual_layout);

        disableEnableControls(false, contentLayout);
        refresh(null);

        switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new AlertDialog.Builder(ManualActivity.this)
                            .setTitle(getString(R.string.activate))
                            .setMessage(getString(R.string.sureactivate))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    disableEnableControls(true, contentLayout);
                                    new SwitchManualTask().execute(true);
                                    manual = true;
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch1.setChecked(false);
                                    disableEnableControls(false, contentLayout);
                                }
                            })
                            .setIcon(R.drawable.ic_warning_black_24dp)
                            .show();
                } else {
                    disableEnableControls(false, contentLayout);
                    new SwitchManualTask().execute(false);
                    manual = false;
                    refresh(null);
                }
            }
        });

        lamp1Text = (TextView) findViewById(R.id.lamp1_text);
        lamp1SeekBar = (SeekBar) findViewById(R.id.lamp1_seekBar);
        lamp1SeekBar.setOnSeekBarChangeListener(new OnLampSeekbarChangeListener(1, lamp1Text, ManualActivity.this));

        lamp2Text = (TextView) findViewById(R.id.lamp2_text);
        lamp2SeekBar = (SeekBar) findViewById(R.id.lamp2_seekBar);
        lamp2SeekBar.setOnSeekBarChangeListener(new OnLampSeekbarChangeListener(2, lamp2Text, ManualActivity.this));

        lamp3Text = (TextView) findViewById(R.id.lamp3_text);
        lamp3SeekBar = (SeekBar) findViewById(R.id.lamp3_seekBar);
        lamp3SeekBar.setOnSeekBarChangeListener(new OnLampSeekbarChangeListener(3, lamp3Text, ManualActivity.this));

        lamp4Text = (TextView) findViewById(R.id.lamp4_text);
        lamp4SeekBar = (SeekBar) findViewById(R.id.lamp4_seekBar);
        lamp4SeekBar.setOnSeekBarChangeListener(new OnLampSeekbarChangeListener(4, lamp4Text, ManualActivity.this));

        lamp5Text = (TextView) findViewById(R.id.lamp5_text);
        lamp5SeekBar = (SeekBar) findViewById(R.id.lamp5_seekBar);
        lamp5SeekBar.setOnSeekBarChangeListener(new OnLampSeekbarChangeListener(5, lamp5Text, ManualActivity.this));

        lamp6Text = (TextView) findViewById(R.id.lamp6_text);
        lamp6SeekBar = (SeekBar) findViewById(R.id.lamp6_seekBar);
        lamp6SeekBar.setOnSeekBarChangeListener(new OnLampSeekbarChangeListener(6, lamp6Text, ManualActivity.this));

        heaterSwitch = (Switch) findViewById(R.id.heater_switch);
        heaterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (manual) {
                    heaterSwitch.setEnabled(false);
                    heaterSwitch.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            heaterSwitch.setEnabled(true);
                        }
                    }, 2000);
                    new SetHeaterTask().execute(isChecked);
                }
            }
        });

        flapButton = (ToggleButton) findViewById(R.id.flap_button);
        flapButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (manual) {
                    flapButton.setEnabled(false);
                    flapButton.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flapButton.setEnabled(true);
                        }
                    }, 30000);
                    new SetFlapTask().execute(isChecked);
                }
            }
        });

    }

    private void disableEnableControls(boolean enable, ViewGroup vg) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                disableEnableControls(enable, (ViewGroup) child);
            }
        }
    }

    @Override
    protected void onStop() {
        new SwitchManualTask().execute(false);
        manual = false;
        super.onStop();
    }

    public void refresh(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RefreshActuatorsTask().execute();
        } else {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationView != null)
            navigationView.setCheckedItem(R.id.nav_manual);
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
                intent = new Intent(getApplicationContext(), HistoryActivity.class);
                break;
            case R.id.nav_manual:
                //intent = new Intent(getApplicationContext(), ManualActivity.class);
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

    public void showSettingsAlert(final String s) {
        runOnUiThread(new Runnable() {
            public void run() {
                new AlertDialog.Builder(ManualActivity.this)
                        .setTitle(getString(R.string.settings_wrong_title))
                        .setMessage(getString(R.string.settings_wrong_message, s))
                        .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ManualActivity.this, SettingsActivity.class);
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

    private class SwitchManualTask extends AsyncTask<Boolean, Void, String> {

        @Override
        protected String doInBackground(Boolean... on) {
            android.util.Log.v("SwitchManualTask", on[0].toString());
            try {
                return new ManualService(ManualActivity.this).toggle(on[0]);
            } catch (IOException e) {
                return e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            if (s != null) {
                ManualActivity.this.showSettingsAlert(s);
            }
        }
    }

    private class SetHeaterTask extends AsyncTask<Boolean, Void, String> {

        @Override
        protected String doInBackground(Boolean... params) {
            android.util.Log.v("SetHeaterTask", params[0].toString());
            try {
                return new ManualService(ManualActivity.this).setHeater(params[0]);
            } catch (IOException e) {
                return e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            if (s != null) {
                ManualActivity.this.showSettingsAlert(s);
            }
        }
    }

    private class SetFlapTask extends AsyncTask<Boolean, Void, String> {

        @Override
        protected String doInBackground(Boolean... params) {
            try {
                return new ManualService(ManualActivity.this).setFlap(params[0]);
            } catch (IOException e) {
                return e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(final String s) {
            super.onPostExecute(s);
            if (s != null) {
                ManualActivity.this.showSettingsAlert(s);
            }
        }
    }

    private class RefreshActuatorsTask extends AsyncTask<Void, Void, Void> {

        Log log = new Log();
        HeaterLog heaterLog = new HeaterLog();
        FlapLog flapLog = new FlapLog();

        @Override
        protected Void doInBackground(Void... params) {
            try {
                log = new LogService(getApplicationContext()).getLatestLog();
                heaterLog = new HeaterLogService(getApplicationContext()).getLatestHeaterLog();
                flapLog = new FlapLogService(getApplicationContext()).getLatestFlapLog();
            } catch (final IOException e) {
                android.util.Log.e("DashboardActivity", e.getLocalizedMessage(), e);
                ManualActivity.this.showSettingsAlert(e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            lamp1SeekBar.setProgress(log.getLamp1());
            lamp2SeekBar.setProgress(log.getLamp2());
            lamp3SeekBar.setProgress(log.getLamp3());
            lamp4SeekBar.setProgress(log.getLamp4());
            lamp5SeekBar.setProgress(log.getLamp5());
            lamp6SeekBar.setProgress(log.getLamp5());

            heaterSwitch.setChecked(heaterLog.isOn());
            flapButton.setChecked(flapLog.isOpen());


        }
    }

    public boolean isManual() {
        return manual;
    }
}
