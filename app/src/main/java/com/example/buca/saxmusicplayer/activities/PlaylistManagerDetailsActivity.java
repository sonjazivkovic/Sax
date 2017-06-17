package com.example.buca.saxmusicplayer.activities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.adapters.SongForPlaylistCursorAdapter;
import com.example.buca.saxmusicplayer.providers.PlaylistProvider;
import com.example.buca.saxmusicplayer.providers.SongProvider;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

/**
 * Created by Stefan on 18/05/2017.
 */

public class PlaylistManagerDetailsActivity extends AppCompatActivity {
    private ContentResolver resolver;
    private Uri playlistUri;
    private Uri songsUri;
    private Cursor playlistCursor;
    private Cursor songsCursor;
    private EditText playlistDescription;
    private Switch visibleInQLSwitch;
    private long playlistID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_manager_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_manager_details_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        playlistID = Long.parseLong(getIntent().getStringExtra(DatabaseContract.PlaylistTable.TABLE_NAME + DatabaseContract.PlaylistTable._ID));

        playlistDescription = (EditText) findViewById(R.id.playlist_description);
        visibleInQLSwitch = (Switch) findViewById(R.id.show_in_navigation);

        resolver = getContentResolver();
        playlistUri = ContentUris.withAppendedId(PlaylistProvider.CONTENT_URI_PLAYLISTS,  playlistID);
        playlistCursor = resolver.query(playlistUri, null, null, null, null);
        if(playlistCursor != null && playlistCursor.moveToFirst()){
            setTitle(playlistCursor.getString(playlistCursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_NAME)));
            String description = playlistCursor.getString(playlistCursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_DESCRIPTION));
            boolean visibleInQL = playlistCursor.getInt(playlistCursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_VISIBLE_IN_QL)) != 0;
            if(description != null && !description.equals(""))
                playlistDescription.setText(description);
            if(visibleInQL)
                visibleInQLSwitch.setChecked(true);
            else
                visibleInQLSwitch.setChecked(false);
        }

        songsUri = SongProvider.CONTENT_URI_SONGS;
        songsCursor = resolver.query(songsUri, null, null, null, null);
        SongForPlaylistCursorAdapter sfpca = new SongForPlaylistCursorAdapter(this, R.layout.playlist_manager_song_list_item, songsCursor, 0, playlistID);
        ListView listView = (ListView) findViewById(R.id.playlist_songs_list_view);
        listView.setAdapter(sfpca);

        visibleInQLSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((Switch)v).isChecked();
                if(checked){
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.PlaylistTable.COLUMN_VISIBLE_IN_QL, 1);
                    resolver.update(playlistUri, cv, null, null);
                }else{
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.PlaylistTable.COLUMN_VISIBLE_IN_QL, 0);
                    resolver.update(playlistUri, cv, null, null);
                }
            }
        });

        //kada se stisne dugme done na tastaturi tada se cuva description u bazu
        playlistDescription.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEND){
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.PlaylistTable.COLUMN_DESCRIPTION, v.getText().toString());
                    resolver.update(playlistUri, cv, null, null);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        playlistCursor.close();
        songsCursor.close();
        super.onDestroy();
    }
}
