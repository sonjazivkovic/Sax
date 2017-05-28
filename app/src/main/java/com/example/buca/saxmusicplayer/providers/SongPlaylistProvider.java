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
 * Created by Stefan on 27/05/2017.
 */

public class SongPlaylistProvider extends ContentProvider {
    private DatabaseHelper dbHelper;

    private static final int SONGSPLAYLISTS = 10;
    private static final int SONGPLAYLIST_ID = 20;

    private static final String AUTHORITY = "com.example.buca.saxmusicplayer";
    private static final String SONGSPLAYLISTS_PATH = "songsplaylists";

    public static final Uri CONTENT_URI_SONGSPLAYLISTS = Uri.parse("content://" + AUTHORITY + "/" + SONGSPLAYLISTS_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sURIMatcher.addURI(AUTHORITY, SONGSPLAYLISTS_PATH, SONGSPLAYLISTS);
        sURIMatcher.addURI(AUTHORITY, SONGSPLAYLISTS_PATH + "/#", SONGPLAYLIST_ID);
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
            case SONGPLAYLIST_ID:
                queryBuilder.appendWhere(DatabaseContract.SongPlaylistTable._ID + "=" + uri.getLastPathSegment());
            case SONGSPLAYLISTS:
                queryBuilder.setTables(DatabaseContract.SongPlaylistTable.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
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
            case SONGSPLAYLISTS:
                id = db.insert(DatabaseContract.SongPlaylistTable.TABLE_NAME, null, values);
                retVal = Uri.parse(SONGSPLAYLISTS_PATH + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return retVal;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType){
            case SONGSPLAYLISTS:
                rowsDeleted = db.delete(DatabaseContract.SongPlaylistTable.TABLE_NAME, selection, selectionArgs);
                break;
            case SONGPLAYLIST_ID:
                String songPlaylistID = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsDeleted = db.delete(DatabaseContract.SongPlaylistTable.TABLE_NAME, DatabaseContract.SongPlaylistTable._ID + "=" + songPlaylistID, null);
                }else{
                    rowsDeleted = db.delete(DatabaseContract.SongPlaylistTable.TABLE_NAME,
                            DatabaseContract.SongPlaylistTable._ID + "=" + songPlaylistID
                                    + "and" + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType){
            case SONGSPLAYLISTS:
                rowsUpdated = db.update(DatabaseContract.SongPlaylistTable.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SONGPLAYLIST_ID:
                String songPlaylistID = uri.getLastPathSegment();
                if(TextUtils.isEmpty(selection)){
                    rowsUpdated = db.update(DatabaseContract.SongPlaylistTable.TABLE_NAME, values, DatabaseContract.SongPlaylistTable._ID + "=" + songPlaylistID, null);
                }else{
                    rowsUpdated = db.update(DatabaseContract.SongPlaylistTable.TABLE_NAME, values,
                            DatabaseContract.SongPlaylistTable._ID + "=" + songPlaylistID
                                    + "and" + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
