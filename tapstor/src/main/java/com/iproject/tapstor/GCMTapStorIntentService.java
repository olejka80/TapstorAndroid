package com.iproject.tapstor;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.iproject.tapstor.helper.TapstorData;
import com.iproject.tapstor.library.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Intent Service creates an Inten on notification received
 *
 * @author Grassos Konstantinos <grassos.konstantinos@gmail.com>
 */
public class GCMTapStorIntentService extends IntentService {

    public static final int NOTIFICATION_ID = 1;
    String TAG = "GCM";
    NotificationCompat.Builder builder;

    // static String SENDER_ID = "977193308962";
    public GCMTapStorIntentService() {
        super("GCMTapStorIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "received");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        Context context = getApplicationContext();
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                sendNotification(intent, context);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                sendNotification(intent, context);
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {

                Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.
                sendNotification(intent, context);
                Log.i(TAG, "Received: " + extras.toString());

            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMTapStorBroadcastReceiver.completeWakefulIntent(intent);
    }

    // This is just one simple example of what you might choose to do with
    // a GCM message.

    // {"message":"iProject: ","stamp":"2015-01-20 19:07:49","pid":null,"type":1,"cid":"444"}

    // Put the message into a notification and post it.
    private void sendNotification(Intent arg1, Context context) {

        Log.i(TAG, "new message");

        try {

            // System.out.println(arg1.getExtras().toString());

            String aps = arg1.getExtras().getString("aps");
            System.out.println(aps);
            JSONObject pushObj;
            pushObj = new JSONObject(aps);
            String message = pushObj.getString("message");

            try {

                String pid = pushObj.getString("pid");
                System.out.println("pid is: " + pid);
                String cid = "";

                try {

                    cid = pushObj.getString("cid");
                    System.out.println("cid id: " + cid);

                } catch (Exception e) {

                    cid = "";
                    Log.e(TAG, e);

                }

                String stamp = "";

                try {

                    stamp = pushObj.getString("stamp");

                    System.out.println("stamp id: " + stamp);

                } catch (Exception e) {
                    Log.e(TAG, e);
                }

                if (message != null) {

                    if (!message.trim().equals("")) {

//						Bitmap bitmap = BitmapFactory.decodeResource(
//								getResources(), R.drawable.tapstor_icon);
                        long[] vibrate = new long[]{100, 200, 100, 500};
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                                this)
                                .setStyle(
                                        new NotificationCompat.BigTextStyle()
                                                .bigText(message)
                                                .setBigContentTitle("Tapstor"))
                                .setSmallIcon(R.drawable.ic_stat_tapstor_icon)

                                .setVibrate(vibrate)
                                .setSound(
                                        RingtoneManager
                                                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setTicker(message).setContentTitle("Tapstor")
                                .setContentText(message);
                        mBuilder.setAutoCancel(true);
                        Intent resultIntent = null;

                        try {

                            if (TapstorData.getInstance().getUserToken() == null) {
                                System.out
                                        .println("null session app is not open");
                                resultIntent = new Intent(this,
                                        SplashTapstorActivity.class);
                                resultIntent.setAction(Intent.ACTION_MAIN);
                                resultIntent
                                        .addCategory(Intent.CATEGORY_LAUNCHER);
                                resultIntent.putExtra("go_to_notifications",
                                        true);

                                // resultIntent.putExtra(
                                // "go_to_notifications", false);
                                // }

                            } else {

                                System.out.println("NOT null data");
                                resultIntent = new Intent(this,
                                        MainViewPagerActivity.class);

                                resultIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                resultIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                resultIntent
                                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                                resultIntent.putExtra("go_to_notifications",
                                        true);

                            }

                            resultIntent
                                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            PendingIntent pendingIntent = PendingIntent
                                    .getActivity(this, 1, resultIntent,
                                            PendingIntent.FLAG_UPDATE_CURRENT);
                            mBuilder.setContentIntent(pendingIntent);
                            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                            double notifId = (Math.random() * 10);

                            Log.e(null, notifId + "notif ID");
                            int unread = 0;
                            try {

                                unread = TapstorData.getInstance()
                                        .getUnreadMessages();

                                unread += 1;

                            } catch (Exception e) {
                                Log.e(TAG, e);
                                unread += 1;
                            }

                            TapstorData.getInstance().setUnreadMessages(unread);

                            mNotificationManager.notify((int) notifId,
                                    mBuilder.build());

                        } catch (Exception e) {
                            System.out.println("do nothing");
                        }

                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, e);
        }

    }

}
