package com.example.buca.saxmusicplayer.beans;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stefan on 20/05/2017.
 */

public class SongBean {

    private long id;

    private String pathToFile;

    private String title;

    private String artist;

    private String album;

    private int year;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public static SongBean createSongFromCursor(Cursor cursor){
        SongBean retVal = new SongBean();
        retVal.setId(cursor.getLong(0));
        retVal.setPathToFile(cursor.getString(1));
        retVal.setTitle(cursor.getString(2));
        retVal.setArtist(cursor.getString(3));
        retVal.setAlbum(cursor.getString(4));
        retVal.setYear(cursor.getInt(5));
        return retVal;
    }

    public static List<SongBean> createSongsFromCursor(Cursor cursor){
        List<SongBean> retVal = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            retVal.add(createSongFromCursor(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        return retVal;
    }
}
