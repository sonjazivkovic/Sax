package com.example.buca.saxmusicplayer.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.providers.PlaylistProvider;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

/**
 * Created by Stefan on 18/05/2017.
 */

public class PlaylistManagerDetailsActivity extends AppCompatActivity {
    private ContentResolver resolver;
    private Uri playlistUri;
    private Cursor playlistCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_manager_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_manager_details_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        resolver = getContentResolver();
        playlistUri = ContentUris.withAppendedId(PlaylistProvider.CONTENT_URI_PLAYLISTS,  Long.parseLong(getIntent().getStringExtra(DatabaseContract.PlaylistTable.TABLE_NAME + DatabaseContract.PlaylistTable._ID)));
        playlistCursor = resolver.query(playlistUri, null, null, null, null);
        if(playlistCursor != null && playlistCursor.moveToFirst()){
            setTitle(playlistCursor.getString(1));

        }

        String[] placeholderList = {"Placeholder Song 1", "Placeholder Song 2", "Placeholder Song 3"};

        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.playlist_manager_song_list_item, R.id.playlist_song_list_item_title, placeholderList);
        ListView listView = (ListView) findViewById(R.id.playlist_songs_list_view);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        playlistCursor.close();
        super.onDestroy();
    }
}
