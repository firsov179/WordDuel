package com.example.wordduel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

public class SettingsMenu extends AppCompatActivity {
    int mCurrentVideoPosition;
    MediaPlayer mMediaPlayer;
    VideoView background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);

        background = findViewById(R.id.backgroundStats);
        background.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.down));
        background.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mMediaPlayer = mediaPlayer;
                // We want our video to play over and over so we set looping to true.
                mMediaPlayer.setLooping(true);
                // We then seek to the current posistion if it has been set and play the video.
                if (mCurrentVideoPosition != 0) {
                    mMediaPlayer.seekTo(mCurrentVideoPosition);
                    mMediaPlayer.start();
                }
                float videoRatio = mediaPlayer.getVideoWidth() / (float) mediaPlayer.getVideoHeight();
                float screenRatio = background.getWidth() / (float)
                        background.getHeight();
                float scaleX = videoRatio / screenRatio;
                if (scaleX >= 1f) {
                    background.setScaleX(scaleX);
                } else {
                    background.setScaleY(1f / scaleX);
                }
            }
        });

        ImageButton vk = findViewById(R.id.vk);
        vk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent urlIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://vk.com/fedorfirsov")
                );
                startActivity(urlIntent);
            }
        });
        ImageButton git = findViewById(R.id.git);
        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent urlIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/fodof91/WordDuel")
                );
                startActivity(urlIntent);
            }
        });

        ImageButton statsButton = findViewById(R.id.statsButton);
        statsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openStatsMenu();
                    }
                }
        );
        ImageButton mainButton = findViewById(R.id.mainButton);
        mainButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );

        Button fir_on = findViewById(R.id.fir_on);
        Button fir_off = findViewById(R.id.fir_off);
        Button sec_on = findViewById(R.id.sec_on);
        Button sec_off = findViewById(R.id.sec_off);
        fir_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fir_on.setBackgroundResource(R.drawable.on);
                fir_off.setBackgroundResource(R.drawable.off);
            }
        });
        
        fir_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fir_on.setBackgroundResource(R.drawable.on_off);
                fir_off.setBackgroundResource(R.drawable.off_on);
            }
        });
        sec_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sec_on.setBackgroundResource(R.drawable.on);
                sec_off.setBackgroundResource(R.drawable.off);
            }
        });

        sec_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sec_on.setBackgroundResource(R.drawable.on_off);
                sec_off.setBackgroundResource(R.drawable.off_on);
            }
        });
    }

    public void openStatsMenu() {
        Intent intent = new Intent(MainActivity.thiss, StatisticMenu.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Capture the current video position and pause the video.
        mCurrentVideoPosition = 0;
        background.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        background.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When the Activity is destroyed, release our MediaPlayer and set it to null.
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
