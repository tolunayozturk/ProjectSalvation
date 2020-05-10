package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;
import com.projectsalvation.pigeotalk.R;

import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    // region Resource Declaration
    MaterialToolbar a_chat_toolbar;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // region Resource Assignment
        a_chat_toolbar = findViewById(R.id.a_chat_toolbar);
        // endregion

        setSupportActionBar(a_chat_toolbar);

        // Enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(getString(R.string.EMPTY_STRING));
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
