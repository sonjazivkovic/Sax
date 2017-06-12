package com.example.buca.saxmusicplayer.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.adapters.ListCursorAdapter;
import com.example.buca.saxmusicplayer.providers.PlaylistProvider;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

/**
 * Created by Stefan on 17/05/2017.
 */

public class PlaylistManagerListActivity extends AppCompatActivity {
    private ContentResolver playlistResolver;
    private Uri playlistUri;
    private Button addPlaylist;
    private Button searchPlaylist;
    private Button cleanSearchPlaylist;
    private EditText playlistName;
    private ListCursorAdapter lca;
    private Cursor playlistCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_manager_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_manager_list_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.playlist_manager);

        playlistResolver = getContentResolver();
        playlistUri = PlaylistProvider.CONTENT_URI_PLAYLISTS;
        playlistCursor = playlistResolver.query(playlistUri, null, null, null, null);
        lca = new ListCursorAdapter(this, R.layout.playlist_manager_list_item, playlistCursor, 0);
        ListView listView = (ListView) findViewById(R.id.playlist_list_view);
        listView.setAdapter(lca);


        playlistName = (EditText) findViewById(R.id.playlist_list_view_playlist_name);
        addPlaylist = (Button) findViewById(R.id.add_new_playlist_button);
        searchPlaylist = (Button) findViewById(R.id.search_playlist_button);
        cleanSearchPlaylist = (Button) findViewById(R.id.clean_search_playlist_button);
        addPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playlistName.getText() != null && !playlistName.getText().toString().equals("")) {
                    ContentValues cv = new ContentValues();
                    cv.put(DatabaseContract.PlaylistTable.COLUMN_NAME, playlistName.getText().toString());
                    cv.put(DatabaseContract.PlaylistTable.COLUMN_DESCRIPTION, "");
                    cv.put(DatabaseContract.PlaylistTable.COLUMN_VISIBLE_IN_QL, false);
                    playlistResolver.insert(playlistUri, cv);
                    playlistCursor = playlistResolver.query(playlistUri, null, null, null, null);
                    lca.changeCursor(playlistCursor);
                }
            }
        });
        searchPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playlistName.getText() != null && !playlistName.getText().toString().equals("")) {
                    String selection = DatabaseContract.PlaylistTable.COLUMN_NAME + " = '" + playlistName.getText().toString() + "'";
                    playlistCursor = playlistResolver.query(playlistUri, null, selection, null, null);
                    lca.changeCursor(playlistCursor);
                }
            }
        });
        cleanSearchPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playlistName.getText() != null && !playlistName.getText().toString().equals("")) {
                    playlistName.setText("");
                    playlistCursor = playlistResolver.query(playlistUri, null, null, null, null);
                    lca.changeCursor(playlistCursor);
                }
            }
        });
    }

}
