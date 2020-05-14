package com.projectsalvation.pigeotalk.Fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.projectsalvation.pigeotalk.R;


public class ChatsFragment extends Fragment {

    // region Resource Declaration
    RecyclerView f_chats_rv;
    // endregion

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // region Resource Assignment
        f_chats_rv = container.findViewById(R.id.f_chats_rv);
        // endregion

        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

}
