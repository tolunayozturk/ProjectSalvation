package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projectsalvation.pigeotalk.Adapter.MessagesRVAdapter;
import com.projectsalvation.pigeotalk.DAO.MessageDAO;
import com.projectsalvation.pigeotalk.DAO.NotificationDAO;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    // region Resource Declaration
    CircleImageView a_chat_civ_photo;
    TextView a_chat_tv_name;
    MaterialToolbar a_chat_toolbar;
    RecyclerView a_chat_rv_messages;
    LinearLayout a_chat_ll_footer;
    CardView a_chat_cv_footer;
    TextView a_chat_tv_presence;

    EditText a_chat_et_message;

    Chip a_chat_chip_attachment;
    Chip a_chat_chip_camera;
    Chip a_chat_chip_emoji;
    Chip a_chat_chip_send;
    // endregion

    private static final String TAG = "ChatActivity";

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    private static final int INTENT_CHOOSE_PHOTO = 200;
    private static final int INTENT_TAKE_PHOTO = 110;

    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<MessageDAO> mMessageDAOS;
    private MessagesRVAdapter mMessageRVAdapter;

    private ChildEventListener mMessageListener;
    private ValueEventListener mRetMessageListener;
    private ValueEventListener mPresenceListener;
    private ValueEventListener mChatListener;

    private String mChatID;
    private String mUserID;
    private String mUserName;
    private String mUserPhotoUrl;

    private Uri mAttachmentPhotoUri;
    private boolean mIsFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // region Resource Assignment
        a_chat_toolbar = findViewById(R.id.a_chat_toolbar);
        a_chat_rv_messages = findViewById(R.id.a_chat_rv_messages);
        a_chat_ll_footer = findViewById(R.id.a_chat_ll_footer);
        a_chat_cv_footer = findViewById(R.id.a_chat_cv_footer);
        a_chat_civ_photo = findViewById(R.id.a_chat_civ_photo);
        a_chat_tv_name = findViewById(R.id.a_chat_tv_name);
        a_chat_tv_presence = findViewById(R.id.a_chat_tv_presence);

        a_chat_et_message = findViewById(R.id.a_chat_et_message);

        a_chat_chip_attachment = findViewById(R.id.a_chat_chip_attachment);
        a_chat_chip_camera = findViewById(R.id.a_chat_chip_camera);
        a_chat_chip_emoji = findViewById(R.id.a_chat_chip_emoji);
        a_chat_chip_send = findViewById(R.id.a_chat_chip_send);
        // endregion

        setSupportActionBar(a_chat_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mMessageDAOS = new ArrayList<>();

        // region Make RV stack from bottom to top
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);

        llm.setStackFromEnd(true);
        a_chat_rv_messages.setLayoutManager(llm);
        // endregion

        // region Add extra padding to bottom of RV
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (4 * scale + 0.5f);
        a_chat_rv_messages.setPadding(0, dpAsPixels, 0, dpAsPixels);
        // endregion

        a_chat_tv_presence.setSelected(true);

        Intent i = getIntent();

        if (i.hasExtra("chatID")) {
            mChatID = i.getExtras().getString("chatID");

            retrieveMessages(mChatID);
            listenMessages(mChatID);

            mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                    .child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot unreadMsg : dataSnapshot.getChildren()) {
                        unreadMsg.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        mUserID = i.getExtras().getString("userID");
        mUserName = i.getExtras().getString("contactName");
        mUserPhotoUrl = i.getExtras().getString("contactPhotoUrl");

        Picasso.get().load(mUserPhotoUrl)
                .fit()
                .centerCrop()
                .into(a_chat_civ_photo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(mUserPhotoUrl).into(
                                a_chat_civ_photo);
                    }
                });

        if (mChatID == null) {
            mChatListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.getValue().toString().equals(mUserID)) {
                            // Found existing chat
                            mChatID = snapshot.getKey();
                            Log.d(TAG, "Found chat! ChatID: " + mChatID);

                            mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                                    .removeEventListener(this);

                            retrieveMessages(mChatID);
                            listenMessages(mChatID);

                            mDatabaseReference.child("user_chats_unread_messages")
                                    .child(mFirebaseAuth.getUid())
                                    .child(mChatID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot unreadMsg : dataSnapshot.getChildren()) {
                                                unreadMsg.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                    .addValueEventListener(mChatListener);
        }

        // region Send button onClick()
        a_chat_chip_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(a_chat_et_message.getText())) {
                    return;
                }

                if (mChatID == null) {
                    createChat();
                }

                if (mChatListener != null) {
                    mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                            .removeEventListener(mChatListener);
                }

                DatabaseReference messageReference = mDatabaseReference
                        .child("chat_messages").child(mChatID);

                String newMessageID = messageReference.push().getKey();

                MessageDAO newMessage = new MessageDAO(
                        a_chat_et_message.getText().toString(),
                        "plaintext",
                        Long.toString(System.currentTimeMillis()),
                        mUserID,
                        mFirebaseAuth.getUid(),
                        newMessageID,
                        "false",
                        "",
                        mChatID
                );

                messageReference.child(Objects.requireNonNull(newMessageID)).setValue(newMessage);

                String newNotificationID = mDatabaseReference.child("notifications").child(mUserID)
                        .push().getKey();

                NotificationDAO newNotification = new NotificationDAO(
                        mFirebaseAuth.getCurrentUser().getDisplayName(),
                        a_chat_et_message.getText().toString(),
                        ""
                );

                mDatabaseReference.child("notifications").child(mUserID).child(newNotificationID)
                        .setValue(newNotification);

                a_chat_et_message.setText("");

                mDatabaseReference.child("chats").child(mChatID)
                        .child("last_message_id").setValue(newMessageID);

                mDatabaseReference.child("user_chats_unread_messages").child(mUserID)
                        .child(mChatID).child(newMessageID).setValue("");

                if (mMessageListener == null) {
                    Log.d(TAG, "MessageListener is null!");
                    retrieveMessages(mChatID);
                    listenMessages(mChatID);

                    mDatabaseReference.child("user_chats_unread_messages")
                            .child(mFirebaseAuth.getUid())
                            .child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot unreadMsg : dataSnapshot.getChildren()) {
                                unreadMsg.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
        // endregion

        a_chat_chip_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ChatActivity.this);

                builder.setItems(
                        new String[]{getString(R.string.text_photo)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
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

        a_chat_chip_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        // region Set toolbar title to contact name
        mDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mFirebaseAuth.getUid())
                        .child("contacts").child(mUserID).exists()) {

                    a_chat_tv_name.setText(mUserName);
                } else {
                    a_chat_tv_name.setText(dataSnapshot.child(mUserID).child("phone_number")
                            .getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // endregion

        listenPresence();

        mMessageRVAdapter = new MessagesRVAdapter(ChatActivity.this, mMessageDAOS);
        a_chat_rv_messages.setAdapter(mMessageRVAdapter);
    }

    private void launchCamera() {
        if (!Util.checkPermission(Manifest.permission.CAMERA, getApplicationContext())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    ChatActivity.this, Manifest.permission.CAMERA)) {

                // Explain to users why we request this permission
                MaterialAlertDialogBuilder alertDialogBuilder =
                        new MaterialAlertDialogBuilder(ChatActivity.this)
                                .setMessage(R.string.dialog_permission_camera_explanation)
                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                                                ChatActivity.this,
                                                PERMISSION_REQUEST_CODE_CAMERA);
                                    }
                                })
                                .setNegativeButton(R.string.action_not_now, null);

                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                permissionExplanationDialog.show();
            } else {
                // No explanation needed; request the permission
                Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                        ChatActivity.this,
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
                    // TODO: Handle error
                }

                if (profilePhotoFile != null) {
                    mAttachmentPhotoUri = FileProvider.getUriForFile(this,
                            "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mAttachmentPhotoUri);
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

    private void uploadAndSend() {
        if (mChatID == null) {
            createChat();
        }

        if (mChatListener != null) {
            mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                    .removeEventListener(mChatListener);
        }

        final DatabaseReference messageReference = mDatabaseReference
                .child("chat_messages").child(mChatID);

        final String newMessageID = messageReference.push().getKey();

        final MessageDAO newMessage = new MessageDAO(
                "",
                "image",
                Long.toString(System.currentTimeMillis()),
                mUserID,
                mFirebaseAuth.getUid(),
                newMessageID,
                "false",
                "",
                mChatID
        );

        messageReference.child(Objects.requireNonNull(newMessageID)).setValue(newMessage);

        mDatabaseReference.child("chats").child(mChatID)
                .child("last_message_id").setValue(newMessageID);

        mDatabaseReference.child("user_chats_unread_messages").child(mUserID)
                .child(mChatID).child(newMessageID).setValue("");

        String path = "chats/" + mChatID + "/image" + "/IMAGE_" + mAttachmentPhotoUri.getLastPathSegment() + ".jpeg";

        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(mAttachmentPhotoUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        UploadTask uploadTask = mStorageReference.child(path).putStream(stream);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl()
                        .addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String downloadUrl = Objects.requireNonNull(task.getResult())
                                        .toString();

                                newMessage.setMessage(downloadUrl);
                                messageReference.child(newMessageID).child("message").setValue(downloadUrl);
                                mMessageRVAdapter.notifyDataSetChanged();

                                String newNotificationID = mDatabaseReference.child("notifications").child(mUserID)
                                        .push().getKey();

                                NotificationDAO newNotification = new NotificationDAO(
                                        mFirebaseAuth.getCurrentUser().getDisplayName(),
                                        "\uD83D\uDCF7 Photo",
                                        downloadUrl
                                );

                                mDatabaseReference.child("notifications").child(mUserID).child(newNotificationID)
                                        .setValue(newNotification);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: Handle error
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                
            }
        });

        if (mMessageListener == null) {
            Log.d(TAG, "MessageListener is null!");
            retrieveMessages(mChatID);
            listenMessages(mChatID);

            mDatabaseReference.child("user_chats_unread_messages")
                    .child(mFirebaseAuth.getUid())
                    .child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot unreadMsg : dataSnapshot.getChildren()) {
                        unreadMsg.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
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
                            // TODO: Handle error
                        }

                        if (profilePhotoFile != null) {
                            mAttachmentPhotoUri = FileProvider.getUriForFile(this,
                                    "com.projectsalvation.pigeotalk.fileprovider", profilePhotoFile);

                            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mAttachmentPhotoUri);
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

                    if (mAttachmentPhotoUri != null) {
                        Log.d(TAG, "onActivityResult: " + mAttachmentPhotoUri.getPath());
                        uploadAndSend();
                    } else {
                        // TODO: Handle error
                    }
                }
                break;
            case INTENT_CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    mAttachmentPhotoUri = data.getData();

                    if (mAttachmentPhotoUri != null) {
                        Log.d(TAG, "onActivityResult: " + mAttachmentPhotoUri.getPath());
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

    private void listenPresence() {
        mPresenceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("isOnline").getValue().toString().equals("false")) {

                    String timestamp = dataSnapshot.child("last_seen").getValue().toString();
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timestamp));

                    a_chat_tv_presence.setText(getString(R.string.text_last_seen,
                            DateUtils.getRelativeTimeSpanString(Long.parseLong(timestamp))));

                } else {
                    a_chat_tv_presence.setText(getString(R.string.text_online));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("users").child(mUserID).child("presence")
                .addValueEventListener(mPresenceListener);
    }

    private void listenMessages(String chatID) {
        mMessageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessageDAO messageDAO = new MessageDAO(
                        dataSnapshot.child("message").getValue().toString(),
                        dataSnapshot.child("messageType").getValue().toString(),
                        dataSnapshot.child("timestamp").getValue().toString(),
                        dataSnapshot.child("recipient").getValue().toString(),
                        dataSnapshot.child("sender").getValue().toString(),
                        dataSnapshot.child("messageId").getValue().toString(),
                        dataSnapshot.child("isRead").getValue().toString(),
                        dataSnapshot.child("seenAt").getValue().toString(),
                        mChatID
                );

                if (messageDAO.getRecipient().equals(mFirebaseAuth.getUid())) {
                    if (dataSnapshot.child("isRead").getValue().toString().equals("false")) {

                        messageDAO.setIsRead("true");
                        messageDAO.setSeenAt(Long.toString(System.currentTimeMillis()));

                        dataSnapshot.getRef().child("isRead").setValue("true");
                        dataSnapshot.getRef().child("seenAt").setValue(
                                Long.toString(System.currentTimeMillis()));
                    }
                }

                if (!mIsFirstTime) {
                    mMessageDAOS.add(messageDAO);
                    mMessageRVAdapter.notifyDataSetChanged();
                    a_chat_rv_messages.smoothScrollToPosition(mMessageRVAdapter.getItemCount() - 1);
                }

                mIsFirstTime = false;
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

        mDatabaseReference.child("chat_messages").child(chatID).limitToLast(1)
                .addChildEventListener(mMessageListener);
    }


    private void retrieveMessages(String chatID) {
        mRetMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot == null) {
                        return;
                    }

                    MessageDAO messageDAO = new MessageDAO(
                            snapshot.child("message").getValue().toString(),
                            snapshot.child("messageType").getValue().toString(),
                            snapshot.child("timestamp").getValue().toString(),
                            snapshot.child("recipient").getValue().toString(),
                            snapshot.child("sender").getValue().toString(),
                            snapshot.child("messageId").getValue().toString(),
                            snapshot.child("isRead").getValue().toString(),
                            snapshot.child("seenAt").getValue().toString(),
                            mChatID
                    );

                    mMessageDAOS.add(messageDAO);
                    mMessageRVAdapter.notifyDataSetChanged();
                    a_chat_rv_messages.smoothScrollToPosition(mMessageRVAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("chat_messages").child(chatID)
                .addListenerForSingleValueEvent(mRetMessageListener);
    }

    private void createChat() {
        String newChatUID = UUID.randomUUID().toString().replace("-", "");

        mDatabaseReference.child("chats").child(newChatUID).child("members")
                .child(mFirebaseAuth.getUid()).setValue("");

        mDatabaseReference.child("chats").child(newChatUID).child("members")
                .child(mUserID).setValue("");

        mDatabaseReference.child("chats").child(newChatUID).child("chatId")
                .setValue(newChatUID);

        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .child(newChatUID).setValue(mUserID);

        mDatabaseReference.child("user_chats").child(mUserID)
                .child(newChatUID).setValue(mFirebaseAuth.getUid());

        mChatID = newChatUID;
        Log.d(TAG, "Created chat! ChatID: " + mChatID);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.a_chat_menuItem_contact_info:
                Intent i = new Intent(ChatActivity.this, ContactInfoActivity.class);
                i.putExtra("userID", mUserID);
                i.putExtra("userPhotoUrl", mUserPhotoUrl);
                i.putExtra("userName", mUserName);
                startActivity(i);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mChatID != null) {
            mDatabaseReference.child("chat_messages").child(mChatID).limitToLast(1)
                    .removeEventListener(mMessageListener);

            mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                    .child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot unreadMsg : dataSnapshot.getChildren()) {
                        unreadMsg.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("last_seen").setValue(System.currentTimeMillis());
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mChatID != null) {
            mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                    .child(mChatID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot unreadMsg : dataSnapshot.getChildren()) {
                        unreadMsg.getRef().removeValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("isOnline").setValue("true");

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("presence")
                .child("last_seen").setValue(System.currentTimeMillis());
    }
}
