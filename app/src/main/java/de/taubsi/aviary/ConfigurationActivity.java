package de.taubsi.aviary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.taubsi.aviary.service.ConfigurationService;
import de.taubsi.aviary.util.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfigurationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    NavigationView navigationView;
    private ListView listView;
    private List<de.taubsi.aviary.model.Configuration> configurationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_config);
        navigationView.setNavigationItemSelectedListener(this);

        listView = (ListView) findViewById(R.id.config_list);
        listView.setOnItemClickListener(new OnItemClickListener());

        refresh();
    }

    private class OnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            de.taubsi.aviary.model.Configuration configuration = null;
            for (de.taubsi.aviary.model.Configuration c : configurationList) {
                if (c.getId() == view.getId())
                    configuration = c;
            }
            if (configuration != null) {
                final de.taubsi.aviary.model.Configuration finalConfiguration = configuration;

                if (configuration.getId() == 4 || configuration.getId() == 5) {
                    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            finalConfiguration.setValue(TimeUtil.getTime(hourOfDay, minute));
                            new SaveConfigurationTask().execute(finalConfiguration);
                        }
                    };

                    int time = (int) configuration.getValue();
                    TimePickerDialog dialog = new TimePickerDialog(ConfigurationActivity.this, onTimeSetListener, TimeUtil.getHours(time), TimeUtil.getMinutes(time), true);
                    dialog.show();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(ConfigurationActivity.this);
                    alert.setTitle(configuration.getConfigurationTitle(ConfigurationActivity.this));

                    LayoutInflater inflater = ConfigurationActivity.this.getLayoutInflater();
                    View layout = inflater.inflate(R.layout.decimal_number_dialog, null);
                    alert.setView(layout);

                    final EditText dialogEditText = (EditText) layout.findViewById(R.id.dialog_edit_text);
                    final TextView dialogTextView = (TextView) layout.findViewById(R.id.dialog_text_view);

                    dialogEditText.setTextLocale(Locale.GERMANY);
                    dialogEditText.setText(String.format(Locale.GERMAN, "%s", configuration.getValue()));
                    if (configuration.getId() >= 6)
                        dialogTextView.setText("Lux");
                    else
                        dialogTextView.setText("°C");

                    alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String result = dialogEditText.getText().toString();
                            finalConfiguration.setValue(Float.parseFloat(result));
                            new SaveConfigurationTask().execute(finalConfiguration);
                        }
                    });

                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    alert.show();
                }
            }
        }
    }

    private void refresh() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new RefreshConfigurationListTask().execute();
        } else {
            Log.v("ConfigurationActivity", "keine Internetverbindung");
        }
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
    protected void onResume() {
        super.onResume();
        if(navigationView != null)
            navigationView.setCheckedItem(R.id.nav_config);
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
                intent = new Intent(getApplicationContext(), ManualActivity.class);
                break;
            case R.id.nav_lampconfig:
                intent = new Intent(getApplicationContext(), LampConfigurationActivity.class);
                break;
            case R.id.nav_config:
                //intent = new Intent(getApplicationContext(), ConfigurationActivity.class);
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

    private class RefreshConfigurationListTask extends AsyncTask<Void, Void, List<de.taubsi.aviary.model.Configuration>> {
        ProgressDialog progress = new ProgressDialog(ConfigurationActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Resources res = getResources();
            progress.setTitle(res.getString(R.string.load_title));
            progress.setMessage(res.getString(R.string.load_description));
            progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
            progress.show();
        }

        @Override
        protected List<de.taubsi.aviary.model.Configuration> doInBackground(Void... params) {
            try {
                ConfigurationService configurationService = new ConfigurationService(ConfigurationActivity.this);
                return configurationService.getConfigurations();
            } catch (final IOException e) {
                ConfigurationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        new AlertDialog.Builder(ConfigurationActivity.this)
                                .setTitle(getString(R.string.settings_wrong_title))
                                .setMessage(getString(R.string.settings_wrong_message, e.getLocalizedMessage()))
                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(ConfigurationActivity.this, SettingsActivity.class);
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
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(final List<de.taubsi.aviary.model.Configuration> configurations) {
            super.onPostExecute(configurations);
            configurationList = configurations;
            ArrayAdapter<de.taubsi.aviary.model.Configuration> adapter = new ArrayAdapter<de.taubsi.aviary.model.Configuration>(ConfigurationActivity.this, R.layout.configuration_listitem, R.id.text1, configurations) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(R.id.text1);
                    TextView text2 = (TextView) view.findViewById(R.id.text2);

                    text1.setText(configurations.get(position).getConfigurationTitle(getApplicationContext()));
                    text2.setText(configurations.get(position).getConfigurationDescription(getApplicationContext()));
                    view.setId(configurations.get(position).getId());
                    return view;
                }
            };

            listView.setAdapter(adapter);
            progress.dismiss();
        }
    }

    private class SaveConfigurationTask extends AsyncTask<de.taubsi.aviary.model.Configuration, Void, String> {

        @Override
        protected String doInBackground(de.taubsi.aviary.model.Configuration... configuration) {
            try {
                ConfigurationService configurationService = new ConfigurationService(ConfigurationActivity.this);
                return configurationService.updateConfiguration(configuration[0]);
            } catch (Exception e) {
                return e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(final String success) {
            super.onPostExecute(success);

            refresh();

            if (success==null) {
                Toast.makeText(ConfigurationActivity.this, getText(R.string.saved), Toast.LENGTH_SHORT).show();
            } else {
                Log.e("ConfigurationActivity", success);
                ConfigurationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        new AlertDialog.Builder(ConfigurationActivity.this)
                                .setTitle(getString(R.string.settings_wrong_title))
                                .setMessage(getString(R.string.settings_wrong_message, success))
                                .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(ConfigurationActivity.this, SettingsActivity.class);
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
        }
    }
}
