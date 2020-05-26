package com.projectsalvation.pigeotalk.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.projectsalvation.pigeotalk.Adapter.GroupMessagesRVAdapter;
import com.projectsalvation.pigeotalk.DAO.MessageDAO;
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
import java.util.MissingFormatArgumentException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    // region Resource Declaration
    CircleImageView a_group_chat_civ_photo;
    TextView a_group_chat_tv_members;
    MaterialToolbar a_group_chat_toolbar;
    RecyclerView a_group_chat_rv_messages;
    LinearLayout a_group_chat_ll_footer;
    CardView a_group_chat_cv_footer;
    TextView a_group_chat_tv_name;

    EditText a_group_chat_et_message;

    Chip a_group_chat_chip_attachment;
    Chip a_group_chat_chip_camera;
    Chip a_group_chat_chip_emoji;
    Chip a_group_chat_chip_send;
    // endregion

    private static final String TAG = "GroupChatActivity";

    private static final int PERMISSION_REQUEST_CODE_CAMERA = 100;
    private static final int INTENT_CHOOSE_PHOTO = 200;
    private static final int INTENT_TAKE_PHOTO = 110;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private ChildEventListener mMessageListener;
    private ValueEventListener mRetMessageListener;
    private ChildEventListener mMemberListener;

    private ArrayList<MessageDAO> mMessageDAOS;
    private GroupMessagesRVAdapter mGroupMessagesRVAdapter;

    private String mGroupID;
    private String mGroupPhotoUrl;
    private String mGroupName;

    private Uri mAttachmentPhotoUri;

    private boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // region Resource Assignment
        a_group_chat_civ_photo = findViewById(R.id.a_group_chat_civ_photo);
        a_group_chat_tv_members = findViewById(R.id.a_group_chat_tv_members);
        a_group_chat_toolbar = findViewById(R.id.a_group_chat_toolbar);
        a_group_chat_rv_messages = findViewById(R.id.a_group_chat_rv_messages);
        a_group_chat_ll_footer = findViewById(R.id.a_group_chat_ll_footer);
        a_group_chat_cv_footer = findViewById(R.id.a_group_chat_cv_footer);
        a_group_chat_et_message = findViewById(R.id.a_group_chat_et_message);
        a_group_chat_chip_attachment = findViewById(R.id.a_group_chat_chip_attachment);
        a_group_chat_chip_camera = findViewById(R.id.a_group_chat_chip_camera);
        a_group_chat_chip_emoji = findViewById(R.id.a_group_chat_chip_emoji);
        a_group_chat_chip_send = findViewById(R.id.a_group_chat_chip_send);
        a_group_chat_tv_name = findViewById(R.id.a_group_chat_tv_name);
        // endregion

        setSupportActionBar(a_group_chat_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // Remove title from toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        mMessageDAOS = new ArrayList<>();

        // region Make RV stack from bottom to top
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);

        llm.setStackFromEnd(true);
        a_group_chat_rv_messages.setLayoutManager(llm);
        // endregion

        // region Add extra padding to bottom of RV
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (4 * scale + 0.5f);
        a_group_chat_rv_messages.setPadding(0, dpAsPixels, 0, dpAsPixels);
        // endregion

        // TODO:
        // a_group_chat_tv_members.setSelected(true);

        Intent i = getIntent();
        mGroupID = i.getExtras().getString("groupID");
        retrieveMessages(mGroupID);
        listenMessages(mGroupID);

        if (i.hasExtra("photoUrl")) {
            mGroupPhotoUrl = i.getExtras().getString("photoUrl");

            Picasso.get().load(mGroupPhotoUrl)
                    .fit()
                    .centerCrop()
                    .into(a_group_chat_civ_photo, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(mGroupPhotoUrl).into(
                                    a_group_chat_civ_photo);
                        }
                    });
        } else {
            mDatabaseReference.child("groups").child(mGroupID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mGroupPhotoUrl = dataSnapshot.child("groupPhotoUrl").getValue().toString();

                            Picasso.get().load(mGroupPhotoUrl)
                                    .fit()
                                    .centerCrop()
                                    .into(a_group_chat_civ_photo, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Picasso.get().load(mGroupPhotoUrl).into(
                                                    a_group_chat_civ_photo);
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        // region Set toolbar title to group name
        if (i.hasExtra("groupName")) {
            mGroupName = i.getExtras().getString("groupName");
            a_group_chat_tv_name.setText(mGroupName);
        } else {
            mDatabaseReference.child("groups").child(mGroupID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            a_group_chat_tv_name.setText(dataSnapshot.child("groupName").getValue().toString());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        // endregion

        mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                .child(mGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
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

        // region Send button onClick()
        a_group_chat_chip_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(a_group_chat_et_message.getText())) {
                    return;
                }

                DatabaseReference messageReference = mDatabaseReference
                        .child("group_messages").child(mGroupID);

                final String newMessageID = messageReference.push().getKey();

                MessageDAO newMessage = new MessageDAO(
                        a_group_chat_et_message.getText().toString(),
                        "plaintext",
                        Long.toString(System.currentTimeMillis()),
                        mFirebaseAuth.getUid(),
                        newMessageID,
                        "false",
                        "",
                        mGroupID
                );

                messageReference.child(Objects.requireNonNull(newMessageID)).setValue(newMessage);

                a_group_chat_et_message.setText("");

                mDatabaseReference.child("groups").child(mGroupID)
                        .child("last_message_id").setValue(newMessageID);

                mDatabaseReference.child("groups").child(mGroupID)
                        .child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot member : dataSnapshot.getChildren()) {
                            if (!member.getKey().equals(mFirebaseAuth.getUid())) {
                                mDatabaseReference.child("user_chats_unread_messages").child(member.getKey())
                                        .child(mGroupID).child(newMessageID).setValue("");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if (mMessageListener == null) {
                    Log.d(TAG, "MessageListener is null!");
                    retrieveMessages(mGroupID);
                    listenMessages(mGroupID);

                    mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                            .child(mGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
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

        // region Display group members
        mDatabaseReference.child("groups").child(mGroupID).child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final StringBuilder sb = new StringBuilder();
                        for (final DataSnapshot user : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: " + user.getKey());

                            if (user.getKey().equals(mFirebaseAuth.getUid())) {
                                sb.append("You");
                                a_group_chat_tv_members.setText(sb);
                                continue;
                            }

                            mDatabaseReference.child("user_contacts").child(mFirebaseAuth.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(user.getKey())) {
                                                sb.append(", ");
                                                sb.append(dataSnapshot.child(user.getKey()).getValue().toString());
                                            } else {
                                                mDatabaseReference.child("users").child(user.getKey())
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                sb.append(", ");
                                                                sb.append(dataSnapshot.child("phone_number").getValue().toString());

                                                                a_group_chat_tv_members.setText(sb);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });
                                            }
                                            a_group_chat_tv_members.setText(sb);
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
        // endregion

        // region Show how PigeoID sharing works
        mDatabaseReference.child("group_messages").child(mGroupID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 1) {
                            MaterialAlertDialogBuilder alertDialogBuilder =
                                    new MaterialAlertDialogBuilder(GroupChatActivity.this)
                                            .setMessage(HtmlCompat.fromHtml(getApplicationContext()
                                                            .getString(R.string.text_share_group_id, mGroupID),
                                                    HtmlCompat.FROM_HTML_MODE_LEGACY))

                                            .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            })
                                            .setNegativeButton("COPY", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ClipboardManager clipboard = (ClipboardManager)
                                                            getApplicationContext()
                                                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("GroupID", mGroupID);
                                                    clipboard.setPrimaryClip(clip);
                                                }
                                            });

                            AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                            permissionExplanationDialog.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
        // endregion

        a_group_chat_chip_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        a_group_chat_chip_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(GroupChatActivity.this);

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

        mGroupMessagesRVAdapter = new GroupMessagesRVAdapter(mMessageDAOS, GroupChatActivity.this);
        a_group_chat_rv_messages.setAdapter(mGroupMessagesRVAdapter);
    }

    private void launchCamera() {
        if (!Util.checkPermission(Manifest.permission.CAMERA, getApplicationContext())) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    GroupChatActivity.this, Manifest.permission.CAMERA)) {

                // Explain to users why we request this permission
                MaterialAlertDialogBuilder alertDialogBuilder =
                        new MaterialAlertDialogBuilder(GroupChatActivity.this)
                                .setMessage(R.string.dialog_permission_camera_explanation)
                                .setPositiveButton(R.string.action_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                                                GroupChatActivity.this,
                                                PERMISSION_REQUEST_CODE_CAMERA);
                                    }
                                })
                                .setNegativeButton(R.string.action_not_now, null);

                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
                permissionExplanationDialog.show();
            } else {
                // No explanation needed; request the permission
                Util.requestPermission(new String[]{Manifest.permission.CAMERA},
                        GroupChatActivity.this,
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

    private void uploadAndSend() {
        final DatabaseReference messageReference = mDatabaseReference
                .child("group_messages").child(mGroupID);

        final String newMessageID = messageReference.push().getKey();

        final MessageDAO newMessage = new MessageDAO(
                "",
                "image",
                Long.toString(System.currentTimeMillis()),
                mFirebaseAuth.getUid(),
                newMessageID,
                "false",
                "",
                mGroupID
        );

        messageReference.child(Objects.requireNonNull(newMessageID)).setValue(newMessage);

        mDatabaseReference.child("groups").child(mGroupID)
                .child("last_message_id").setValue(newMessageID);

        mDatabaseReference.child("groups").child(mGroupID)
                .child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot member : dataSnapshot.getChildren()) {
                    if (!member.getKey().equals(mFirebaseAuth.getUid())) {
                        mDatabaseReference.child("user_chats_unread_messages").child(member.getKey())
                                .child(mGroupID).child(newMessageID).setValue("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        InputStream stream = null;
        try {
            stream = getContentResolver().openInputStream(mAttachmentPhotoUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (stream == null) {
            Log.d(TAG, "uploadAndSend: " + stream.toString());
        }

        UploadTask uploadTask = mStorageReference.child("chats/" + mGroupID
                + "/image" + "/IMAGE_" + mAttachmentPhotoUri.getLastPathSegment() + ".jpeg").putStream(stream);

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
                                mGroupMessagesRVAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // TODO: Handle error
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // TODO: Handle error
            }
        });

        if (mMessageListener == null) {
            Log.d(TAG, "MessageListener is null!");
            retrieveMessages(mGroupID);
            listenMessages(mGroupID);

            mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                    .child(mGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void listenMessages(String groupID) {
        mMessageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                MessageDAO messageDAO = new MessageDAO(
                        dataSnapshot.child("message").getValue().toString(),
                        dataSnapshot.child("messageType").getValue().toString(),
                        dataSnapshot.child("timestamp").getValue().toString(),
                        dataSnapshot.child("sender").getValue().toString(),
                        dataSnapshot.child("messageId").getValue().toString(),
                        dataSnapshot.child("isRead").getValue().toString(),
                        dataSnapshot.child("seenAt").getValue().toString(),
                        mGroupID
                );

                if (!messageDAO.getSender().equals(mFirebaseAuth.getUid())) {
                    if (dataSnapshot.child("isRead").getValue().toString().equals("false")) {
                        messageDAO.setIsRead("true");
                        messageDAO.setSeenAt(Long.toString(System.currentTimeMillis()));
                        dataSnapshot.getRef().child("isRead").setValue("true");
                        dataSnapshot.getRef().child("seenAt").setValue(Long.toString(System.currentTimeMillis()));
                    }
                }

                if (!isFirstTime) {
                    mMessageDAOS.add(messageDAO);
                    mGroupMessagesRVAdapter.notifyDataSetChanged();
                    a_group_chat_rv_messages.smoothScrollToPosition(mGroupMessagesRVAdapter.getItemCount() - 1);
                }

                isFirstTime = false;
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

        mDatabaseReference.child("group_messages").child(groupID).limitToLast(1)
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
                            snapshot.child("sender").getValue().toString(),
                            snapshot.child("messageId").getValue().toString(),
                            snapshot.child("isRead").getValue().toString(),
                            snapshot.child("seenAt").getValue().toString(),
                            mGroupID
                    );

                    mMessageDAOS.add(messageDAO);
                    mGroupMessagesRVAdapter.notifyDataSetChanged();
                    a_group_chat_rv_messages.smoothScrollToPosition(mGroupMessagesRVAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("group_messages").child(chatID)
                .addListenerForSingleValueEvent(mRetMessageListener);
    }

    public static void setGroupCreatedMessage(DatabaseReference databaseReference,
                                              FirebaseAuth firebaseAuth, String groupID) {
        DatabaseReference messageReference = databaseReference
                .child("group_messages").child(groupID);

        final String newMessageID = messageReference.push().getKey();

        MessageDAO newMessage = new MessageDAO(
                firebaseAuth.getCurrentUser().getDisplayName() + " created this group",
                "system",
                Long.toString(System.currentTimeMillis()),
                "system",
                newMessageID,
                "false",
                "",
                groupID
        );

        messageReference.child(Objects.requireNonNull(newMessageID)).setValue(newMessage);

        databaseReference.child("groups").child(groupID)
                .child("last_message_id").setValue(newMessageID);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.a_group_chat_menuItem_group_info:
                Intent i = new Intent(GroupChatActivity.this, GroupInfoActivity.class);
                i.putExtra("groupID", mGroupID);
                i.putExtra("groupName", mGroupName);
                i.putExtra("groupPhotoUrl", mGroupPhotoUrl);
                startActivity(i);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_chat_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                .child(mGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
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

    @Override
    protected void onPause() {
        super.onPause();

        mDatabaseReference.child("user_chats_unread_messages").child(mFirebaseAuth.getUid())
                .child(mGroupID).addListenerForSingleValueEvent(new ValueEventListener() {
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
