package com.example.wordduel;

import static com.example.wordduel.LoginMenu.executePost;
import static com.example.wordduel.LoginMenu.executePostRun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class WaitGameMenu extends AppCompatActivity {

    int mCurrentVideoPosition;
    MediaPlayer mMediaPlayer;
    VideoView background;
    static JSONObject data;
    Boolean active;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_game);
        active = true;
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
        ImageButton imageButton =  findViewById(R.id.goButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                active = false;
                JSONObject request =  new JSONObject();
                int code = 404;
                try {
                    request.put("type", "cancel queue");
                    request.put("uid", MainActivity.uid);
                    JSONObject response = executePost(request);
                    code = response.getInt("status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
        Timer timer = new Timer();
        timer.schedule(
        new TimerTask() {
            @Override
            public void run() {
                check_queue();
            }
        }, 500);
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

    void check_queue() {
        ImageView gm = findViewById(R.id.imageView4);
        gm.setImageResource(MainActivity.gamemods[MainActivity.game_modes_ind]);
        TextView textView = findViewById(R.id.timer_text);
        JSONObject request =  new JSONObject();
        try {
            request.put("type", "add to queue");
            request.put("uid", MainActivity.uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (Objects.equals(MainActivity.game_modes[MainActivity.game_modes_ind], "train")) {
            JSONObject response = executePostRun(request);
        }
        JSONObject response = executePostRun(request);
        request = new JSONObject();
        try {
            request.put("type", "check queue");
            request.put("uid", MainActivity.uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int code = -1;
        long starttime = System.currentTimeMillis();
        while (code != 200) {
            if (!active) {
                return;
            }
            response = executePostRun(request);
            try {
                code = response.getInt("status");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            long millis = System.currentTimeMillis() - starttime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            textView.setText(String.format("Ожидание игры [%02d:%02d]", minutes, seconds));
        }
        data = response;
        openGame();
    }


    public void openGame() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        active = false;
        finish();

    }
}