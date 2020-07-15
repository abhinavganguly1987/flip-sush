package com.toto.sush;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.toto.sush.service.IncomingCallService;

import static com.toto.sush.LogSwitch.LOG_INFO;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String LOG_TAG = "^^^[TOTO] MainActivity";

    private DrawerLayout sushDrawerLayout;

    //Sush Switch
    private SwitchCompat switchCompat;

    private Handler handler = new Handler();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sush_draw_main);

        //Preferences to save the switch state
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();


        if (LOG_INFO) Log.i(LOG_TAG, " In onCreate ");

        //checking the current Phone Ringing state, Sush only works when phone is in Ringing state
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || am.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            if (LOG_INFO) Log.i(LOG_TAG, " Phone is on Vibrate or Silent.. thus app won't work ");

            //Disabling the switch
            CharSequence toastText = "Aww snaps... Sush can only work when your phone is in Ringer mode";
            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
            sushKillerThread.start();

        } else {

            if (LOG_INFO) Log.i(LOG_TAG, " Phone is on mode " + am.getRingerMode());

            //creating the toolbar
            Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbarSush);
            setSupportActionBar(myToolbar);

            sushDrawerLayout = findViewById(R.id.activity_sush_draw_main);
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            ActionBarDrawerToggle actionBarDrawerToggle =
                    new ActionBarDrawerToggle(this,
                            sushDrawerLayout,
                            myToolbar,
                            R.string.navigatio_drawer_open,
                            R.string.navigatio_drawer_close);

            sushDrawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            //creating the toolbar finished

            final Intent incomingCallIntent = new Intent(this, IncomingCallService.class);
            //creating a swicth component
            switchCompat = (SwitchCompat) findViewById(R.id
                    .toggleSush);
            switchCompat.setSwitchPadding(40);
            //check if previously any switch state was set
            switchCompat.setChecked(sharedPref.getBoolean(getString(R.string.switch_state), false));
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @RequiresApi(api = Build.VERSION_CODES.O)
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    editor.putBoolean(getString(R.string.switch_state), compoundButton.isChecked());
                    editor.apply();
                    SushRunnable sushRunnable = new SushRunnable(incomingCallIntent);

                    if (compoundButton.isChecked()) {
                        handler.post(sushRunnable);
                    } else {
                        stopService(incomingCallIntent);
                        handler.removeCallbacksAndMessages(sushRunnable);
                    }
                }
            });

        }
    }

    Thread sushKillerThread = new Thread() {
        @Override
        public void run() {
            try {
                Thread.sleep(Toast.LENGTH_LONG); // As I am using LENGTH_LONG in Toast
                MainActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume() {
        if (LOG_INFO) Log.i(LOG_TAG, " In onResume... ");

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (LOG_INFO) Log.i(LOG_TAG, " In onDestroy... ");

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if (sushDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            sushDrawerLayout.closeDrawer(GravityCompat.START);
        }
        if (!switchCompat.isChecked()) {
            if (LOG_INFO) Log.i(LOG_TAG, " In onBackPressed... Destroying since switch wasn't on");
            this.finish();
        } else {
            if (LOG_INFO) Log.i(LOG_TAG, " In onBackPressed...Moving task to back switch is on");
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_menu, menu);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.auto_sms:
//                getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.fragment_container, new QuickSMSFragment())
//                        .commit();
                break;
            case R.id.about:
                Toast.makeText(getApplicationContext(), R.string.share, Toast.LENGTH_SHORT).show();
                break;
        }
        sushDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //Inner runnable class to run the IncomingCallService on a separate thread
    class SushRunnable implements Runnable {

        Intent incomingCallIntent;

        public SushRunnable(Intent incomingCallIntent) {
            this.incomingCallIntent = incomingCallIntent;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            startForegroundService(incomingCallIntent);
            Log.i(LOG_TAG, "SushRunnable started");
        }


    }

}


