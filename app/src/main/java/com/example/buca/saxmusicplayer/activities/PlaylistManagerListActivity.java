package com.example.buca.saxmusicplayer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.buca.saxmusicplayer.R;

/**
 * Created by Stefan on 17/05/2017.
 */

public class PlaylistManagerListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_manager_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_manager_list_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.playlist_manager);

        String[] placeholderList = {"Placeholder Playlist 1", "Placeholder Playlist 2", "Placeholder Playlist 3"};

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.playlist_manager_list_item, R.id.text_item_view, placeholderList);
        ListView listView = (ListView) findViewById(R.id.playlist_list_view);
        listView.setAdapter(adapter);

        Button addPlaylist = (Button) findViewById(R.id.add_new_playlist_button);
        addPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlaylistManagerListActivity.this, PlaylistManagerDetailsActivity.class);
                startActivity(intent);
            }
        });

    }
}
