package com.projectsalvation.pigeotalk.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: FOR TESTING PURPOSES - DO NOT FORGET TO REMOVE
        // FirebaseAuth.getInstance().signOut();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            Intent i = new Intent(LaunchActivity.this, HomePageActivity.class);
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(LaunchActivity.this, ValidatePhoneNumberActivity.class);
            startActivity(i);
            finish();
        }

        // TODO: Implement a proper splash screen!
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
