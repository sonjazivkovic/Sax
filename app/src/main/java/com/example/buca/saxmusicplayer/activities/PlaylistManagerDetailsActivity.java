package com.example.buca.saxmusicplayer.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.buca.saxmusicplayer.R;

/**
 * Created by Stefan on 18/05/2017.
 */

public class PlaylistManagerDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_manager_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_manager_details_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.playlist_manager);

        String[] placeholderList = {"Placeholder Song 1", "Placeholder Song 2", "Placeholder Song 3"};

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.playlist_manager_list_item, R.id.text_item_view, placeholderList);
        ListView listView = (ListView) findViewById(R.id.playlist_songs_list_view);
        listView.setAdapter(adapter);

    }
}
