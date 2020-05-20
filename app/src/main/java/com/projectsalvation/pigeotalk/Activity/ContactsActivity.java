package com.projectsalvation.pigeotalk.Activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
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

import jagerfield.mobilecontactslibrary.Contact.Contact;
import jagerfield.mobilecontactslibrary.ImportContacts;

public class ContactsActivity extends AppCompatActivity {

    // region Resource Declaration
    LinearLayout a_contacts_ll_progress;
    TextView a_contacts_tv_no_contacts;
    RecyclerView a_contacts_rv;
    MaterialToolbar a_contacts_toolbar;
    //endregion

    private final String TAG = "ContactsActivity";

    private Map<String, String> mFoundRegisteredContacts;
    private Map<String, String> mRegisteredPhoneNumbers;
    private ArrayList<Contact> mContactsOnDevice;
    private ArrayList<ContactDAO> mContactDAOS;

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
        a_contacts_tv_no_contacts = findViewById(R.id.a_contacts_tv_no_contacts);
        a_contacts_ll_progress = findViewById(R.id.a_contacts_ll_progress);
        a_contacts_toolbar = findViewById(R.id.a_contacts_toolbar);
        a_contacts_rv = findViewById(R.id.a_contacts_rv);
        // endregion

        setSupportActionBar(a_contacts_toolbar);

        // Enable back button
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mPhoneNumberUtil = PhoneNumberUtil.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        ImportContacts importContacts = new ImportContacts(ContactsActivity.this);
        mContactsOnDevice = importContacts.getContacts();

        mFoundRegisteredContacts = new HashMap<>();
        mRegisteredPhoneNumbers = new HashMap<>();
        mContactDAOS = new ArrayList<>();

        // Black magic to fix RV's layout error
        a_contacts_rv.setLayoutManager(new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false));

        a_contacts_tv_no_contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.text_invite_message));
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent,
                        getString(R.string.text_invite_to_pigeotalk));
                startActivity(shareIntent);
            }
        });

        // region Import device contacts and compare with registered numbers in our database
        // then load found contacts

        // Show the progress bar while loading
        a_contacts_ll_progress.setVisibility(View.VISIBLE);

        mDatabaseReference.child("user_phone_numbers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mRegisteredPhoneNumbers.put(snapshot.getKey(), snapshot.getValue().toString());
                }

                for (Contact contact : mContactsOnDevice) {
                    if (contact.getNumbers().isEmpty()) {
                        continue;
                    }

                    String deviceContactPhoneNumber = contact.getNumbers().getFirst().elementValue();


                    try {
                        final Phonenumber.PhoneNumber numberProto = mPhoneNumberUtil.parse(deviceContactPhoneNumber,
                                Locale.getDefault().getCountry());

                        for (final Map.Entry<String, String> entry : mRegisteredPhoneNumbers.entrySet()) {
                            if (mFirebaseAuth.getCurrentUser().getPhoneNumber()
                                    .contains(String.valueOf(numberProto.getNationalNumber()))) {

                                continue;
                            }

                            if (entry.getKey().contains(String.valueOf(numberProto.getNationalNumber()))) {
                                mDatabaseReference.child("users")
                                        .child(mFirebaseAuth.getUid())
                                        .child("contacts")
                                        .child(entry.getValue()).setValue(contact.getDisplaydName());

                                mDatabaseReference.child("user_contacts").child(mFirebaseAuth.getUid())
                                        .child(entry.getValue()).setValue(contact.getDisplaydName());

                                mFoundRegisteredContacts.put(entry.getValue(), contact.getDisplaydName());

                                // Found contact, hide no contact info
                                a_contacts_tv_no_contacts.setVisibility(View.GONE);
                            }
                        }
                    } catch (NumberParseException e) {
                        Log.d(TAG, "NumberParseException()" + e.toString());
                    }
                }

                loadContacts();

                // Set the toolbar subtitle to the number of contacts in user's device who is using PTalk.
                a_contacts_toolbar.setSubtitle(
                        getString(R.string.subtitle_contact_size, mFoundRegisteredContacts.size()));

                // Hide the progress bar after loading
                a_contacts_ll_progress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        // endregion

        mContactsRVAdapter = new ContactsRVAdapter(getApplicationContext(), mContactDAOS);
        a_contacts_rv.setAdapter(mContactsRVAdapter);
    }

    private void loadContacts() {
        mContactDAOS.clear();

        if (mFoundRegisteredContacts.size() == 0) {
            // No contact found, show no contact info
            a_contacts_tv_no_contacts.setVisibility(View.VISIBLE);
        }

        for (final Map.Entry<String, String> entry : mFoundRegisteredContacts.entrySet()) {
            mDatabaseReference.child("users").child(entry.getKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ContactDAO contactDAO = new ContactDAO(
                                    entry.getKey(),
                                    entry.getValue(),
                                    dataSnapshot.child("about").getValue().toString(),
                                    "",
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.a_contacts_menuItem_refresh:
                // TODO: Refresh
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return true;
    }

}
