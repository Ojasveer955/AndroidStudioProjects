package com.example.videorecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button recordButton;

    // check all permissions
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                // Check if all requested permissions were granted
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    // Permissions granted after request, launch video capture
                    launchVideoCapture();
                } else {
                    // Permissions denied
                    Toast.makeText(this, "Camera and/or Storage permissions denied. Cannot record video.", Toast.LENGTH_LONG).show();
                }
            });

    // Launcher for handling the result from the video capture activity
    private final ActivityResultLauncher<Intent> videoCaptureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Video capture was successful, get the URI
                    Uri videoUri = result.getData().getData();
                    if (videoUri != null) {
                        Log.d(TAG, "Video saved to: " + videoUri.toString());
                        Toast.makeText(this, "Video saved: " + videoUri.toString(), Toast.LENGTH_LONG).show();
                        // You can now use the videoUri (e.g., play it, upload it, etc.)
                    } else {
                         Log.e(TAG, "Video URI is null after successful capture.");
                         Toast.makeText(this, "Failed to get video location.", Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    // User cancelled the video capture
                    Toast.makeText(this, "Video recording cancelled.", Toast.LENGTH_SHORT).show();
                } else {
                    // Video capture failed for some other reason
                    Log.e(TAG, "Video recording failed, result code: " + result.getResultCode());
                    Toast.makeText(this, "Failed to record video.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // Use the correct layout file name here if it's different
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Use the correct button ID here if it's different
        recordButton = findViewById(R.id.button);

        recordButton.setOnClickListener(v -> {
            // Check permissions and launch capture when button is clicked
            checkPermissionsAndLaunchCapture();
        });
    }

    /**
     * Checks for necessary permissions (Camera and potentially Storage)
     * and launches the video capture intent if granted, or requests permissions otherwise.
     */
    private void checkPermissionsAndLaunchCapture() {
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        // Always check for Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        // Check for Storage permission only if required (Android 9/Pie (API 28) and below)
        // For API 29+, ACTION_VIDEO_CAPTURE doesn't require explicit storage permission
        // as it saves to the standard MediaStore location.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (permissionsNeeded.isEmpty()) {
            // All necessary permissions are already granted
            launchVideoCapture();
        } else {
            // Request the missing permissions
            Log.d(TAG, "Requesting permissions: " + permissionsNeeded);
            requestPermissionLauncher.launch(permissionsNeeded.toArray(new String[0]));
        }
    }

    /**
     * Launches the standard Android camera app to capture video.
     */
    private void launchVideoCapture() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Check if there's an activity (camera app) that can handle this intent
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
             Log.d(TAG, "Launching video capture intent.");
             videoCaptureLauncher.launch(takeVideoIntent);
        } else {
             Log.e(TAG, "No activity found to handle ACTION_VIDEO_CAPTURE intent.");
             Toast.makeText(this, "No camera app found to record video.", Toast.LENGTH_LONG).show();
        }
    }
}