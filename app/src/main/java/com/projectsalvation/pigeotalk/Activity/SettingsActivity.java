package com.projectsalvation.pigeotalk.Activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.projectsalvation.pigeotalk.R;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    // region Resource Declaration
    Toolbar a_settings_toolbar;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // region Resource Assignment
        a_settings_toolbar = findViewById(R.id.a_settings_toolbar);
        // endregion

        setSupportActionBar(a_settings_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
