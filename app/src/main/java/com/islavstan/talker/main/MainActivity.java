package com.islavstan.talker.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "stas";

    Button level1Btn, level2Btn, blockBtn, shareBtn, femaleBtn;


    PreferenceHelper preferenceHelper;
    QBUser qbUser;
    WebRtcSessionManager webRtcSessionManager;
    boolean isRunForCall;

    QBChatDialog groupChatDialog;

    private QBChatDialogParticipantListener participantListener;


    List<QBUser> allOnlineUsersList = new ArrayList<>();

    boolean firstLaunching = true;
    private ProgressDialog mProgressDialog;


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


// слушатель онлайн юзеров
        participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {

                if (qbPresence.getType().toString().equals("offline")) {

                    Log.d(TAG, "user with id " + qbPresence.getUserId() + " go offline");
                    removeUserById(qbPresence.getUserId());

                } else if (qbPresence.getType().toString().equals("online")) {
                    if (qbPresence.getUserId() != qbUser.getId()) {
                        Log.d(TAG, "user with id " + qbPresence.getUserId() + " go online");
                        List<Integer> list = new ArrayList<>();
                        list.add(qbPresence.getUserId());
                        getRandomUser(list);
                    }

                }


            }
        };


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
                                // groupChatDialog.addParticipantListener(participantListener);
                                DiscussionHistory discussionHistory = new DiscussionHistory();
                                discussionHistory.setMaxStanzas(0);
                                groupChatDialog.join(discussionHistory, new QBEntityCallback() {
                                    @Override
                                    public void onSuccess(Object o, Bundle bundle) {
                                        Log.d("stas", "join success");
                                        // getOnlineUsers();
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


    private Collection<Integer> getOnlineUsers() {
        mProgressDialog.show();
        Collection<Integer> onlineUsers = null;
        try {
            onlineUsers = groupChatDialog.getOnlineUsers();


            for (int i : onlineUsers) {
                Log.d("stas", "onlineUser = " + i);
            }

        } catch (XMPPException e) {

        }
        return onlineUsers;



      /*  if (onlineUsers != null) {
            getRandomUser(onlineUsers);
        }else {
            groupChatDialog.addParticipantListener(participantListener);
            firstLaunching = false;
        }*/
    }


    private void getRandomUser(Collection<Integer> onlineUsers) {
        Log.d(TAG, " getRandomUser ");
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsersByIDs(onlineUsers, pagedRequestBuilder).performAsync(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                Log.d(TAG, " getRandomUser  success");
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

               /* if(firstLaunching) {
                    removeUserById(qbUser.getId());
                    groupChatDialog.addParticipantListener(participantListener);
                    firstLaunching = false;
                }*/

            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error getRandomUser  " + responseException.getMessage());
                mProgressDialog.dismiss();
            }
        });
    }


    private void getFemaleUser(Collection<Integer> onlineUsers) {
        Log.d(TAG, " getFemaleUser ");
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsersByIDs(onlineUsers, pagedRequestBuilder).performAsync(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                Log.d(TAG, " getRandomUser  success");
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

            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error getRandomUser  " + responseException.getMessage());
                mProgressDialog.dismiss();
            }
        });
    }

    private void getLevel2User(Collection<Integer> onlineUsers) {
        Log.d(TAG, " getLevel2User ");
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);

        QBUsers.getUsersByIDs(onlineUsers, pagedRequestBuilder).performAsync(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                Log.d(TAG, " getLevel2User  success");
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

            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error getRandomUser  " + responseException.getMessage());
                mProgressDialog.dismiss();
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
        getFemaleUser(getOnlineUsers());
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
        getLevel2User(getOnlineUsers());
    }

    private void callToRandomUser() {

        getRandomUser(getOnlineUsers());





       /* if (allOnlineUsersList.size() > 0) {
            int min = 0;
            int max = allOnlineUsersList.size()- 1;
            int randomNum = min + (int) (Math.random() * ((max - min) + 1));
            Log.d(TAG, "max = "+ max+ " randomNum = "+ randomNum+ " allOnlineUsersList.size() = "+allOnlineUsersList.size());
            startCall(allOnlineUsersList.get(randomNum).getId());

        } else Toast.makeText(this, R.string.no_users_online, Toast.LENGTH_SHORT).show();*/
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