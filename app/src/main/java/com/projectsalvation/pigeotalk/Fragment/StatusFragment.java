package com.projectsalvation.pigeotalk.Fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.projectsalvation.pigeotalk.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment {

    // region Resource Declaration
    RelativeLayout f_status_rl;
    CircleImageView f_status_civ_profile_photo;
    TextView f_status_tv_status_timestamp;
    Button f_status_add_status;
    RecyclerView f_status_rv;
    TextView f_status_tv_no_status;
    // endregion

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mFirebaseAuth;


    public StatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        // region Resource Assignment
        f_status_rl = view.findViewById(R.id.f_status_rl);
        f_status_civ_profile_photo = view.findViewById(R.id.f_status_civ_profile_photo);
        f_status_tv_status_timestamp = view.findViewById(R.id.f_status_tv_status_timestamp);
        f_status_add_status = view.findViewById(R.id.f_status_add_status);
        f_status_rv = view.findViewById(R.id.f_status_rv);
        f_status_tv_no_status = view.findViewById(R.id.f_status_tv_no_status);
        // endregion

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();



        return view;
    }

}
