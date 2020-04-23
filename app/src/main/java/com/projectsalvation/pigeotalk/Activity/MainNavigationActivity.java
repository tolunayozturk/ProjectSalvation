package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.projectsalvation.pigeotalk.Adapter.MainNavigationAdapter;
import com.projectsalvation.pigeotalk.R;

public class MainNavigationActivity extends AppCompatActivity {

    // region Resource Declaration
    Toolbar MainNavigation_toolbar;
    ViewPager MainNavigation_viewpager;
    TabLayout MainNavigation_tablayout;
    //endregion

    MainNavigationAdapter mainNavigationAdapter;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);

        //region Resource Assignment
        MainNavigation_toolbar = findViewById(R.id.MainNavigation_toolbar);
        MainNavigation_viewpager = findViewById(R.id.MainNavigation_viewpager);
        MainNavigation_tablayout = findViewById(R.id.MainNavigation_tablayout);
        //endregion

        setSupportActionBar(MainNavigation_toolbar);

        auth = FirebaseAuth.getInstance();

        mainNavigationAdapter = new MainNavigationAdapter(getSupportFragmentManager(),0);
        MainNavigation_viewpager.setAdapter(mainNavigationAdapter);
        MainNavigation_tablayout.setupWithViewPager(MainNavigation_viewpager);
    }


    //region Options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i;
        switch (item.getItemId()){
            case R.id.dropdown_settings:
                i = new Intent(MainNavigationActivity.this,SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.dropdown_logout:
                auth.signOut();
                i = new Intent(MainNavigationActivity.this,ValidatePhoneNumberActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //endregion
}
