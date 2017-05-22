package com.example.buca.saxmusicplayer.beans;

/**
 * Created by Stefan on 20/05/2017.
 */

public class SongPlaylistBean {

    private long id;

    private long songId;

    private long playlistId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }
}
