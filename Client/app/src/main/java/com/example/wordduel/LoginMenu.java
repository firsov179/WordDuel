package com.example.wordduel;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class LoginMenu extends AppCompatActivity {
    public static JSONObject stats_info;
    public static int uid;
    private int mCurrentVideoPosition;
    private MediaPlayer mMediaPlayer;
    private VideoView background;

    public static JSONObject executePost(JSONObject json) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(MainActivity.clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(MainActivity.clientSocket.getOutputStream()));
            out.write(json.toString() + "\n");
            out.flush();
            String x = in.readLine();
            return new JSONObject(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject executePostRun(JSONObject json) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(MainActivity.clientrunSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(MainActivity.clientrunSocket.getOutputStream()));
            out.write(json.toString() + "\n");
            out.flush();
            String x = in.readLine();
            return new JSONObject(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_menu);
        background = findViewById(R.id.background3);
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

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MainActivity.clientSocket = new Socket("192.168.0.150", 7007);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread thread2 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    MainActivity.clientrunSocket = new Socket("192.168.0.150", 7007);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        thread2.start();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (MainActivity.clientSocket == null) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Не удалось установить соединение с сервером.", Toast.LENGTH_SHORT);
            toast.show();
        }

        Button loginButton = (Button) findViewById(R.id.loginButton);
        Button nextButton = (Button) findViewById(R.id.nextButton);
        Button registorButton = (Button) findViewById(R.id.registorButton);
        ImageButton backButton = findViewById(R.id.backbut);
        EditText email = (EditText) findViewById(R.id.editEmail);
        EditText password = (EditText) findViewById(R.id.editPassword);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButton.setVisibility(View.VISIBLE);
                registorButton.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.INVISIBLE);
                email.setVisibility(View.INVISIBLE);
                password.setVisibility(View.INVISIBLE);
                backButton.setVisibility(View.INVISIBLE);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
            @Override
            public void onClick(View v) {
                JSONObject request = new JSONObject();
                try {
                    request.put("type", "login");
                    request.put("email", email.getText());
                    request.put("password", password.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject response = executePost(request);

                try {
                    if (response.getInt("status") == 200) {
                        LoginMenu.uid = response.getInt("uid");
                        LoginMenu.stats_info = response;
                        loadStats();
                        openMain();
                        finish();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Неверный логин или пароль!", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextButton.setVisibility(View.INVISIBLE);
                registorButton.setVisibility(View.INVISIBLE);
                loginButton.setVisibility(View.VISIBLE);
                email.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
            }
        });

        registorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistor();
            }
        });
        background.start();
    }

    private void loadStats() {
        try {
            StatisticMenu.count_games = LoginMenu.stats_info.getInt("count_games");
            StatisticMenu.games_wins = LoginMenu.stats_info.getInt("games_wins");
            StatisticMenu.words_wins = LoginMenu.stats_info.getInt("words_wins");
            StatisticMenu.rangs_games = LoginMenu.stats_info.getInt("rangs_games");
            StatisticMenu.rangs_wins = LoginMenu.stats_info.getInt("rangs_wins");
            StatisticMenu.series_rang = LoginMenu.stats_info.getInt("series_rang");
            StatisticMenu.series_rang_cur = LoginMenu.stats_info.getInt("series_rang_cur");
            StatisticMenu.series_comm = LoginMenu.stats_info.getInt("series_comm");
            StatisticMenu.series_comm_cur = LoginMenu.stats_info.getInt("series_comm_cur");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void showRegistor() {
        Intent intent = new Intent(this, RegisterMenu.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Capture the current video position and pause the video.
        mCurrentVideoPosition = mMediaPlayer.getCurrentPosition();
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