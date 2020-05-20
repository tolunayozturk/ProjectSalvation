package com.projectsalvation.pigeotalk.Fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.Adapter.GroupChatListRVAdapter;
import com.projectsalvation.pigeotalk.DAO.GroupChatDAO;
import com.projectsalvation.pigeotalk.R;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {

    // region Resource Declaration
    RecyclerView f_groups_rv;
    // endregion

    private static String TAG = "GroupsFragment";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private ValueEventListener mNewMessageListener;

    private GroupChatListRVAdapter mGroupChatListRVAdapter;
    private ArrayList<GroupChatDAO> mGroupChatDAOS;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // region Resource Assignment
        f_groups_rv = view.findViewById(R.id.f_groups_rv);
        // endregion

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mGroupChatDAOS = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);

        f_groups_rv.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(f_groups_rv.getContext(),
                layoutManager.getOrientation());
        f_groups_rv.addItemDecoration(dividerItemDecoration);

        mGroupChatListRVAdapter = new GroupChatListRVAdapter(getActivity().getApplicationContext(), mGroupChatDAOS);
        f_groups_rv.setAdapter(mGroupChatListRVAdapter);

        return view;
    }

    private void retrieveChats() {

        mNewMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mGroupChatDAOS.clear();

                for (final DataSnapshot chat : dataSnapshot.getChildren()) {
                    final GroupChatDAO groupChatDAO = new GroupChatDAO(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null);

                    groupChatDAO.setChatId(chat.getKey());

                    Log.d(TAG, "onDataChange - ChatID: " + groupChatDAO.getChatId());

                    mDatabaseReference.child("groups").child(groupChatDAO.getChatId())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull final DataSnapshot groupData) {
                                    groupChatDAO.setPhotoUrl(groupData.child("groupPhotoUrl")
                                            .getValue().toString());

                                    groupChatDAO.setGroupName(groupData.child("groupName").getValue().toString());

                                    Log.d(TAG, "onDataChange - " + groupChatDAO.getPhotoUrl());

                                    mDatabaseReference.child("group_messages").child(groupChatDAO.getChatId())
                                            .limitToLast(1)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot msg : dataSnapshot.getChildren()) {
                                                        groupChatDAO.setLastMessage(msg.child("message")
                                                                .getValue().toString());

                                                        groupChatDAO.setMessageType(msg.child("messageType")
                                                                .getValue().toString());

                                                        groupChatDAO.setTimestamp(msg.child("timestamp")
                                                                .getValue().toString());

                                                        groupChatDAO.setSenderId(msg.child("sender").getValue().toString());

                                                        mDatabaseReference.child("user_chats_unread_messages")
                                                                .child(mFirebaseAuth.getUid())
                                                                .child(groupChatDAO.getChatId())
                                                                .addListenerForSingleValueEvent(
                                                                        new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                groupChatDAO.setUnreadMessageCount(
                                                                                        String.valueOf(dataSnapshot.getChildrenCount()));

                                                                                groupChatDAO.setIsMuted("false");

                                                                                mGroupChatDAOS.add(groupChatDAO);
                                                                                mGroupChatListRVAdapter.notifyDataSetChanged();
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

        mDatabaseReference.child("user_groups").child(mFirebaseAuth.getUid())
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

        mDatabaseReference.child("user_groups").child(mFirebaseAuth.getUid())
                .removeEventListener(mNewMessageListener);
    }
}
