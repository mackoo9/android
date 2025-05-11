package com.example.myapplication.util;


import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AudioPermissionHelper {
    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    public static boolean checkPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{android.Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION
        );
    }
}