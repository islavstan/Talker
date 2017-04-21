package com.islavstan.talker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.quickblox.users.model.QBUser;


public class PreferenceHelper {
    private static PreferenceHelper instance;
    private Context context;
    private SharedPreferences preferences;

    public static final String QB_USER_LOGIN = "qb_user_login";
    public static final String QB_USER_PASSWORD = "qb_user_password";
    public static final String QB_USER_SEX = "qb_user_sex";
    public static final String QB_USER_ID = "qb_user_id";

    public static PreferenceHelper getInstance() {
        if (instance == null) {
            instance = new PreferenceHelper();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);

    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return preferences.getString(key, "");

    }

    public void saveQbUser(QBUser qbUser, String sex) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(QB_USER_ID, qbUser.getId());
        editor.putString(QB_USER_LOGIN, qbUser.getLogin());
        editor.putString(QB_USER_PASSWORD, Constants.PASSWORD);
        editor.putString(QB_USER_SEX, sex);
        editor.apply();

    }

    public boolean hasQbUser() {
        int qbUser = preferences.getInt(QB_USER_ID, 0);
        return qbUser != 0;
    }

    public QBUser getQbUser() {
        if (hasQbUser()) {
            int id = preferences.getInt(QB_USER_ID, 0);
            String login = preferences.getString(QB_USER_LOGIN, "");
            String password = preferences.getString(QB_USER_PASSWORD, "");
           // String sex = preferences.getString(QB_USER_SEX, "");
            QBUser user = new QBUser(login, password);
            user.setId(id);
            return user;
        } else {
            return null;
        }
    }

}
