package com.projectsalvation.pigeotalk.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projectsalvation.pigeotalk.Activity.ChatActivity;
import com.projectsalvation.pigeotalk.DAO.ChatDAO;
import com.projectsalvation.pigeotalk.Fragment.ChatsFragment;
import com.projectsalvation.pigeotalk.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListRVAdapter extends RecyclerView.Adapter<ChatListRVAdapter.ViewHolder> {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    private Context mContext;
    private ArrayList<ChatDAO> mChatDAOS;

    public ChatListRVAdapter(Context mContext, ArrayList<ChatDAO> mChatDAOS) {
        this.mContext = mContext;
        this.mChatDAOS = mChatDAOS;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chats_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ViewHolder newHolder = holder;
        final ChatDAO chatDAO = mChatDAOS.get(position);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        Picasso.get().load(chatDAO.getPhotoUrl())
                .fit()
                .centerCrop()
                .into(newHolder.l_chats_list_civ_photo, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(chatDAO.getPhotoUrl())
                                .fit()
                                .centerCrop()
                                .into(newHolder.l_chats_list_civ_photo);
                    }
                });

        newHolder.l_chats_list_tv_display_name.setText(chatDAO.getName());
        newHolder.l_chats_list_tv_last_message.setText(chatDAO.getLastMessage());

        String time = "";
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(chatDAO.getTimestamp()));

        int ampm = calendar.get(Calendar.AM_PM);
        if (DateFormat.is24HourFormat(mContext)) {
            time = DateFormat.format("HH:mm", calendar).toString();
        } else {
            if (ampm == Calendar.AM) {
                time = DateFormat.format("hh:mm", calendar).toString() + " AM";
            } else if (ampm == Calendar.PM) {
                time = DateFormat.format("hh:mm", calendar).toString() + " PM";
            }
        }

        newHolder.l_chats_list_tv_time.setText(time);

        if (chatDAO.getIsMuted().equals("false")) {
            newHolder.l_chats_list_chip_mute.setVisibility(View.GONE);
        } else {
            newHolder.l_chats_list_chip_mute.setVisibility(View.VISIBLE);
        }

        newHolder.l_chats_list_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext.getApplicationContext(), ChatActivity.class);
                i.putExtra("userID", chatDAO.getUserId());
                i.putExtra("contactName", chatDAO.getName());
                i.putExtra("contactPhotoUrl", chatDAO.getPhotoUrl());
                i.putExtra("chatID", chatDAO.getChatId());

                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.getApplicationContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChatDAOS.size();
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
