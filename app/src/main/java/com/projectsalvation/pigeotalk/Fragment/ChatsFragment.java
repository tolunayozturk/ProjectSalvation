package com.projectsalvation.pigeotalk.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    // endregion

    private static String TAG = "ChatsFragment";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

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
        // endregion

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mChatDAOS = new ArrayList<>();

        f_chats_rv.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));

        mChatListRVAdapter = new ChatListRVAdapter(getActivity().getApplicationContext(), mChatDAOS);
        f_chats_rv.setAdapter(mChatListRVAdapter);

        return view;
    }

    private void getChats() {
        mDatabaseReference.child("user_chats").child(mFirebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot == null) {
                                return;
                            }

                            Log.d(TAG, snapshot.toString());
                            getChatInformation(snapshot);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void getChatInformation(final DataSnapshot snapshot) {
        mDatabaseReference.child("chat_messages").child(snapshot.getKey()).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (final DataSnapshot msg : dataSnapshot.getChildren()) {
                            String recipientId = msg.child("recipient").getValue().toString();

                            mDatabaseReference.child("users").child(recipientId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            setUpRecyclerView(msg, dataSnapshot);
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
    }

    private void setUpRecyclerView(final DataSnapshot msgSnapshot, final DataSnapshot userSnapshot) {
        final String photoUrl = userSnapshot.child("profile_photo_url").getValue().toString();
        final String recipientId = msgSnapshot.child("recipient").getValue().toString();

        mDatabaseReference.child("users").child(mFirebaseAuth.getUid())
                .child("contacts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = "";
                        if (dataSnapshot.hasChild(recipientId)) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(recipientId)) {
                                    name = snapshot.getValue().toString();
                                    Log.d(TAG, name);
                                }
                            }
                        } else {
                            name = userSnapshot.child("name").getValue().toString();
                            Log.d(TAG, name);
                        }

                        ChatDAO chatDAO = new ChatDAO(
                                msgSnapshot.child("chatId").getValue().toString(),
                                photoUrl,
                                name,
                                msgSnapshot.child("message").getValue().toString(),
                                msgSnapshot.child("timestamp").getValue().toString(),
                                "0",
                                "false",
                                msgSnapshot.child("recipient").getValue().toString());

                        mChatDAOS.add(chatDAO);
                        mChatListRVAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mChatDAOS.clear();
        getChats();
    }
}
