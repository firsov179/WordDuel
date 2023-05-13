package com.example.wordduel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class RegisterMenu extends AppCompatActivity {
    int mCurrentVideoPosition;
    MediaPlayer mMediaPlayer;
    VideoView background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_menu);

        Button register_button = (Button) findViewById(R.id.loginButton);

        background = findViewById(R.id.backgroundRegister);
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
        ImageButton backButton = findViewById(R.id.backbut);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        EditText editEmail = findViewById(R.id.editEmail);
        EditText editNick = findViewById(R.id.editNick);
        EditText editTextPassword = findViewById(R.id.editTextPassword);
        EditText editTextPassword2 = findViewById(R.id.editTextPassword2);

        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String a = editEmail.getText().toString();
                if (!editTextPassword.getText().toString().equals(editTextPassword2.getText().toString())) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Введеные пароли не совпадают.", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }

                JSONObject request = new JSONObject();
                try {
                    request.put("type", "register");
                    request.put("email", editEmail.getText());
                    request.put("password", editTextPassword.getText());
                    request.put("game name", editNick.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject response = LoginMenu.executePost(request);

                try {
                    if (response.getInt("status") == 201) {
                        finish();
                    } else if (response.getInt("status") == 409) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Ошибка регистрации! Вы уже регистрировались используя эту почту.", Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Ошибка регистрации! Попробуйте позже.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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