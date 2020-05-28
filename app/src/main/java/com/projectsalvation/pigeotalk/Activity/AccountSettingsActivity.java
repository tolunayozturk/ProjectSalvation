package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

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
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    // region Resource Declaration
    MaterialToolbar a_account_sett_toolbar;
    CircleImageView a_account_sett_civ_profile_photo;
    EditText a_account_sett_et_user_name;
    TextView a_account_sett_tv_phone_number;
    TextView a_account_sett_et_about;
    Chip a_account_sett_chip_add_photo;
    // endregion

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private Uri mProfilePhotoUri;

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    private static final int INTENT_CHOOSE_PHOTO = 200;
    private static final int INTENT_TAKE_PHOTO = 110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        // region Resource Assignment
        a_account_sett_toolbar = findViewById(R.id.a_account_sett_toolbar);
        a_account_sett_civ_profile_photo = findViewById(R.id.a_account_sett_civ_profile_photo);
        a_account_sett_et_user_name = findViewById(R.id.a_account_sett_et_user_name);
        a_account_sett_tv_phone_number = findViewById(R.id.a_account_sett_tv_phone_number);
        a_account_sett_et_about = findViewById(R.id.a_account_sett_et_about);
        a_account_sett_chip_add_photo = findViewById(R.id.a_account_sett_chip_add_photo);
        // endregion

        setSupportActionBar(a_account_sett_toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        Picasso.get().load(mFirebaseAuth.getCurrentUser().getPhotoUrl())
                .fit()
                .centerCrop()
                .into(a_account_sett_civ_profile_photo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(mFirebaseAuth.getCurrentUser().getPhotoUrl())
                                .fit()
                                .centerCrop()
                                .into(a_account_sett_civ_profile_photo);
                    }
                });

        a_account_sett_et_user_name.setText(mFirebaseAuth.getCurrentUser().getDisplayName());

        a_account_sett_tv_phone_number.setText(mFirebaseAuth.getCurrentUser().getPhoneNumber());

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        a_account_sett_et_about.setText(dataSnapshot.child("about").getValue().toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        // region Change Photo
        a_account_sett_civ_profile_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AccountSettingsActivity.this);

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

        a_account_sett_chip_add_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(AccountSettingsActivity.this);

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
        // endregion

        a_account_sett_et_user_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d("AccountSettingsActivity", "onEditorAction: " + actionId);
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    Util.updateUserProfile(mFirebaseAuth.getCurrentUser(), a_account_sett_et_user_name
                            .getText().toString(), mFirebaseAuth.getCurrentUser().getPhotoUrl());

                    mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("name")
                            .setValue(a_account_sett_et_user_name.getText().toString());

                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(a_account_sett_et_user_name.getWindowToken(), 0);
                    a_account_sett_et_user_name.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        });

        a_account_sett_et_about.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                    mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("about")
                            .setValue(a_account_sett_et_about.getText().toString());

                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(a_account_sett_et_about.getWindowToken(), 0);
                    a_account_sett_et_about.setCursorVisible(false);
                    return true;
                }
                return false;
            }
        });

        a_account_sett_civ_profile_photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new StfalconImageViewer.Builder<>(AccountSettingsActivity.this, new String[]{mFirebaseAuth.getCurrentUser().getPhotoUrl().toString()}, new ImageLoader<String>() {
                    @Override
                    public void loadImage(ImageView imageView, String imageUrl) {
                        Picasso.get().load(imageUrl).into(imageView);
                    }
                }).withStartPosition(0).show();
                return true;
            }
        });
    }

    private void launchCamera() {
        if (!Util.checkPermission(Manifest.permission.CAMERA, getApplicationContext())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    AccountSettingsActivity.this, Manifest.permission.CAMERA)) {

                // Explain to users why we request this permission
                MaterialAlertDialogBuilder alertDialogBuilder =
                        new MaterialAlertDialogBuilder(AccountSettingsActivity.this)
                                .setMessage(R.string.dialog_permission_camera_explanation)
                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                                                AccountSettingsActivity.this,
                                                PERMISSION_REQUEST_CODE_CAMERA);
                                    }
                                })
                                .setNegativeButton(R.string.action_not_now, null);

                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                permissionExplanationDialog.show();
            } else {
                // No explanation needed; request the permission
                Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                        AccountSettingsActivity.this,
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
                    Snackbar.make(a_account_sett_civ_profile_photo,
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
                            Snackbar.make(a_account_sett_civ_profile_photo,
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
                                .into(a_account_sett_civ_profile_photo);

                        uploadAndUpdate(mProfilePhotoUri);
                    } else {
                        Snackbar.make(a_account_sett_civ_profile_photo,
                                R.string.text_profile_photo_upload_failed,
                                BaseTransientBottomBar.LENGTH_LONG)
                                .show();
                    }
                }
                break;
            case INTENT_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    mProfilePhotoUri = data.getData();

                    if (mProfilePhotoUri != null) {
                        Picasso.get().load(mProfilePhotoUri).noFade()
                                .into(a_account_sett_civ_profile_photo);

                        uploadAndUpdate(mProfilePhotoUri);
                    } else {
                        Snackbar.make(a_account_sett_civ_profile_photo,
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

        String path = mFirebaseAuth.getUid() + "/pp.jpeg";
        UploadTask uploadTask = mStorageReference.child(path).putStream(stream);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String downloadUrl = Objects.requireNonNull(task.getResult()).toString();

                        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("profile_photo_url")
                                .setValue(downloadUrl);

                        Util.updateUserProfile(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()), mFirebaseAuth.getCurrentUser().getDisplayName(), Uri.parse(downloadUrl));
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
