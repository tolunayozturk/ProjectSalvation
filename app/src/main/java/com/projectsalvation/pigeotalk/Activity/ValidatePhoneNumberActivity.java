package com.projectsalvation.pigeotalk.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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
    // endregion

    PhoneNumberUtil phoneNumberUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_phone_number);

        // region Resource Assignment
        ValidatePN_spinner_country = findViewById(R.id.ValidatePN_spinner_country);
        ValidatePN_et_country_code = findViewById(R.id.ValidatePN_et_country_code);
        ValidatePN_et_phone_number = findViewById(R.id.ValidatePN_et_phone_number);
        ValidatePN_btn_next = findViewById(R.id.ValidatePN_btn_next);
        // endregion

        phoneNumberUtil = PhoneNumberUtil.getInstance();

        // Make edit_country_code not editable
        ValidatePN_et_country_code.setKeyListener(null);

        // region Fill ValidatePN_spinner_country
        List<String> countryArray = new ArrayList<>();

        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            String item = locale.getDisplayName() + " " + "(" + countryCode + ")";
            countryArray.add(item);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, countryArray
        );

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
                String selectedCountryCode = selectedItem.substring(selectedItem.length() - 3, selectedItem.length() - 1);
                int countryCode = phoneNumberUtil.getCountryCodeForRegion(selectedCountryCode);

                ValidatePN_et_country_code.setHint("+" + countryCode);

                PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher =
                        new PhoneNumberFormattingTextWatcher(selectedCountryCode);

                ValidatePN_et_phone_number.removeTextChangedListener(phoneNumberFormattingTextWatcher);
                ValidatePN_et_phone_number.addTextChangedListener(phoneNumberFormattingTextWatcher);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ValidatePN_btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = ValidatePN_et_phone_number.getText().toString();
                if (phoneNumber.isEmpty()) { return; }

                String selectedItem = ValidatePN_spinner_country.getSelectedItem().toString();
                String countryCode = selectedItem.substring(
                        selectedItem.length() - 3, selectedItem.length() - 1
                );

                try {
                    Phonenumber.PhoneNumber numberProto = phoneNumberUtil.parse(
                            phoneNumber,
                            countryCode
                    );

                    boolean isValidPhoneNumber = phoneNumberUtil.isValidNumber(numberProto);
                    String formattedPhoneNumber = phoneNumberUtil.format(
                            numberProto,
                            PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                    );

                    if (isValidPhoneNumber) {
                        // Valid phone number
                    } else {
                        // Not a valid phone number
                    }
                } catch (NumberParseException e) {
                    // TODO: Not a phone number. Handle error
                    e.printStackTrace();
                }
            }
        });
    }
}
