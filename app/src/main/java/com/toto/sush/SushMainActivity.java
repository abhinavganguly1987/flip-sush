package com.toto.sush;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

public class SushMainActivity extends AppCompatActivity {


    private String LOG_TAG="[TOTO] SushMainActivity";


    IntentFilter filter;
    private Intent sushIntent;
    private SushResponseReceiver receiver;
    private boolean mIsSushResponseReceiverRegistered=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Log.i(LOG_TAG," In onCreate ");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sush_main);

        filter = new IntentFilter(SushResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new SushResponseReceiver();
        registerReceiver(receiver, filter);


        sushIntent = new Intent(this,SushIntentService.class);



    }


    public void startSensing(View switchView) {
        boolean isChecked = ((Switch) switchView).isChecked();
        Log.i(LOG_TAG, "startSensing FIRED");

        sushIntent.putExtra(SushIntentService.PARAM_IN_ISCHECKED,isChecked);
        startService(sushIntent);

    }



    protected  void onResume(){
        Log.i(LOG_TAG, " In onResume... ");
        super.onResume();

        if(!mIsSushResponseReceiverRegistered ){

            if(null==receiver)
                receiver = new SushResponseReceiver();
                Log.i(LOG_TAG, " In onResume... registering BROADCAST RECEIVER since it was null");


            filter = new IntentFilter(SushResponseReceiver.ACTION_RESP);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            registerReceiver(receiver, filter);

            mIsSushResponseReceiverRegistered=true;

        }
    }

    @Override
    protected void onDestroy() {
        Log.i(LOG_TAG, " In onDestroy... ");

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(911);
        notificationManager.cancelAll();

        super.onDestroy();
    }

    protected void onPause(){
        Log.i(LOG_TAG, " In onPause... ");
        super.onPause();

        if(mIsSushResponseReceiverRegistered ){
            Log.i(LOG_TAG, " In onPause... a registered receiver found ... thus unregistering it");
            unregisterReceiver(receiver);
            receiver=null;
            mIsSushResponseReceiverRegistered=false;
        }


    }

        @Override
        public void onBackPressed(){

            Log.i(LOG_TAG, " In onBackPressed... ");moveTaskToBack(true);
        }


        //Inner class implementing broadcast receiver
            public class SushResponseReceiver extends BroadcastReceiver {

            public static final String ACTION_RESP =
                    "com.toto.sush.MESSAGE_PROCESSED";

            @Override
            public void onReceive(Context context, Intent intent) {


                Log.i("SushResponseReceiver", "in onReceive");
                Switch susSwitch = (Switch)findViewById(R.id.toggleSush);
                Bundle b = intent.getExtras();
                boolean isChecked = (boolean)b.get(SushIntentService.PARAM_OUT_ISCHECKED);
        //        Boolean.parseBoolean(intent.getStringExtra(SushIntentService.PARAM_OUT_ISCHECKED));
                Log.i("SushResponseReceiver", "in onReceive:: isChecked "+isChecked);
                susSwitch.setChecked(isChecked);

            }


        }

}
