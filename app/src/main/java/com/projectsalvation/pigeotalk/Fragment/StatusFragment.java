package com.projectsalvation.pigeotalk.Fragment;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projectsalvation.pigeotalk.Activity.HomePageActivity;
import com.projectsalvation.pigeotalk.Adapter.StatusListRVAdapter;
import com.projectsalvation.pigeotalk.DAO.NotificationDAO;
import com.projectsalvation.pigeotalk.DAO.StatusDAO;
import com.projectsalvation.pigeotalk.R;
import com.projectsalvation.pigeotalk.Utility.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment {

    // region Resource Declaration
    RelativeLayout f_status_rl;
    CircleImageView f_status_civ_profile_photo;
    TextView f_status_tv_status_timestamp;
    Button f_status_add_status;
    RecyclerView f_status_rv;
    TextView f_status_tv_no_status;
    // endregion

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    private static final int INTENT_CHOOSE_PHOTO = 200;
    private static final int INTENT_TAKE_PHOTO = 110;

    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private FirebaseAuth mFirebaseAuth;

    private ChildEventListener mStatusEventListener;

    private ArrayList<StatusDAO> mStatusDAOS;
    private StatusListRVAdapter mStatusListRVAdapter;

    private Uri mStatusPhotoUri;


    public StatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_status, container, false);

        // region Resource Assignment
        f_status_rl = view.findViewById(R.id.f_status_rl);
        f_status_civ_profile_photo = view.findViewById(R.id.f_status_civ_profile_photo);
        f_status_tv_status_timestamp = view.findViewById(R.id.f_status_tv_status_timestamp);
        f_status_add_status = view.findViewById(R.id.f_status_add_status);
        f_status_rv = view.findViewById(R.id.f_status_rv);
        f_status_tv_no_status = view.findViewById(R.id.f_status_tv_no_status);
        // endregion

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mStatusDAOS = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);

        f_status_rv.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(f_status_rv.getContext(),
                layoutManager.getOrientation());
        f_status_rv.addItemDecoration(dividerItemDecoration);

        Picasso.get().load(mFirebaseAuth.getCurrentUser().getPhotoUrl())
                .fit()
                .centerCrop()
                .into(f_status_civ_profile_photo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(mFirebaseAuth.getCurrentUser().getPhotoUrl())
                                .fit()
                                .centerCrop()
                                .into(f_status_civ_profile_photo);
                    }
                });

        f_status_add_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());

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

        mStatusListRVAdapter = new StatusListRVAdapter(getActivity(), mStatusDAOS);
        f_status_rv.setAdapter(mStatusListRVAdapter);
        return view;
    }

    private void uploadAndSend() {
        final DatabaseReference statusReference = mDatabaseReference.child("user_status")
                .child(mFirebaseAuth.getUid());

        String path = "status/" + mFirebaseAuth.getUid() + "/IMAGE_" + mStatusPhotoUri.getLastPathSegment() + ".jpeg";

        InputStream stream = null;
        try {
            stream = getActivity().getContentResolver().openInputStream(mStatusPhotoUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        UploadTask uploadTask = mStorageReference.child(path).putStream(stream);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String downloadUrl = Objects.requireNonNull(task.getResult())
                                .toString();

                        String timestamp = Long.toString(System.currentTimeMillis());

                        statusReference.child(timestamp).setValue(downloadUrl);
                    }
                });
            }
        });
    }

    private void launchCamera() {
        if (!Util.checkPermission(Manifest.permission.CAMERA, getContext())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    getActivity(), Manifest.permission.CAMERA)) {

                // Explain to users why we request this permission
                MaterialAlertDialogBuilder alertDialogBuilder =
                        new MaterialAlertDialogBuilder(getContext())
                                .setMessage(R.string.dialog_permission_camera_explanation)
                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                                                getActivity(),
                                                PERMISSION_REQUEST_CODE_CAMERA);
                                    }
                                })
                                .setNegativeButton(R.string.action_not_now, null);

                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                permissionExplanationDialog.show();
            } else {
                // No explanation needed; request the permission
                Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                        getActivity(),
                        PERMISSION_REQUEST_CODE_CAMERA);
            }
        } else {
            // Permission has already been granted
            Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                File profilePhotoFile = null;

                try {
                    profilePhotoFile = createImageFile();
                } catch (IOException e) {
                    // TODO: Handle error
                }

                if (profilePhotoFile != null) {
                    mStatusPhotoUri = FileProvider.getUriForFile(getContext(),
                            "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mStatusPhotoUri);
                    startActivityForResult(takePhotoIntent, INTENT_TAKE_PHOTO);
                }
            }
        }
    }

    private void launchPhotoLibrary() {
        Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                .setType("image/*");

        if (choosePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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

        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                    if (takePhotoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        File profilePhotoFile = null;

                        try {
                            profilePhotoFile = createImageFile();
                        } catch (IOException e) {
                            // TODO: Handle error
                        }

                        if (profilePhotoFile != null) {
                            mStatusPhotoUri = FileProvider.getUriForFile(getContext(),
                                    "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mStatusPhotoUri);
                            startActivityForResult(takePhotoIntent, INTENT_TAKE_PHOTO);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case INTENT_TAKE_PHOTO:
                if (resultCode == getActivity().RESULT_OK) {

                    if (mStatusPhotoUri != null) {
                        uploadAndSend();
                    } else {
                        // TODO: Handle error
                    }
                }
                break;
            case INTENT_CHOOSE_PHOTO:
                if (resultCode == getActivity().RESULT_OK) {
                    mStatusPhotoUri = data.getData();

                    if (mStatusPhotoUri != null) {
                        uploadAndSend();
                    } else {
                        // TODO: Handle error
                    }
                }
                break;
            default:
                super.onActivityResult(resultCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mDatabaseReference.child("user_contacts").child(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot contact : dataSnapshot.getChildren()) {
                            f_status_tv_no_status.setVisibility(View.GONE);

                            mStatusEventListener = new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                    Log.d("StatusFragment", "onChildAdded: " + dataSnapshot.getKey());
                                    Log.d("StatusFragment", "onChildAdded: " + dataSnapshot.getValue().toString());

                                    StatusDAO statusDAO = new StatusDAO(
                                            dataSnapshot.getValue().toString(),
                                            dataSnapshot.getKey(),
                                            contact.getValue().toString()
                                    );

                                    mStatusDAOS.add(statusDAO);
                                    mStatusListRVAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };

                            mDatabaseReference.child("user_status").child(contact.getKey())
                                    .addChildEventListener(mStatusEventListener);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        mDatabaseReference.child("user_contacts").child(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot contact : dataSnapshot.getChildren()) {
                            f_status_tv_no_status.setVisibility(View.GONE);

                            mDatabaseReference.child("user_status").child(contact.getKey())
                                    .removeEventListener(mStatusEventListener);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
