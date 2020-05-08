package com.projectsalvation.pigeotalk.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public final class Util {

    private Util() { }

    public static void requestPermission(String[] permissions, Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
        );
    }

    public static boolean checkPermission(String permission, Context context) {
        if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    public static void updateUserProfile(FirebaseUser user, String name, Uri photoUri) {
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest
                .Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUri)
                .build();

        user.updateProfile(profileChangeRequest);
    }
}
