package com.projectsalvation.pigeotalk.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.concurrent.TimeUnit;

public class VerifySMSActivity extends AppCompatActivity {

    // region Resource Declaration
    TextView VerifySMS_tv_formatted_number;
    OtpView VerifySMS_otpView_sms_code;
    Button VerifySMS_btn_resend_sms_code;
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
        VerifySMS_tv_formatted_number = findViewById(R.id.VerifySMS_tv_formatted_number);
        VerifySMS_otpView_sms_code = findViewById(R.id.VerifySMS_otpView_sms_code);
        VerifySMS_btn_resend_sms_code = findViewById(R.id.VerifySMS_btn_resend_sms_code);
        // endregion

        Intent i = getIntent();
        mFormattedPhoneNumber = i.getExtras().getString("formattedPhoneNumber");
        String countryCodeStr = i.getExtras().getString("countryCodeStr");

        VerifySMS_tv_formatted_number.setText(mFormattedPhoneNumber);

        mPhoneAuthProvider = PhoneAuthProvider.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        VerifySMS_otpView_sms_code.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                mVerificationCode = otp;

                PhoneAuthCredential credential =
                        PhoneAuthProvider.getCredential(mVerificationId, mVerificationCode);

                signInWithPhoneAuthCredential(credential);
            }
        });

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
                            VerifySMS_otpView_sms_code.setText(mVerificationCode);

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

        VerifySMS_btn_resend_sms_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationCode(mFormattedPhoneNumber, mResendToken);
            }
        });

        // Match the language of the SMS message to the language of the user
        mFirebaseAuth.setLanguageCode(countryCodeStr);

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

                            FirebaseUser user = task.getResult().getUser();

                            Intent i = new Intent(VerifySMSActivity.this, RegisterActivity.class);
                            i.putExtra("userObj", user);
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
        VerifySMS_btn_resend_sms_code.setEnabled(false);

        mPhoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                VERIFICATION_CODE_TIMEOUT_DURATION,
                TimeUnit.SECONDS,
                VerifySMSActivity.this,
                mVerificationCallbacks
        );

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                VerifySMS_btn_resend_sms_code.setEnabled(true);
            }
        }, VERIFICATION_CODE_TIMEOUT_DURATION * 1000);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token) {
        VerifySMS_btn_resend_sms_code.setEnabled(false);

        mPhoneAuthProvider.verifyPhoneNumber(
                phoneNumber,
                VERIFICATION_CODE_TIMEOUT_DURATION,
                TimeUnit.SECONDS,
                VerifySMSActivity.this,
                mVerificationCallbacks,
                token
        );

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                VerifySMS_btn_resend_sms_code.setEnabled(true);
            }
        }, VERIFICATION_CODE_TIMEOUT_DURATION * 1000);
    }
}