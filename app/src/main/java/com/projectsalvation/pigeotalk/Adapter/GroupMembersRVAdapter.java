package com.projectsalvation.pigeotalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projectsalvation.pigeotalk.Activity.ContactInfoActivity;
import com.projectsalvation.pigeotalk.DAO.UserDAO;
import com.projectsalvation.pigeotalk.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupMembersRVAdapter extends RecyclerView.Adapter<GroupMembersRVAdapter.ViewHolder> {

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private Context mContext;
    private ArrayList<UserDAO> mUserDAOS;

    public GroupMembersRVAdapter(Context mContext, ArrayList<UserDAO> mUserDAOS) {
        this.mContext = mContext;
        this.mUserDAOS = mUserDAOS;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chats_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        final ViewHolder newHolder = holder;
        final UserDAO userDAO = mUserDAOS.get(position);

        Picasso.get().load(userDAO.getPhotoUrl())
                .fit()
                .centerCrop()
                .into(newHolder.l_chats_list_civ_photo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(userDAO.getPhotoUrl())
                                .fit()
                                .centerCrop()
                                .into(newHolder.l_chats_list_civ_photo);
                    }
                });

        newHolder.l_chats_list_tv_time.setVisibility(View.GONE);
        newHolder.l_chats_list_chip_mute.setVisibility(View.GONE);
        newHolder.l_chats_list_chip_new_message_count.setVisibility(View.GONE);

        newHolder.l_chats_list_tv_display_name.setText(userDAO.getDisplayName());
        newHolder.l_chats_list_tv_last_message.setText(userDAO.getAbout());

        newHolder.l_chats_list_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext.getApplicationContext(), ContactInfoActivity.class);
                i.putExtra("userID", userDAO.getUserID());
                i.putExtra("userName", userDAO.getDisplayName());
                i.putExtra("userPhotoUrl", userDAO.getPhotoUrl());

                mContext.getApplicationContext().startActivity(i);
            }
        });

//        newHolder.l_chats_list_rl.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                MaterialAlertDialogBuilder alertDialogBuilder =
//                        new MaterialAlertDialogBuilder(mContext.getApplicationContext())
//                                .setMessage(mContext.getString(R.string.dialog_remove_group_member))
//                                .setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        // TODO: Remove member
//                                        // mUserDAOS.remove(position);
//                                        // notifyDataSetChanged();
//                                    }
//                                })
//                                .setNegativeButton(R.string.action_no, null);
//
//                AlertDialog permissionExplanationDialog = alertDialogBuilder.create();
//                permissionExplanationDialog.show();
//                return true;
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mUserDAOS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // region Resource Declaration
        RelativeLayout l_chats_list_rl;
        CircleImageView l_chats_list_civ_photo;
        TextView l_chats_list_tv_display_name;
        TextView l_chats_list_tv_last_message;
        TextView l_chats_list_tv_time;
        Chip l_chats_list_chip_mute;
        Chip l_chats_list_chip_new_message_count;
        // endregion

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // region Resource Assignment
            l_chats_list_rl = itemView.findViewById(R.id.l_chats_list_rl);
            l_chats_list_civ_photo = itemView.findViewById(R.id.l_chats_list_civ_photo);
            l_chats_list_tv_display_name = itemView.findViewById(R.id.l_chats_list_tv_display_name);
            l_chats_list_tv_last_message = itemView.findViewById(R.id.l_chats_list_tv_last_message);
            l_chats_list_tv_time = itemView.findViewById(R.id.l_chats_list_tv_time);
            l_chats_list_chip_mute = itemView.findViewById(R.id.l_chats_list_chip_mute);
            l_chats_list_chip_new_message_count = itemView.findViewById(R.id.l_chats_list_chip_new_message_count);
            // endregion
        }
    }
}
