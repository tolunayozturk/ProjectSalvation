package com.projectsalvation.pigeotalk.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.projectsalvation.pigeotalk.R;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        // TODO: Implement the splash screen and remove this redirection
        Intent i = new Intent(LaunchActivity.this, ValidatePhoneNumberActivity.class);
        startActivity(i);
    }
}
