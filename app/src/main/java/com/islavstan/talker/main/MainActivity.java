package com.islavstan.talker.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.islavstan.talker.R;
import com.islavstan.talker.call_functions.service.CallService;
import com.islavstan.talker.utils.Consts;
import com.islavstan.talker.utils.PreferenceHelper;
import com.islavstan.talker.utils.WebRtcSessionManager;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {

    String TAG = "stas";

    PreferenceHelper preferenceHelper;
    QBUser qbUser;
     WebRtcSessionManager webRtcSessionManager;
    boolean isRunForCall;

    public static void startActivity(Context context, int flags) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(flags);
        context.startActivity(intent);
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, boolean isRunForCall) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Consts.EXTRA_IS_STARTED_FOR_CALL, isRunForCall);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();
        preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(getApplicationContext());

        qbUser = preferenceHelper.getQbUser();
        if (qbUser != null) {
            CallService.start(MainActivity.this, qbUser);
            signIn(qbUser);

        }


    }


    public void signIn(QBUser qbUser) {
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d(TAG, "onSuccess signIn " + result.getLogin());

            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error signIn  " + responseException.getMessage());

            }
        });
    }

    private void initFields() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isRunForCall = extras.getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
        }
        webRtcSessionManager = WebRtcSessionManager.getInstance(getApplicationContext());
    }
}