package com.example.musicplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PlayAudioActivity extends AppCompatActivity {

    private static final String TAG = "PlayAudioActivity";

    private TextView trackTitle, currentTime, totalTime;
    private SeekBar seekBar;
    private ImageButton playPauseButton, previousButton, nextButton;

    private MediaPlayer mediaPlayer;
    private Uri audioUri;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_audio); // Ensure this matches your layout file name

        trackTitle = findViewById(R.id.track_title);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        playPauseButton = findViewById(R.id.play_pause_button);
        previousButton = findViewById(R.id.previous_button); // Add logic if needed
        nextButton = findViewById(R.id.next_button);         // Add logic if needed

        // Get the URI passed from MainActivity
        audioUri = getIntent().getData();
        // Alternatively, if passed as an extra:
        // String uriString = getIntent().getStringExtra("AUDIO_URI");
        // if (uriString != null) {
        //     audioUri = Uri.parse(uriString);
        // }

        if (audioUri != null) {
            Log.d(TAG, "Received audio URI: " + audioUri.toString());
            // Extract and display track title (optional, requires more complex URI handling)
            // String title = getFileName(audioUri); // Implement getFileName helper
            // trackTitle.setText(title != null ? title : "Unknown Track");
            trackTitle.setText("Loading..."); // Placeholder
            initializeMediaPlayer();
        } else {
            Log.e(TAG, "No audio URI received");
            Toast.makeText(this, "Error: No audio file specified", Toast.LENGTH_LONG).show();
            finish(); // Close activity if no URI
        }

        setupButtonClickListeners();
        setupSeekBarChangeListener();
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            // Crucial: Use the application context and the URI to set the data source
            mediaPlayer.setDataSource(getApplicationContext(), audioUri);

            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "MediaPlayer prepared");
                seekBar.setMax(mediaPlayer.getDuration());
                totalTime.setText(formatTime(mediaPlayer.getDuration()));
                trackTitle.setText(getFileName(audioUri)); // Update title once prepared
                // Optionally start playing immediately after preparation
                // playAudio();
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Playback completed");
                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                seekBar.setProgress(mediaPlayer.getDuration()); // Show seekbar at end
                handler.removeCallbacks(updateSeekBar); // Stop updating seek bar
                // Optionally handle next track or repeat logic here
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                 Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                 Toast.makeText(PlayAudioActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                 // Consider finishing the activity or resetting the player
                 finish();
                 return true; // Indicates we handled the error
            });
            mediaPlayer.prepareAsync(); // Prepare in background, non-blocking
            Log.d(TAG, "MediaPlayer preparing...");

        } catch (IOException e) {
            Log.e(TAG, "Error setting data source for MediaPlayer", e);
            Toast.makeText(this, "Error loading audio file", Toast.LENGTH_LONG).show();
            finish(); // Close activity if the file can't be loaded
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal Argument Exception setting data source", e);
            Toast.makeText(this, "Invalid audio file", Toast.LENGTH_LONG).show();
            finish();
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception setting data source - Permission issue?", e);
            Toast.makeText(this, "Permission denied for audio file", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupButtonClickListeners() {
        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                // Check if prepared - might not be ready immediately after onCreate
                try {
                    if (mediaPlayer.isPlaying()) {
                        pauseAudio();
                    } else {
                        playAudio();
                    }
                } catch (IllegalStateException e) {
                    // MediaPlayer might not be in a valid state (e.g., not prepared yet)
                    Log.e(TAG, "MediaPlayer not in a valid state for play/pause", e);
                    Toast.makeText(this, "Player not ready yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add listeners for previous/next if implementing playlist functionality
        previousButton.setOnClickListener(v -> Toast.makeText(this, "Previous clicked (not implemented)", Toast.LENGTH_SHORT).show());
        nextButton.setOnClickListener(v -> Toast.makeText(this, "Next clicked (not implemented)", Toast.LENGTH_SHORT).show());
    }

     private void setupSeekBarChangeListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userSelectedPosition = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    currentTime.setText(formatTime(progress)); // Update time immediately while dragging
                    userSelectedPosition = progress; // Store the position the user is dragging to
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Pause updates from the handler while the user is interacting
                 if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                     handler.removeCallbacks(updateSeekBar);
                 }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    try {
                        mediaPlayer.seekTo(userSelectedPosition);
                        // If playing, restart the handler updates
                        if (mediaPlayer.isPlaying()) {
                            handler.post(updateSeekBar);
                        } else {
                            // If paused, just update the current time display based on seek
                            currentTime.setText(formatTime(userSelectedPosition));
                            seekBar.setProgress(userSelectedPosition); // Ensure seekbar reflects final position
                        }
                    } catch (IllegalStateException e) {
                         Log.e(TAG, "MediaPlayer not in a valid state for seeking", e);
                    }
                }
            }
        });
    }


    private void playAudio() {
        // Check if mediaPlayer is initialized and not null
        if (mediaPlayer != null) {
             try {
                 // Check if it's not already playing
                 if (!mediaPlayer.isPlaying()) {
                     mediaPlayer.start();
                     playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                     Log.d(TAG, "Playback started");
                     handler.post(updateSeekBar); // Start updating seek bar
                 }
             } catch (IllegalStateException e) {
                 // This can happen if start() is called in an invalid state (e.g., released, error)
                 Log.e(TAG, "Cannot start playback: MediaPlayer in invalid state.", e);
                 Toast.makeText(this, "Error starting playback", Toast.LENGTH_SHORT).show();
                 // May need to re-initialize or handle the error state
             }
        } else {
             Log.w(TAG, "playAudio called but mediaPlayer is null");
        }
    }

    private void pauseAudio() {
        // Check if mediaPlayer is initialized and not null
        if (mediaPlayer != null) {
             try {
                 // Check if it's actually playing
                 if (mediaPlayer.isPlaying()) {
                     mediaPlayer.pause();
                     playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                     Log.d(TAG, "Playback paused");
                     handler.removeCallbacks(updateSeekBar); // Stop updating seek bar
                 }
             } catch (IllegalStateException e) {
                 // This can happen if pause() is called in an invalid state
                 Log.e(TAG, "Cannot pause playback: MediaPlayer in invalid state.", e);
             }
        } else {
             Log.w(TAG, "pauseAudio called but mediaPlayer is null");
        }
    }

    // Runnable to update seek bar and current time
    private Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null) {
                try {
                    // Only update if playing
                    if (mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentPosition);
                        currentTime.setText(formatTime(currentPosition));
                        // Schedule the next update
                        handler.postDelayed(this, 500); // Update every 500ms for smoother UI
                    }
                } catch (IllegalStateException e) {
                    // Player might have been released or entered an error state
                    Log.e(TAG, "Error updating seek bar: MediaPlayer state issue.", e);
                    handler.removeCallbacks(this); // Stop trying to update
                }
            }
        }
    };

    // Helper to format milliseconds to mm:ss
    private String formatTime(int millis) {
        // Ensure non-negative duration
        if (millis < 0) millis = 0;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                       TimeUnit.MINUTES.toSeconds(minutes);

        return String.format("%02d:%02d", minutes, seconds);
    }

     // Helper to get file name from URI (improved robustness)
    private String getFileName(Uri uri) {
        String result = null;
        // Try retrieving the display name column (most reliable for content URIs)
        if (uri != null && "content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                         result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                 // Catch potential SecurityException or other issues querying the ContentResolver
                 Log.w(TAG, "Error getting file name from content URI: " + e.getMessage());
            }
        }

        // Fallback for file URIs or if display name is unavailable
        if (result == null && uri != null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        // Basic cleanup: remove extension if present
        if (result != null && result.contains(".")) {
            try {
                result = result.substring(0, result.lastIndexOf('.'));
            } catch (IndexOutOfBoundsException e) {
                // Handle cases where filename might start with "."
                Log.w(TAG, "Filename cleanup issue: " + result);
            }
        }

        return result != null ? result : "Unknown Track";
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Optional: Pause playback if the activity is paused but not destroyed
        // if (mediaPlayer != null && mediaPlayer.isPlaying()) {
        //     pauseAudio();
        // }
        // If you want music to stop when app goes to background, pause or stop here.
        // If you want background playback, you need a Service.
    }

     @Override
    protected void onStop() {
        super.onStop();
        // If you want music to stop when the activity is no longer visible,
        // consider pausing or stopping here. For background playback, use a Service.
        // Example: Stop playback fully when activity stops
        // if (mediaPlayer != null) {
        //     if (mediaPlayer.isPlaying()) {
        //         mediaPlayer.stop(); // Stops and invalidates the player
        //     }
        //     // Consider releasing here if you don't need the player state upon returning
        //     // mediaPlayer.release();
        //     // mediaPlayer = null;
        //     // handler.removeCallbacks(updateSeekBar);
        // }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release the MediaPlayer resources completely when the activity is destroyed
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar); // Stop any pending updates
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop(); // Stop playback before releasing
                }
                mediaPlayer.release(); // Release system resources
            } catch (IllegalStateException e) {
                 Log.e(TAG,"Error stopping/releasing MediaPlayer in onDestroy", e);
            }
            mediaPlayer = null; // Set to null to prevent further use
            Log.d(TAG, "MediaPlayer released in onDestroy");
        }
    }
}