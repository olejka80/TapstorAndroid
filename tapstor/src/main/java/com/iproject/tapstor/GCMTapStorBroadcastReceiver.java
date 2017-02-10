package com.iproject.tapstor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.iproject.tapstor.library.Log;

/**
 * BroadCast receiver to wake up on notification received
 *
 * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
 */
public class GCMTapStorBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RECEIVER", "received");
        // Explicitly specify that GCMTapStorIntentService will handle the
        // intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                GCMTapStorIntentService.class.getName());
        // Start the service, keeping the device awake while it is
        // launching.
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
