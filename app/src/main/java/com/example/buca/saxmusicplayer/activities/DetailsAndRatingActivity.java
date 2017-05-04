package com.example.buca.saxmusicplayer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.buca.saxmusicplayer.R;

/**
 * Created by Stefan on 04/05/2017.
 */

public class DetailsAndRatingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_and_rating);

        Toolbar toolbar = (Toolbar) findViewById(R.id.details_rating_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
