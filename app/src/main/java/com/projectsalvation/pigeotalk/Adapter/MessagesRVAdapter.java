package com.projectsalvation.pigeotalk.Adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.DAO.MessageDAO;

import com.projectsalvation.pigeotalk.R;

import java.util.ArrayList;
import java.util.Calendar;

public class MessagesRVAdapter extends RecyclerView.Adapter<MessagesRVAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<MessageDAO> messageDAOS;

    private DatabaseReference mDatabaseReference;

    public MessagesRVAdapter(Context mContext, ArrayList<MessageDAO> messageDAOS) {
        this.mContext = mContext;
        this.messageDAOS = messageDAOS;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_message_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ViewHolder newHolder = holder;
        final MessageDAO messageDAO = messageDAOS.get(position);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        if (messageDAO.getMessageType().equals("plaintext")) {
            newHolder.l_chat_message_fl.setVisibility(View.GONE);
            newHolder.l_chat_message_btn_voice_attachment.setVisibility(View.GONE);

            if (messageDAO.getSender().equals(FirebaseAuth.getInstance().getUid())) {
                newHolder.l_chat_messages_ll.setGravity(Gravity.END);
            }

            newHolder.l_chat_message_tv_message.setText(messageDAO.getMessage());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(messageDAO.getTimestamp()));
            String time = DateFormat.format("hh:mm", calendar).toString();

            newHolder.l_chat_message_tv_timestamp.setText(time);

            listenMessageReadState(messageDAO, newHolder);
        }
    }

    private void listenMessageReadState(final MessageDAO messageDAO, final ViewHolder holder) {
        mDatabaseReference
                .child("chat_messages").child(messageDAO.getChatId())
                .child(messageDAO.getMessageId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey().equals("false")) {
                    mDatabaseReference.child("chat_messages").child(messageDAO.getChatId())
                            .child(messageDAO.getMessageId()).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!messageDAO.getSender().equals(FirebaseAuth.getInstance().getUid())) {
                                        holder.l_chat_message_tv_timestamp.
                                                setCompoundDrawablesWithIntrinsicBounds(
                                                null, null, null, null);
                                    } else {
                                        holder.l_chat_message_tv_timestamp
                                                .setCompoundDrawablesWithIntrinsicBounds(
                                                        null, null,
                                                        ContextCompat.getDrawable(mContext,
                                                                R.drawable.ic_checkmark_24dp)
                                                , null);
                                    }
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

    @Override
    public int getItemCount() {
        return messageDAOS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // region Resource Declaration
        LinearLayout l_chat_messages_ll;
        CardView l_chat_messages_cv;
        Button l_chat_message_btn_voice_attachment;
        FrameLayout l_chat_message_fl;
        ImageView l_chat_message_iv_image_attachment;
        Button l_chat_message_btn_video_play;
        TextView l_chat_message_tv_message;
        TextView l_chat_message_tv_timestamp;

        // endregion
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // region Resource Assignment
            l_chat_messages_ll = itemView.findViewById(R.id.l_chat_messages_ll);
            l_chat_messages_cv = itemView.findViewById(R.id.l_chat_messages_cv);
            l_chat_message_btn_voice_attachment = itemView.findViewById(R.id.l_chat_message_btn_voice_attachment);
            l_chat_message_fl = itemView.findViewById(R.id.l_chat_message_fl);
            l_chat_message_iv_image_attachment = itemView.findViewById(R.id.l_chat_message_iv_image_attachment);
            l_chat_message_btn_video_play = itemView.findViewById(R.id.l_chat_message_btn_video_play);
            l_chat_message_tv_message = itemView.findViewById(R.id.l_chat_message_tv_message);
            l_chat_message_tv_timestamp = itemView.findViewById(R.id.l_chat_message_tv_timestamp);
            // endregion
        }
    }
}
