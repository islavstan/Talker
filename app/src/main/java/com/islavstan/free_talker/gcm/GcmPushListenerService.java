package com.islavstan.free_talker.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import com.islavstan.free_talker.call_functions.service.CallService;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.quickblox.users.model.QBUser;


public class GcmPushListenerService extends GcmListenerService {
    private static final String TAG = "stas2";
    PreferenceHelper preferenceHelper;


    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString(GcmConsts.EXTRA_GCM_MESSAGE);
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(getApplicationContext());
         QBUser qbUser = preferenceHelper.getQbUser();
         startLoginService(qbUser);
        }


    private void startLoginService(QBUser qbUser){
        CallService.start(this, qbUser);//здесь нужно поменять на метод который будет показывать пуш
    }
}