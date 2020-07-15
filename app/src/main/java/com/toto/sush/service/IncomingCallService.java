package com.toto.sush.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.toto.sush.MainActivity;
import com.toto.sush.R;

import static com.toto.sush.LogSwitch.LOG_ERROR;
import static com.toto.sush.LogSwitch.LOG_INFO;
/**
 * Created by abhinavganguly on 09/07/2020.
 */
public class IncomingCallService extends Service implements SensorEventListener {

    private String LOG_TAG = ">>>[TOTO] IncomingCallService";
    private static final String SUSH_CHANNEL_ID = "com.toto.sush.NOTIFICATIONCHANNEL";
    private static final CharSequence SUSH_CHANNEL_NAME = "SUSH NOTIFICATION CHANNEL";
    private int SUSH_NOTIFICATION_ID = 314;
    private static boolean mIsSensorUpdateEnabled = false;

    //For sensor
    private SensorManager sensorManager;
    private Sensor accelerometer;


    @Override
    public void onCreate() {
        if (LOG_INFO) Log.e(LOG_TAG, "In onCreate");

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (LOG_INFO) Log.e(LOG_TAG, "In onStartCommand");

        Toast.makeText(this, "Sush service starting", Toast.LENGTH_SHORT).show();

        Bundle b = new Bundle();
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        b.putInt("RINGER_MODE", am.getRingerMode());

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        b.putInt("CALL_STATE", tm.getCallState());

        mIsSensorUpdateEnabled = true;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        createNotification();

        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        if (LOG_INFO) Log.e(LOG_TAG, "In onDestroy");

        mIsSensorUpdateEnabled = false;
        sensorManager.unregisterListener(this);

        Toast.makeText(this, "Sush service done", Toast.LENGTH_SHORT).show();
        stopForeground(true);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (LOG_ERROR) Log.i(LOG_TAG, "In onSensorChanged");
        if (mIsSensorUpdateEnabled) {

            float deltaZ = event.values[2];

            if (LOG_ERROR) Log.i(LOG_TAG, "Sensors fired....Z =" + deltaZ);

            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            if (LOG_INFO)
                Log.e(LOG_TAG, "Audio Manager ringerMode....  = " + getRingerMode(am.getRingerMode()));

            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (LOG_ERROR) Log.e(LOG_TAG, "Telephony Manager = " + tm.getCallState());


            //When a call is incoming
            if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                if (LOG_INFO)
                    Log.i(LOG_TAG, "Phone is Ringing, and is it face down = " + isPhoneFaceDown(deltaZ));

                //if Phone is flipped face down
                if (isPhoneFaceDown(deltaZ)) {

                    //check if Phone is in Ringer mode, if yes then put it in Vibrate mode
                    if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {

                        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    }
                    //else check if phone is facing up, and  in Vibrate mode, the put it back to Ringer mode
                } else {
                    if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

                    }
                }

                //else if phone stopped ringing, and thus went into idle state
            } else if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {

                //If Phone was in Vibrate mode, then put it back to Ringer mode
                if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                    am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
            }

        } else {
            if (LOG_INFO) Log.i(LOG_TAG, "Sensors Just STOPPED firing....");
            sensorManager.unregisterListener(this);
        }

    }

    private boolean isPhoneFaceDown(float z) {
        return z < 0;
    }

    private String getRingerMode(int ringer_mode) {

        switch (ringer_mode) {
            case AudioManager.RINGER_MODE_NORMAL:
                return "RINGER_MODE_NORMAL";
            case AudioManager.RINGER_MODE_SILENT:
                return "RINGER_MODE_SILENT";
            case AudioManager.RINGER_MODE_VIBRATE:
                return "RINGER_MODE_VIBRATE";
            default:
                return "UNKNOWN RINGER MODE";

        }
    }

    /**
     * This is just a method needs to be overridden nothing much is happening here
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do Nothing

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification() {

        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this, channel)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_caption))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        startForeground(SUSH_NOTIFICATION_ID, notification);

    }

    @NonNull
    @TargetApi(26)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = new NotificationChannel(SUSH_CHANNEL_ID, SUSH_CHANNEL_NAME, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.GREEN);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return SUSH_CHANNEL_ID;
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.check_box_black_24dp : R.drawable.trayicon;
    }
}
