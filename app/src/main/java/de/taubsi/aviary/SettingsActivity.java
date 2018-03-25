package de.taubsi.aviary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String KEY_PREF_API_URL = "api_url_setting";
    public static final String KEY_PREF_API_KEY = "api_key_setting";
    public static final String KEY_PREF_FLAP_NOTIFICATION = "flap_notification_setting";
    public static final String KEY_PREF_FLAP_AUTO_NOTIFICATION = "flap_auto_notification_setting";

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_lampconfig);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(navigationView != null)
            navigationView.setCheckedItem(R.id.nav_settings);
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
                intent = new Intent(getApplicationContext(), ConfigurationActivity.class);
                break;
            case R.id.nav_settings:
                //intent = new Intent(getApplicationContext(), SettingsActivity.class);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if (intent != null)
            startActivity(intent);
        return true;
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.v(" ## key", key);
            Log.v(" ## KEY_PREF_FLAP", SettingsActivity.KEY_PREF_FLAP_NOTIFICATION);
            Log.v(" ## KEY_PREF_FLAP_AUTO", key);
            if(key.equals(SettingsActivity.KEY_PREF_FLAP_NOTIFICATION) || key.equals(SettingsActivity.KEY_PREF_FLAP_AUTO_NOTIFICATION)) {
                boolean flapSetting = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_FLAP_NOTIFICATION, false);
                boolean flapAutoSetting = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_FLAP_AUTO_NOTIFICATION, false);
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
        }
    }
}
