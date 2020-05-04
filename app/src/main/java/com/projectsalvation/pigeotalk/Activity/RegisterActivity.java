package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.projectsalvation.pigeotalk.Database.User;
import com.projectsalvation.pigeotalk.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
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
    private static final int PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE = 200;

    private static final int INTENT_TAKE_PHOTO = 101;
    private static final int INTENT_CHOOSE_PHOTO = 201;

    private FirebaseAuth mFirebaseAuth;
    private Bitmap profilePhotoBitmap;

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // Not calling **super**, disables back button in current screen.
    }

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

        mFirebaseAuth = FirebaseAuth.getInstance();

        // Intent i = getIntent();
        // Objects.requireNonNull(i.getExtras()).get("userId");
        // i.getExtras().get("formattedPhoneNumber");

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
                                        if (!checkPermission(Manifest.permission.CAMERA)) {
                                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                                    RegisterActivity.this, Manifest.permission.CAMERA)) {

                                                // Explain to user why we need this permission
                                                MaterialAlertDialogBuilder alertDialogBuilder =
                                                        new MaterialAlertDialogBuilder(RegisterActivity.this)
                                                                .setMessage(R.string.dialog_permission_camera_explanation)
                                                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        requestPermission(new String[]{Manifest.permission.CAMERA},
                                                                                PERMISSION_REQUEST_CODE_CAMERA);
                                                                    }
                                                                }).setNegativeButton(R.string.action_not_now, null);

                                                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                                                permissionExplanationDialog.show();
                                            } else {
                                                // No explanation needed; request the permission
                                                requestPermission(new String[]{Manifest.permission.CAMERA},
                                                        PERMISSION_REQUEST_CODE_CAMERA);
                                            }
                                        } else {
                                            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                                            if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                                                startActivityForResult(
                                                        takePhotoIntent, INTENT_TAKE_PHOTO
                                                );
                                            }
                                        }
                                        break;
                                    case 1:
                                        Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK,
                                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                                                .setType("image/*");

                                        if (choosePhotoIntent.resolveActivity(getPackageManager()) != null) {
                                            startActivityForResult(
                                                    choosePhotoIntent, INTENT_CHOOSE_PHOTO
                                            );
                                        }
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
                    Snackbar.make(a_register_et_user_name, R.string.text_user_name_cannot_be_empty,
                            BaseTransientBottomBar.LENGTH_LONG).show();
                }

                // TODO: Handle database operations
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePhotoIntent, INTENT_TAKE_PHOTO);
                    }
                }
            case PERMISSION_REQUEST_CODE_READ_EXTERNAL_STORAGE:
                break;
        } // end switch
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case INTENT_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bundle extras = Objects.requireNonNull(data).getExtras();
                    profilePhotoBitmap = (Bitmap) extras.get("data");

                    a_register_civ_profile_photo.setImageBitmap(profilePhotoBitmap);
                    a_register_iv_add_photo_icon.setVisibility(View.INVISIBLE);
                }
                break;
            case INTENT_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Uri selectedPhotoUri = Objects.requireNonNull(data).getData();

                    try {
                        profilePhotoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedPhotoUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    a_register_civ_profile_photo.setImageBitmap(profilePhotoBitmap);
                    //Picasso.get().load(selectedPhotoUri).noFade().into(a_register_civ_profile_photo);
                    a_register_iv_add_photo_icon.setVisibility(View.INVISIBLE);
                }
                break;
            default:
                super.onActivityResult(resultCode, resultCode, data);
                break;
        } // end switch
    }

    private void requestPermission(String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(
                this,
                permissions,
                requestCode
        );
    }

    private boolean checkPermission(String permission) {
        if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }
}
