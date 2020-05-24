package com.projectsalvation.pigeotalk.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.projectsalvation.pigeotalk.DAO.MessageDAO;
import com.projectsalvation.pigeotalk.R;
import com.projectsalvation.pigeotalk.Utility.Util;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.ImageLoader;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupMessagesRVAdapter extends RecyclerView.Adapter<GroupMessagesRVAdapter.ViewHolder> {

    private static final String TAG = "GroupMessagesRVAdapter";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private ArrayList<MessageDAO> messageDAOS;
    private Context mContext;

    private ChildEventListener mImageStateListener;

    public GroupMessagesRVAdapter(ArrayList<MessageDAO> messageDAOS, Context mContext) {
        this.messageDAOS = messageDAOS;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chat_group_message_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ViewHolder newHolder = holder;
        final MessageDAO messageDAO = messageDAOS.get(position);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        if (messageDAO.getMessageType().equals("plaintext")) {
            newHolder.l_chat_group_message_fl.setVisibility(View.GONE);
            newHolder.l_chat_group_message_btn_voice_attachment.setVisibility(View.GONE);

            newHolder.l_chat_group_message_tv_message.setVisibility(View.VISIBLE);

            // If message_type is plaintext, move timestamp to end
            newHolder.l_chat_group_messages_ll_child.setOrientation(LinearLayout.HORIZONTAL);

            newHolder.l_chat_group_message_tv_message.setText(messageDAO.getMessage());
        } else if (messageDAO.getMessageType().equals("image")) {
            newHolder.l_chat_group_message_tv_message.setVisibility(View.GONE);
            newHolder.l_chat_group_message_btn_voice_attachment.setVisibility(View.GONE);
            newHolder.l_chat_group_message_btn_video_play.setVisibility(View.GONE);

            newHolder.l_chat_group_message_fl.setVisibility(View.VISIBLE);
            newHolder.l_chat_group_message_iv_image_attachment.setVisibility(View.VISIBLE);
            newHolder.l_chat_group_message_pb.setVisibility(View.VISIBLE);

            // If message_type is image, move timestamp to bottom
            newHolder.l_chat_group_messages_ll_child.setOrientation(LinearLayout.VERTICAL);

            listenImageState(messageDAO, newHolder);

            if (!messageDAO.getMessage().equals("")) {
                Picasso.get().load(messageDAO.getMessage())
                        .fit()
                        .centerCrop()
                        .into(holder.l_chat_group_message_iv_image_attachment, new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.l_chat_group_message_pb.setVisibility(View.GONE);

                                holder.l_chat_group_message_iv_image_attachment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new StfalconImageViewer.Builder<>(mContext, new String[]{messageDAO.getMessage()}, new ImageLoader<String>() {
                                            @Override
                                            public void loadImage(ImageView imageView, String imageUrl) {
                                                Picasso.get().load(imageUrl).into(imageView);
                                            }
                                        }).withStartPosition(0).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                Picasso.get().load(messageDAO.getMessage()).into(
                                        holder.l_chat_group_message_iv_image_attachment);

                                holder.l_chat_group_message_iv_image_attachment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new StfalconImageViewer.Builder<>(mContext, new String[]{messageDAO.getMessage()}, new ImageLoader<String>() {
                                            @Override
                                            public void loadImage(ImageView imageView, String imageUrl) {
                                                Picasso.get().load(imageUrl).into(imageView);
                                            }
                                        }).withStartPosition(0).show();
                                    }
                                });
                            }
                        });
            }
        } else if (messageDAO.getMessageType().equals("system")) {
            newHolder.l_chat_group_message_fl.setVisibility(View.GONE);
            newHolder.l_chat_group_message_btn_voice_attachment.setVisibility(View.GONE);
            newHolder.l_chat_group_message_tv_timestamp.setVisibility(View.GONE);
            newHolder.l_chat_group_message_tv_sender_name.setVisibility(View.GONE);

            newHolder.l_chat_group_message_tv_message.setVisibility(View.VISIBLE);

            newHolder.l_chat_group_messages_ll.setGravity(Gravity.CENTER);

            newHolder.l_chat_group_messages_cv.setCardBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.colorAccent));
            newHolder.l_chat_group_message_tv_message.setTextColor(
                    ContextCompat.getColor(mContext, R.color.white));

            newHolder.l_chat_group_message_tv_message.setText(messageDAO.getMessage());

            return;
        }

        // If self message, move it to end and set its bg color
        if (messageDAO.getSender().equals(FirebaseAuth.getInstance().getUid())) {
            newHolder.l_chat_group_messages_ll.setGravity(Gravity.END);
            newHolder.l_chat_group_messages_cv.setCardBackgroundColor(
                    ContextCompat.getColor(mContext, R.color.message_bg));
        }

        if (messageDAO.getSender().equals(mFirebaseAuth.getUid())) {
            newHolder.l_chat_group_message_tv_sender_name.setText("You");
        } else {
            mDatabaseReference.child("user_contacts").child(mFirebaseAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(messageDAO.getSender())) {
                                String uid = messageDAO.getSender();
                                newHolder.l_chat_group_message_tv_sender_name.setText(
                                        dataSnapshot.child(messageDAO.getSender()).getValue().toString());

                                newHolder.l_chat_group_message_tv_sender_name.setTextColor(
                                        Util.ColorFromString(uid));

                            } else {
                                mDatabaseReference.child("users").child(messageDAO.getSender())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                String phoneNumber = dataSnapshot.child("phone_number").getValue().toString();
                                                String name = dataSnapshot.child("name").getValue().toString();
                                                String uid = dataSnapshot.child("uid").getValue().toString();
                                                // TODO: Maybe append the public name to number?
                                                newHolder.l_chat_group_message_tv_sender_name.setText(
                                                        mContext.getString(R.string.text_colored_name, phoneNumber)
                                                );

                                                newHolder.l_chat_group_message_tv_sender_name.setTextColor(
                                                        Util.ColorFromString(uid));
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

        String time = "";
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(messageDAO.getTimestamp()));

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

        newHolder.l_chat_group_message_tv_timestamp.setText(time);

        // Remove checkmark from recipient
        if (!messageDAO.getSender().equals(mFirebaseAuth.getUid())) {
            holder.l_chat_group_message_tv_timestamp.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    null,
                    null
            );
        }

        if (messageDAO.getSender().equals(mFirebaseAuth.getUid())
                && messageDAO.getIsRead().equals("true")) {
            holder.l_chat_group_message_tv_timestamp.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    ContextCompat.getDrawable(mContext, R.drawable.ic_doubletick_24dp),
                    null
            );
        }

        listenMessageSeenState(messageDAO, newHolder);

    }

    private void listenImageState(final MessageDAO messageDAO, final ViewHolder holder) {
        mImageStateListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildChanged(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                if (messageDAO.getMessageType().equals("image") && dataSnapshot.getKey().equals("message")) {
                    Picasso.get().load(dataSnapshot.getValue().toString())
                            .fit()
                            .centerCrop()
                            .into(holder.l_chat_group_message_iv_image_attachment, new Callback() {
                                @Override
                                public void onSuccess() {
                                    holder.l_chat_group_message_pb.setVisibility(View.GONE);

                                    holder.l_chat_group_message_iv_image_attachment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new StfalconImageViewer.Builder<>(mContext.getApplicationContext(), new String[]{dataSnapshot.getValue().toString()}, new ImageLoader<String>() {
                                                @Override
                                                public void loadImage(ImageView imageView, String imageUrl) {
                                                    Picasso.get().load(imageUrl).into(imageView);
                                                }
                                            }).show();
                                        }
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(dataSnapshot.getValue().toString()).into(
                                            holder.l_chat_group_message_iv_image_attachment);
                                    holder.l_chat_group_message_pb.setVisibility(View.GONE);

                                    holder.l_chat_group_message_iv_image_attachment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new StfalconImageViewer.Builder<>(mContext.getApplicationContext(), new String[]{dataSnapshot.getValue().toString()}, new ImageLoader<String>() {
                                                @Override
                                                public void loadImage(ImageView imageView, String imageUrl) {
                                                    Picasso.get().load(imageUrl).into(imageView);
                                                }
                                            }).show();
                                        }
                                    });
                                }
                            });

                    holder.l_chat_group_message_pb.setVisibility(View.GONE);
                }
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

        mDatabaseReference.child("group_messages").child(messageDAO.getChatId())
                .child(messageDAO.getMessageId())
                .addChildEventListener(mImageStateListener);
    }

    private void listenMessageSeenState(final MessageDAO messageDAO, final ViewHolder holder) {
        mDatabaseReference.child("group_messages").child(messageDAO.getChatId()).limitToLast(1)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        if (messageDAO.getSender().equals(mFirebaseAuth.getUid())) {
                            holder.l_chat_group_message_tv_timestamp.setCompoundDrawablesWithIntrinsicBounds(
                                    null,
                                    null,
                                    ContextCompat.getDrawable(mContext, R.drawable.ic_doubletick_24dp),
                                    null
                            );
                        }

                        mDatabaseReference.child("group_messages").child(messageDAO.getChatId()).limitToLast(1)
                                .removeEventListener(this);
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
                });
    }

    @Override
    public int getItemCount() {
        return messageDAOS.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // region Resource Declaration
        LinearLayout l_chat_group_messages_ll;
        LinearLayout l_chat_group_messages_ll_child;
        CardView l_chat_group_messages_cv;
        Button l_chat_group_message_btn_voice_attachment;
        FrameLayout l_chat_group_message_fl;
        ImageView l_chat_group_message_iv_image_attachment;
        Button l_chat_group_message_btn_video_play;
        TextView l_chat_group_message_tv_message;
        TextView l_chat_group_message_tv_timestamp;
        TextView l_chat_group_message_tv_sender_name;
        ProgressBar l_chat_group_message_pb;
        // endregion

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // region Resource Assignment
            l_chat_group_messages_ll = itemView.findViewById(R.id.l_chat_group_messages_ll);
            l_chat_group_messages_ll_child = itemView.findViewById(R.id.l_chat_group_messages_ll_child);
            l_chat_group_messages_cv = itemView.findViewById(R.id.l_chat_group_messages_cv);
            l_chat_group_message_btn_voice_attachment = itemView.findViewById(R.id.l_chat_group_message_btn_voice_attachment);
            l_chat_group_message_fl = itemView.findViewById(R.id.l_chat_group_message_fl);
            l_chat_group_message_iv_image_attachment = itemView.findViewById(R.id.l_chat_group_message_iv_image_attachment);
            l_chat_group_message_btn_video_play = itemView.findViewById(R.id.l_chat_group_message_btn_video_play);
            l_chat_group_message_tv_message = itemView.findViewById(R.id.l_chat_group_message_tv_message);
            l_chat_group_message_tv_timestamp = itemView.findViewById(R.id.l_chat_group_message_tv_timestamp);
            l_chat_group_message_tv_sender_name = itemView.findViewById(R.id.l_chat_group_message_tv_sender_name);
            l_chat_group_message_pb = itemView.findViewById(R.id.l_chat_group_message_pb);
            // endregion
        }
    }
}
