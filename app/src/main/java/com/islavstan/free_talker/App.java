package com.islavstan.free_talker;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import io.fabric.sdk.android.Fabric;


public class App extends Application {
   /* public static final String APP_ID = "56925";
    public static final String AUTH_KEY = "hkQgprSY7YBSVbr";
    public static final String AUTH_SECRET = "JGPJjry9xZMsH8S";
    public static final String ACCOUNT_KEY = "ZVtq8i2UTCe26RRHtzsG";

    */


//мои данные
    public static final String APP_ID = "56759";
    public static final String AUTH_KEY = "dLs2MNFVvAsDGDx";
    public static final String AUTH_SECRET = "CtAjyHPcOFZKeyj";
    public static final String ACCOUNT_KEY = "p2QyCkAoiU5fiqEeHMes";


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
        QBSettings.getInstance().setEndpoints("https://api.quickblox.com", "chat.quickblox.com", ServiceZone.PRODUCTION);
        QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);

    }




}
