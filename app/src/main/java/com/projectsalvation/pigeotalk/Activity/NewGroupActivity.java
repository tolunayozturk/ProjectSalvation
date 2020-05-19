package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.IntentCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projectsalvation.pigeotalk.R;
import com.projectsalvation.pigeotalk.Utility.Util;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewGroupActivity extends AppCompatActivity {

    // region Resource Declaration
    MaterialToolbar a_new_group_toolbar;
    CircleImageView a_new_group_civ_group_photo;
    ImageView a_new_group_iv_add_photo_icon;
    EditText a_new_group_et_group_name;
    Button a_new_group_btn_next;
    // endregion

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    private static final int INTENT_CHOOSE_PHOTO = 200;
    private static final int INTENT_TAKE_PHOTO = 110;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private Uri mGroupPhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        // region Resource Assignment
        a_new_group_toolbar = findViewById(R.id.a_new_group_toolbar);
        a_new_group_civ_group_photo = findViewById(R.id.a_new_group_civ_group_photo);
        a_new_group_et_group_name = findViewById(R.id.a_new_group_et_group_name);
        a_new_group_iv_add_photo_icon = findViewById(R.id.a_new_group_iv_add_photo_icon);
        a_new_group_btn_next = findViewById(R.id.a_new_group_btn_next);
        // endregion

        setSupportActionBar(a_new_group_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        a_new_group_civ_group_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder =
                        new MaterialAlertDialogBuilder(NewGroupActivity.this);

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

        a_new_group_btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(a_new_group_et_group_name.getText())) {
                    Snackbar.make(a_new_group_et_group_name,
                            getString(R.string.text_group_name_cannot_be_empty),
                            BaseTransientBottomBar.LENGTH_LONG)
                            .show();

                    return;
                }

                // Disable the button to avoid multiple user clicks
                a_new_group_btn_next.setEnabled(false);

                final String newGroupUID = UUID.randomUUID().toString().replace("-", "");

                mDatabaseReference.child("groups").child(newGroupUID).child("members")
                        .child(mFirebaseAuth.getUid()).setValue("");

                mDatabaseReference.child("groups").child(newGroupUID).child("groupId")
                        .setValue(newGroupUID);

                mDatabaseReference.child("user_groups").child(mFirebaseAuth.getUid())
                        .child(newGroupUID).setValue("");

                mDatabaseReference.child("groups").child(newGroupUID).child("groupName")
                        .setValue(a_new_group_et_group_name.getText().toString());

                InputStream stream = null;
                UploadTask uploadTask = null;

                a_new_group_civ_group_photo.setDrawingCacheEnabled(true);
                a_new_group_civ_group_photo.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) a_new_group_civ_group_photo.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                uploadTask = mStorageReference.child("groups/" + newGroupUID
                        + "/gp.jpeg").putBytes(data);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        String downloadUrl = Objects.requireNonNull(task.getResult())
                                                .toString();

                                        mDatabaseReference.child("groups").child(newGroupUID)
                                                .child("groupPhotoUrl").setValue(downloadUrl);

                                        Intent i = new Intent(NewGroupActivity.this, GroupChatActivity.class);
                                        i.putExtra("groupID", newGroupUID);
                                        i.putExtra("photoUrl", downloadUrl);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(i);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            } // end onClick()
        });
    }

    private void launchCamera() {
        if (!Util.checkPermission(Manifest.permission.CAMERA, getApplicationContext())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    NewGroupActivity.this, Manifest.permission.CAMERA)) {

                // Explain to users why we request this permission
                MaterialAlertDialogBuilder alertDialogBuilder =
                        new MaterialAlertDialogBuilder(NewGroupActivity.this)
                                .setMessage(R.string.dialog_permission_camera_explanation)
                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                                                NewGroupActivity.this,
                                                PERMISSION_REQUEST_CODE_CAMERA);
                                    }
                                })
                                .setNegativeButton(R.string.action_not_now, null);

                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                permissionExplanationDialog.show();
            } else {
                // No explanation needed; request the permission
                Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                        NewGroupActivity.this,
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
                    Snackbar.make(a_new_group_civ_group_photo,
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
                            Snackbar.make(a_new_group_civ_group_photo,
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
                                .fit()
                                .centerInside()
                                .into(a_new_group_civ_group_photo);
                    } else {
                        Snackbar.make(a_new_group_civ_group_photo,
                                R.string.text_profile_photo_upload_failed,
                                BaseTransientBottomBar.LENGTH_LONG)
                                .show();
                    }

                    a_new_group_iv_add_photo_icon.setVisibility(View.INVISIBLE);
                }
                break;
            case INTENT_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    mGroupPhotoUri = data.getData();

                    if (mGroupPhotoUri != null) {
                        Picasso.get().load(mGroupPhotoUri).noFade()
                                .fit()
                                .centerInside()
                                .into(a_new_group_civ_group_photo);
                    } else {
                        Snackbar.make(a_new_group_civ_group_photo,
                                R.string.text_profile_photo_upload_failed,
                                BaseTransientBottomBar.LENGTH_LONG)
                                .show();
                    }

                    a_new_group_iv_add_photo_icon.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                super.onActivityResult(resultCode, resultCode, data);
                break;
        }
    }
}
