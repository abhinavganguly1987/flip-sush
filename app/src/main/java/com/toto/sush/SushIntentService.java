package com.toto.sush;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import static com.toto.sush.LogSwitch.LOG_ERROR;
import static com.toto.sush.LogSwitch.LOG_INFO;

/**
 * Created by abhinavganguly on 12/05/2017.
 */

public class SushIntentService extends IntentService implements SensorEventListener{

    //For sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static boolean mIsSensorUpdateEnabled = false;

    //for trayicon notification
    private NotificationManager sushNotificationManager;
    public static final int NOTIFICATION_EX = 911;


    public static final String PARAM_IN_ISCHECKED="IN_ISCHECKED";
    public static final String PARAM_OUT_ISCHECKED="OUT_ISCHECKED";


    private String LOG_TAG="SushIntentService";


    public SushIntentService() {
        super("SushIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(LOG_INFO) Log.i(LOG_TAG," In onHandleIntent ");
        //working on sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer
            if(LOG_INFO)  Log.i(LOG_TAG," Ahaa ! an accelerometer found...");

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            //  will start it on toggle switch
            //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        } else {

            if(LOG_ERROR)  Log.e(LOG_TAG," Aww Snap ! no accelerometer found...");
        }

        //working on notification

        sushNotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        Bundle b = intent.getExtras();
//        boolean isChecked = Boolean.parseBoolean(intent.getStringExtra(PARAM_IN_ISCHECKED));

        if(LOG_INFO) Log.i(LOG_TAG," In onHandleIntent, Bundle : "+b);
        if(null !=b){

            boolean isChecked = (boolean)b.get(PARAM_IN_ISCHECKED);

            if(LOG_INFO)  Log.i(LOG_TAG," In onHandleIntent, isChecked : "+isChecked);

            if(isChecked){
                mIsSensorUpdateEnabled = true;
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                startNotificationIconDisplay();

            }else {

                mIsSensorUpdateEnabled = false;
                sensorManager.unregisterListener(this);
                stopNotificationIconDisplay();
            }

            Intent broadcastIntent = new Intent();

            broadcastIntent.setAction(SushMainActivity.SushResponseReceiver.ACTION_RESP);
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra(PARAM_OUT_ISCHECKED, isChecked);
            sendBroadcast(broadcastIntent);
        }


    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (mIsSensorUpdateEnabled) {

            float deltaZ = event.values[2];

            if(LOG_INFO)  Log.i(LOG_TAG, "Sensors fired....Z ="+ deltaZ);

            AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            if(tm.getCallState()==TelephonyManager.CALL_STATE_RINGING){

                if (deltaZ < 0) {

                    if(am.getRingerMode()==AudioManager.RINGER_MODE_NORMAL){
                        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    }

                }else {
                    if(am.getRingerMode()==AudioManager.RINGER_MODE_VIBRATE){
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    }
                }

            }

        } else {
            if(LOG_INFO) Log.i(LOG_TAG, "Sensors Just STOPPED firing....");
            sensorManager.unregisterListener(this);
        }

    }

    /**
     * This is just a method needs to be overridden nothing much is happening here
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do Nothing

    }

    //This method is not behaving as expected, so need to investigate on this in future,
    //however it is not blocking the functionality
    @Override
    public void onTaskRemoved(Intent rootIntent) {

        if(LOG_INFO) Log.i(LOG_TAG," In onTaskRemoved ");

        stopNotificationIconDisplay();
        stopSelf();
//        super.onTaskRemoved(rootIntent);
    }





    private void startNotificationIconDisplay(){

        if(LOG_INFO)  Log.i(LOG_TAG," In startNotificationIconDisplay ");


        Intent intentNotify = new Intent(this, SushMainActivity.class);
        intentNotify.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intentNotify,0);



        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(getNotificationIcon())
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_caption))
                        .setContentIntent(pIntent)
                        .setOngoing(true);

        Notification notificationCompat = mBuilder.build();

        sushNotificationManager.notify(NOTIFICATION_EX, notificationCompat);


    }

    private void stopNotificationIconDisplay(){

        if(LOG_INFO)   Log.i(LOG_TAG," In stopNotificationIconDisplay() ");

        sushNotificationManager.cancel(NOTIFICATION_EX);
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ?  R.drawable.check_box_black_24dp: R.drawable.trayicon ;
    }

}
