package com.islavstan.talker.registration;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.islavstan.talker.R;
import com.islavstan.talker.main.MainActivity;
import com.islavstan.talker.utils.DatePicker;
import com.islavstan.talker.utils.PreferenceHelper;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, RegistrationContract.View {

    private EditText birthET;
    private EditText sexET;
    private Button registerBtn;
    PreferenceHelper preferenceHelper;
    RegistrationPresenter presenter;
    private ProgressDialog mProgressDialog;



    public static void startActivity(Context context, int flags) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(flags);
        context.startActivity(intent);
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        context.startActivity(intent);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        bindViews();


    }

    private void bindViews() {
        birthET = (EditText) findViewById(R.id.date_of_birth);
        sexET = (EditText) findViewById(R.id.sex);
        registerBtn = (Button) findViewById(R.id.registerBtn);
        preferenceHelper = PreferenceHelper.getInstance();
        preferenceHelper.init(getApplicationContext());
        presenter = new RegistrationPresenter(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);

        registerBtn.setOnClickListener(this);
        sexET.setOnClickListener(this);
        birthET.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        switch (viewId) {
            case R.id.date_of_birth:
                showDateDialog();
                break;
            case R.id.sex:
                showSexDialog();
                break;
            case R.id.registerBtn:
                registration();
        }
    }

    private void registration() {
        String date = birthET.getText().toString();
        String sex = sexET.getText().toString();
        if (date.equals("") || sex.equals("")) {
            Toast.makeText(this, R.string.blank_fields, Toast.LENGTH_SHORT).show();
        } else {
            registration(sex, date, preferenceHelper);
        }
    }


    private void showSexDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sex);
        builder.setItems(R.array.sex_array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0)
                    sexET.setText(R.string.male);
                else sexET.setText(R.string.female);
            }
        });
        builder.create().show();
    }


    private void showDateDialog() {
        DialogFragment dateDialog = new DatePicker();
        dateDialog.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void registration(String sex, String birthday, PreferenceHelper preferenceHelper) {
        mProgressDialog.show();
        presenter.registration(sex, birthday, preferenceHelper);
    }

    @Override
    public void onRegistrationSuccess() {
        mProgressDialog.dismiss();
        Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();
        MainActivity.startActivity(this, Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);


    }

    @Override
    public void onRegistrationFailure(String message) {
        mProgressDialog.dismiss();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
