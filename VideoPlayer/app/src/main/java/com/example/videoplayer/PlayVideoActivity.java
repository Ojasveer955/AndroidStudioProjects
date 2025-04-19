package com.example.videoplayer; // Replace with your actual package name

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class PlayVideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    private Uri videoUri;
    private int currentPosition = 0; // To save playback position

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        videoView = findViewById(R.id.videoView);

        // Get the video URI passed from MainActivity
        videoUri = getIntent().getData();

        if (videoUri != null) {
            setupVideoPlayer();
        } else {
            Toast.makeText(this, "Error: Video URI not found.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if URI is missing
        }

        // Restore playback position if available
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt("currentPosition", 0);
        }
    }

    private void setupVideoPlayer() {
        // Create MediaController
        mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView); // Anchor controls to VideoView

        // Set MediaController for VideoView
        videoView.setMediaController(mediaController);

        // Set the video URI
        videoView.setVideoURI(videoUri);

        // Add listeners
        videoView.setOnPreparedListener(mp -> {
            // Video is ready to play
            Toast.makeText(PlayVideoActivity.this, "Video prepared", Toast.LENGTH_SHORT).show();
            if (currentPosition > 0) {
                videoView.seekTo(currentPosition); // Resume from saved position
            } else {
                videoView.start(); // Start from beginning
            }
            // Optionally hide status bar/navigation bar for immersive experience
            // getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        });

        videoView.setOnCompletionListener(mp -> {
            // Video finished playing
            Toast.makeText(PlayVideoActivity.this, "Playback finished", Toast.LENGTH_SHORT).show();
            // Optionally finish activity or loop video
            // finish();
            // videoView.seekTo(0);
            // videoView.start();
        });

        videoView.setOnErrorListener((mp, what, extra) -> {
            // Error occurred during playback
            Toast.makeText(PlayVideoActivity.this, "Error playing video", Toast.LENGTH_SHORT).show();
            // Log the error details: Log.e("VideoPlayer", "Error: what=" + what + ", extra=" + extra);
            finish(); // Close activity on error
            return true; // Indicate error was handled
        });

        // Request focus for the VideoView so it can receive media key events
        videoView.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video and save current position
        if (videoView.isPlaying()) {
            currentPosition = videoView.getCurrentPosition();
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video playback if it was playing before pause
        if (videoUri != null && !videoView.isPlaying()) {
             // Check if we have a saved position > 0 to avoid restarting from beginning on initial resume
            if (currentPosition > 0) {
                 videoView.seekTo(currentPosition);
            }
            // Consider whether you want it to auto-start on resume or wait for user interaction
            // videoView.start(); // Uncomment if you want it to auto-play on resume
        }
    }

     @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current playback position
        if (videoView != null) {
             outState.putInt("currentPosition", videoView.getCurrentPosition());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}