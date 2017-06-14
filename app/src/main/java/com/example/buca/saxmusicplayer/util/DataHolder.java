package com.example.buca.saxmusicplayer.util;

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
    }

    public static void previousSong(){
        if(songPosition > 0)
            songPosition--;
    }

    public static SongBean getCurrentSong(){
        return songsToPlay.get(songPosition);
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
}
