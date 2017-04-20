package com.islavstan.talker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.quickblox.users.model.QBUser;


public class PreferenceHelper {
    private static PreferenceHelper instance;
    private Context context;
    private SharedPreferences preferences;

    public static final String QB_USER_LOGIN = "qb_user_login";
    public static final String QB_USER_PASSWORD = "qb_user_password";
    public static final String QB_USER_SEX = "qb_user_sex";

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
        if (qbUser.getId() != null)
            editor.putString(QB_USER_LOGIN, qbUser.getLogin());
        editor.putString(QB_USER_PASSWORD, qbUser.getPassword());
        editor.putString(QB_USER_SEX, sex);
        editor.apply();

    }

    public boolean hasQbUser() {
        String qbUser = preferences.getString(QB_USER_LOGIN, "");
        if (qbUser.equals(""))
            return false;
        else return true;
    }

    public QBUser getQbUser() {
        if (hasQbUser()) {
            String login = preferences.getString(QB_USER_LOGIN, "");
            String password = preferences.getString(QB_USER_PASSWORD, "");
            String sex = preferences.getString(QB_USER_SEX, "");
            QBUser user = new QBUser(login, password);
            return user;
        } else {
            return null;
        }
    }

}
