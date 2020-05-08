package com.projectsalvation.pigeotalk.Activity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.projectsalvation.pigeotalk.Adapter.ContactsRVAdapter;
import com.projectsalvation.pigeotalk.DAO.ContactDAO;
import com.projectsalvation.pigeotalk.R;
import com.projectsalvation.pigeotalk.Utility.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import jagerfield.mobilecontactslibrary.Contact.Contact;
import jagerfield.mobilecontactslibrary.ImportContacts;

public class ContactsActivity extends AppCompatActivity {

    // region Resource Declaration
    LinearLayout a_contacts_ll_progress;
    RecyclerView a_contacts_rv;
    Toolbar a_contacts_toolbar;
    //endregion

    private final String TAG = "ContactsActivity";

    private Map<String, String> mRegisteredNumbers;
    private ArrayList<Contact> mDeviceContacts;
    private ArrayList<ContactDAO> mContactDAOS;
    private ArrayList<String> mUserContacts;

    private DatabaseReference mDatabaseReference;
    private ContactsRVAdapter mContactsRVAdapter;
    private PhoneNumberUtil mPhoneNumberUtil;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onResume() {
        super.onResume();

        if (!Util.checkPermission(Manifest.permission.READ_CONTACTS, getApplicationContext())) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // region Resource Assignment
        a_contacts_ll_progress = findViewById(R.id.a_contacts_ll_progress);
        a_contacts_toolbar = findViewById(R.id.a_contacts_toolbar);
        a_contacts_rv = findViewById(R.id.a_contacts_rv);
        // endregion

        setSupportActionBar(a_contacts_toolbar);

        a_contacts_ll_progress.setVisibility(View.VISIBLE);

        // Black magic.
        a_contacts_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mUserContacts = new ArrayList<>();
        mContactDAOS = new ArrayList<>();

        mFirebaseAuth = FirebaseAuth.getInstance();
        mPhoneNumberUtil = PhoneNumberUtil.getInstance();

        mContactsRVAdapter = new ContactsRVAdapter(getApplicationContext(), mContactDAOS);
        a_contacts_rv.setAdapter(mContactsRVAdapter);

        // region Import device contacts and compare with registered numbers in our database
        ImportContacts importContacts = new ImportContacts(ContactsActivity.this);
        mDeviceContacts = importContacts.getContacts();
        mRegisteredNumbers = new HashMap<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabaseReference.child("registered_numbers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mRegisteredNumbers.put(snapshot.getKey(), snapshot.getValue().toString());
                }

                for (Contact contact : mDeviceContacts) {
                    String deviceContactPhoneNumber = contact.getNumbers().getFirst().elementValue();

                    try {
                        Phonenumber.PhoneNumber numberProto = mPhoneNumberUtil.parse(deviceContactPhoneNumber,
                                Locale.getDefault().getCountry());

                        for (Map.Entry<String, String> entry : mRegisteredNumbers.entrySet()) {
                            if (mFirebaseAuth.getCurrentUser().getPhoneNumber()
                                    .contains(String.valueOf(numberProto.getNationalNumber()))) {

                                continue;
                            }

                            if (entry.getKey().contains(String.valueOf(numberProto.getNationalNumber()))) {
                                mDatabaseReference.child("users")
                                        .child(mFirebaseAuth.getCurrentUser().getUid())
                                        .child("contacts")
                                        .child(entry.getKey()).setValue(entry.getValue());

                                mUserContacts.add(entry.getValue());
                            }
                        }
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                }

                loadContacts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // endregion
    }

    private void loadContacts() {
        for (int i = 0; i < mUserContacts.size(); i++) {
            mDatabaseReference.child("users").child(mUserContacts.get(i))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ContactDAO contactDAO = new ContactDAO(
                                    dataSnapshot.child("name").getValue().toString(),
                                    dataSnapshot.child("about").getValue().toString(),
                                    "MOBILE",
                                    dataSnapshot.child("profile_photo_url").getValue().toString());

                            mContactDAOS.add(contactDAO);
                            mContactsRVAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // TODO: Handle error
                        }
                    });
        }

        a_contacts_ll_progress.setVisibility(View.GONE);
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
