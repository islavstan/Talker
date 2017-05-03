package com.islavstan.free_talker.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.islavstan.free_talker.App;
import com.islavstan.free_talker.Manifest;
import com.islavstan.free_talker.R;
import com.islavstan.free_talker.activities.CallActivity;
import com.islavstan.free_talker.call_functions.service.CallService;
import com.islavstan.free_talker.gcm.PushNotificationSender;
import com.islavstan.free_talker.utils.Constants;
import com.islavstan.free_talker.utils.Consts;
import com.islavstan.free_talker.utils.InternetConnection;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.islavstan.free_talker.utils.WebRtcSessionManager;
import com.master.permissionhelper.PermissionHelper;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.ServiceZone;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
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
    private AdView mAdView;

    int callType;
    boolean internetConnection = true;

    ImageButton refreshBtn;
    TextView onlineTV;
    List<QBUser> listForOnline = new ArrayList<>();


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
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        internetConnection = InternetConnection.hasConnection(this);


        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        refreshBtn = (ImageButton) findViewById(R.id.refreshBtn);
        onlineTV = (TextView) findViewById(R.id.onlineTV);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshOnlineUsers();
            }
        });

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
            Log.d(TAG, qbUser.getId()+" my id");

        }

        if (isRunForCall && webRtcSessionManager.getCurrentSession() != null) {
            CallActivity.start(MainActivity.this, true);
        }


    }

    private void refreshOnlineUsers() {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(1);
        pagedRequestBuilder.setPerPage(50);
        QBUsers.getUsersByIDs(getOnlineUsersForCount(), pagedRequestBuilder).performAsync(new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                Log.d(TAG, " refreshOnlineUsers  success");
                int male = 0;
                int female = 0;
                listForOnline.clear();
                listForOnline.addAll(result);
                if (listForOnline.size() > 0) {
                    for (int i = 0; i < listForOnline.size(); i++) {
                        if (listForOnline.get(i).getTags().get(0).equals("Male")) {
                            male++;
                        } else female++;
                    }
                    Log.d(TAG, "Online:" + " Male " + male + " Female " + female);
                    onlineTV.setText("Online:" + " Male " + male + ", Female " + female);
                } else onlineTV.setText(R.string.online);


            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "error getRandomUser  " + responseException.getMessage());

            }
        });
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
        if (mAdView != null) {
            mAdView.pause();
        }

        if (groupChatDialog != null) {
            try {
                groupChatDialog.leave();
                groupChatDialog = null;
            } catch (XMPPException | SmackException.NotConnectedException e) {

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }


    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("stas", "onStart");
        if (groupChatDialog == null) {
            getChatDialogById();
        }
    }

    public void signIn(final QBUser qbUser) {
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
                                refreshOnlineUsers();
                                Log.d("stas", "join success");
                                if (!internetConnection) {
                                    internetConnection = true;
                                    getRandomUser(getOnlineUsers(), callType);
                                }
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
            if (groupChatDialog != null)
                onlineUsers = groupChatDialog.getOnlineUsers();
        } catch (XMPPException e) {

        }
        return onlineUsers;
    }


    private Collection<Integer> getOnlineUsersForCount() {
        Collection<Integer> onlineUsers = null;
        try {
            if (groupChatDialog != null)
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
                                    mProgressDialog.dismiss();
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
                                    mProgressDialog.dismiss();


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
                if (InternetConnection.hasConnection(this))
                    callToRandomUser();
                else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    internetConnection = false;
                }
                break;

            case R.id.level2Btn:
                if (InternetConnection.hasConnection(this))
                    callToLevel2User();
                else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
                    internetConnection = false;
                }
                break;

            case R.id.blockBtn:
                Toast.makeText(this, R.string.free, Toast.LENGTH_LONG).show();
                blockAdmob();
                break;

            case R.id.shareBtn:
                shareAppToWhatsapp();
                break;
            case R.id.femaleBtn:
                Toast.makeText(this, R.string.free, Toast.LENGTH_LONG).show();
                blockAdmob();
                break;
        }
    }


    private void callToFemale() {
        callType = 3;
        if (!internetConnection || groupChatDialog == null) {
            callSecondTime();
        } else
            getRandomUser(getOnlineUsers(), 3);
    }


    private void shareAppToWhatsapp() {
        PackageManager pm = this.getPackageManager();
        if (isPackageInstalled("com.whatsapp", pm)) {
            String whatsAppMessage = "https://play.google.com/store/apps/details?id=com.islavstan.free_talker";
            Log.d("stas", whatsAppMessage);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, whatsAppMessage);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } else Toast.makeText(MainActivity.this, R.string.whatsapp, Toast.LENGTH_SHORT).show();

    }


    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void blockAdmob() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.islavstan.talker")));
    }

    private void callToLevel2User() {
        callType = 2;
        if (preferenceHelper.getBoolean(PreferenceHelper.LEVEL2_EXIST)) {
            if (!internetConnection || groupChatDialog == null) {
                callSecondTime();
            } else
                getRandomUser(getOnlineUsers(), 2);
        } else {
            if (preferenceHelper.getInt(PreferenceHelper.LEVEL2) >= 20) {
                QBUser user = new QBUser();
                user.setLogin(preferenceHelper.getLogin());
                user.setId(preferenceHelper.getId());
                StringifyArrayList list = new StringifyArrayList();
                list.add(preferenceHelper.getSex());
                list.add("level2");
                user.setTags(list);
                QBUsers.updateUser(user).performAsync(new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        preferenceHelper.putBoolean(PreferenceHelper.LEVEL2_EXIST, true);
                        if (!internetConnection || groupChatDialog == null) {
                            callSecondTime();
                        } else
                            getRandomUser(getOnlineUsers(), 2);
                    }

                    @Override
                    public void onError(QBResponseException responseException) {
                        Toast.makeText(MainActivity.this, responseException.getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                });


            } else {
                Toast.makeText(MainActivity.this, "To enjoy this feature, you need 20 conversations that last more than 4 minutes. Currently you have " +
                        preferenceHelper.getInt(PreferenceHelper.LEVEL2) + " such conversation(s)", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void callToRandomUser() {
        callType = 1;
        if (!internetConnection || groupChatDialog == null) {
            callSecondTime();
        } else
          //  getRandomUser(getOnlineUsers(), 1);
        startCall(27143020);
    }

    private void callSecondTime() {
        mProgressDialog.show();
        QBSettings.getInstance().init(getApplicationContext(), App.APP_ID, App.AUTH_KEY, App.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(App.ACCOUNT_KEY);
        QBSettings.getInstance().setEndpoints("https://api.quickblox.com", "chat.quickblox.com", ServiceZone.PRODUCTION);
        QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        CallService.start(MainActivity.this, qbUser);
        signIn(qbUser);
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
            ArrayList<Integer> sendList = getIds();
            removeId(opponentId, sendList);
            ArrayList<Integer> opponentsList = new ArrayList<>();
            opponentsList.add(opponentId);
            QBRTCTypes.QBConferenceType conferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
            QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());
            QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
            WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);

            
            ArrayList<Integer> recipients = new ArrayList<Integer>();
            recipients.add(opponentId);
            PushNotificationSender.sendPushMessage(recipients, qbUser.getLogin());



            CallActivity.start(this, false, sendList, opponentId);

        }
    }


    private ArrayList<Integer> getIds() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < allOnlineUsersList.size(); i++) {
            list.add(allOnlineUsersList.get(i).getId());
        }
        return list;
    }


    private void removeId(int id, ArrayList<Integer> list) {
        if (list.size() > 1) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == id) {
                    list.remove(i);

                }
            }

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