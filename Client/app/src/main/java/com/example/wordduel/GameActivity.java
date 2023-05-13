package com.example.wordduel;

import static com.example.wordduel.LoginMenu.executePost;
import static com.example.wordduel.LoginMenu.executePostRun;

import static java.lang.Integer.min;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends AppCompatActivity {

    private int selectedI = 0;
    private int selectedJ = 0;
    private int selectedId = -1;
    private boolean selectedOrientation = true;
    private Crossword crossword;
    private int mCurrentVideoPosition;
    private MediaPlayer mMediaPlayer;
    private VideoView background;
    public static int your_score = 0;
    public static int enemy_score = 0;
    private int crossword_id;
    private int width;
    Set<String> used = new HashSet<>();
    public static int[] rangs = new int[]{R.drawable.red, R.drawable.orange, R.drawable.yelow,
            R.drawable.green, R.drawable.cyan, R.drawable.blue, R.drawable.purple};
    public static String[] subrangs = new String[]{"C", "B", "A"};
    public static int[] rangpoints = new int[]{R.drawable.zero, R.drawable.one, R.drawable.two};
    boolean active = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        ImageView rang = findViewById(R.id.rang);
        TextView rang_subcategory = findViewById(R.id.subrang);
        TextView name = findViewById(R.id.your_name);

        rang.setImageResource(rangs[MainActivity.rang_cat]);
        rang_subcategory.setText(subrangs[MainActivity.rang_subcat]);
        try {
            name.setText(LoginMenu.stats_info.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        ConstraintLayout keyboard = findViewById(R.id.constraintLayout);
        ViewGroup.LayoutParams params = keyboard.getLayoutParams();
        params.width = width - 20;
        keyboard.setLayoutParams(params);

        createKeyboard();
        JSONObject data = new JSONObject();
        JSONObject enemy = new JSONObject();
        try {
            enemy = WaitGameMenu.data.getJSONObject("enemy");
            data = WaitGameMenu.data.getJSONObject("crossword");
            crossword_id = WaitGameMenu.data.getInt("crossword_id");


            ImageView enemy_rang = findViewById(R.id.enemy_rang);
            TextView enemy_subrang = findViewById(R.id.enemy_subrang);
            TextView enemy_name = findViewById(R.id.enemy_name);

            enemy_rang.setImageResource(rangs[enemy.getInt("rang_category")]);
            enemy_subrang.setText(subrangs[enemy.getInt("rang_subcategory")]);
            enemy_name.setText(enemy.getString("email"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        crossword = new Crossword(data, crossword_id);
        createCrossword();

        background = findViewById(R.id.backgroundStats);
        background.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.midle));
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

        ImageButton check = findViewById(R.id.checkButton2);
        check.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                active = false;
                StringBuilder res = new StringBuilder();
                int i = crossword.getI(selectedId);
                int j = crossword.getJ(selectedId);
                for (int q = 0; q < crossword.getSize(selectedId); ++q) {
                    Button curButton = findViewById(179 * i + j);
                    res.append(curButton.getText());
                    if (selectedOrientation) {
                        j++;
                    } else {
                        i++;
                    }
                }
                JSONObject request = new JSONObject();
                try {
                    request.put("type", "check word");
                    request.put("crossword_id", crossword.crossword_id);
                    request.put("word_id", crossword.getWordId(selectedId));
                    request.put("word", res.toString());
                    JSONObject response = executePost(request);
                    if (response == null || response.getInt("status") != 200) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Неверно!", Toast.LENGTH_SHORT);
                        toast.show();
                        return;
                    }
                    used.add(crossword.getWordId(selectedId));
                    your_score++;
                    makeGood(selectedId, true);
                    TextView yourScore = findViewById(R.id.yourScore);
                    yourScore.setText(Integer.toString(your_score));

                    findFirstWord();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                active = true;
            }
        });
        Thread thread = new Thread(new Runnable(){
            public void run() {
                gameStatus();
            }
        });
        thread.start();
    }

    private void makeGood(int word_id, boolean you) {
        int i = crossword.getI(word_id);
        int j = crossword.getJ(word_id);
        for (int q = 0; q < crossword.getSize(word_id); ++q) {
            Button curButton = findViewById(179 * i + j);
            curButton.setClickable(false);
            if (you) {
                curButton.setBackgroundResource(R.drawable.good_letter_your);
            } else {
                curButton.setBackgroundResource(R.drawable.good_letter_enemy);
            }

            crossword.setGood(i, j);
            if (crossword.getOrientation(word_id)) {
                j++;
            } else {
                i++;
            }
        }
    }

    private void setWord(int word_id, String ans) {
        int i = crossword.getI(word_id);
        int j = crossword.getJ(word_id);
        for (int q = 0; q < crossword.getSize(word_id); ++q) {
            Button curButton = findViewById(179 * i + j);
            String letter = String.valueOf(ans.charAt(q));
            curButton.setText(letter);
            if (crossword.getOrientation(word_id)) {
                j++;
            } else {
                i++;
            }
        }
    }

    public void createCrossword() {
        TableLayout tableLayout = findViewById(R.id.crossWord);
        ViewGroup.LayoutParams params = tableLayout.getLayoutParams();
        params.width = width - 20;
        params.height = width - 20;
        tableLayout.setLayoutParams(params);

        int size = min(tableLayout.getLayoutParams().height / crossword.getHeight(), tableLayout.getLayoutParams().width / crossword.getWidth());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        for (int i = 0; i < crossword.getHeight(); ++i) {
            LinearLayout linearLayout = new LinearLayout(this);
            for (int j = 0; j < crossword.getWidth(); ++j) {
                Button button = new Button(this);
                button.setId(179 * i + j);
                button.setLayoutParams(layoutParams);
                button.setMinHeight(0);
                button.setPadding(0, 0, 0, 0);
                button.setWidth(0);
                button.setTextSize(12);
                button.setTypeface(ResourcesCompat.getFont(this, R.font.main_font));
                if (crossword.getStatus(i, j) != -1) {
                    if (selectedId == -1) {
                        selectLetter(i, j, crossword.getOrientation(crossword.getStatus(i, j)));
                    } else {
                        button.setBackgroundResource(R.drawable.main_letter);
                    }
                } else {
                    button.setVisibility(View.INVISIBLE);
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int vId = v.getId();
                        int i = vId / 179;
                        int j = vId - i * 179;
                        if (crossword.getStatus(i, j) == -1) {
                            return;
                        }

                        unmarkWord();
                        if (selectedI == i && selectedJ == j) {
                            selectedOrientation = !selectedOrientation;
                            selectLetter(i, j, selectedOrientation);
                        } else {
                            selectLetter(i, j, selectedOrientation);
                        }
                        markWord(i, j, selectedOrientation);
                    }
                });
                linearLayout.addView(button);
            }
            tableLayout.addView(linearLayout);
        }
        markWord(selectedI, selectedJ, selectedOrientation);
    }

    private void unmarkWord() {
        if (selectedOrientation) {
            for (int curJ = selectedJ - 1; curJ != -1 && crossword.getStatus(selectedI, curJ) != -1; curJ--) {
                if (crossword.getStatus(selectedI, curJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * selectedI + curJ);
                curButton.setBackgroundResource(R.drawable.main_letter);
            }
            for (int curJ = selectedJ + 1; curJ != crossword.getWidth() && crossword.getStatus(selectedI, curJ) != -1; curJ++) {
                if (crossword.getStatus(selectedI, curJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * selectedI + curJ);
                curButton.setBackgroundResource(R.drawable.main_letter);
            }
        } else {
            for (int curI = selectedI - 1; curI != -1 && crossword.getStatus(curI, selectedJ) != -1; curI--) {
                if (crossword.getStatus(curI, selectedJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * curI + selectedJ);
                curButton.setBackgroundResource(R.drawable.main_letter);
            }
            for (int curI = selectedI + 1; curI != crossword.getHeight() && crossword.getStatus(curI, selectedJ) != -1; curI++) {
                if (crossword.getStatus(curI, selectedJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * curI + selectedJ);
                curButton.setBackgroundResource(R.drawable.main_letter);
            }
        }
        Button curButton = findViewById(179 * selectedI + selectedJ);
        curButton.setBackgroundResource(R.drawable.main_letter);
    }

    private void markWord(int i, int j, boolean orient) {
        if (orient) {
            for (int curJ = j - 1; curJ != -1 && crossword.getStatus(i, curJ) != -1; curJ--) {
                if (crossword.getStatus(selectedI, curJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * i + curJ);
                curButton.setBackgroundResource(R.drawable.selected_letter);
            }
            for (int curJ = j + 1; curJ != crossword.getWidth() && crossword.getStatus(i, curJ) != -1; curJ++) {
                if (crossword.getStatus(selectedI, curJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * i + curJ);
                curButton.setBackgroundResource(R.drawable.selected_letter);
            }
        } else {
            for (int curI = i - 1; curI != -1 && crossword.getStatus(curI, j) != -1; curI--) {
                if (crossword.getStatus(curI, selectedJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * curI + j);
                curButton.setBackgroundResource(R.drawable.selected_letter);
            }
            for (int curI = i + 1; curI != crossword.getHeight() && crossword.getStatus(curI, j) != -1; curI++) {
                if (crossword.getStatus(curI, selectedJ) == -2) {
                    continue;
                }
                Button curButton = findViewById(179 * curI + j);
                curButton.setBackgroundResource(R.drawable.selected_letter);
            }
        }
        Button curButton = findViewById(179 * i + j);
        curButton.setBackgroundResource(R.drawable.super_selected_letter);
    }

    private void createKeyboard() {
        ArrayList<List<String>> letters = new ArrayList<>();
        letters.add(Arrays.asList("Й", "Ц", "У", "К", "Е", "Н", "Г", "Ш", "Щ", "З", "Х", "Ъ"));
        letters.add(Arrays.asList("Ф", "Ы", "В", "А", "П", "Р", "О", "Л", "Д", "Ж", "Э"));
        letters.add(Arrays.asList("Я", "Ч", "С", "М", "И", "Т", "Ь", "Б", "Ю"));
        LinearLayout keyboard = findViewById(R.id.keyboard);
        ViewGroup.LayoutParams params = keyboard.getLayoutParams();
        params.width = width - 20;
        keyboard.setLayoutParams(params);
        int height = (keyboard.getLayoutParams().height - 10) / 3;
        int width = (keyboard.getLayoutParams().width - 11 * 7) / 12;

        LinearLayout linearLayout = new LinearLayout(this);
        addLine(linearLayout, letters.get(0), height, width, 0);
        keyboard.addView(linearLayout);
        addSpace(keyboard);

        linearLayout = new LinearLayout(this);
        ImageView half = new ImageView(this);
        half.setLayoutParams(new LinearLayout.LayoutParams(width / 2, height));
        linearLayout.addView(half);
        addLine(linearLayout, letters.get(1), height, width, 1);
        keyboard.addView(linearLayout);
        addSpace(keyboard);

        linearLayout = new LinearLayout(this);
        half = new ImageView(this);
        half.setLayoutParams(new LinearLayout.LayoutParams(width + width / 2 + 7, height));
        linearLayout.addView(half);
        addLine(linearLayout, letters.get(2), height, width, 2);
        keyboard.addView(linearLayout);
    }

    private void addSpace(LinearLayout keyboard) {
        LinearLayout linearLayout = new LinearLayout(this);
        ImageView half = new ImageView(this);
        half.setLayoutParams(new LinearLayout.LayoutParams(7, 7));
        linearLayout.addView(half);
        keyboard.addView(linearLayout);
    }

    private void addLine(LinearLayout linearLayout, List<String> letters, int height, int width, int k) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        LinearLayout.LayoutParams layoutParamsSpace = new LinearLayout.LayoutParams(7, height);
        for (int j = 0; j < letters.size(); ++j) {

            Button button = new Button(this);
            button.setLayoutParams(layoutParams);
            button.setId(17900 + 12 * k + j);
            button.setBackgroundResource(R.drawable.button);
            button.setText(letters.get(j));
            button.setTypeface(ResourcesCompat.getFont(this, R.font.main_font));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int vId = v.getId();
                    Button x = findViewById(vId);
                    Button cur = findViewById(179 * selectedI + selectedJ);
                    cur.setText(x.getText());
                    cur.setBackgroundResource(R.drawable.selected_letter);
                    if (selectedOrientation) {
                        int curJ = selectedJ;
                        do {
                            selectedJ++;
                        } while (selectedJ != crossword.getWidth() && crossword.getStatus(selectedI, selectedJ) == -2 && crossword.getStatus(selectedI, selectedJ) != -1);
                        if (selectedJ == crossword.getWidth() || crossword.getStatus(selectedI, selectedJ) == -1) {

                            selectedJ = curJ;
                        }
                    } else {
                        int curI = selectedI;
                        do {
                            selectedI++;
                        } while (selectedI != crossword.getHeight() && crossword.getStatus(selectedI, selectedJ) == -2 && crossword.getStatus(selectedI, selectedJ) != -1);
                        if (selectedI == crossword.getHeight() || crossword.getStatus(selectedI, selectedJ) == -1) {
                            selectedI = curI;
                        }
                    }
                    cur = findViewById(179 * selectedI + selectedJ);
                    cur.setBackgroundResource(R.drawable.super_selected_letter);
                }
            });
            linearLayout.addView(button);

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParamsSpace);
            linearLayout.addView(imageView);
        }
    }

    private void selectLetter(int i, int j, boolean orient) {
        TextView definition = findViewById(R.id.definition);
        selectedI = i;
        selectedJ = j;
        selectedId = crossword.getStatus(i, j, orient);
        if (selectedId == -1) {
            selectedId = crossword.getStatus(i, j, !orient);
            selectedOrientation = !orient;
        } else {
            selectedOrientation = orient;
        }
        definition.setText(crossword.getDefinition(selectedId));
    }

    private void findFirstWord() {
        for (int i = 0; i < crossword.getHeight(); ++i) {
            for (int j = 0; j < crossword.getWidth(); ++j) {
                if (crossword.getStatus(i, j) > 0) {
                    selectLetter(i, j, crossword.getOrientation(crossword.getStatus(i, j)));
                    markWord(selectedI, selectedJ, selectedOrientation);
                    return;
                }
            }
        }
        Intent intent = new Intent(this, ResultActivityMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    @SuppressLint("DefaultLocale")
    private void gameStatus() {
        int code = 200;
        JSONObject response, request, guessed = new JSONObject();
        request = new JSONObject();
        try {
            request.put("type", "game status");
            request.put("crossword_id", crossword_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView textView = findViewById(R.id.timer);
        int seconds = 600;
        while (code == 200) {
            if (!active) {
                continue;
            }
            response = executePostRun(request);
            try {
                code = response.getInt("status");
                guessed = response.getJSONObject("guessed");
                seconds = response.getInt("time");
                int minutes = seconds / 60;
                seconds = seconds % 60;
                textView.setText(String.format("%02d:%02d", minutes, seconds));
                if (guessed.length() == 0) {
                    continue;
                }
                for (int i = 0; i < guessed.names().length(); i++) {
                    String word_id = guessed.names().getString(i);
                    String ans = guessed.getString(word_id);
                    int x = crossword.wordid_to_id.getInt(word_id);
                    if (!used.contains(word_id)) {
                        used.add(word_id);
                        enemy_score++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setWord(x, ans);
                                makeGood(x, false);
                                TextView yourScore = findViewById(R.id.enemyScore);
                                yourScore.setText(Integer.toString(enemy_score));
                                if (selectedId == x) {
                                    findFirstWord();
                                }
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(this, ResultActivityMenu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}