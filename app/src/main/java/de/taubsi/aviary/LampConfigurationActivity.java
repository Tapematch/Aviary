package de.taubsi.aviary;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;
import de.taubsi.aviary.model.FullLampConfiguration;
import de.taubsi.aviary.model.LampConfiguration;
import de.taubsi.aviary.service.LampConfigurationService;
import de.taubsi.aviary.util.FullLampConfigurationTimeComparator;
import de.taubsi.aviary.util.LampConfigurationTimeComparator;
import de.taubsi.aviary.util.TimeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LampConfigurationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lamp_configuration);

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

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(navigationView != null)
            navigationView.setCheckedItem(R.id.nav_lampconfig);
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
                //intent = new Intent(getApplicationContext(), LampConfigurationActivity.class);
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0)
                return LampDashboardFragment.newInstance();
            else
                return LampConfigurationFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 7;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Resources res = getResources();
            if(position == 0)
                return res.getString(R.string.dashboard);
            else
                return String.format(getResources().getString(R.string.lamp_number), position);
        }
    }

    public static class LampDashboardFragment extends Fragment {

        WebView webview;

        public static LampDashboardFragment newInstance() {
            return new LampDashboardFragment();
        }

        public LampDashboardFragment() {
        }

        @Override
        public void onResume() {
            super.onResume();
            new RefreshLampDashboardTask().execute();
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.lamp_dashboard_fragment, container, false);
            webview = (WebView) view.findViewById(R.id.lamp_line_graphs);
            return view;
        }

        private class RefreshLampDashboardTask extends AsyncTask<Void, Void, List<FullLampConfiguration>> {

            @Override
            protected List<FullLampConfiguration> doInBackground(Void... params) {
                try {
                    LampConfigurationService lampConfigurationService = new LampConfigurationService(getContext());
                    List<FullLampConfiguration> lampConfigurations = lampConfigurationService.getFullLampConfigurations();
                    Collections.sort(lampConfigurations, new FullLampConfigurationTimeComparator());
                    return lampConfigurations;
                } catch (IOException e) {
                    android.util.Log.e("LampConfigurationAct", e.getLocalizedMessage(), e);
                }
                return new ArrayList<>();
            }

            @Override
            protected void onPostExecute(List<FullLampConfiguration> fullLampConfigurations) {
                super.onPostExecute(fullLampConfigurations);

                webview.setBackgroundColor(0x00000000);
                String content =
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
                                "      data.addColumn('timeofday', 'Tageszeit');" +
                                "      data.addColumn('number', '1');" +
                                "      data.addColumn('number', '2');" +
                                "      data.addColumn('number', '3');" +
                                "      data.addColumn('number', '4');" +
                                "      data.addColumn('number', '5');" +
                                "      data.addColumn('number', '6');" +
                                "" +
                                "      data.addRows([";

                for(FullLampConfiguration fullLampConfiguration : fullLampConfigurations){
                    int time = fullLampConfiguration.getTime();
                    String data = "[[" + TimeUtil.getHours(time) + ", " + TimeUtil.getMinutes(time) + ", 0], " + fullLampConfiguration.getLamp1() + ", "  + fullLampConfiguration.getLamp2() + ", "  + fullLampConfiguration.getLamp3() + ", "  + fullLampConfiguration.getLamp4() + ", "  + fullLampConfiguration.getLamp5() + ", "  + fullLampConfiguration.getLamp6() + "],";
                    Log.v("### DATA", data);
                    content = content + data;
                }

                content = content.substring(0, content.length()-1);
                content = content +
                                "      ]);" +
                                "" +
                                "      var options = {" +
                                "        backgroundColor: { fill:'transparent' }," +
                                "        height: 250," +
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

                WebSettings webSettings = webview.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webview.requestFocusFromTouch();
                webview.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);
            }
        }
    }

    public static class LampConfigurationFragment extends Fragment {

        private ListView listView;
        private static final String ARG_LAMP_NUMBER = "lamp_number";

        public static LampConfigurationFragment newInstance(int lampNumber) {
            LampConfigurationFragment fragment = new LampConfigurationFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_LAMP_NUMBER, lampNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public LampConfigurationFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final int lampNumber = getArguments().getInt(ARG_LAMP_NUMBER);
            View rootView = inflater.inflate(R.layout.lamp_configuration_fragment, container, false);
            listView = (ListView) rootView.findViewById(R.id.lamp_config_list);

            FloatingActionButton floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.fab);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LampConfiguration newLampConfiguration = new LampConfiguration();
                    newLampConfiguration.setNumber(lampNumber);
                    saveLampConfiguration(newLampConfiguration);
                }
            });

            new RefreshLampConfigurationTask().execute(lampNumber);
            return rootView;
        }

        private class RefreshLampConfigurationTask extends AsyncTask<Integer, Void, List<LampConfiguration>> {

            @Override
            protected List<LampConfiguration> doInBackground(Integer... lampNumber) {
                try {
                    LampConfigurationService lampConfigurationService = new LampConfigurationService(getContext());
                    List<LampConfiguration> lampConfigurations = lampConfigurationService.getLampConfigurations(lampNumber[0]);
                    Collections.sort(lampConfigurations, new LampConfigurationTimeComparator());
                    return lampConfigurations;
                } catch (final IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.settings_wrong_title))
                                    .setMessage(getString(R.string.settings_wrong_message, e.getLocalizedMessage()))
                                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getActivity(), SettingsActivity.class);
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
            protected void onPostExecute(final List<LampConfiguration> lampConfigurations) {
                super.onPostExecute(lampConfigurations);

                ArrayAdapter<LampConfiguration> adapter = new ArrayAdapter<LampConfiguration>(getActivity(), R.layout.lamp_configuration_listitem, R.id.time_text, lampConfigurations) {
                    @Override
                    public View getView(final int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        TextView timeText = (TextView) view.findViewById(R.id.time_text);
                        TextView percentText = (TextView) view.findViewById(R.id.percent_text);

                        int time = lampConfigurations.get(position).getTime();
                        timeText.setText(getResources().getString(R.string.time, TimeUtil.getHours(time), TimeUtil.getMinutes(time)));
                        percentText.setText(getResources().getString(R.string.percent, lampConfigurations.get(position).getValue()));

                        ImageButton editButton = (ImageButton) view.findViewById(R.id.edit_button);
                        editButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveLampConfiguration(lampConfigurations.get(position));
                            }
                        });

                        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
                        deleteButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new DeleteLampConfigurationTask().execute(lampConfigurations.get(position));
                            }
                        });

                        return view;
                    }
                };

                listView.setAdapter(adapter);
            }
        }

        private void saveLampConfiguration(final LampConfiguration lampConfiguration) {

            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    lampConfiguration.setTime(TimeUtil.getTime(hourOfDay, minute));

                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setTitle(getResources().getString(R.string.brightness));

                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View layout = inflater.inflate(R.layout.decimal_number_dialog, null);
                    alert.setView(layout);

                    final EditText dialogEditText = (EditText) layout.findViewById(R.id.dialog_edit_text);
                    final TextView dialogTextView = (TextView) layout.findViewById(R.id.dialog_text_view);

                    dialogEditText.setTextLocale(Locale.GERMANY);
                    dialogEditText.setText(Integer.toString(lampConfiguration.getValue()));

                    dialogTextView.setText("%");

                    alert.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String result = dialogEditText.getText().toString();
                            lampConfiguration.setValue(Integer.parseInt(result));
                            new SaveLampConfigurationTask().execute(lampConfiguration);
                        }
                    });

                    alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

                    alert.show();

                }
            };

            int time = lampConfiguration.getTime();
            TimePickerDialog dialog = new TimePickerDialog(getActivity(), onTimeSetListener, TimeUtil.getHours(time), TimeUtil.getMinutes(time), true);
            dialog.show();
        }

        private class SaveLampConfigurationTask extends AsyncTask<LampConfiguration, Void, String> {

            int lampNumber;

            @Override
            protected String doInBackground(LampConfiguration... lampConfiguration) {
                lampNumber = lampConfiguration[0].getNumber();
                try {
                    LampConfigurationService lampConfigurationService = new LampConfigurationService(getContext());
                    return lampConfigurationService.saveLampConfiguration(lampConfiguration[0]);
                } catch (IOException e) {
                    return e.getLocalizedMessage();
                }
            }

            @Override
            protected void onPostExecute(final String success) {
                super.onPostExecute(success);

                new RefreshLampConfigurationTask().execute(lampNumber);

                if (success==null) {
                    Toast.makeText(getActivity(), getText(R.string.saved), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ConfigurationActivity", "Fehler beim Speichern!");
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.settings_wrong_title))
                                    .setMessage(getString(R.string.settings_wrong_message, success))
                                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getActivity(), SettingsActivity.class);
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

        private class DeleteLampConfigurationTask extends AsyncTask<LampConfiguration, Void, String> {

            int lampNumber;

            @Override
            protected String doInBackground(LampConfiguration... lampConfiguration) {
                lampNumber = lampConfiguration[0].getNumber();
                try {
                    LampConfigurationService lampConfigurationService = new LampConfigurationService(getContext());
                    return lampConfigurationService.deleteLampConfig(lampConfiguration[0].getId());
                } catch (IOException e) {
                    return e.getLocalizedMessage();
                }
            }

            @Override
            protected void onPostExecute(final String success) {
                super.onPostExecute(success);

                new RefreshLampConfigurationTask().execute(lampNumber);

                if (success==null) {
                    Toast.makeText(getActivity(), getText(R.string.deleted), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("ConfigurationActivity", "Fehler beim Löschen!");
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            new AlertDialog.Builder(getActivity())
                                    .setTitle(getString(R.string.settings_wrong_title))
                                    .setMessage(getString(R.string.settings_wrong_message, success))
                                    .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(getActivity(), SettingsActivity.class);
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
}
