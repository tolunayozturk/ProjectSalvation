package com.projectsalvation.pigeotalk.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;


import com.google.android.material.tabs.TabLayout;
import com.projectsalvation.pigeotalk.Adapter.MainNavigationAdapter;
import com.projectsalvation.pigeotalk.R;

public class MainNavigationActivity extends AppCompatActivity {


    private Toolbar MainNavigation_toolbar;
    private ViewPager MainNavigation_viewpager;
    private TabLayout MainNavigation_tablayout;

    private MainNavigationAdapter mainNavigationAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);


        MainNavigation_toolbar = findViewById(R.id.MainNavigation_toolbar);
        MainNavigation_viewpager = findViewById(R.id.MainNavigation_viewpager);
        MainNavigation_tablayout = findViewById(R.id.MainNavigation_tablayout);

        mainNavigationAdapter = new MainNavigationAdapter(getSupportFragmentManager(),0);
        MainNavigation_viewpager.setAdapter(mainNavigationAdapter);

        MainNavigation_tablayout.setupWithViewPager(MainNavigation_viewpager);
    }
}
