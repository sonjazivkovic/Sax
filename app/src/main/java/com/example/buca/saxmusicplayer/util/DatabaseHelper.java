package com.example.buca.saxmusicplayer.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Stefan on 22/05/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context ctx){
        if(sInstance == null){
            sInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context ctx) {
        super(ctx, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(DatabaseContract.SongTable.CREATE_TABLE);
        db.execSQL(DatabaseContract.PlaylistTable.CREATE_TABLE);
        db.execSQL(DatabaseContract.SongPlaylistTable.CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        if(oldVersion != newVersion){
            db.execSQL(DatabaseContract.SongPlaylistTable.DELETE_TABLE);
            db.execSQL(DatabaseContract.PlaylistTable.DELETE_TABLE);
            db.execSQL(DatabaseContract.SongTable.DELETE_TABLE);
            onCreate(db);
        }
    }
}
