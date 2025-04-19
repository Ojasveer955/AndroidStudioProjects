package com.example.musicplayer;

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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button selectMusicButton;

    // Launcher for requesting permissions
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                    Log.d(TAG, "Storage permission granted");
                    launchFilePicker();
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied.
                    Log.w(TAG, "Storage permission denied");
                    Toast.makeText(this, "Permission needed to select music files", Toast.LENGTH_SHORT).show();
                }
            });

    // Launcher for picking an audio file
    private final ActivityResultLauncher<Intent> pickAudioLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
                        Log.d(TAG, "Audio file selected: " + audioUri.toString());
                        // Grant temporary read permission (important for security and compatibility)
                        getContentResolver().takePersistableUriPermission(audioUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        // Start PlayAudioActivity and pass the URI
                        Intent playIntent = new Intent(MainActivity.this, PlayAudioActivity.class);
                        playIntent.setData(audioUri); // Pass URI via setData for standard handling
                        // Alternatively, pass as an extra:
                        // playIntent.putExtra("AUDIO_URI", audioUri.toString());
                        startActivity(playIntent);
                    } else {
                         Log.w(TAG, "Failed to get audio URI from result");
                         Toast.makeText(this, "Failed to select audio file", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Audio file selection cancelled or failed");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectMusicButton = findViewById(R.id.select_music_button);

        selectMusicButton.setOnClickListener(v -> {
            Log.d(TAG, "Select music button clicked");
            checkPermissionAndPickAudio();
        });
    }

    private void checkPermissionAndPickAudio() {
        String permission;
        // Determine the correct permission based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            permission = Manifest.permission.READ_MEDIA_AUDIO;
        } else { // Below Android 13
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // Check if permission is already granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission already granted, launching file picker");
            launchFilePicker();
        } else {
            // Request the permission
            Log.d(TAG, "Requesting permission: " + permission);
            requestPermissionLauncher.launch(permission);
        }
    }

    private void launchFilePicker() {
        // Use ACTION_OPEN_DOCUMENT for persistent access grants
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*"); // Specify MIME type for audio files
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        // Use ACTION_GET_CONTENT for simpler selection (might not grant persistent access easily)
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.setType("audio/*");
        // intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
             pickAudioLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error launching file picker", e);
            Toast.makeText(this, "Cannot open file picker", Toast.LENGTH_SHORT).show();
            // Fallback or inform user
            Intent fallbackIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fallbackIntent.setType("audio/*");
            fallbackIntent.addCategory(Intent.CATEGORY_OPENABLE);
             try {
                 pickAudioLauncher.launch(fallbackIntent);
             } catch (Exception e2) {
                 Log.e(TAG, "Error launching fallback file picker", e2);
                 Toast.makeText(this, "No app available to pick audio files", Toast.LENGTH_LONG).show();
             }
        }
    }
}