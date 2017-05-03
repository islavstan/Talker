package com.islavstan.free_talker.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.islavstan.free_talker.R;
import com.islavstan.free_talker.call_functions.service.CallService;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.quickblox.users.model.QBUser;


public class GcmPushListenerService extends GcmListenerService {
    private static final String TAG = "stas";
    //PreferenceHelper preferenceHelper;
    NotificationManager mNotificationManager;
    // QBUser qbUser;


    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);
        // preferenceHelper = PreferenceHelper.getInstance();
        //preferenceHelper.init(getApplicationContext());
        //qbUser = preferenceHelper.getQbUser();
        showNotification();
    }


    private void showNotification(/*QBUser qbUser*/) {
        Log.d(TAG, "startLoginService");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(100, notification);
        //CallService.startFromPush(this, qbUser);//здесь нужно поменять на метод который будет показывать пуш
    }


    private Notification createNotification() {
        Intent takeIntent = new Intent(this, CallReceiver.class);
        takeIntent.putExtra("action", "Take");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), takeIntent, 0);
        Intent rejectIntent = new Intent(this, CallReceiver.class);
        rejectIntent.putExtra("action", "Reject");
        PendingIntent pIntent2 = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), rejectIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("IELTS Live Speaking")
                .setContentText("Incoming call")
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.drawable.call_phone_answer_black, "Take", pIntent)
                .addAction(R.drawable.call_end_black, "Cancel", pIntent2)
                .setOngoing(true);

        return builder.build();
    }


}