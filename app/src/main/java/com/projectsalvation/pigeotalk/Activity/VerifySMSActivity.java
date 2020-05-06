package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;
import com.projectsalvation.pigeotalk.R;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerifySMSActivity extends AppCompatActivity {

    // region Resource Declaration
    TextView a_verify_sms_tv_phone_number;
    OtpView a_verify_sms_otpView_sms_code;
    Button a_verify_sms_btn_resend_code;
    TextView a_verify_sms_tv_request_info;
    // endregion

    private static final String TAG = "VerifySMSActivity";

    private static final int VERIFICATION_CODE_TIMEOUT_DURATION = 10;

    private String mVerificationId;
    private String mVerificationCode;
    private String mFormattedPhoneNumber;

    private FirebaseAuth mFirebaseAuth;
    private PhoneAuthProvider mPhoneAuthProvider;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mVerificationCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_sms);

        // region Resource Assignment
        a_verify_sms_tv_phone_number = findViewById(R.id.a_verify_sms_tv_phone_number);
        a_verify_sms_otpView_sms_code = findViewById(R.id.a_verify_sms_otpView_sms_code);
        a_verify_sms_btn_resend_code = findViewById(R.id.a_verify_sms_btn_resend_code);
        a_verify_sms_tv_request_info = findViewById(R.id.a_verify_sms_request_info);
        // endregion

        Intent i = getIntent();
        mFormattedPhoneNumber = i.getExtras().getString("formattedPhoneNumber");
        String countryCodeStr = i.getExtras().getString("countryCodeStr");

        a_verify_sms_tv_phone_number.setText(mFormattedPhoneNumber);

        mPhoneAuthProvider = PhoneAuthProvider.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        a_verify_sms_otpView_sms_code.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                mVerificationCode = otp;

                if (mVerificationId == null || mVerificationCode == null) {
                    Snackbar.make(a_verify_sms_otpView_sms_code, R.string.text_verify_phone_number_failed,
                            BaseTransientBottomBar.LENGTH_LONG).show();

                    return;
                }

                PhoneAuthCredential credential = PhoneAuthProvider
                        .getCredential(mVerificationId, mVerificationCode);

                signInWithPhoneAuthCredential(credential);
            }
        });

        // region Verification Callbacks
        mVerificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                Log.d(TAG, "onVerificationCompleted:" + credential);

                if (credential.getSmsCode() == null) {
                    // TODO: Handle instant verification when verification code is null.
                } else {
                    mVerificationCode = credential.getSmsCode();
                    a_verify_sms_otpView_sms_code.setText(mVerificationCode);

                    signInWithPhoneAuthCredential(credential);

                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // TODO: Show a message and update the UI
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }

            // TODO: Implement onCodeAutoRetrievalTimeOut?
        };
        // endregion

        a_verify_sms_btn_resend_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(mFormattedPhoneNumber, mResendToken);
            }
        });

        // Match the language of the SMS message to the language of the user
        mFirebaseAuth.setLanguageCode(countryCodeStr != null ? countryCodeStr : "en");

        // Send SMS verification code
        sendVerificationCode(mFormattedPhoneNumber);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            Intent i = new Intent(VerifySMSActivity.this, RegisterActivity.class);
                            startActivity(i);
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // TODO: Handle the error. The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode(String phoneNumber) {
        startRequestCountdown();

        mPhoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                VERIFICATION_CODE_TIMEOUT_DURATION,
                TimeUnit.SECONDS,
                VerifySMSActivity.this,
                mVerificationCallbacks
        );
    }

    private void sendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        startRequestCountdown();

        mPhoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                VERIFICATION_CODE_TIMEOUT_DURATION,
                TimeUnit.SECONDS,
                VerifySMSActivity.this,
                mVerificationCallbacks,
                token
        );
    }

    private void startRequestCountdown() {
        a_verify_sms_btn_resend_code.setEnabled(false);

        new CountDownTimer(VERIFICATION_CODE_TIMEOUT_DURATION * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                a_verify_sms_tv_request_info.setText(getString(R.string.text_verify_sms_request_info,
                        millisUntilFinished / 1000));
            }

            public void onFinish() {
                a_verify_sms_tv_request_info.setText(R.string.text_verify_sms_request_now);
                a_verify_sms_btn_resend_code.setEnabled(true);
            }
        }.start();
    }

    @Override
    public void onBackPressed() { }
}