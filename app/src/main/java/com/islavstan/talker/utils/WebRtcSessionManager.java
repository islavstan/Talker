package com.islavstan.talker.utils;

import android.content.Context;
import android.util.Log;

import com.islavstan.talker.main.MainActivity;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacksImpl;


public class WebRtcSessionManager extends QBRTCClientSessionCallbacksImpl {
    private static final String TAG = "VOMER_CALL";

    private static WebRtcSessionManager instance;
    private Context context;
    private static QBRTCSession currentSession;
    private WebRtcSessionManager(Context context) {
        this.context = context;
    }

    public static WebRtcSessionManager getInstance(Context context){
        if (instance == null){
            Log.d(TAG, "instance = new WebRtcSessionManager");
            instance = new WebRtcSessionManager(context);
        }
        return instance;
    }

    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;


    }

    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        Log.d(TAG, "onReceiveNewSession to WebRtcSessionManager");
        //Toast.makeText(context,"ring!!!", Toast.LENGTH_LONG).show();
        if (currentSession == null){
            setCurrentSession(session);
            MainActivity.start(context, true);
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {
        Log.d(TAG, "onSessionClosed WebRtcSessionManager");
        if (session.equals(getCurrentSession())){
            setCurrentSession(null);
        }
    }
}