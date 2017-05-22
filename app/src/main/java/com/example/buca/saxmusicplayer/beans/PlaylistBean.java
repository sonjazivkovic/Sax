package com.example.buca.saxmusicplayer.beans;

/**
 * Created by Stefan on 20/05/2017.
 */

public class PlaylistBean {

    private long id;

    private String playlistName;

    private String description;

    private Boolean visibleInQuickLaunch;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getVisibleInQuickLaunch() {
        return visibleInQuickLaunch;
    }

    public void setVisibleInQuickLaunch(Boolean visibleInQuickLaunch) {
        this.visibleInQuickLaunch = visibleInQuickLaunch;
    }
}
