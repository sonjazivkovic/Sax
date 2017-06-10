package com.example.buca.saxmusicplayer.util;

import android.provider.BaseColumns;

/**
 * Created by Stefan on 22/05/2017.
 */

public final class DatabaseContract {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SaxMusicPlayer.db";

    private DatabaseContract() {}

    public static abstract class SongTable implements BaseColumns{
        public static final String TABLE_NAME = "songs";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ARTIST = "artist";
        public static final String COLUMN_ALBUM = "album";
        public static final String COLUMN_YEAR = "year";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PATH + " TEXT," + COLUMN_TITLE + " TEXT," + COLUMN_ARTIST + " TEXT," + COLUMN_ALBUM + " TEXT,"
                + COLUMN_YEAR + " INTEGER)";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS" + TABLE_NAME;

    }

    public static abstract class PlaylistTable implements BaseColumns{
        public static final String TABLE_NAME = "playlists";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_VISIBLE_IN_QL = "visible";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
                + COLUMN_NAME + " TEXT," + COLUMN_DESCRIPTION + " TEXT," + COLUMN_VISIBLE_IN_QL + " INTEGER DEFAULT 0)";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS" + TABLE_NAME;

    }

    public static abstract class SongPlaylistTable implements BaseColumns{
        public static final String TABLE_NAME = "sp_rel_table";
        public static final String COLUMN_PLAYLIST_ID = "playlist_id";
        public static final String COLUMN_SONG_ID = "song_id";

        public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PLAYLIST_ID + " INTEGER," + COLUMN_SONG_ID + " INTEGER,"
                + " FOREIGN KEY(" + COLUMN_PLAYLIST_ID + ") REFERENCES " + PlaylistTable.TABLE_NAME + "(" + PlaylistTable._ID + "),"
                + " FOREIGN KEY(" + COLUMN_SONG_ID + ") REFERENCES " + SongTable.TABLE_NAME + "(" + SongTable._ID + "))";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS" + TABLE_NAME;

    }
}
