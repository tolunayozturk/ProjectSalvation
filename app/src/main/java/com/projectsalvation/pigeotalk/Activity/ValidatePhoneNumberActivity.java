package com.projectsalvation.pigeotalk.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.text.HtmlCompat;

import android.telephony.PhoneNumberFormattingTextWatcher;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.View;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.projectsalvation.pigeotalk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ValidatePhoneNumberActivity extends AppCompatActivity {

    // region Resource Declaration
    Spinner ValidatePN_spinner_country;
    EditText ValidatePN_et_country_code;
    EditText ValidatePN_et_phone_number;
    MaterialButton ValidatePN_btn_next;
    Toolbar ValidatePN_toolbar;
    // endregion

    private static final String TAG = "ValidatePhoneNumberActivity";

    private PhoneNumberUtil mPhoneNumberUtil;
    private String mCountryCodeStr;
    private String mFormattedPhoneNumber;
    private boolean mIsValidPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_phone_number);

        // region Resource Assignment
        ValidatePN_spinner_country = findViewById(R.id.ValidatePN_spinner_country);
        ValidatePN_et_country_code = findViewById(R.id.ValidatePN_et_country_code);
        ValidatePN_et_phone_number = findViewById(R.id.ValidatePN_et_phone_number);
        ValidatePN_btn_next = findViewById(R.id.ValidatePN_btn_next);
        ValidatePN_toolbar = findViewById(R.id.ValidatePN_toolbar);
        // endregion

        setSupportActionBar(ValidatePN_toolbar);

        mPhoneNumberUtil = PhoneNumberUtil.getInstance();

        // Make edit_country_code not editable
        ValidatePN_et_country_code.setKeyListener(null);

        // region Fill ValidatePN_spinner_country
        List<String> countryArray = new ArrayList<>();

        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            String item = locale.getDisplayName() + " " + "(" + countryCode + ")";
            countryArray.add(item);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, countryArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ValidatePN_spinner_country.setAdapter(adapter);
        //endregion

        // region Set default item in ValidatePN_spinner_country to default locale of the user
        String defaultCountry = Locale.getDefault().getDisplayCountry() + " " + "(" +
                Locale.getDefault().getCountry() + ")";

        int indexOfDefaultCountry = countryArray.indexOf(defaultCountry);
        ValidatePN_spinner_country.setSelection(indexOfDefaultCountry);
        // endregion

        ValidatePN_spinner_country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = ValidatePN_spinner_country.getSelectedItem().toString();
                mCountryCodeStr = selectedItem.substring(selectedItem.length() - 3,
                        selectedItem.length() - 1);

                int countryCode = mPhoneNumberUtil.getCountryCodeForRegion(mCountryCodeStr);

                ValidatePN_et_country_code.setHint("+" + countryCode);

                PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher =
                        new PhoneNumberFormattingTextWatcher(mCountryCodeStr);

                ValidatePN_et_phone_number.removeTextChangedListener(phoneNumberFormattingTextWatcher);
                ValidatePN_et_phone_number.addTextChangedListener(phoneNumberFormattingTextWatcher);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ValidatePN_btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = ValidatePN_et_phone_number.getText().toString();

                if (phoneNumber.isEmpty()) {
                    // TODO: Phone number field is empty. Show a message and update the UI.
                    return;
                }

                try {
                    Phonenumber.PhoneNumber numberProto = mPhoneNumberUtil.parse(
                            phoneNumber,
                            mCountryCodeStr
                    );

                    mIsValidPhoneNumber = mPhoneNumberUtil.isValidNumber(numberProto);

                    mFormattedPhoneNumber = mPhoneNumberUtil.format(
                            numberProto,
                            PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                    );

                    // region Prompt a dialog to confirm the number before continuing to SMS verification
                    MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(
                            ValidatePhoneNumberActivity.this);

                    materialAlertDialogBuilder.setMessage(HtmlCompat.fromHtml(
                            getString(R.string.dialog_confirm_number, mFormattedPhoneNumber),
                            HtmlCompat.FROM_HTML_MODE_LEGACY)
                    );

                    materialAlertDialogBuilder.setPositiveButton(
                            R.string.action_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mIsValidPhoneNumber) {
                                        Intent i = new Intent(ValidatePhoneNumberActivity.this,
                                                VerifySMSActivity.class);

                                        i.putExtra("formattedPhoneNumber", mFormattedPhoneNumber);
                                        i.putExtra("countryCodeStr", mCountryCodeStr);
                                        startActivity(i);
                                    } else {
                                        // TODO: Invalid number. Show a message and update the UI.
                                    }
                                }
                            });

                    materialAlertDialogBuilder.setNegativeButton(R.string.action_edit, null);

                    AlertDialog numberConfirmationDialog = materialAlertDialogBuilder.create();
                    numberConfirmationDialog.show();
                    // endregion

                } catch (NumberParseException e) {
                    // TODO: Invalid number. Show a message and update the UI.
                    e.printStackTrace();
                }
            }
        });
    }
}