package com.example.buca.saxmusicplayer.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.adapters.PlaylistCursorAdapter;
import com.example.buca.saxmusicplayer.providers.PlaylistProvider;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

import java.util.Locale;

/**
 * Created by Stefan on 17/05/2017.
 */

public class PlaylistManagerListActivity extends AppCompatActivity {
    private ContentResolver playlistResolver;
    private Uri playlistUri;
    private ImageButton addPlaylist;
    private ImageButton searchPlaylist;
    private ImageButton cleanSearchPlaylist;
    private EditText playlistName;
    private PlaylistCursorAdapter lca;
    private Cursor playlistCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeLang();
        setContentView(R.layout.playlist_manager_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.playlist_manager_list_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.playlist_manager);

        //preuzimamo sve plejliste i pravimo adapter za listu plejlisti
        playlistResolver = getContentResolver();
        playlistUri = PlaylistProvider.CONTENT_URI_PLAYLISTS;
        playlistCursor = playlistResolver.query(playlistUri, null, null, null, null);
        lca = new PlaylistCursorAdapter(this, R.layout.playlist_manager_list_item, playlistCursor, 0);
        ListView listView = (ListView) findViewById(R.id.playlist_list_view);
        listView.setAdapter(lca);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //hide KB
                InputMethodManager imm =  (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(playlistName.getWindowToken(), 0);
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
        });

        playlistName = (EditText) findViewById(R.id.playlist_list_view_playlist_name);
        addPlaylist = (ImageButton) findViewById(R.id.add_new_playlist_button);
        searchPlaylist = (ImageButton) findViewById(R.id.search_playlist_button);
        cleanSearchPlaylist = (ImageButton) findViewById(R.id.clean_search_playlist_button);
        addPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playlistName.getText() != null && !playlistName.getText().toString().equals("")) {
                    String selection = DatabaseContract.PlaylistTable.COLUMN_NAME + " = '" + playlistName.getText().toString() + "'";
                    Cursor checkForExistingPlaylist = playlistResolver.query(playlistUri, null, selection, null, null);
                    if(checkForExistingPlaylist != null && checkForExistingPlaylist.moveToFirst()) {
                        Toast t = Toast.makeText(PlaylistManagerListActivity.this, R.string.playlist_duplicate, Toast.LENGTH_LONG);
                        t.setGravity(Gravity.TOP, 0, 0);
                        t.show();
                    }else{
                        ContentValues cv = new ContentValues();
                        cv.put(DatabaseContract.PlaylistTable.COLUMN_NAME, playlistName.getText().toString());
                        cv.put(DatabaseContract.PlaylistTable.COLUMN_DESCRIPTION, "");
                        playlistResolver.insert(playlistUri, cv);
                        playlistCursor = playlistResolver.query(playlistUri, null, null, null, null);
                        lca.changeCursor(playlistCursor);
                    }
                    checkForExistingPlaylist.close();
                }
            }
        });
        searchPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playlistName.getText() != null && !playlistName.getText().toString().equals("")) {
                    String selection = DatabaseContract.PlaylistTable.COLUMN_NAME + " LIKE ?";
                    String[] selectionArgs = {"%"+playlistName.getText().toString()+"%"};
                    playlistCursor = playlistResolver.query(playlistUri, null, selection, selectionArgs, null);
                    lca.changeCursor(playlistCursor);
                }
            }
        });
        cleanSearchPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    playlistName.setText("");
                    playlistCursor = playlistResolver.query(playlistUri, null, null, null, null);
                    lca.changeCursor(playlistCursor);
            }
        });
    }

    @Override
    protected void onDestroy() {
        playlistCursor.close();
        lca.dismissDialog();
        super.onDestroy();
    }

    public void changeLang() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        String languageToLoad = sp.getString("language_preference", "en"); // your language
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}
