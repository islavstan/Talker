package com.islavstan.talker.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.islavstan.talker.Manifest;
import com.islavstan.talker.R;
import com.islavstan.talker.activities.CallActivity;
import com.islavstan.talker.call_functions.service.CallService;
import com.islavstan.talker.utils.Constants;
import com.islavstan.talker.utils.Consts;
import com.islavstan.talker.utils.PreferenceHelper;
import com.islavstan.talker.utils.WebRtcSessionManager;
import com.master.permissionhelper.PermissionHelper;
import com.quickblox.chat.QBChatService;
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

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "stas";

    Button level1Btn, level2Btn, blockBtn, shareBtn, femaleBtn;


    PreferenceHelper preferenceHelper;
    QBUser qbUser;
    WebRtcSessionManager webRtcSessionManager;
    boolean isRunForCall;

    QBChatDialog groupChatDialog;


    List<QBUser> allOnlineUsersList = new ArrayList<>();


    private ProgressDialog mProgressDialog;
    PermissionHelper permissionHelper;


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


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.please_wait));
        mProgressDialog.setMessage(getString(R.string.looking_for_an_opponent));
        mProgressDialog.setIndeterminate(true);


        initFields();
        preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(getApplicationContext());
        qbUser = preferenceHelper.getQbUser();
        if (qbUser != null) {
            CallService.start(MainActivity.this, qbUser);
            signIn(qbUser);

        }


        if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
            CallActivity.start(MainActivity.this, true);
        }


    }


    private void removeUserById(int id) {
        if (allOnlineUsersList.size() != 0) {
            for (int i = 0; i < allOnlineUsersList.size(); i++) {
                if (allOnlineUsersList.get(i).getId() == id) {
                    allOnlineUsersList.remove(i);
                    Log.d(TAG, "delete user with id " + id);
                }
            }
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d("stas", "onPause");
        if (groupChatDialog != null) {
            try {
                groupChatDialog.leave();
                groupChatDialog = null;
            } catch (XMPPException | SmackException.NotConnectedException e) {

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("stas", "onStart");
        if (groupChatDialog == null) {
            getChatDialogById();
        }


    }

    public void signIn(QBUser qbUser) {
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                Log.d(TAG, "onSuccess signIn " + result.getLogin());

                getChatDialogById();
                //получаю чат

            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error signIn  " + responseException.getMessage());

            }
        });
    }


    private void getChatDialogById() {
        QBRestChatService.getChatDialogById(Constants.GROUP_DIALOG_ID).performAsync(
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle params) {
                        Log.d("stas", "getChatDialogById success");
                        //присоединяемся к чату
                        groupChatDialog = dialog;
                        DiscussionHistory discussionHistory = new DiscussionHistory();
                        discussionHistory.setMaxStanzas(0);
                        groupChatDialog.join(discussionHistory, new QBEntityCallback() {
                            @Override
                            public void onSuccess(Object o, Bundle bundle) {
                                Log.d("stas", "join success");

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

    private void initFields() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isRunForCall = extras.getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
        }
        webRtcSessionManager = WebRtcSessionManager.getInstance(getApplicationContext());
    }


    private Collection<Integer> getOnlineUsers() {
        mProgressDialog.show();
        Collection<Integer> onlineUsers = null;
        try {
            onlineUsers = groupChatDialog.getOnlineUsers();
        } catch (XMPPException e) {

        }
        return onlineUsers;
    }


    private void getRandomUser(final Collection<Integer> onlineUsers, final int type) {

        permissionHelper = new PermissionHelper(this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        permissionHelper.request(new PermissionHelper.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                Log.d(TAG, "onPermissionGranted() called");

                Log.d(TAG, " getRandomUser ");
                QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
                pagedRequestBuilder.setPage(1);
                pagedRequestBuilder.setPerPage(50);

                QBUsers.getUsersByIDs(onlineUsers, pagedRequestBuilder).performAsync(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                    @Override
                    public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                        Log.d(TAG, " getRandomUser  success");

                        switch (type) {
                            case 1:
                                allOnlineUsersList.clear();
                                allOnlineUsersList.addAll(result);
                                removeUserById(qbUser.getId());

                                if (allOnlineUsersList.size() > 0) {
                                    int min = 0;
                                    int max = allOnlineUsersList.size() - 1;
                                    int randomNum = min + (int) (Math.random() * ((max - min) + 1));
                                    Log.d(TAG, "max = " + max + " randomNum = " + randomNum + " allOnlineUsersList.size() = " + allOnlineUsersList.size());
                                    startCall(allOnlineUsersList.get(randomNum).getId());


                                } else
                                    Toast.makeText(MainActivity.this, R.string.no_users_online, Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                                break;

                            case 2:
                                allOnlineUsersList.clear();
                                allOnlineUsersList.addAll(result);
                                removeUserById(qbUser.getId());
                                if (allOnlineUsersList.size() > 0) {
                                    for (int i = 0; i < allOnlineUsersList.size(); i++) {
                                        if (allOnlineUsersList.get(i).getTags().size() == 1) {
                                            Log.d(TAG, "remove not level 2 users");
                                            allOnlineUsersList.remove(i);
                                        }
                                    }
                                    if (allOnlineUsersList.size() > 0) {
                                        int min = 0;
                                        int max = allOnlineUsersList.size() - 1;
                                        int randomNum = min + (int) (Math.random() * ((max - min) + 1));
                                        Log.d(TAG, "max = " + max + " randomNum = " + randomNum + " allOnlineUsersList.size() = " + allOnlineUsersList.size());
                                        startCall(allOnlineUsersList.get(randomNum).getId());
                                    } else
                                        Toast.makeText(MainActivity.this, R.string.no_users_online, Toast.LENGTH_SHORT).show();
                                } else
                                    Toast.makeText(MainActivity.this, R.string.no_users_online, Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();

                                break;

                            case 3:
                                allOnlineUsersList.clear();
                                allOnlineUsersList.addAll(result);
                                removeUserById(qbUser.getId());

                                if (allOnlineUsersList.size() > 0) {
                                    for (int i = 0; i < allOnlineUsersList.size(); i++) {
                                        if (allOnlineUsersList.get(i).getTags().get(0).equals("Male")) {
                                            Log.d(TAG, "remove Male");
                                            allOnlineUsersList.remove(i);
                                        }
                                    }
                                    if (allOnlineUsersList.size() > 0) {
                                        int min = 0;
                                        int max = allOnlineUsersList.size() - 1;
                                        int randomNum = min + (int) (Math.random() * ((max - min) + 1));
                                        Log.d(TAG, "max = " + max + " randomNum = " + randomNum + " allOnlineUsersList.size() = " + allOnlineUsersList.size());
                                        startCall(allOnlineUsersList.get(randomNum).getId());
                                    } else
                                        Toast.makeText(MainActivity.this, R.string.no_users_online, Toast.LENGTH_SHORT).show();


                                } else
                                    Toast.makeText(MainActivity.this, R.string.no_users_online, Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                                break;
                        }


                    }

                    @Override
                    public void onError(QBResponseException responseException) {
                        Log.d(TAG, "error getRandomUser  " + responseException.getMessage());
                        mProgressDialog.dismiss();
                    }
                });


            }

            @Override
            public void onPermissionDenied() {
                Log.d(TAG, "onPermissionDenied() called");
            }

            @Override
            public void onPermissionDeniedBySystem() {
                Log.d(TAG, "onPermissionDeniedBySystem() called");
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
        getRandomUser(getOnlineUsers(), 3);
    }


    private void shareAppToWhatsapp() {
        String whatsAppMessage = "your app link";
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);

    }

    private void blockAdmob() {
    }

    private void callToLevel2User() {
        getRandomUser(getOnlineUsers(), 2);
    }

    private void callToRandomUser() {
        getRandomUser(getOnlineUsers(), 1);

    }


    private boolean isLoggedInChat(int opponentId) {
        if (!QBChatService.getInstance().isLoggedIn()) {
            tryReLoginToChat(opponentId);
            return false;
        }
        return true;
    }

    private void tryReLoginToChat(int opponentId) {
        CallService.start(this, qbUser);
        startCall(opponentId);
    }


    private void startCall(int opponentId) {
        if (isLoggedInChat(opponentId)) {
            ArrayList<Integer> opponentsList = new ArrayList<>();
            opponentsList.add(opponentId);
            QBRTCTypes.QBConferenceType conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

            QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());

            QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);

            WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);

            CallActivity.start(this, false);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelper != null) {
            permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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