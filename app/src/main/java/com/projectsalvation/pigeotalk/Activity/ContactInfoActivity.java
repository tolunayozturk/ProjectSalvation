package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
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

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ContactInfoActivity extends AppCompatActivity {

    // region Resource Declaration
    MaterialToolbar a_contact_info_toolbar;
    ImageView a_contact_info_iv_profile_photo;
    TextView a_contact_info_tv_user_name;
    TextView a_contact_info_tv_last_seen;
    TextView a_contact_info_tv_phone_number;
    TextView a_contact_info_tv_about;
    AppBarLayout a_contact_info_abl;
    CollapsingToolbarLayout a_contact_info_ctl;
    View a_contact_info_gradient;
    // endregion

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private ValueEventListener mPresenceListener;

    private String mUserID;
    private String mUserName;
    private String mUserPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_info);

        // region Resource Assignment
        a_contact_info_toolbar = findViewById(R.id.a_contact_info_toolbar);
        a_contact_info_iv_profile_photo = findViewById(R.id.a_contact_info_iv_profile_photo);
        a_contact_info_tv_user_name = findViewById(R.id.a_contact_info_tv_user_name);
        a_contact_info_tv_last_seen = findViewById(R.id.a_contact_info_tv_last_seen);
        a_contact_info_tv_phone_number = findViewById(R.id.a_contact_info_tv_phone_number);
        a_contact_info_tv_about = findViewById(R.id.a_contact_info_tv_about);
        a_contact_info_abl = findViewById(R.id.a_contact_info_abl);
        a_contact_info_ctl = findViewById(R.id.a_contact_info_ctl);
        a_contact_info_gradient = findViewById(R.id.a_contact_info_gradient);
        // endregion

        setSupportActionBar(a_contact_info_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        Intent i = getIntent();
        mUserID = i.getStringExtra("userID");
        mUserName = i.getStringExtra("userName");
        mUserPhotoUrl = i.getStringExtra("userPhotoUrl");

        Picasso.get().load(mUserPhotoUrl)
                .fit()
                .centerCrop()
                .into(a_contact_info_iv_profile_photo, new Callback() {
                    @Override
                    public void onSuccess() {
                        a_contact_info_gradient.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(mUserPhotoUrl)
                                .fit()
                                .centerCrop()
                                .into(a_contact_info_iv_profile_photo);
                        a_contact_info_gradient.setVisibility(View.VISIBLE);
                    }
                });

        a_contact_info_tv_user_name.setText(mUserName);

        a_contact_info_iv_profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StfalconImageViewer.Builder<>(ContactInfoActivity.this, new String[]{mUserPhotoUrl}, new ImageLoader<String>() {
                    @Override
                    public void loadImage(ImageView imageView, String imageUrl) {
                        Picasso.get().load(imageUrl).into(imageView);
                    }
                }).withStartPosition(0).show();
            }
        });

        mDatabaseReference.child("users").child(mUserID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        a_contact_info_tv_phone_number.setText(dataSnapshot.child("phone_number").getValue().toString());
                        a_contact_info_tv_about.setText(dataSnapshot.child("about").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        listenPresence();
    }

    private void listenPresence() {
        mPresenceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("isOnline").getValue().toString().equals("false")) {

                    String timestamp = dataSnapshot.child("last_seen").getValue().toString();
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timestamp));

                    a_contact_info_tv_last_seen.setText(getString(R.string.text_last_seen,
                            DateUtils.getRelativeTimeSpanString(Long.parseLong(timestamp))));

                } else {
                    a_contact_info_tv_last_seen.setText(getString(R.string.text_online));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("users").child(mUserID).child("presence")
                .addValueEventListener(mPresenceListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabaseReference.child("users").child(mUserID).child("presence")
                .removeEventListener(mPresenceListener);
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
}
