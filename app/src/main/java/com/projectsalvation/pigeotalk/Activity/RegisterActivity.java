package com.projectsalvation.pigeotalk.Activity;

import android.Manifest;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {

    // region Resource Declaration
    CircleImageView a_register_civ_profile_photo;
    ImageView a_register_iv_add_photo_icon;
    EditText a_register_et_user_name;
    Button a_register_btn_next;
    // endregion

    private static final String TAG = "RegisterActivity";

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    private static final int INTENT_CHOOSE_PHOTO = 200;
    private static final int INTENT_TAKE_PHOTO = 110;

    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private FirebaseAuth mFirebaseAuth;
    private Uri mProfilePhotoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // region Resouce Assignment
        a_register_civ_profile_photo = findViewById(R.id.a_register_civ_profile_photo);
        a_register_iv_add_photo_icon = findViewById(R.id.a_register_iv_add_photo_icon);
        a_register_et_user_name = findViewById(R.id.a_register_et_user_name);
        a_register_btn_next = findViewById(R.id.a_register_btn_next);
        // endregion

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();


        a_register_civ_profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(RegisterActivity.this);

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

        a_register_btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(a_register_et_user_name.getText())) {
                    Snackbar.make(a_register_et_user_name,
                            R.string.text_user_name_cannot_be_empty,
                            BaseTransientBottomBar.LENGTH_LONG)
                            .show();

                    return;
                }

                // Disable the button to avoid multiple user clicks
                a_register_btn_next.setEnabled(false);

                mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("name")
                        .setValue(a_register_et_user_name.getText().toString());

                mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("about")
                        .setValue("Hey there! I'm using PigeoTalk.");

                mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("phone_number")
                        .setValue(mFirebaseAuth.getCurrentUser().getPhoneNumber());

                mDatabaseReference.child("registered_numbers")
                        .child(mFirebaseAuth.getCurrentUser().getPhoneNumber())
                        .setValue(mFirebaseAuth.getUid());

                // region Upload user profile photo and get its download url

                // Because putBytes() accepts a byte[], it requires our app
                // to hold the entire contents of a file in memory at once.
                // Consider using putStream() or putFile() to use less memory.
                a_register_civ_profile_photo.setDrawingCacheEnabled(true);
                a_register_civ_profile_photo.buildDrawingCache();
                Bitmap bitmap = ((BitmapDrawable) a_register_civ_profile_photo.getDrawable()).getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = mStorageReference.child(mFirebaseAuth.getUid()
                        + "/profile-photo.jpeg").putBytes(data);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getStorage().getDownloadUrl()
                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        String downloadUrl = Objects.requireNonNull(task.getResult())
                                                .toString();

                                        mDatabaseReference.child("users")
                                                .child(mFirebaseAuth.getUid()).child("profile_photo_url")
                                                .setValue(downloadUrl);

                                        Util.updateUserProfile(mFirebaseAuth.getCurrentUser(),
                                                a_register_et_user_name.getText().toString(),
                                                Uri.parse(downloadUrl));

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(a_register_civ_profile_photo,
                                        R.string.text_profile_photo_upload_failed,
                                        BaseTransientBottomBar.LENGTH_LONG)
                                        .show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(a_register_civ_profile_photo,
                                R.string.text_profile_photo_upload_failed,
                                BaseTransientBottomBar.LENGTH_LONG)
                                .show();
                    }
                });
                // endregion

                Intent i = new Intent(RegisterActivity.this, HomePageActivity.class);
                startActivity(i);
            }
        });
    }

    private void launchCamera() {
        if (!Util.checkPermission(Manifest.permission.CAMERA, getApplicationContext())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    RegisterActivity.this, Manifest.permission.CAMERA)) {

                // Explain to users why we request this permission
                MaterialAlertDialogBuilder alertDialogBuilder =
                        new MaterialAlertDialogBuilder(RegisterActivity.this)
                                .setMessage(R.string.dialog_permission_camera_explanation)
                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                                                RegisterActivity.this,
                                                PERMISSION_REQUEST_CODE_CAMERA);
                                    }
                                })
                                .setNegativeButton(R.string.action_not_now, null);

                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                permissionExplanationDialog.show();
            } else {
                // No explanation needed; request the permission
                Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                        RegisterActivity.this,
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
                    Snackbar.make(a_register_civ_profile_photo,
                            R.string.text_profile_photo_upload_failed,
                            BaseTransientBottomBar.LENGTH_LONG)
                            .show();
                }

                if (profilePhotoFile != null) {
                    mProfilePhotoUri = FileProvider.getUriForFile(this,
                            "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mProfilePhotoUri);
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
                            Snackbar.make(a_register_civ_profile_photo,
                                    R.string.text_profile_photo_upload_failed,
                                    BaseTransientBottomBar.LENGTH_LONG)
                                    .show();
                        }

                        if (profilePhotoFile != null) {
                            mProfilePhotoUri = FileProvider.getUriForFile(this,
                                    "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mProfilePhotoUri);
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

                    if (mProfilePhotoUri != null) {
                        Picasso.get().load(mProfilePhotoUri).noFade()
                                .resize(800, 800)
                                .centerInside()
                                .into(a_register_civ_profile_photo);
                    } else {
                        Snackbar.make(a_register_civ_profile_photo,
                                R.string.text_profile_photo_upload_failed,
                                BaseTransientBottomBar.LENGTH_LONG)
                                .show();
                    }

                    a_register_iv_add_photo_icon.setVisibility(View.INVISIBLE);
                }
                break;
            case INTENT_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedPhotoUri = Objects.requireNonNull(data).getData();

                    Picasso.get().load(selectedPhotoUri).noFade()
                            .resize(800, 800)
                            .centerInside()
                            .into(a_register_civ_profile_photo);

                    a_register_iv_add_photo_icon.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                super.onActivityResult(resultCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onBackPressed() {
    }
}
