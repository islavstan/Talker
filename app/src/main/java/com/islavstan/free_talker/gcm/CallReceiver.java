package com.islavstan.free_talker.gcm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.islavstan.free_talker.call_functions.service.CallService;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.quickblox.users.model.QBUser;


public class CallReceiver extends BroadcastReceiver {

    public CallReceiver() {

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("action");
        PreferenceHelper preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(context.getApplicationContext());
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        QBUser qbUser = preferenceHelper.getQbUser();
        switch (action) {
            case "Take":
                CallService.start(context, qbUser);
                break;
            case "Reject":
                mNotificationManager.cancel(100);
                break;
        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }
}
