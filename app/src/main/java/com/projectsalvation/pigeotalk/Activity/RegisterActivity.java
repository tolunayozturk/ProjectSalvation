package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.projectsalvation.pigeotalk.Database.User;
import com.projectsalvation.pigeotalk.R;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    // region Resource Declaration
    Toolbar Register_toolbar;
    Button Register_btn_register;
    CircularImageView Register_imgView_profile_picture;
    TextInputEditText Register_textInput_userfullname;
    TextInputEditText Register_textInput_user_about;
    TextInputLayout Register_textLayout_userfullname;
    TextInputLayout Register_textLayout_user_about;
    // endregion

    private final int PERMISSION_CODE = 1000;
    private final int IMAGE_CAPTURE_CODE = 1001;

    FirebaseUser firebaseUser;

    Uri profileUri;

    DatabaseReference databaseReference;
    StorageReference userProfilePictureRef;

    User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userProfilePictureRef = FirebaseStorage.getInstance().getReference("User Profile Pictures");

        // region Resource Assignment
        Register_toolbar = findViewById(R.id.Register_toolbar);
        Register_btn_register = findViewById(R.id.Register_btn_register);
        Register_imgView_profile_picture = findViewById(R.id.Register_imgView_profile_picture);
        Register_textInput_user_about = findViewById(R.id.Register_textInput_user_about);
        Register_textInput_userfullname = findViewById(R.id.Register_textInput_userfullname);
        Register_textLayout_userfullname = findViewById(R.id.Register_textLayout_userfullname);
        Register_textLayout_user_about = findViewById(R.id.Register_textLayout_user_about);

        // endregion

        FirebaseAuth auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();

        Register_btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Register_textInput_userfullname.getText() == null || Register_textInput_userfullname.getText().toString().trim().isEmpty()){
                    Register_textLayout_userfullname.setError("Please enter your name");
                }else{
                    user.setUserFullName(Register_textInput_userfullname.getText().toString());
                    if(Register_textInput_user_about == null ||Register_textInput_user_about.getText().toString().trim().isEmpty()){
                        user.setUserAbout("Hey There! I am using PigeoTalk");
                    }else{
                        user.setUserAbout(Register_textInput_user_about.getText().toString());
                    }
                    user.setUserPhone(firebaseUser.getPhoneNumber());
                    databaseReference.child(Objects.requireNonNull(firebaseUser.getPhoneNumber())).setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){
                                Intent i = new Intent(RegisterActivity.this,MainNavigationActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                            }else{
                                System.err.println("Register Error : " + databaseError.getMessage());
                            }
                        }
                    });
                }
            }
        });


        Register_imgView_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    chooseAPhoto();
            }
        });
    }

    private void chooseAPhoto(){
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                String[] permissions = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,PERMISSION_CODE);
            }else {
                openCamera();
                //TODO: Option to select an image from gallery
            }
        }

    private void openCamera(){
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.TITLE,"PigeoTalk Picture");
            profileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,profileUri);
            startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
        }

    //Checks Camera and Storage Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "You have to give camera permission to take a photo", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Register_imgView_profile_picture.setImageURI(profileUri);
            StorageReference filePath = userProfilePictureRef.child(firebaseUser.getPhoneNumber() + ".jpg");

            filePath.putFile(profileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Photo Successful", Toast.LENGTH_SHORT).show();
                        final String photoUrl = task.getResult().getStorage().getDownloadUrl().toString();
                        user.setUserProfilePicture(photoUrl);
                    }else{
                        String errorMessage = task.getException().toString();
                        System.err.println("Error massage : " + errorMessage);
                        Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
