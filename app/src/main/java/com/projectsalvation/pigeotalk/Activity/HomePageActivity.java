package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;
import com.projectsalvation.pigeotalk.Fragment.CallsFragment;
import com.projectsalvation.pigeotalk.Fragment.CameraFragment;
import com.projectsalvation.pigeotalk.Fragment.ChatsFragment;
import com.projectsalvation.pigeotalk.Fragment.StatusFragment;
import com.projectsalvation.pigeotalk.R;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {

    // region Resource Declaration
    TabLayout HomePage_tabs;
    Toolbar HomePage_toolbar;
    ViewPager HomePage_viewpager;
    // endregion

    private CameraFragment cameraFragment;
    private ChatsFragment chatsFragment;
    private StatusFragment statusFragment;
    private CallsFragment callsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // region Resource Assignment
        HomePage_tabs = findViewById(R.id.HomePage_tabs);
        HomePage_toolbar = findViewById(R.id.HomePage_toolbar);
        HomePage_viewpager = findViewById(R.id.HomePage_viewpager);
        // endregion

        setSupportActionBar(HomePage_toolbar);

        cameraFragment = new CameraFragment();
        chatsFragment = new ChatsFragment();
        statusFragment = new StatusFragment();
        callsFragment = new CallsFragment();

        HomePage_tabs.setupWithViewPager(HomePage_viewpager);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);

        viewPagerAdapter.addFragment(cameraFragment, "");
        viewPagerAdapter.addFragment(chatsFragment, "CHATS");
        viewPagerAdapter.addFragment(statusFragment, "STATUS");
        viewPagerAdapter.addFragment(callsFragment, "CALLS");

        HomePage_viewpager.setAdapter(viewPagerAdapter);

        HomePage_tabs.getTabAt(0).setIcon(R.drawable.ic_camera_alt_white_24dp);

        HomePage_viewpager.setCurrentItem(1);

        // region Shrink the first item in tab layout (camera item)
        LinearLayout layout = ((LinearLayout) ((LinearLayout) HomePage_tabs.getChildAt(0))
                .getChildAt(0));

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.weight = 0.64f; // e.g. 0.5f
        layout.setLayoutParams(layoutParams);
        // endregion

        HomePage_viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /* Maybe will use these methods to set and remove new message badge from the tab layout?
    public void setNewMessageBadge() {
        BadgeDrawable newMessageBadge = Objects.requireNonNull(HomePage_tabs.getTabAt(1))
                .getOrCreateBadge();

        int currentNewMessage = newMessageBadge.getNumber();
        newMessageBadge.setNumber(currentNewMessage + 1);
        newMessageBadge.setVisible(true);
    }

    public void removeNewMessageBadge() {
        BadgeDrawable newMessageBadge = Objects.requireNonNull(HomePage_tabs.getTabAt(1))
                .getOrCreateBadge();

        newMessageBadge.setVisible(false);
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // case R.id.HomePage_menuItem_settings:
        }

        return false;
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}
