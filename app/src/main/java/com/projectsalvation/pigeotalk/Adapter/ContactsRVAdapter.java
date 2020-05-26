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

import com.projectsalvation.pigeotalk.Activity.ChatActivity;
import com.projectsalvation.pigeotalk.DAO.ContactDAO;
import com.projectsalvation.pigeotalk.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsRVAdapter extends RecyclerView.Adapter<ContactsRVAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<ContactDAO> mContactDAOS;

    public ContactsRVAdapter(Context mContext, ArrayList<ContactDAO> mContactDAOS) {
        this.mContext = mContext;
        this.mContactDAOS = mContactDAOS;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contacts_list_layout, parent,
                false);

        return new ContactsRVAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ContactDAO contactDAO = mContactDAOS.get(position);

        Picasso.get().load(contactDAO.getProfilePhotoUrl())
                .fit()
                .centerCrop()
                .into(
                holder.l_contacts_list_civ_profile_photo, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(contactDAO.getProfilePhotoUrl()).into(
                                holder.l_contacts_list_civ_profile_photo);
                    }
                });

        holder.l_contacts_list_tv_display_name.setText(contactDAO.getName());
        holder.l_contact_list_tv_about.setText(contactDAO.getAbout());
        holder.l_contacts_list_tv_num_type.setText(contactDAO.getNumberType());

        holder.l_contacts_list_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext.getApplicationContext(), ChatActivity.class);
                i.putExtra("userID", contactDAO.getUserId());
                i.putExtra("contactName", contactDAO.getName());
                i.putExtra("contactPhotoUrl", contactDAO.getProfilePhotoUrl());

                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.getApplicationContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mContactDAOS.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // region Resource Declaration
        CircleImageView l_contacts_list_civ_profile_photo;
        TextView l_contacts_list_tv_display_name;
        TextView l_contacts_list_tv_num_type;
        RelativeLayout l_contacts_list_rl;
        TextView l_contact_list_tv_about;
        // endregion

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // region Resource Assignment
            l_contacts_list_civ_profile_photo = itemView.findViewById(R.id.l_contacts_list_civ_profile_photo);
            l_contacts_list_tv_display_name = itemView.findViewById(R.id.l_contacts_list_tv_display_name);
            l_contacts_list_tv_num_type = itemView.findViewById(R.id.l_contacts_list_tv_num_type);
            l_contact_list_tv_about = itemView.findViewById(R.id.l_contacts_list_tv_about);
            l_contacts_list_rl = itemView.findViewById(R.id.l_contacts_list_rl);
            // endregion
        }
    }
}
