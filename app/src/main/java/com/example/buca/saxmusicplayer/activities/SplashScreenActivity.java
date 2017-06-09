package com.example.buca.saxmusicplayer.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.beans.SongBean;
import com.example.buca.saxmusicplayer.util.DataHolder;

import java.util.ArrayList;


/**
 * Created by Buca on 4/15/2017.
 */

public class SplashScreenActivity extends Activity {
    private static int SPLASH_DISPLAY_LENGTH = 3000;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        preferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        boolean initScanDone = preferences.getBoolean(getString(R.string.initial_scan_done_key), false);
        if(!initScanDone){
            initialSongLoading();
        }else{
            loadSongsFromDatabase();
        }
        DataHolder.setCurrentSongPosition(0);
        DataHolder.setResetAndPrepare(true);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void initialSongLoading(){
        ArrayList<SongBean> listOfSongs = new ArrayList<>();

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR};
        String selection = MediaStore.Audio.Media.DURATION + " > 150000";
        Cursor musicCursor = musicResolver.query(musicUri, projection, selection, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            do {
                SongBean song = new SongBean();
                song.setPathToFile(musicCursor.getString(0));
                song.setTitle(musicCursor.getString(1));
                song.setArtist(musicCursor.getString(2));
                song.setAlbum(musicCursor.getString(3));
                song.setYear(musicCursor.getInt(4));
                listOfSongs.add(song);
            }
            while (musicCursor.moveToNext());
        }

        DataHolder.setSongsToPlay(listOfSongs);
    }

    private void loadSongsFromDatabase(){

    }
}
