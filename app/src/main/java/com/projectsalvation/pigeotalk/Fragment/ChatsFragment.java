package com.projectsalvation.pigeotalk.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.Adapter.ChatListRVAdapter;
import com.projectsalvation.pigeotalk.DAO.ChatDAO;
import com.projectsalvation.pigeotalk.DAO.MessageDAO;
import com.projectsalvation.pigeotalk.R;

import java.util.ArrayList;

import static android.icu.util.ULocale.getName;


public class ChatsFragment extends Fragment {

    // region Resource Declaration
    RecyclerView f_chats_rv;
    // endregion

    private static String TAG = "ChatsFragment";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private ChildEventListener mChatListener;
    private ChildEventListener mNewMessageListener;

    private ChatListRVAdapter mChatListRVAdapter;
    private ArrayList<ChatDAO> mChatDAOS;

    private boolean isFirstTime = true;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // region Resource Assignment
        f_chats_rv = view.findViewById(R.id.f_chats_rv);
        // endregion

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mChatDAOS = new ArrayList<>();

        f_chats_rv.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));

        mChatListRVAdapter = new ChatListRVAdapter(getActivity().getApplicationContext(), mChatDAOS);
        f_chats_rv.setAdapter(mChatListRVAdapter);

        return view;
    }

    private void loadChats() {
        mChatDAOS.clear();
        f_chats_rv.removeAllViewsInLayout();
        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot chat : dataSnapshot.getChildren()) {
                            final ChatDAO chatDAO = new ChatDAO(null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null);

                            chatDAO.setChatId(chat.getKey());
                            chatDAO.setUserId(chat.getValue().toString());

                            Log.d(TAG, "onDataChange - ChatID: " + chatDAO.getChatId());
                            Log.d(TAG, "onDataChange - RecipientID " + chatDAO.getUserId());

                            updatePhoto(chatDAO, chatDAO.getUserId());
                            updateDisplayName(chatDAO, chatDAO.getUserId());
                            updateUI(chatDAO, chatDAO.getChatId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updateUI(final ChatDAO chatDAO, String chatID) {
        mDatabaseReference.child("chat_messages").child(chatID).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot msgSnapshot : dataSnapshot.getChildren()) {
                            chatDAO.setLastMessage(msgSnapshot.child("message").getValue().toString());
                            chatDAO.setTimestamp(msgSnapshot.child("timestamp").getValue().toString());
                            chatDAO.setUnreadMessageCount("0");
                            chatDAO.setIsMuted("false");

                            Log.d(TAG, "updateUI - LastMessage: " + chatDAO.getLastMessage());
                            Log.d(TAG, "updateUI - Timestamp: " + chatDAO.getTimestamp());

                            Log.d(TAG, "updateUI - Add item to RV");
                            mChatDAOS.add(chatDAO);
                            mChatListRVAdapter.notifyDataSetChanged();

                            listenNewMessages(chatDAO.getChatId());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updateDisplayName(final ChatDAO chatDAO, final String userID) {
        mDatabaseReference.child("users").child(mFirebaseAuth.getUid()).child("contacts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userID)) {
                            chatDAO.setName(dataSnapshot.child(userID).getValue().toString());

                            Log.d(TAG, "updateDisplayName - DisplayName: " + chatDAO.getName());
                        } else {
                            // TODO: If user is not in the contact list, get it's PT name
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void updatePhoto(final ChatDAO chatDAO, String userID) {
        mDatabaseReference.child("users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chatDAO.setPhotoUrl(
                                dataSnapshot.child("profile_photo_url").getValue().toString());

                        Log.d(TAG, "updatePhoto - PhotoUrl: " + chatDAO.getPhotoUrl());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void listenNewMessages(final String chatID) {
        mNewMessageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded: " + s);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                loadChats();

                Log.d(TAG, "onChildChanged: " + s);

                mDatabaseReference.child("chats").child(chatID)
                        .removeEventListener(mNewMessageListener);
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

        Log.d(TAG, "listenNewMessages: " + chatID);
        mDatabaseReference.child("chats").child(chatID)
                .addChildEventListener(mNewMessageListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadChats();
    }
}
