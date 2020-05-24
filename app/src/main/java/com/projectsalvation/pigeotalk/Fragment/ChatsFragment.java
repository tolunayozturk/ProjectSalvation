package com.projectsalvation.pigeotalk.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.Adapter.ChatListRVAdapter;
import com.projectsalvation.pigeotalk.DAO.ChatDAO;
import com.projectsalvation.pigeotalk.R;

import java.util.ArrayList;


public class ChatsFragment extends Fragment {

    // region Resource Declaration
    RecyclerView f_chats_rv;
    TextView f_chats_tv_no_chats;
    // endregion

    private static String TAG = "ChatsFragment";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private ValueEventListener mNewMessageListener;

    private ChatListRVAdapter mChatListRVAdapter;
    private ArrayList<ChatDAO> mChatDAOS;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        // region Resource Assignment
        f_chats_rv = view.findViewById(R.id.f_chats_rv);
        f_chats_tv_no_chats = view.findViewById(R.id.f_chats_tv_no_chats);
        // endregion

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mChatDAOS = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);

        f_chats_rv.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(f_chats_rv.getContext(),
                layoutManager.getOrientation());
        f_chats_rv.addItemDecoration(dividerItemDecoration);

        mChatListRVAdapter = new ChatListRVAdapter(getActivity().getApplicationContext(), mChatDAOS);
        f_chats_rv.setAdapter(mChatListRVAdapter);

        return view;
    }

    private void retrieveChats() {

        mNewMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    f_chats_tv_no_chats.setVisibility(View.VISIBLE);
                    return;
                }
                mChatDAOS.clear();

                for (final DataSnapshot chat : dataSnapshot.getChildren()) {
                    f_chats_tv_no_chats.setVisibility(View.GONE);
                    final ChatDAO chatDAO = new ChatDAO(null,
                            null,
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

                    mDatabaseReference.child("users").child(chatDAO.getUserId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot userData) {
                                    chatDAO.setPhotoUrl(userData.child("profile_photo_url")
                                            .getValue().toString());

                                    Log.d(TAG, "onDataChange - " + chatDAO.getPhotoUrl());

                                    mDatabaseReference.child("chat_messages").child(chatDAO.getChatId())
                                            .limitToLast(1)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot msg : dataSnapshot.getChildren()) {
                                                        chatDAO.setLastMessage(msg.child("message")
                                                                .getValue().toString());

                                                        chatDAO.setMessageType(msg.child("messageType")
                                                                .getValue().toString());

                                                        chatDAO.setTimestamp(msg.child("timestamp")
                                                                .getValue().toString());

                                                        mDatabaseReference.child("user_chats_unread_messages")
                                                                .child(mFirebaseAuth.getUid())
                                                                .child(chatDAO.getChatId())
                                                                .addListenerForSingleValueEvent(
                                                                        new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                chatDAO.setUnreadMessageCount(
                                                                                        String.valueOf(dataSnapshot.getChildrenCount()));

                                                                                chatDAO.setIsMuted("false");

                                                                                mDatabaseReference.child("user_contacts").child(mFirebaseAuth.getUid())
                                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                if (dataSnapshot.hasChild(chatDAO.getUserId())) {
                                                                                                    String name = dataSnapshot.child(chatDAO
                                                                                                            .getUserId()).getValue().toString();

                                                                                                    chatDAO.setName(name);
                                                                                                } else {
                                                                                                    chatDAO.setName(userData.child("phone_number").getValue().toString());
                                                                                                }

                                                                                                Log.d(TAG, "onDataChange: " + chatDAO.getName());

                                                                                                mChatDAOS.add(chatDAO);
                                                                                                mChatListRVAdapter.notifyDataSetChanged();

                                                                                            }

                                                                                            @Override
                                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                            }
                                                                                        });
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                            }
                                                                        }
                                                                );
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
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
        };

        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .addValueEventListener(mNewMessageListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        retrieveChats();
    }

    @Override
    public void onPause() {
        super.onPause();

        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .removeEventListener(mNewMessageListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .removeEventListener(mNewMessageListener);
    }
}
