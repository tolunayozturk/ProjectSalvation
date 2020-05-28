package com.projectsalvation.pigeotalk.Adapter;

import android.content.Context;
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
import com.projectsalvation.pigeotalk.DAO.NotificationDAO;
import com.projectsalvation.pigeotalk.DAO.StatusDAO;
import com.projectsalvation.pigeotalk.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class StatusListRVAdapter extends RecyclerView.Adapter<StatusListRVAdapter.ViewHolder> {

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;

    private Context mContext;
    private ArrayList<StatusDAO> mStatusDAOS;

    public StatusListRVAdapter(Context mContext, ArrayList mStatusDAOS) {
        this.mContext = mContext;
        this.mStatusDAOS = mStatusDAOS;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.chats_list_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        final ViewHolder newHolder = holder;
        final StatusDAO statusDAO = mStatusDAOS.get(position);


    }

    @Override
    public int getItemCount() {
        return mStatusDAOS.size();
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
