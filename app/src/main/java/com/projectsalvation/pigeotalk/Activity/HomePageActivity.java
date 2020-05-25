package com.projectsalvation.pigeotalk.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.Adapter.HomeViewPagerAdapter;
import com.projectsalvation.pigeotalk.Fragment.GroupsFragment;
import com.projectsalvation.pigeotalk.Fragment.CameraFragment;
import com.projectsalvation.pigeotalk.Fragment.ChatsFragment;
import com.projectsalvation.pigeotalk.Fragment.StatusFragment;
import com.projectsalvation.pigeotalk.R;
import com.projectsalvation.pigeotalk.Utility.Util;

import java.util.Objects;

public class HomePageActivity extends AppCompatActivity {

    // region Resource Declaration
    static TabLayout a_home_page_tab_layout;
    MaterialToolbar a_home_page_toolbar;
    ViewPager a_home_page_viewpager;
    FloatingActionButton a_home_page_fab_new_message;
    // endregion

    private static final String TAG = "HomePageActivity";

    private static final int PERMISSION_REQUEST_CODE_READ_CONTACTS = 300;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // region Resource Assignment
        a_home_page_tab_layout = findViewById(R.id.a_home_page_tab_layout);
        a_home_page_toolbar = findViewById(R.id.a_home_page_toolbar);
        a_home_page_viewpager = findViewById(R.id.a_home_page_viewpager);
        a_home_page_fab_new_message = findViewById(R.id.a_home_page_fab_new_message);
        // endregion

        setSupportActionBar(a_home_page_toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // region Set up fragments
        CameraFragment cameraFragment = new CameraFragment();
        ChatsFragment chatsFragment = new ChatsFragment();
        StatusFragment statusFragment = new StatusFragment();
        GroupsFragment groupsFragment = new GroupsFragment();

        a_home_page_tab_layout.setupWithViewPager(a_home_page_viewpager);

        HomeViewPagerAdapter viewPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager(), 0);

        viewPagerAdapter.addFragment(cameraFragment, getString(R.string.EMPTY_STRING));
        viewPagerAdapter.addFragment(chatsFragment, getString(R.string.title_chats));
        viewPagerAdapter.addFragment(groupsFragment, getString(R.string.title_groups));
        viewPagerAdapter.addFragment(statusFragment, getString(R.string.title_status));
        // endregion

        a_home_page_viewpager.setAdapter(viewPagerAdapter);

        // Set camera icon to the first tab of the tab layout
        Objects.requireNonNull(a_home_page_tab_layout.getTabAt(0))
                .setIcon(R.drawable.ic_camera_alt_white_24dp);

        // Set starting tab to Chats
        a_home_page_viewpager.setCurrentItem(1);

        // region Shrink the first item in tab layout (camera tab)
        LinearLayout layout = ((LinearLayout) ((LinearLayout) a_home_page_tab_layout.getChildAt(0))
                .getChildAt(0));

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.weight = 0.64f;
        layout.setLayoutParams(layoutParams);
        // endregion

        a_home_page_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // region New message FAB onClick()
        a_home_page_fab_new_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Util.checkPermission(Manifest.permission.READ_CONTACTS, getApplicationContext())) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            HomePageActivity.this, Manifest.permission.READ_CONTACTS)) {

                        // Explain to users why we request this permission
                        MaterialAlertDialogBuilder alertDialogBuilder =
                                new MaterialAlertDialogBuilder(HomePageActivity.this)
                                        .setMessage(R.string.dialog_permission_read_contacts_explanation)
                                        .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Util.requestPermission(new String[]{Manifest.permission.READ_CONTACTS},
                                                        HomePageActivity.this,
                                                        PERMISSION_REQUEST_CODE_READ_CONTACTS);
                                            }
                                        })
                                        .setNegativeButton(R.string.action_not_now, null);

                        AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                        permissionExplanationDialog.show();
                    } else {
                        // No explanation needed; request the permission
                        Util.requestPermission(new String[]{Manifest.permission.READ_CONTACTS},
                                HomePageActivity.this,
                                PERMISSION_REQUEST_CODE_READ_CONTACTS);
                    }
                } else {
                    // Permission has already been granted
                    Intent i = new Intent(HomePageActivity.this, ContactsActivity.class);
                    startActivity(i);
                }
            }
        });
        // endregion
    }

    public static void setBadge(int count, int index) {
        BadgeDrawable badge = a_home_page_tab_layout.getTabAt(index).getOrCreateBadge();
        badge.setNumber(count);
        badge.setVisible(true);
    }

    public static void removeBadge(int index) {
        a_home_page_tab_layout.getTabAt(index).removeBadge();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.a_home_page_menuItem_new_group:
                Intent i = new Intent(HomePageActivity.this, NewGroupActivity.class);
                startActivity(i);
                break;
            case R.id.a_home_page_menuItem_join_group:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage(getString(R.string.text_paste_the_group_id));

                // Set an EditText view to get user input
                final EditText input = new EditText(this);
                input.requestFocus();
                alert.setView(input);

                alert.setPositiveButton("JOIN", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mDatabaseReference.child("groups").addListenerForSingleValueEvent(
                                new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (TextUtils.isEmpty(input.getText()) || input.length() < 32) {
                                            Snackbar.make(a_home_page_toolbar,
                                                    getString(R.string.text_invalid_pigeoid),
                                                    BaseTransientBottomBar.LENGTH_LONG)
                                                    .show();

                                            return;
                                        }

                                        if (dataSnapshot.hasChild(input.getText().toString())) {
                                            mDatabaseReference.child("groups").child(input.getText().toString())
                                                    .child("members").child(mFirebaseAuth.getUid()).setValue("");

                                            mDatabaseReference.child("user_groups").child(mFirebaseAuth.getUid())
                                                    .child(input.getText().toString()).setValue("");

                                            Intent i = new Intent(HomePageActivity.this, GroupChatActivity.class);
                                            i.putExtra("groupID", input.getText().toString());
                                            startActivity(i);

                                            return;
                                        } else {
                                            Snackbar.make(a_home_page_toolbar,
                                                    getString(R.string.text_invalid_pigeoid),
                                                    BaseTransientBottomBar.LENGTH_LONG)
                                                    .show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                }
                        );

                    }
                });

                alert.setNegativeButton(R.string.action_not_now, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert.show();
                break;
            case R.id.a_home_page_menuItem_settings:
                i = new Intent(HomePageActivity.this, SettingsActivity.class);
                startActivity(i);
                break;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(HomePageActivity.this, ContactsActivity.class);
                    startActivity(i);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("isOnline").setValue("false");

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("last_seen").setValue(System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid()).keepSynced(true);
        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).keepSynced(true);
        mDatabaseReference.child("chat_messages").child(mFirebaseAuth.getUid()).keepSynced(true);
        mDatabaseReference.child("chats").child(mFirebaseAuth.getUid()).keepSynced(true);

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("isOnline").setValue("true");

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("last_seen").setValue(System.currentTimeMillis());

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("isOnline").onDisconnect().setValue("false");

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("last_seen").onDisconnect().setValue(System.currentTimeMillis());
    }
}