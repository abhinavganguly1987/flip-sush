package com.toto.sush;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.toto.sush.LogSwitch.LOG_INFO;
import static com.toto.sush.SushIntentService.NOTIFICATION_EX;

/**
 * Created by abhinavganguly on 12/05/2017.
 */

public class SushMainActivity extends AppCompatActivity implements
        SwitchCompat.OnCheckedChangeListener {


    private String LOG_TAG = "[TOTO] SushMainActivity";

    //SushResponseReceiver is used to receive broadcasted notifications from  intent service SushIntentService
    private SushResponseReceiver receiver;

    private IntentFilter filter;
    private Intent sushIntent;

    private boolean mIsSushResponseReceiverRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        if (LOG_INFO) Log.i(LOG_TAG, " In onCreate ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sush_main);

        //creating a swicth component
        SwitchCompat switchCompat = (SwitchCompat) findViewById(R.id
                .toggleSush);
        switchCompat.setSwitchPadding(40);
        switchCompat.setOnCheckedChangeListener(this);

        //checking the current Phone Ringing state, Sush only works when phone is in Ringing state
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || am.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            if (LOG_INFO) Log.i(LOG_TAG, " Phone is on Vibrate or Silent.. thus app won't work ");

            //Disabling the switch
            switchCompat.setClickable(false);
            //And then showing a Toast message
//            LayoutInflater inflater = getLayoutInflater();
//            View layout = inflater.inflate(R.layout.custom_toast,
//                    (ViewGroup) findViewById(R.id.custom_toast_container));
//
//            TextView text = (TextView) layout.findViewById(R.id.text);
//            text.setText("Aww snaps... Sush only can work when your phone is in Ringer mode");
//
//            Toast toast = new Toast(getApplicationContext());
//            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//            toast.setDuration(Toast.LENGTH_LONG);
//            toast.setView(layout);
//            toast.show();
            CharSequence toastText = "Aww snaps... Sush only can work when your phone is in Ringer mode";
            Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG).show();
            sushKillerThread.start();

        }else {


            filter = new IntentFilter(SushResponseReceiver.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new SushResponseReceiver();
            registerReceiver(receiver, filter);
            mIsSushResponseReceiverRegistered = true;


            sushIntent = new Intent(this, SushIntentService.class);
        }


    }
    Thread sushKillerThread = new Thread(){
        @Override
        public void run() {
            try {
                Thread.sleep(Toast.LENGTH_LONG); // As I am using LENGTH_LONG in Toast
                SushMainActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.toggleSush:
                sushIntent.putExtra(SushIntentService.PARAM_IN_ISCHECKED, isChecked);
                startService(sushIntent);
                break;
            default:
                break;
        }

    }


    protected void onResume() {
        if (LOG_INFO) Log.i(LOG_TAG, " In onResume... ");
        super.onResume();

        if (!mIsSushResponseReceiverRegistered) {

            if (null == receiver)
                receiver = new SushResponseReceiver();
            if (LOG_INFO)
                Log.i(LOG_TAG, " In onResume... registering BROADCAST RECEIVER since it was null");


            filter = new IntentFilter(SushResponseReceiver.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(receiver, filter);

            mIsSushResponseReceiverRegistered = true;

        }
    }

    @Override
    protected void onDestroy() {
        if (LOG_INFO) Log.i(LOG_TAG, " In onDestroy... ");

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_EX);
        notificationManager.cancelAll();

        super.onDestroy();
    }

    protected void onPause() {
        if (LOG_INFO) Log.i(LOG_TAG, " In onPause... ");
        super.onPause();

        if (mIsSushResponseReceiverRegistered) {

            if (LOG_INFO)
                Log.i(LOG_TAG, " In onPause... a registered receiver found ... thus unregistering it");

            unregisterReceiver(receiver);
            receiver = null;
            mIsSushResponseReceiverRegistered = false;
        }


    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, Menu menu, int deviceId) {
        super.onProvideKeyboardShortcuts(data, menu, deviceId);
    }

    @Override
    public void onBackPressed() {

        if (LOG_INFO) Log.i(LOG_TAG, " In onBackPressed... ");
        moveTaskToBack(true);
    }


    //Inner class implementing broadcast receiver
    public class SushResponseReceiver extends BroadcastReceiver {

        public static final String ACTION_RESP =
                "com.toto.sush.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {


            if (LOG_INFO) Log.i("SushResponseReceiver", "in onReceive");

            SwitchCompat susSwitch = (SwitchCompat) findViewById(R.id.toggleSush);
            Bundle b = intent.getExtras();
            boolean isChecked = (boolean) b.get(SushIntentService.PARAM_OUT_ISCHECKED);

            if (LOG_INFO) Log.i("SushResponseReceiver", "in onReceive:: isChecked " + isChecked);

            susSwitch.setChecked(isChecked);

        }


    }

}
