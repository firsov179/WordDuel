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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class ResultActivityMenu extends AppCompatActivity {

    int mCurrentVideoPosition;
    MediaPlayer mMediaPlayer;
    VideoView background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
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
        String game_result = GameActivity.your_score > GameActivity.enemy_score ? "win" :
                (GameActivity.your_score < GameActivity.enemy_score ? "lose" : "draw");
        if (!Objects.equals(MainActivity.game_modes[MainActivity.game_modes_ind], "train")) {
            JSONObject request = new JSONObject();
            try {
                request.put("type", "update_results");
                request.put("game_type", MainActivity.game_modes[MainActivity.game_modes_ind]);
                request.put("game_result", game_result);
                request.put("uid", MainActivity.uid);
                request.put("words_wins", GameActivity.your_score);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject response = LoginMenu.executePost(request);

            ImageView imageView = findViewById(R.id.gameModeBack);
            if (game_result.equals("win")) {
                imageView.setImageResource(R.drawable.back_final_win);
            } else if (game_result.equals("lose")){
                imageView.setImageResource(R.drawable.back_final_lose);
            } else {
                imageView.setImageResource(R.drawable.back_final);
            }
        }
        TextView textView2 = findViewById(R.id.textView2);
        textView2.setText(Integer.toString(GameActivity.enemy_score));
        TextView textView3 = findViewById(R.id.textView3);
        textView3.setText(Integer.toString(GameActivity.your_score));

        ImageButton goButton = findViewById(R.id.goButton);
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Objects.equals(MainActivity.game_modes[MainActivity.game_modes_ind], "train")) {
                    StatisticMenu.games_wins += game_result.equals("win") ? 1 : 0;
                    StatisticMenu.words_wins += GameActivity.your_score;
                    if (Objects.equals(MainActivity.game_modes[MainActivity.game_modes_ind], "rang")) {
                        if (game_result.equals("win")) {
                            MainActivity.rang_poi++;
                            if (MainActivity.rang_poi == 3) {
                                MainActivity.rang_poi = 0;
                                MainActivity.rang_subcat++;
                                if (MainActivity.rang_subcat == 3) {
                                    MainActivity.rang_subcat = 0;
                                    if (MainActivity.rang_cat != 6) {
                                        MainActivity.rang_cat++;
                                    }
                                }
                            }
                        } else if (game_result.equals("lose") && MainActivity.rang_poi != 0) {
                            MainActivity.rang_poi--;

                        }

                        StatisticMenu.rangs_games += 1;
                        StatisticMenu.rangs_wins += game_result.equals("win") ? 1 : 0;
                        StatisticMenu.series_rang_cur = game_result.equals("win") ? (StatisticMenu.series_rang_cur + 1) :
                                (game_result.equals("lose") ? 0 : StatisticMenu.series_rang_cur);
                        if (StatisticMenu.series_rang < StatisticMenu.series_rang_cur) {
                            StatisticMenu.series_rang = StatisticMenu.series_rang_cur;
                        }
                    } else {
                        StatisticMenu.series_comm_cur = game_result.equals("win") ? (StatisticMenu.series_comm_cur + 1) :
                                (game_result.equals("lose") ? 0 : StatisticMenu.series_comm_cur);
                        if (StatisticMenu.series_comm < StatisticMenu.series_comm_cur) {
                            StatisticMenu.series_comm = StatisticMenu.series_comm_cur;
                        }
                    }
                }
                finish();
            }
        });

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