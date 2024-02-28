package com.trava.utilities.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsDetector extends BroadcastReceiver {
    private OnDetectSmsListener listener;
    private String phoneNumberFilter;
    private String filter;

    /**
     * Set result listener
     *
     * @param listener OnDetectSmsListener
     */
    public void setDetectSmsListener(OnDetectSmsListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = getIncomingMessage(pdusObj[i], bundle);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    if (phoneNumberFilter != null && !phoneNumber.equals(phoneNumberFilter)) {
                        return;
                    }
                    String message = currentMessage.getDisplayMessageBody();
                    if (filter != null && !message.matches(filter)) {
                        return;
                    }

                    if (listener != null) {
                        listener.onSmsDetected(message);
                    }
                } // end for loop 
            } // bundle is null 

        } catch (Exception e) {
            Log.e("SmsDetector", "Exception smsReceiver" + e);
        }
    }

    private SmsMessage getIncomingMessage(Object aObject, Bundle bundle) {
        SmsMessage currentSMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String format = bundle.getString("format");
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject, format);
        } else {
            currentSMS = SmsMessage.createFromPdu((byte[]) aObject);
        }
        return currentSMS;
    }

    /**
     * Set phone number filter
     *
     * @param phoneNumberFilter phone number
     */
    public void setPhoneNumberFilter(String phoneNumberFilter) {
        this.phoneNumberFilter = phoneNumberFilter;
    }

    /**
     * set message filter with regexp
     *
     * @param regularExpression regexp
     */
    public void setFilter(String regularExpression) {
        this.filter = regularExpression;
    }
} 