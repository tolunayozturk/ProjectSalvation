package com.projectsalvation.pigeotalk.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.projectsalvation.pigeotalk.Fragment.CallFragment;
import com.projectsalvation.pigeotalk.Fragment.ChatFragment;
import com.projectsalvation.pigeotalk.Fragment.StatusFragment;

public class MainNavigationAdapter extends FragmentPagerAdapter {

    public MainNavigationAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 1:
                return new StatusFragment();
            case 2:
                return  new CallFragment();
            case 0:
            default:
                return new ChatFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Chats";
            case 1:
                return "Status";
            case 2:
                return  "Calls";
            default:
                return null;
        }
    }
}
