package com.islavstan.talker.registration;

import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.islavstan.talker.R;
import com.islavstan.talker.utils.DatePicker;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText birthET;
    private EditText sexET;
    private Button registerBtn;

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
}
