package com.example.wordduel;

import org.json.JSONException;
import org.json.JSONObject;

class Word {
    public int i, j, size;
    boolean orientation;
    public String definition, word_id;

    public Word(JSONObject json) throws JSONException {
        i = json.getInt("i");
        j = json.getInt("j");
        word_id = json.getString("word_id");
        size = json.getInt("size");
        orientation = json.getBoolean("orientation");
        definition = json.getString("definition");
    }
}
