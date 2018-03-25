package de.taubsi.aviary;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;
import de.taubsi.aviary.model.Configuration;
import de.taubsi.aviary.service.CameraService;
import de.taubsi.aviary.service.NetworkConfigurationService;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private VideoView videoView;
    private LibVLC videoVlc;

    private Button upButton;
    private Button downButton;
    private Button rightButton;
    private Button leftButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_camera);
        navigationView.setNavigationItemSelectedListener(this);

        videoView = (VideoView)this.findViewById(R.id.camera_videoview);

        upButton = (Button)this.findViewById(R.id.button_up);
        downButton = (Button)this.findViewById(R.id.button_down);
        rightButton = (Button)this.findViewById(R.id.button_right);
        leftButton = (Button)this.findViewById(R.id.button_left);

        upButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        new ControlCameraTask().execute("up", "true");
                        break;
                }
                return true;
            }
        });

        downButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        new ControlCameraTask().execute("down", "true");
                        break;
                }
                return true;
            }
        });

        leftButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        new ControlCameraTask().execute("left", "true");
                        break;
                }
                return true;
            }
        });

        rightButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        new ControlCameraTask().execute("right", "true");
                        break;
                }
                return true;
            }
        });

        new GetCameraUrlTask().execute();
    }

    private class ControlCameraTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                CameraService cameraService = new CameraService(getApplicationContext());
                cameraService.controlCamera(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class GetCameraUrlTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            try {
                NetworkConfigurationService networkConfigurationService = new NetworkConfigurationService(getApplicationContext());
                return networkConfigurationService.getConfiguration("camera_url").getValue();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String url) {
            super.onPostExecute(url);
            if(url != null){
                ArrayList<String> options = new ArrayList<String>();
                options.add("--no-drop-late-frames");
                options.add("--no-skip-frames");
                options.add("-vvv");
                options.add("--rtsp-tcp");
                videoVlc = new LibVLC(getApplicationContext(), options);

                org.videolan.libvlc.MediaPlayer newVideoMediaPlayer = new org.videolan.libvlc.MediaPlayer(videoVlc);
                final IVLCVout vOut = newVideoMediaPlayer.getVLCVout();
                vOut.setVideoView(videoView); //videoView is a pre-defined view which is part of the layout
                vOut.attachViews();

                Media videoMedia = new Media (videoVlc, Uri.parse(url));
                newVideoMediaPlayer.setMedia(videoMedia);
                newVideoMediaPlayer.play();
            }
        }
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
                //intent = new Intent(getApplicationContext(), CameraActivity.class);
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
}
