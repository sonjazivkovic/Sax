package com.example.buca.saxmusicplayer.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.beans.SongBean;
import com.example.buca.saxmusicplayer.providers.SongProvider;
import com.example.buca.saxmusicplayer.util.DataHolder;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

import java.util.ArrayList;


/**
 * Created by Buca on 4/15/2017.
 */

public class SplashScreenActivity extends Activity {
    private static int SPLASH_DISPLAY_LENGTH = 3000;
    private SharedPreferences preferences;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE_PHONE = 123;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        checkPermissions();

        preferences = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        boolean initScanDone = preferences.getBoolean(getString(R.string.initial_scan_done_key), false);
        if(!initScanDone){
            initialSongLoading();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.initial_scan_done_key), true);
            editor.commit();
            DataHolder.setActivePlaylistId(-1);
        }else{
            loadSongsFromDatabase();
            DataHolder.setActivePlaylistId(-1);
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
        Uri songsUri = SongProvider.CONTENT_URI_SONGS;
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

                //saving to db
                ContentValues cv = new ContentValues();
                cv.put(DatabaseContract.SongTable.COLUMN_PATH, musicCursor.getString(0));
                cv.put(DatabaseContract.SongTable.COLUMN_TITLE, musicCursor.getString(1));
                cv.put(DatabaseContract.SongTable.COLUMN_ARTIST, musicCursor.getString(2));
                cv.put(DatabaseContract.SongTable.COLUMN_ALBUM, musicCursor.getString(3));
                cv.put(DatabaseContract.SongTable.COLUMN_YEAR, musicCursor.getInt(4));
                musicResolver.insert(songsUri, cv);
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();

        DataHolder.setSongsToPlay(listOfSongs);
    }

    private void loadSongsFromDatabase(){
        ArrayList<SongBean> listOfSongs = new ArrayList<>();
        ContentResolver musicResolver = getContentResolver();
        Uri songsUri = SongProvider.CONTENT_URI_SONGS;
        Cursor musicCursor = musicResolver.query(songsUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()) {
            do {
                SongBean song = new SongBean();
                song.setPathToFile(musicCursor.getString(1));
                song.setTitle(musicCursor.getString(2));
                song.setArtist(musicCursor.getString(3));
                song.setAlbum(musicCursor.getString(4));
                song.setYear(musicCursor.getInt(5));
                listOfSongs.add(song);
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();

        DataHolder.setSongsToPlay(listOfSongs);
    }

    /*Ovim proveravamo da li uredjaj dozvoljava citanje sa eksterne kartice i stanja telefona nasoj aplikaciji*/
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE_PHONE);
        }
    }

    /*Ovde proveravamo da li smo dobili dozvolu, za sada sam samo napisala poruku i za slucaj da jesmo i za slucaj da nismo*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE_PHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.read_storage_phone_approved), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.read_storage_phone_denied), Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }
}
