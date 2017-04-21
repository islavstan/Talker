package com.islavstan.talker.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.islavstan.talker.R;
import com.islavstan.talker.call_functions.service.CallService;
import com.islavstan.talker.utils.Constants;
import com.islavstan.talker.utils.Consts;
import com.islavstan.talker.utils.PreferenceHelper;
import com.islavstan.talker.utils.WebRtcSessionManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {

    String TAG = "stas";

    PreferenceHelper preferenceHelper;
    QBUser qbUser;
     WebRtcSessionManager webRtcSessionManager;
    boolean isRunForCall;

    QBChatDialog groupChatDialog;

    private QBChatDialogParticipantListener participantListener;

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
// слушатель онлайн юзеров
        participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {

                Log.d(TAG, "processPresence  dialog id = "+dialogId + " "+qbPresence.getType()+ qbPresence.getUserId() );

            }
        };


    }




    public void signIn(QBUser qbUser) {
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d(TAG, "onSuccess signIn " + result.getLogin());



                QBRestChatService.getChatDialogById(Constants.GROUP_DIALOG_ID).performAsync(
                        new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog dialog, Bundle params) {
                               Log.d("stas", "getChatDialogById success" );
                                //присоединяемся к чату
                                groupChatDialog = dialog;
                                groupChatDialog.addParticipantListener(participantListener);
                                DiscussionHistory discussionHistory = new DiscussionHistory();
                                discussionHistory.setMaxStanzas(0);
                                groupChatDialog.join(discussionHistory, new QBEntityCallback() {
                                    @Override
                                    public void onSuccess(Object o, Bundle bundle) {
                                        Log.d("stas", "join success" );
                                        getOnlineUsers();
                                    }

                                    @Override
                                    public void onError(QBResponseException e) {
                                        Log.d(TAG, "error join  " + e.getMessage());
                                    }
                                });





                            }

                            @Override
                            public void onError(QBResponseException responseException) {
                                Log.d(TAG, "error getChatDialogById  " + responseException.getMessage());
                            }
                        });





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


    private void getOnlineUsers(){
        Collection<Integer> onlineUsers = null;
        try {
            onlineUsers = groupChatDialog.getOnlineUsers();
            for(int i: onlineUsers){
                Log.d("stas","onlineUser = "+i );
            }

        } catch (XMPPException e) {

        }
    }






}