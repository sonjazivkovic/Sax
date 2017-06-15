package com.example.buca.saxmusicplayer.adapters;
import android.content.Context;

import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

public class CustomGridCursorAdapter extends ResourceCursorAdapter {
    private Context mContext;

    public CustomGridCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView playlistName = (TextView)view.findViewById(R.id.grid_text);
        playlistName.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_NAME)));

        final ImageView playlistImage = (ImageView)view.findViewById(R.id.grid_image);
        playlistImage.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID)));
        playlistImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MainActivity)v.getContext()).loadPlaylist(Long.parseLong(v.getTag().toString()));
            }
        });
    }
}