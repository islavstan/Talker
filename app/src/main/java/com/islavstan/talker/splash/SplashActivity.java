package com.islavstan.talker.splash;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.islavstan.talker.main.MainActivity;
import com.islavstan.talker.registration.RegistrationActivity;
import com.islavstan.talker.utils.PreferenceHelper;

public class SplashActivity extends AppCompatActivity {
    PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(getApplicationContext());

        if (preferenceHelper.hasQbUser()) {
            MainActivity.startActivity(this);
            finish();
        } else {
            RegistrationActivity.startActivity(this);
            finish();
        }

    }
}
