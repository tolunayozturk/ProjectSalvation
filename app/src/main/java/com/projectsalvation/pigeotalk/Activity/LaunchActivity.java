package com.projectsalvation.pigeotalk.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();

        // TODO: FOR TESTING PURPOSES - DO NOT FORGET TO REMOVE
        // FirebaseAuth.getInstance().signOut();

        // Enable offline capabilities
        // FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(firebaseUser.getUid()).child("presence")
                .child("isOnline").setValue("true");

        databaseReference.child("users").child(firebaseUser.getUid()).child("presence")
                .child("isOnline").onDisconnect().setValue("false");

        databaseReference.child("users").child(firebaseUser.getUid()).child("presence")
                .child("last_seen").setValue(System.currentTimeMillis());

        databaseReference.child("users").child(firebaseUser.getUid()).child("presence")
                .child("last_seen").onDisconnect().setValue(System.currentTimeMillis());

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
