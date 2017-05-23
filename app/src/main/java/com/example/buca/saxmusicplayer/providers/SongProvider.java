package com.example.buca.saxmusicplayer.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.example.buca.saxmusicplayer.util.DatabaseContract;
import com.example.buca.saxmusicplayer.util.DatabaseHelper;

/**
 * Created by Stefan on 23/05/2017.
 */

public class SongProvider extends ContentProvider {
    private DatabaseHelper dbHelper;

    private static final int SONGS = 10;
    private static final int SONG_ID = 20;

    private static final String AUTHORITY = "com.example.buca.saxmusicplayer";
    private static final String SONGS_PATH = "songs";

    public static final Uri CONTENT_URI_SONGS = Uri.parse("content://" + AUTHORITY + "/" + SONGS_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sURIMatcher.addURI(AUTHORITY, SONGS_PATH, SONGS);
        sURIMatcher.addURI(AUTHORITY, SONGS_PATH + "/#", SONG_ID);
    }

    @Override
    public boolean onCreate(){
        dbHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        int uriType = sURIMatcher.match(uri);
        switch(uriType){
            case SONG_ID:
                queryBuilder.appendWhere(DatabaseContract.SongTable._ID + "=" + uri.getLastPathSegment());
            case SONGS:
                queryBuilder.setTables(DatabaseContract.SongTable.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Uknown URI:" + uri);
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri){
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values){
        Uri retVal = null;
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = 0;
        switch(uriType){
            case SONGS:
                id = db.insert(DatabaseContract.SongTable.TABLE_NAME, null, values);
                retVal = Uri.parse(SONGS_PATH + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Uknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = 0;
        int rowsDeleted = 0;
        switch (uriType){
            case SONGS:
                rowsDeleted = db.delete(DatabaseContract.SongTable.TABLE_NAME, selection, selectionArgs);
                break;
            case SONG_ID:
                String songID = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsDeleted = db.delete(DatabaseContract.SongTable.TABLE_NAME, DatabaseContract.SongTable._ID + "=" + songID, null);
                }else{
                    rowsDeleted = db.delete(DatabaseContract.SongTable.TABLE_NAME,
                            DatabaseContract.SongTable._ID + "=" + songID
                                + "and" + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Uknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long id = 0;
        int rowsUpdated = 0;
        switch (uriType){
            case SONGS:
                rowsUpdated = db.update(DatabaseContract.SongTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SONG_ID:
                String songID = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsUpdated = db.update(DatabaseContract.SongTable.TABLE_NAME, values, DatabaseContract.SongTable._ID + "=" + songID, null);
                }else{
                    rowsUpdated = db.update(DatabaseContract.SongTable.TABLE_NAME, values,
                            DatabaseContract.SongTable._ID + "=" + songID
                                    + "and" + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Uknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
