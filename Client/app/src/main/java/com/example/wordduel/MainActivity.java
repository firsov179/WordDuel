package com.example.wordduel;

import static com.example.wordduel.R.*;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static JSONObject stats_info;
    public static Socket clientrunSocket;
    public static Socket clientSocket;
    int mCurrentVideoPosition;
    static int uid;
    MediaPlayer mMediaPlayer;
    VideoView background;
    public static String[] game_modes = new String[]{"rang", "common", "train"};
    public static int[] gamemods = new int[] {R.drawable.rating_gamemode, drawable.common, drawable.train};
    public static int game_modes_ind = 0;
    static Context thiss;
    static int rang_cat;
    static int rang_subcat;
    static int rang_poi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        MainActivity.uid = LoginMenu.uid;
        TextView game_name = findViewById(R.id.game_name);
        try {
            game_name.setText(LoginMenu.stats_info.getString("game_name"));
            rang_cat = LoginMenu.stats_info.getInt("rang_category");
            rang_subcat = LoginMenu.stats_info.getInt("rang_subcategory");
            rang_poi = LoginMenu.stats_info.getInt("rang_points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateRang();
        thiss = this;
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


        ImageButton lb = (ImageButton) findViewById(id.leftButton);
        ImageView gm = findViewById(R.id.imageView4);
        lb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game_modes_ind += game_modes.length - 1;
                game_modes_ind %= game_modes.length;
                gm.setImageResource(gamemods[game_modes_ind]);
            }
        });
        ImageButton rb = (ImageButton) findViewById(id.rightButton);
        rb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                game_modes_ind += 1;
                game_modes_ind %= game_modes.length;
                gm.setImageResource(gamemods[game_modes_ind]);
            }
        });


        ImageButton startButton = (ImageButton) findViewById(R.id.goButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWait();
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

        ImageButton settingsButton = findViewById(id.settingsButton);
        settingsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openSettingsMenu();
                    }
                }
        );

        background.start();
    }

    public void updateRang() {
        int[] rangs = new int[]{R.drawable.red, R.drawable.orange, R.drawable.yelow,
                R.drawable.green, R.drawable.cyan, R.drawable.blue, R.drawable.purple};
        String[] subrangs = new String[]{"C", "B", "A"};
        int[] rangpoints = new int[]{R.drawable.zero, R.drawable.one, R.drawable.two};
        ImageView rang = findViewById(R.id.rang);
        ImageView rang_points = findViewById(R.id.points);
        TextView rang_subcategory = findViewById(R.id.subrang);

        rang.setImageResource(rangs[rang_cat]);
        rang_subcategory.setText(subrangs[rang_subcat]);
        rang_points.setImageResource(rangpoints[rang_poi]);

    }

    public void openWait() {
        Intent intent = new Intent(this, WaitGameMenu.class);
        startActivity(intent);
    }

    public void openResult() {
        Intent intent = new Intent(this, ResultActivityMenu.class);
        startActivity(intent);
    }

    public void openStatsMenu() {
        Intent intent = new Intent(MainActivity.thiss, StatisticMenu.class);
        startActivity(intent);
    }

    public void openSettingsMenu() {
        Intent intent = new Intent(MainActivity.thiss, SettingsMenu.class);
        startActivity(intent);
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
        updateRang();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // When the Activity is destroyed, release our MediaPlayer and set it to null.
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}