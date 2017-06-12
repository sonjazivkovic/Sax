package com.example.buca.saxmusicplayer.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.providers.PlaylistProvider;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

/**
 * Created by Stefan on 12/06/2017.
 */

public class ListCursorAdapter extends ResourceCursorAdapter {

    public ListCursorAdapter(Context context, int layout, Cursor cursor, int flags){
        super(context, layout, cursor, flags);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView playlistName = (TextView) view.findViewById(R.id.text_item_view);
        playlistName.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_NAME)));
        TextView description = (TextView) view.findViewById(R.id.text_item_view_description);
        description.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_DESCRIPTION)));

        Button edit = (Button) view.findViewById(R.id.edit_playlist_button);
        Button delete = (Button) view.findViewById(R.id.remove_playlist_button);
        edit.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID)));
        delete.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID)));

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("ID Playlist Edit:", v.getTag().toString());
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver resolver = v.getContext().getContentResolver();
                Uri playlistUri = PlaylistProvider.CONTENT_URI_PLAYLISTS;
                resolver.delete(playlistUri, DatabaseContract.PlaylistTable._ID + " = " + v.getTag().toString(), null);
                Cursor newCursor = resolver.query(playlistUri, null, null, null, null);
                changeCursor(newCursor);
            }
        });
    }
}
