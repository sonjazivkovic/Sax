package com.example.buca.saxmusicplayer.adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.activities.PlaylistManagerDetailsActivity;
import com.example.buca.saxmusicplayer.providers.PlaylistProvider;
import com.example.buca.saxmusicplayer.providers.SongPlaylistProvider;
import com.example.buca.saxmusicplayer.util.DataHolder;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

/**
 * Created by Stefan on 12/06/2017.
 */

public class PlaylistCursorAdapter extends ResourceCursorAdapter {

    public PlaylistCursorAdapter(Context context, int layout, Cursor cursor, int flags){
        super(context, layout, cursor, flags);
    }

    //funkcija koja popunjava jedan element u listi playlisti
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView playlistName = (TextView) view.findViewById(R.id.text_item_view);
        playlistName.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_NAME)));
        TextView description = (TextView) view.findViewById(R.id.text_item_view_description);
        description.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_DESCRIPTION)));

        ImageButton edit = (ImageButton) view.findViewById(R.id.edit_playlist_button);
        ImageButton delete = (ImageButton) view.findViewById(R.id.remove_playlist_button);
        edit.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID)));
        delete.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID)));

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playlistDetails = new Intent(v.getContext(), PlaylistManagerDetailsActivity.class);
                playlistDetails.putExtra(DatabaseContract.PlaylistTable.TABLE_NAME + DatabaseContract.PlaylistTable._ID, v.getTag().toString());
                v.getContext().startActivity(playlistDetails);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(DataHolder.getActivePlaylistId() != Long.parseLong(v.getTag().toString())){
                    ContentResolver resolver = v.getContext().getContentResolver();
                    Uri playlistUri = PlaylistProvider.CONTENT_URI_PLAYLISTS;
                    Uri songPlaylistUri = SongPlaylistProvider.CONTENT_URI_SONGSPLAYLISTS;
                    resolver.delete(songPlaylistUri, DatabaseContract.SongPlaylistTable.COLUMN_PLAYLIST_ID + " = " + v.getTag().toString(), null);
                    resolver.delete(playlistUri, DatabaseContract.PlaylistTable._ID + " = " + v.getTag().toString(), null);
                    SharedPreferences preferences = v.getContext().getSharedPreferences(v.getContext().getString(R.string.preference_file_key), Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("default_playlist_preference", "none");
                    editor.commit();
                    Cursor newCursor = resolver.query(playlistUri, null, null, null, null);
                    changeCursor(newCursor);
                }else{
                    Toast.makeText(v.getContext(), R.string.cant_delete_active_playlist, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
