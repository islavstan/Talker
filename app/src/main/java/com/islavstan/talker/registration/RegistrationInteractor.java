package com.islavstan.talker.registration;


import android.os.Bundle;
import android.util.Log;

import com.islavstan.talker.utils.Constants;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Random;

public class RegistrationInteractor implements RegistrationContract.RegistrationInteractor {

    @Override
    public void registration(String sex, String birthday) {
        final QBUser newUser = new QBUser();
        StringifyArrayList<String> sexList = new StringifyArrayList<>();
        sexList.add(sex);
        newUser.setLogin(randomString() + "_" + birthday);
        newUser.setPassword(Constants.PASSWORD);
        //в тег записываю пол
        newUser.setTags(sexList);
        QBUsers.signUp(newUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser result, Bundle params) {
                initQBUSer(result);
            }

            @Override
            public void onError(QBResponseException e) {


            }
        });
    }


    private String randomString() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        return output;
    }
}
