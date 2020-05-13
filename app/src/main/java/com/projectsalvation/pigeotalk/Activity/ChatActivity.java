package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ChatActivity extends AppCompatActivity {

    // region Resource Declaration
    MaterialToolbar a_chat_toolbar;
    RecyclerView a_chat_rv_messages;
    LinearLayout a_chat_ll_footer;
    CardView a_chat_cv_footer;

    EditText a_chat_et_message;

    Chip a_chat_chip_attachment;
    Chip a_chat_chip_camera;
    Chip a_chat_chip_emoji;
    Chip a_chat_chip_send;
    // endregion

    private static final String TAG = "ChatActivity";

    private int flag = 0;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<MessageDAO> mMessageDAOS;
    private ValueEventListener mMessageListener;
    private MessagesRVAdapter mMessageRVAdapter;

    private String mChatID;
    private String mUserID;
    private String mUserName;
    private String mUserPhotoUrl;
    private String previousActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // region Resource Assignment
        a_chat_toolbar = findViewById(R.id.a_chat_toolbar);
        a_chat_rv_messages = findViewById(R.id.a_chat_rv_messages);
        a_chat_ll_footer = findViewById(R.id.a_chat_ll_footer);
        a_chat_cv_footer = findViewById(R.id.a_chat_cv_footer);

        a_chat_et_message = findViewById(R.id.a_chat_et_message);

        a_chat_chip_attachment = findViewById(R.id.a_chat_chip_attachment);
        a_chat_chip_camera = findViewById(R.id.a_chat_chip_camera);
        a_chat_chip_emoji = findViewById(R.id.a_chat_chip_emoji);
        a_chat_chip_send = findViewById(R.id.a_chat_chip_send);
        // endregion

        setSupportActionBar(a_chat_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(getString(R.string.EMPTY_STRING));
        getSupportActionBar().setSubtitle(getString(R.string.EMPTY_STRING));

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        mMessageDAOS = new ArrayList<>();

        // Make RV stack from bottom to top
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);

        llm.setStackFromEnd(true);
        a_chat_rv_messages.setLayoutManager(llm);

        Intent i = getIntent();
        if (!i.hasExtra("userID")) {
            Log.d(TAG, "!i.hasExtra(\"userID\"): false");
            return;
        }

        if (!i.hasExtra("prevActivity")) {
            Log.d(TAG, "!i.hasExtra(\"prevActivity\"): false");
            return;
        }

        if (i.hasExtra("chatID")) {
            mChatID = i.getExtras().getString("chatID");
        }

        previousActivity = i.getExtras().getString("prevActivity");
        mUserID = i.getExtras().getString("userID");
        mUserName = i.getExtras().getString("contactName");
        mUserPhotoUrl = i.getExtras().getString("contactPhotoUrl");

        // region If user is coming from ContactsActivity, check if there is an existing chat between
        // the user and the clicked user. If no, create one, if yes, get the existing chatUID
        if (previousActivity.equals("ContactsActivity")) {
            mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getValue().toString().equals(mUserID)) {
                                    // Found existing chat between users
                                    mChatID = snapshot.getKey();
                                    Log.d(TAG, "Got existing chat with the chatID: " + mChatID);
                                    loadMessages(mChatID);
                                    listenMessages(mChatID);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        if (previousActivity.equals("HomePageActivity")) {
            loadMessages(mChatID);
            listenMessages(mChatID);
        }
        // endregion

        a_chat_chip_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(a_chat_et_message.getText())) {
                    return;
                }

                if (mChatID == null) {
                    createChat();
                }


                DatabaseReference messageReference = mDatabaseReference
                        .child("chat_messages").child(mChatID);

                String newMessageID = messageReference.push().getKey();

                Long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = tsLong.toString();

                MessageDAO newMessage = new MessageDAO(
                        a_chat_et_message.getText().toString(),
                        "plaintext",
                        timestamp,
                        mUserID,
                        mFirebaseAuth.getUid(),
                        newMessageID,
                        "false",
                        mChatID
                );

                messageReference.child(newMessageID).setValue(newMessage);

                mDatabaseReference.child("chats").child(mChatID)
                        .child("last_message_id").setValue(newMessageID);

                if (mMessageListener == null) {
                    Log.d(TAG, "mMessageListener is null. Start listening messages.");
                    loadMessages(mChatID);
                    listenMessages(mChatID);
                }

                a_chat_et_message.setText("");
            }
        });

        // region Set toolbar title to contact's name
        mDatabaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(mFirebaseAuth.getUid()).child("contacts").child(mUserID).exists()) {
                    getSupportActionBar().setTitle(mUserName);
                } else {
                    getSupportActionBar().setTitle(dataSnapshot.child(mFirebaseAuth.getUid())
                            .child("contacts").child(mUserID).getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // endregion

        mMessageRVAdapter = new MessagesRVAdapter(getApplicationContext(), mMessageDAOS);
        a_chat_rv_messages.setAdapter(mMessageRVAdapter);
    }

    private void listenMessages(String chatID) {
        mMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (flag == 0) {
                    flag++;
                    return;
                }

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
                            snapshot.child("read").getValue().toString(),
                            snapshot.child("messageId").getValue().toString(),
                            mChatID
                    );

                    Log.d(TAG, "Add item");

                    mMessageDAOS.add(messageDAO);
                    a_chat_rv_messages.smoothScrollToPosition(mMessageRVAdapter.getItemCount() - 1);
                    mMessageRVAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("chat_messages").child(chatID).limitToLast(1)
                .addValueEventListener(mMessageListener);
    }

    private void loadMessages(String chatID) {
        mMessageListener = new ValueEventListener() {
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
                            snapshot.child("read").getValue().toString(),
                            snapshot.child("messageId").getValue().toString(),
                            mChatID
                    );

                    Log.d(TAG, "Add item");

                    mMessageDAOS.add(messageDAO);
                    a_chat_rv_messages.smoothScrollToPosition(mMessageRVAdapter.getItemCount() - 1);
                    mMessageRVAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.child("chat_messages").child(chatID)
                .addListenerForSingleValueEvent(mMessageListener);
    }

    private void createChat() {
        String newChatUID = UUID.randomUUID().toString().replace("-", "");

        mDatabaseReference.child("chats").child(newChatUID).child("members")
                .child(mFirebaseAuth.getUid()).setValue(true);

        mDatabaseReference.child("chats").child(newChatUID).child("members")
                .child(mUserID).setValue(true);

        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .child(newChatUID).setValue(mUserID);

        mDatabaseReference.child("user_chats").child(mUserID)
                .child(newChatUID).setValue(mFirebaseAuth.getUid());

        mChatID = newChatUID;
        Log.d(TAG, "Created chat with the UID: " + mChatID);
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
}
