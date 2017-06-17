package com.example.buca.saxmusicplayer.util;

import android.content.res.Resources;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.beans.SongBean;

import java.util.ArrayList;

/**
 * Created by Stefan on 31/05/2017.
 */

public class DataHolder {
    private static ArrayList<SongBean> songsToPlay;
    private static int songPosition;
    private static boolean resetAndPrepare;

    private static long activePlaylistId;
    private static String activePlaylistName;
    public static boolean isRepeated;
    public static boolean isShuffled;

    public static ArrayList<SongBean> getSongsToPlay(){
        return songsToPlay;
    }

    public static void setSongsToPlay(ArrayList<SongBean> newSongsToPlay){
        songsToPlay = newSongsToPlay;
    }

    public static int getCurrentSongPosition(){ return songPosition; }

    public static void setCurrentSongPosition(int newSongPosition){
        songPosition = newSongPosition;
    }

    public static void nextSong(){
        if(songPosition < songsToPlay.size() - 1)
            songPosition++;
        else if(songPosition == songsToPlay.size() - 1)
            songPosition = 0;
    }

    public static void previousSong(){
        if(songPosition > 0)
            songPosition--;
        else if(songPosition == 0)
            songPosition = songsToPlay.size() - 1;
    }

    public static SongBean getCurrentSong(){
        if(songsToPlay.size() == 0){
            SongBean emptyList = new SongBean();
            emptyList.setPathToFile("");
            emptyList.setTitle("EMPTY");
            emptyList.setYear(0);
            emptyList.setAlbum("EMPTY");
            emptyList.setArtist("EMPTY");
            return emptyList;
        }else{
            return songsToPlay.get(songPosition);
        }

    }

    public static boolean getResetAndPrepare(){
        return resetAndPrepare;
    }

    public static void setResetAndPrepare(boolean newValue){
        resetAndPrepare = newValue;
    }

    public static long getActivePlaylistId() {
        return activePlaylistId;
    }

    public static void setActivePlaylistId(long activePlaylistId) {
        DataHolder.activePlaylistId = activePlaylistId;
    }

    public static String getActivePlaylistName() {
        return activePlaylistName;
    }

    public static void setActivePlaylistName(String activePlaylistName) {
        DataHolder.activePlaylistName = activePlaylistName;
    }
}
