package com.projectsalvation.pigeotalk.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    // region Resource Declaration
    MaterialToolbar a_settings_toolbar;
    CircleImageView a_settings_civ_profile_photo;
    TextView a_settings_tv_name;
    TextView a_settings_tv_about;
    TextView a_settings_tv_account;
    // endregion

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // region Resource Assignment
        a_settings_toolbar = findViewById(R.id.a_settings_toolbar);
        a_settings_civ_profile_photo = findViewById(R.id.a_settings_civ_profile_photo);
        a_settings_tv_name = findViewById(R.id.a_settings_tv_name);
        a_settings_tv_about = findViewById(R.id.a_settings_tv_about);
        a_settings_tv_account = findViewById(R.id.a_settings_tv_account);
        // endregion

        setSupportActionBar(a_settings_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        a_settings_tv_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this, AccountSettingsActivity.class);
                startActivity(i);
            }
        });
    }

    private void loadProfile() {
        Picasso.get().load(mFirebaseAuth.getCurrentUser().getPhotoUrl())
                .fit()
                .centerCrop()
                .into(a_settings_civ_profile_photo);

        a_settings_civ_profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StfalconImageViewer.Builder<>(SettingsActivity.this, new String[]{mFirebaseAuth.getCurrentUser().getPhotoUrl().toString()}, new ImageLoader<String>() {
                    @Override
                    public void loadImage(ImageView imageView, String imageUrl) {
                        Picasso.get().load(imageUrl).into(imageView);
                    }
                }).withStartPosition(0).show();
            }
        });

        a_settings_tv_name.setText(mFirebaseAuth.getCurrentUser().getDisplayName());

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        a_settings_tv_about.setText(dataSnapshot.child("about").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadProfile();
    }
}
