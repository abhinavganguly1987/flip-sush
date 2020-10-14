package com.toto.sush;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
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
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.toto.sush.fragment.QuickResponseDialogFragment;
import com.toto.sush.service.IncomingCallService;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.toto.sush.LogSwitch.LOG_INFO;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        QuickResponseDialogFragment.QuickResponseDialogListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private String LOG_TAG = "^^^[TOTO] MainActivity";
    public static String SUSH_PREFS = "sushPrefs";

    private DrawerLayout sushDrawerLayout;

    //Sush Switch
    private SwitchCompat switchCompat;

    private Handler handler = new Handler();
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sush_draw_main);

        //Preferences to save the switch state
        sharedPref = this.getSharedPreferences(SUSH_PREFS, Context.MODE_PRIVATE);
        editor = sharedPref.edit();


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
            getSupportActionBar().setDisplayShowTitleEnabled(false);

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
            switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {

                editor.putBoolean(getString(R.string.switch_state), compoundButton.isChecked());
                editor.apply();
                SushRunnable sushRunnable = new SushRunnable(incomingCallIntent);

                if (compoundButton.isChecked()) {
                    handler.post(sushRunnable);
                } else {
                    stopService(incomingCallIntent);
                    handler.removeCallbacksAndMessages(sushRunnable);
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

        switch (menuItem.getItemId()) {
            case R.id.auto_sms:
                sushDrawerLayout.closeDrawer(GravityCompat.START);
                checkForPermissionsForQuickResponseFeature();
                break;
            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutSushActivity.class);
                startActivity(aboutIntent);
                break;
        }
        sushDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkForPermissionsForQuickResponseFeature() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PERMISSION_GRANTED) {
//            && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PERMISSION_GRANTED) {

            requestForPermissions();
        } else {
            showQuickResponseDialog();

        }
    }

    private void requestForPermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG)) {

            Snackbar.make(sushDrawerLayout, R.string.sms_call_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_CALL_LOG},
                            1);

                }
            }).show();

        } else {
            Snackbar.make(sushDrawerLayout, R.string.sms_call_access_permission_not_available, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_CALL_LOG},
                    1);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults.length == 2 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {

                Snackbar.make(sushDrawerLayout, R.string.sms_call_access_permission_granted, Snackbar.LENGTH_LONG).show();
                showQuickResponseDialog();
            } else {
                Snackbar.make(sushDrawerLayout, R.string.sms_call_access_permission_denied, Snackbar.LENGTH_LONG).show();

            }
        }
    }


    private void showQuickResponseDialog() {
        int preselectedQuickSMSIndex = sharedPref.getInt(getString(R.string.quick_SMS_index), -1);

        if (LOG_INFO)   Log.i(LOG_TAG, " in showQuickResponseDialog, quickSMSIndex in sharedPrefs is " + preselectedQuickSMSIndex);

        FragmentManager fm = getSupportFragmentManager();
        QuickResponseDialogFragment quickResponseDialogFragment = QuickResponseDialogFragment.newInstance(preselectedQuickSMSIndex);
        quickResponseDialogFragment.show(fm, "quick_response_dialog_fragment");

    }

    @Override
    public void onDialogPositiveClick(int selectedListIndex) {
        if (LOG_INFO)  Log.i(LOG_TAG, " in onDialogPositiveClick, quickSMSIndex in sharedPrefs is " + selectedListIndex);

        editor.putInt(getString(R.string.quick_SMS_index), selectedListIndex);
        editor.commit();
    }

    public void onShareAction(MenuItem item) {
        Intent appShareIntent = new Intent(Intent.ACTION_SEND);
        String appLink = getString(R.string.playstore_url) + getPackageName();
        appShareIntent.setType("text/plain");
        appShareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_extra_subject));
        appShareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_extra_text) + appLink);
        startActivity(Intent.createChooser(appShareIntent, "Share via..."));
    }

    public void onRateThisAppAction(MenuItem item){
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));

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
            if (LOG_INFO) Log.i(LOG_TAG, "SushRunnable started");
        }


    }

}


