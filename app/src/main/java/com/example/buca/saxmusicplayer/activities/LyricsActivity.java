package com.example.buca.saxmusicplayer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.buca.saxmusicplayer.R;

/**
 * Created by Stefan on 04/05/2017.
 */

public class LyricsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyrics);

        Toolbar toolbar = (Toolbar) findViewById(R.id.lyrics_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
