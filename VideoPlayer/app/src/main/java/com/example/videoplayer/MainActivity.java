package com.example.videoplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// Add this import if it's missing
import androidx.core.graphics.Insets;

public class MainActivity extends AppCompatActivity {

    private Button selectVideoButton;

    // Launcher for picking a video
    private final ActivityResultLauncher<Intent> pickVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri videoUri = result.getData().getData();
                    if (videoUri != null) {
                        // Proceed to PlayVideoActivity
                        Intent playIntent = new Intent(MainActivity.this, PlayVideoActivity.class);
                        playIntent.setData(videoUri); // Pass the URI via intent data
                        startActivity(playIntent);
                    } else {
                        Toast.makeText(this, "Failed to get video URI", Toast.LENGTH_SHORT).show();
                    }
                } else {
                     Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show();
                }
            });

    // Launcher for requesting permissions
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted, launch video picker
                    launchVideoPicker();
                } else {
                    // Permission denied
                    Toast.makeText(this, "Storage permission is required to select videos", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Adjust padding for system bars (optional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            // Change the type here from android.graphics.Insets to androidx.core.graphics.Insets
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectVideoButton = findViewById(R.id.button);

        selectVideoButton.setOnClickListener(v -> {
            // Check permissions and launch picker
            checkPermissionAndPickVideo();
        });
    }

    private void checkPermissionAndPickVideo() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_VIDEO
            permission = Manifest.permission.READ_MEDIA_VIDEO;
        } else {
            // Older versions use READ_EXTERNAL_STORAGE
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            launchVideoPicker();
        } else {
            // Request permission
            requestPermissionLauncher.launch(permission);
        }
    }

    private void launchVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        // You could also use ACTION_GET_CONTENT:
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.setType("video/*");
        pickVideoLauncher.launch(intent);
    }
}