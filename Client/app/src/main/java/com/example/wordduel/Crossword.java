package com.example.wordduel;

import static java.lang.Integer.max;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Crossword {
    private int[][][] map = new int[0][][];
    private Word[] words = new Word[0];
    public int crossword_id;
    public JSONObject wordid_to_id = new JSONObject();

    Crossword(JSONObject data, int c_id) {
        int n, height, width;
        JSONArray wordsStrings;
        try {
            crossword_id = c_id;
            n = data.getInt("size");
            wordsStrings = data.getJSONArray("words");
            height = data.getInt("height");
            width = data.getInt("width");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        words = new Word[n];
        for (int i = 0; i < n; ++i) {
            try {
                words[i] = new Word(wordsStrings.getJSONObject(i));
                wordid_to_id.put(words[i].word_id, i);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }

        map = new int[height][width][2];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                map[i][j] = new int[]{-1, -1};
            }
        }
        int k = 0;
        for (Word word : words) {
            for (int j = 0; j < word.size; ++j) {
                if (!word.orientation) {
                    map[word.i + j][word.j][1] = k;
                } else {
                    map[word.i][word.j + j][0] = k;
                }
            }
            k++;
        }
    }

    public int getStatus(int i, int j, boolean d) {
        if (d) {
            return map[i][j][0];
        }
        return map[i][j][1];
    }

    public int getStatus(int i, int j) {
        return max(map[i][j][0], map[i][j][1]);
    }

    public int getHeight() {
        return map.length;
    }

    public int getWidth() {
        return map[0].length;
    }

    public boolean getOrientation(int id) {
        return words[id].orientation;
    }

    public int getSize(int id) {
        return words[id].size;
    }

    public String getDefinition(int id) {
        return words[id].definition;
    }

    public String getWordId(int id) {
        return words[id].word_id;
    }

    public int getI(int id) {
        return words[id].i;
    }

    public int getJ(int id) {
        return words[id].j;
    }

    public void setGood(int i, int j) {
        map[i][j][0] = -2;
        map[i][j][1] = -2;
    }
}
