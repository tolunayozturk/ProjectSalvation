package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.Adapter.ContactsRVAdapter;
import com.projectsalvation.pigeotalk.DAO.ContactDAO;
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

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<String> chatUIDs;
    private Map<String, ArrayList<String>> members;

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

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        chatUIDs = new ArrayList<>();

        getSupportActionBar().setTitle(getString(R.string.EMPTY_STRING));
        getSupportActionBar().setSubtitle(getString(R.string.EMPTY_STRING));

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
                                }
                            }

                            a_chat_chip_send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (TextUtils.isEmpty(a_chat_et_message.getText())) {
                                        return;
                                    }

                                    if (mChatID == null) {
                                        createChat();
                                    }

                                    /*
                                    String newMessageID = UUID.randomUUID().toString()
                                            .replace("-", "");

                                    DatabaseReference messageReference = mDatabaseReference
                                            .child("chat_messages").child(mChatID)
                                            .child(newMessageID);

                                    messageReference.child("message")
                                            .setValue(a_chat_et_message.getText().toString());

                                    mDatabaseReference.child("chats").child(mChatID)
                                            .child("last_message_id").setValue(newMessageID);

                                    // ...
                                    */

                                    a_chat_et_message.setText("");
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
        // endregion

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
    }

    private void createChat() {
        String newChatUID = UUID.randomUUID().toString().replace("-", "");

        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .child(newChatUID).setValue(mUserID);

        mDatabaseReference.child("user_chats").child(mUserID)
                .child(newChatUID).setValue(mFirebaseAuth.getUid());

        mDatabaseReference.child("chats").child(newChatUID).child("members")
                .child(mFirebaseAuth.getUid()).setValue(true);

        mDatabaseReference.child("chats").child(newChatUID).child("members")
                .child(mUserID).setValue(true);

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
