package com.projectsalvation.pigeotalk.Utility;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public final class Util {

    private Util() {
    }

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

    public static int ColorFromString(String str) {
        byte[] bytes = str.getBytes();

        int r = Math.abs(bytes[2]);
        int g = Math.abs(bytes[4]);
        int b = Math.abs(bytes[8]);

        Log.d("UTIL", "ColorFromString: " + r + g + b);
        return Color.argb(255, 256-r+128, 256-g+32, 256-b+64);
    }
}
