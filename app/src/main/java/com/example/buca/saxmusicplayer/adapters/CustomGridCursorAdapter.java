package com.example.buca.saxmusicplayer.adapters;
import android.content.ContentResolver;
import android.content.Context;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.providers.PlaylistProvider;
import com.example.buca.saxmusicplayer.util.DataHolder;
import com.example.buca.saxmusicplayer.util.DatabaseContract;


public class CustomGridCursorAdapter extends ResourceCursorAdapter {
    private Context mContext;
    private TextView playlistName;




    public CustomGridCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        playlistName = (TextView)view.findViewById(R.id.grid_image);
        playlistName.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_NAME)));

        playlistName.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID)));

        playlistName.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {
                ((MainActivity)v.getContext()).loadPlaylist(Long.parseLong(v.getTag().toString()));
                Uri playlistUri = PlaylistProvider.CONTENT_URI_PLAYLISTS;
                ContentResolver playlistResolver = v.getContext().getContentResolver();
                String where = DatabaseContract.PlaylistTable.COLUMN_VISIBLE_IN_QL + " = 1";
                Cursor playlistCursor = playlistResolver.query(playlistUri, null, where, null, null);
                changeCursor(playlistCursor);

            }
        });
        if (DataHolder.getActivePlaylistId() == cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID))) {
            playlistName.setBackground(ContextCompat.getDrawable(context, R.drawable.border_single_grid_playing));
        }
        else {
            playlistName.setBackground(ContextCompat.getDrawable(context, R.drawable.border_single_grid));
        }


    }
}