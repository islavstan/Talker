package com.islavstan.talker.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.islavstan.talker.R;
import com.islavstan.talker.activities.CallActivity;
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
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

import static android.R.attr.max;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "stas";

    Button level1Btn, level2Btn, blockBtn, shareBtn, femaleBtn;


    PreferenceHelper preferenceHelper;
    QBUser qbUser;
    WebRtcSessionManager webRtcSessionManager;
    boolean isRunForCall;

    QBChatDialog groupChatDialog;

    private QBChatDialogParticipantListener participantListener;

    int min = 0;
    int max = 0;
    int randomNum = min + (int) (Math.random() * ((max - min) + 1));


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

        level1Btn = (Button) findViewById(R.id.level1Btn);
        level2Btn = (Button) findViewById(R.id.level2Btn);
        blockBtn = (Button) findViewById(R.id.blockBtn);
        shareBtn = (Button) findViewById(R.id.shareBtn);
        femaleBtn = (Button) findViewById(R.id.femaleBtn);

        level1Btn.setOnClickListener(this);
        level2Btn.setOnClickListener(this);
        blockBtn.setOnClickListener(this);
        shareBtn.setOnClickListener(this);
        femaleBtn.setOnClickListener(this);


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

                Log.d(TAG, "processPresence  dialog id = " + dialogId + " " + qbPresence.getType() + qbPresence.getUserId());

            }
        };


    }


    public void signIn(QBUser qbUser) {
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d(TAG, "onSuccess signIn " + result.getLogin());


                //получаю чат
                QBRestChatService.getChatDialogById(Constants.GROUP_DIALOG_ID).performAsync(
                        new QBEntityCallback<QBChatDialog>() {
                            @Override
                            public void onSuccess(QBChatDialog dialog, Bundle params) {
                                Log.d("stas", "getChatDialogById success");
                                //присоединяемся к чату
                                groupChatDialog = dialog;
                                groupChatDialog.addParticipantListener(participantListener);
                                DiscussionHistory discussionHistory = new DiscussionHistory();
                                discussionHistory.setMaxStanzas(0);
                                groupChatDialog.join(discussionHistory, new QBEntityCallback() {
                                    @Override
                                    public void onSuccess(Object o, Bundle bundle) {
                                        Log.d("stas", "join success");
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


    private void getOnlineUsers() {
        Collection<Integer> onlineUsers = null;
        try {
            onlineUsers = groupChatDialog.getOnlineUsers();


            for (int i : onlineUsers) {
                Log.d("stas", "onlineUser = " + i);
            }

        } catch (XMPPException e) {

        }
        if (onlineUsers != null)
            getUsersById(onlineUsers);
    }


    private void getUsersById(Collection<Integer> onlineUsers) {
        Log.d(TAG, " getUsersById ");
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsersByIDs(onlineUsers, pagedRequestBuilder).performAsync(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                Log.d(TAG, " getUsersById  success");
                for (QBUser qbUser : result) {
                    Log.d(TAG, qbUser.getTags().get(0));
                }

            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error getUsersById  " + responseException.getMessage());
            }
        });
    }


    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.level1Btn:
                callToRandomUser();
                break;

            case R.id.level2Btn:
                callToLevel2User();
                break;

            case R.id.blockBtn:
                blockAdmob();
                break;

            case R.id.shareBtn:
                shareAppToWhatsapp();
                break;

            case R.id.femaleBtn:
                callToFemale();
                break;


        }
    }

    private void callToFemale() {
    }


    private void shareAppToWhatsapp() {
    }

    private void blockAdmob() {
    }

    private void callToLevel2User() {
    }

    private void callToRandomUser() {


    }


    private void startCall(int opponentId) {
        ArrayList<Integer> opponentsList = new ArrayList<>();
        opponentsList.add(opponentId);
        QBRTCTypes.QBConferenceType conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());

        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);

        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);

        CallActivity.start(this, false);


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            isRunForCall = intent.getExtras().getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
            if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
                CallActivity.start(MainActivity.this, true);
            }
        }
    }






}