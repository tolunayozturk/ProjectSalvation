package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.Adapter.ContactsRVAdapter;
import com.projectsalvation.pigeotalk.Adapter.MessagesRVAdapter;
import com.projectsalvation.pigeotalk.DAO.ContactDAO;
import com.projectsalvation.pigeotalk.DAO.MessageDAO;
import com.projectsalvation.pigeotalk.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;

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

    private DatabaseReference mDatabaseReference;
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

    private boolean isFirstTime = true;

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

                a_chat_et_message.setText("");

                mDatabaseReference.child("chats").child(mChatID)
                        .child("last_message_id").setValue(newMessageID);

                if (mMessageListener == null) {
                    Log.d(TAG, "MessageListener is null!");
                    retrieveMessages(mChatID);
                    listenMessages(mChatID);
                }
            }
        });
        // endregion

        a_chat_chip_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Attachments
            }
        });

        // region Set toolbar title to contact name
        mDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(mFirebaseAuth.getUid()).child("contacts").child(mUserID).exists()) {
                    a_chat_tv_name.setText(mUserName);
                } else {
                    a_chat_tv_name.setText(dataSnapshot.child(mUserID).child("phone_number").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // endregion

        listenPresence();

        mMessageRVAdapter = new MessagesRVAdapter(getApplicationContext(), mMessageDAOS);
        a_chat_rv_messages.setAdapter(mMessageRVAdapter);
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
                        dataSnapshot.getRef().child("seenAt").setValue(Long.toString(System.currentTimeMillis()));
                    }
                }

                if (!isFirstTime) {
                    mMessageDAOS.add(messageDAO);
                    mMessageRVAdapter.notifyDataSetChanged();
                    a_chat_rv_messages.smoothScrollToPosition(mMessageRVAdapter.getItemCount() - 1);
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
                .child(mFirebaseAuth.getUid()).setValue("true");

        mDatabaseReference.child("chats").child(newChatUID).child("members")
                .child(mUserID).setValue("true");

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
            case R.id.a_chat_menuItem_contact_info:
                break;
            case R.id.a_chat_menuItem_voice_call:
                break;
            case R.id.a_chat_menuItem_video_call:
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
        }
    }
}
