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
import java.util.Locale;

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

            // If message_type is plaintext, move timestamp to end
            newHolder.L_chat_messages_ll_child.setOrientation(LinearLayout.HORIZONTAL);

            // If self message, move it to end and set its bg color
            if (messageDAO.getSender().equals(FirebaseAuth.getInstance().getUid())) {
                newHolder.l_chat_messages_ll.setGravity(Gravity.END);
                newHolder.l_chat_messages_cv.setCardBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.message_bg));
            }

            newHolder.l_chat_message_tv_message.setText(messageDAO.getMessage());

            String time;
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.setTimeInMillis(Long.parseLong(messageDAO.getTimestamp()));

            int ampm = calendar.get(Calendar.AM_PM);
            if (ampm == Calendar.AM) {
                time = DateFormat.format("hh:mm", calendar).toString() + " AM";
            } else if (ampm == Calendar.PM) {
                time = DateFormat.format("hh:mm", calendar).toString() + " PM";
            } else {
                time = DateFormat.format("hh:mm", calendar).toString();
            }

            // Remove check mark from recipient
            if (messageDAO.getRecipient().equals(FirebaseAuth.getInstance().getUid())) {
                newHolder.l_chat_message_tv_timestamp.
                        setCompoundDrawablesWithIntrinsicBounds(
                                null, null, null, null);
            }

            newHolder.l_chat_message_tv_timestamp.setText(time);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public int getItemCount() {
        return messageDAOS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // region Resource Declaration
        LinearLayout l_chat_messages_ll;
        LinearLayout L_chat_messages_ll_child;
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
            L_chat_messages_ll_child = itemView.findViewById(R.id.l_chat_messages_ll_child);
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
