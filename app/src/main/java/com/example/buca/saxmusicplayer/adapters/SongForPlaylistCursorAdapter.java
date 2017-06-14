package com.example.buca.saxmusicplayer.adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.providers.SongPlaylistProvider;
import com.example.buca.saxmusicplayer.util.DataHolder;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

/**
 * Created by Stefan on 14/06/2017.
 */

public class SongForPlaylistCursorAdapter extends ResourceCursorAdapter {

    private long playlistID;
    private ContentResolver resolver;
    Uri songPlaylistUri = SongPlaylistProvider.CONTENT_URI_SONGSPLAYLISTS;

    public SongForPlaylistCursorAdapter(Context context, int layout, Cursor cursor, int flags, long playlistID){
        super(context, layout, cursor, flags);
        this.playlistID = playlistID;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView songTitle = (TextView) view.findViewById(R.id.playlist_song_list_item_title);
        songTitle.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.SongTable.COLUMN_TITLE)));
        CheckBox songInPlaylist = (CheckBox) view.findViewById(R.id.add_remove_song_button);
        songInPlaylist.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.SongTable._ID)));

        resolver = view.getContext().getContentResolver();
        String selection = DatabaseContract.SongPlaylistTable.COLUMN_PLAYLIST_ID + " = " + playlistID + " and "
                + DatabaseContract.SongPlaylistTable.COLUMN_SONG_ID + " = " + cursor.getLong(cursor.getColumnIndex(DatabaseContract.SongTable._ID));
        Cursor songExistInPlaylistCursor = resolver.query(songPlaylistUri, null, selection, null, null);
        if(songExistInPlaylistCursor != null && songExistInPlaylistCursor.moveToFirst())
            songInPlaylist.setChecked(true);
        songExistInPlaylistCursor.close();

        songInPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DataHolder.getActivePlaylistId() != playlistID) {
                    boolean checked = ((CheckBox) v).isChecked();
                    if (checked) {
                        ContentValues cv = new ContentValues();
                        cv.put(DatabaseContract.SongPlaylistTable.COLUMN_PLAYLIST_ID, playlistID);
                        cv.put(DatabaseContract.SongPlaylistTable.COLUMN_SONG_ID, Long.parseLong(v.getTag().toString()));
                        resolver.insert(songPlaylistUri, cv);
                    } else {
                        String where = DatabaseContract.SongPlaylistTable.COLUMN_PLAYLIST_ID + " = " + playlistID + " and "
                                + DatabaseContract.SongPlaylistTable.COLUMN_SONG_ID + " = " + Long.parseLong(v.getTag().toString());
                        resolver.delete(songPlaylistUri, where, null);
                    }
                }else{
                    Toast.makeText(v.getContext(), R.string.cant_add_rem_song_active_playlist, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
