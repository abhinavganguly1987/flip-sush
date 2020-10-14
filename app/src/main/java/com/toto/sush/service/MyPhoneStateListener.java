package com.toto.sush.service;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MyPhoneStateListener extends PhoneStateListener {

    private String LOG_TAG = "~~~[TOTO] MyPhoneStateListener";

    private String incomingPhoneNumber = "";

    public String getIncomingPhoneNumber() {
        return incomingPhoneNumber;
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        super.onCallStateChanged(state, phoneNumber);

        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                Log.e(LOG_TAG, "onCallStateChanged phone number is = " + phoneNumber);
                incomingPhoneNumber = phoneNumber;
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                incomingPhoneNumber = "";
                break;

        }
    }
}
