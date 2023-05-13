package com.example.wordduel;

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

public class StatisticMenu extends AppCompatActivity {

    int mCurrentVideoPosition;
    MediaPlayer mMediaPlayer;
    VideoView background;
    public static int count_games;
    public static int games_wins;
    public static int words_wins;
    public static int rangs_games;
    public static int rangs_wins;
    public static int series_rang;
    public static int series_rang_cur;
    public static int series_comm;
    public static int series_comm_cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic_menu);
        TextView count_games_text = findViewById(R.id.count_games);
        TextView games_wins_text = findViewById(R.id.games_wins);
        TextView words_wins_text = findViewById(R.id.words_wins);
        TextView rangs_games_text = findViewById(R.id.rangs_games);
        TextView rangs_wins_text = findViewById(R.id.rangs_wins);
        TextView comm_games_text = findViewById(R.id.comm_games);
        TextView comm_wins_text = findViewById(R.id.comm_wins);

        TextView series_rang_text = findViewById(R.id.series_rang);
        TextView series_rang_cur_text = findViewById(R.id.series_rang_cur);
        TextView series_comm_text = findViewById(R.id.series_comm);
        TextView series_comm_cur_text = findViewById(R.id.series_comm_cur);

        TextView full_percent = findViewById(R.id.full_percent);
        TextView comm_percent = findViewById(R.id.comm_percent);
        TextView rang_percent = findViewById(R.id.rang_percent);

        count_games_text.setText(Integer.toString(count_games));
        games_wins_text.setText(Integer.toString(games_wins));
        words_wins_text.setText(Integer.toString(words_wins));
        rangs_games_text.setText(Integer.toString(rangs_games));
        rangs_wins_text.setText(Integer.toString(rangs_wins));
        comm_games_text.setText(Integer.toString(count_games - rangs_games));
        comm_wins_text.setText(Integer.toString(games_wins - rangs_wins));

        series_rang_text.setText(Integer.toString(series_rang));
        series_rang_cur_text.setText(Integer.toString( series_rang_cur));
        series_comm_text.setText(Integer.toString(series_comm));
        series_comm_cur_text.setText(Integer.toString(series_comm_cur));
        if (rangs_games == 0) {
            rang_percent.setText("-");
        } else {
            rang_percent.setText(Integer.toString((100 * rangs_wins + rangs_games / 2) / rangs_games)+ "%");
        }
        if (count_games == 0) {
            comm_percent.setText("-");
            full_percent.setText("-");
        } else if (count_games != rangs_games) {
            full_percent.setText(Integer.toString((100 * games_wins + count_games / 2) / count_games)+ "%");
            comm_percent.setText(Integer.toString((100 * (games_wins - rangs_wins) + (count_games - rangs_games) / 2) / (count_games - rangs_games))+ "%");
        } else {
            full_percent.setText(Integer.toString((100 * games_wins + count_games / 2) / count_games) + "%");
            comm_percent.setText("-");
        }




        int[] rangs = new int[]{R.drawable.red, R.drawable.orange, R.drawable.yelow,
                R.drawable.green, R.drawable.cyan, R.drawable.blue, R.drawable.purple};
        String[] subrangs = new String[]{"C", "B", "A"};
        int[] rangpoints = new int[]{R.drawable.zero, R.drawable.one, R.drawable.two};
        ImageView rang = findViewById(R.id.rang);
        TextView rang_subcategory = findViewById(R.id.subrang);

        rang.setImageResource(rangs[MainActivity.rang_cat]);
        rang_subcategory.setText(subrangs[MainActivity.rang_subcat]);




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

        ImageButton mainButton = findViewById(R.id.mainButton);
        mainButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                }
        );
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openSettingsMenu();
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Capture the current video position and pause the video.
        mCurrentVideoPosition = 0;
        background.pause();
    }

    public void openSettingsMenu() {
        Intent intent = new Intent(MainActivity.thiss, SettingsMenu.class);
        startActivity(intent);
        finish();
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