package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projectsalvation.pigeotalk.Adapter.GroupMembersRVAdapter;
import com.projectsalvation.pigeotalk.DAO.UserDAO;
import com.projectsalvation.pigeotalk.R;
import com.projectsalvation.pigeotalk.Utility.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class GroupInfoActivity extends AppCompatActivity {

    // region Resource Declaration
    AppBarLayout a_group_info_abl;
    CollapsingToolbarLayout a_group_info_ctl;
    ImageView a_group_info_iv_group_photo;
    EditText a_group_info_et_group_name;
    TextView a_group_info_tv_member_count;
    MaterialToolbar a_group_info_toolbar;
    RecyclerView a_group_info_rv_members;
    TextView a_group_info_tv_change_group_photo;
    FrameLayout a_group_info_fl;
    // endregion

    private static final String TAG = "GroupInfoActivity";

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    private static final int INTENT_CHOOSE_PHOTO = 200;
    private static final int INTENT_TAKE_PHOTO = 110;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private ArrayList<UserDAO> mUserDAOS;
    private GroupMembersRVAdapter mGroupMembersRVAdapter;

    private String mGroupID;
    private String mGroupName;
    private String mGroupPhotoUrl;

    private Uri mGroupPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        // region Resource Assignment
        a_group_info_abl = findViewById(R.id.a_group_info_abl);
        a_group_info_ctl = findViewById(R.id.a_group_info_ctl);
        a_group_info_iv_group_photo = findViewById(R.id.a_group_info_iv_group_photo);
        a_group_info_et_group_name = findViewById(R.id.a_group_info_et_group_name);
        a_group_info_tv_member_count = findViewById(R.id.a_group_info_tv_member_count);
        a_group_info_toolbar = findViewById(R.id.a_group_info_toolbar);
        a_group_info_rv_members = findViewById(R.id.a_group_info_rv_members);
        a_group_info_tv_change_group_photo = findViewById(R.id.a_group_info_tv_change_group_photo);
        a_group_info_fl = findViewById(R.id.a_group_info_fl);
        // end region

        setSupportActionBar(a_group_info_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mUserDAOS = new ArrayList<>();

        a_group_info_rv_members.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));

        Intent i = getIntent();
        mGroupID = i.getStringExtra("groupID");
        mGroupName = i.getStringExtra("groupName");
        mGroupPhotoUrl = i.getStringExtra("groupPhotoUrl");

        Picasso.get().load(mGroupPhotoUrl)
                .fit()
                .centerCrop()
                .into(a_group_info_iv_group_photo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(mGroupPhotoUrl)
                                .fit()
                                .centerCrop()
                                .into(a_group_info_iv_group_photo);
                    }
                });

        a_group_info_iv_group_photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new StfalconImageViewer.Builder<>(GroupInfoActivity.this, new String[]{mGroupPhotoUrl}, new ImageLoader<String>() {
                    @Override
                    public void loadImage(ImageView imageView, String imageUrl) {
                        Picasso.get().load(imageUrl).into(imageView);
                    }
                }).withStartPosition(0).show();
                return true;
            }
        });

        a_group_info_et_group_name.setText(mGroupName);
        mDatabaseReference.child("groups").child(mGroupID).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        a_group_info_tv_member_count.setText(getString(R.string.text_member_count, dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        a_group_info_tv_change_group_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(GroupInfoActivity.this);

                builder.setItems(
                        new String[]{getString(R.string.text_take_a_photo), getString(R.string.text_choose_from_gallery)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        launchCamera();
                                        break;
                                    case 1:
                                        launchPhotoLibrary();
                                        break;
                                } // end switch
                            }
                        }
                );

                AlertDialog addProfilePhotoDialog = builder.create();
                addProfilePhotoDialog.show();
            }
        });

        a_group_info_et_group_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    mDatabaseReference.child("groups").child(mGroupID).child("groupName")
                            .setValue(a_group_info_et_group_name.getText().toString());

                    InputMethodManager imm = (InputMethodManager) getApplicationContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);

                    imm.hideSoftInputFromWindow(a_group_info_et_group_name.getWindowToken(), 0);
                    a_group_info_et_group_name.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        });

        mDatabaseReference.child("groups").child(mGroupID).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot member : dataSnapshot.getChildren()) {
                            final UserDAO user = new UserDAO(
                                    "",
                                    "",
                                    "",
                                    ""
                            );

                            if (mFirebaseAuth.getUid().equals(member.getKey())) {
                                user.setUserID(mFirebaseAuth.getUid());
                                user.setDisplayName(mFirebaseAuth.getCurrentUser().getDisplayName());
                                user.setPhotoUrl(mFirebaseAuth.getCurrentUser().getPhotoUrl().toString());

                                mDatabaseReference.child("users").child(mFirebaseAuth.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                user.setAbout(dataSnapshot.child("about").getValue().toString());

                                                mUserDAOS.add(user);
                                                mGroupMembersRVAdapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                continue;
                            }

                            user.setUserID(member.getKey());
                            mDatabaseReference.child("user_contacts").child(mFirebaseAuth.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull final DataSnapshot contactsSnapshot) {
                                            mDatabaseReference.child("users").child(member.getKey())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (contactsSnapshot.hasChild(member.getKey())) {
                                                                user.setDisplayName(contactsSnapshot
                                                                        .child(member.getKey()).getValue().toString());
                                                            } else {
                                                                user.setDisplayName(dataSnapshot
                                                                        .child("phone_number").getValue().toString());
                                                            }

                                                            user.setPhotoUrl(dataSnapshot
                                                                    .child("profile_photo_url").getValue().toString());

                                                            user.setAbout(dataSnapshot
                                                                    .child("about").getValue().toString());

                                                            mUserDAOS.add(user);
                                                            mGroupMembersRVAdapter.notifyDataSetChanged();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        mGroupMembersRVAdapter = new GroupMembersRVAdapter(GroupInfoActivity.this, mUserDAOS);
        a_group_info_rv_members.setAdapter(mGroupMembersRVAdapter);
    }

    private void launchCamera() {
        if (!Util.checkPermission(Manifest.permission.CAMERA, getApplicationContext())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    GroupInfoActivity.this, Manifest.permission.CAMERA)) {

                // Explain to users why we request this permission
                MaterialAlertDialogBuilder alertDialogBuilder =
                        new MaterialAlertDialogBuilder(GroupInfoActivity.this)
                                .setMessage(R.string.dialog_permission_camera_explanation)
                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                                                GroupInfoActivity.this,
                                                PERMISSION_REQUEST_CODE_CAMERA);
                                    }
                                })
                                .setNegativeButton(R.string.action_not_now, null);

                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                permissionExplanationDialog.show();
            } else {
                // No explanation needed; request the permission
                Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                        GroupInfoActivity.this,
                        PERMISSION_REQUEST_CODE_CAMERA);
            }
        } else {
            // Permission has already been granted
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                File profilePhotoFile = null;

                try {
                    profilePhotoFile = createImageFile();
                } catch (IOException e) {
                    Snackbar.make(a_group_info_iv_group_photo,
                            R.string.text_profile_photo_upload_failed,
                            BaseTransientBottomBar.LENGTH_LONG)
                            .show();
                }

                if (profilePhotoFile != null) {
                    mGroupPhotoUri = FileProvider.getUriForFile(this,
                            "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mGroupPhotoUri);
                    startActivityForResult(takePhotoIntent, INTENT_TAKE_PHOTO);
                }
            }
        }
    }

    private void launchPhotoLibrary() {
        Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .setType("image/*");

        if (choosePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(
                    choosePhotoIntent, INTENT_CHOOSE_PHOTO
            );
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());

        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                        File profilePhotoFile = null;

                        try {
                            profilePhotoFile = createImageFile();
                        } catch (IOException e) {
                            Snackbar.make(a_group_info_iv_group_photo,
                                    R.string.text_profile_photo_upload_failed,
                                    BaseTransientBottomBar.LENGTH_LONG)
                                    .show();
                        }

                        if (profilePhotoFile != null) {
                            mGroupPhotoUri = FileProvider.getUriForFile(this,
                                    "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mGroupPhotoUri);
                            startActivityForResult(takePhotoIntent, INTENT_TAKE_PHOTO);
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case INTENT_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {

                    if (mGroupPhotoUri != null) {
                        Picasso.get().load(mGroupPhotoUri).noFade()
                                .into(a_group_info_iv_group_photo);

                        uploadAndUpdate(mGroupPhotoUri);
                    } else {
                        Snackbar.make(a_group_info_iv_group_photo,
                                R.string.text_profile_photo_upload_failed,
                                BaseTransientBottomBar.LENGTH_LONG)
                                .show();
                    }
                }
                break;
            case INTENT_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    mGroupPhotoUri = data.getData();

                    if (mGroupPhotoUri != null) {
                        Picasso.get().load(mGroupPhotoUri).noFade()
                                .into(a_group_info_iv_group_photo);

                        uploadAndUpdate(mGroupPhotoUri);
                    } else {
                        Snackbar.make(a_group_info_iv_group_photo,
                                R.string.text_profile_photo_upload_failed,
                                BaseTransientBottomBar.LENGTH_LONG)
                                .show();
                    }
                }
                break;
            default:
                super.onActivityResult(resultCode, resultCode, data);
                break;
        }
    }

    private void uploadAndUpdate(Uri profilePhotoUri) {
        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(profilePhotoUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String path = "groups" + "/" + mGroupID + "/" + "gp.jpeg";
        UploadTask uploadTask = mStorageReference.child(path).putStream(stream);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String downloadUrl = Objects.requireNonNull(task.getResult()).toString();

                        mDatabaseReference.child("groups").child(mGroupID)
                                .child("groupPhotoUrl").setValue(downloadUrl);
                    }
                });

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
}
