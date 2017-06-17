package com.example.buca.saxmusicplayer.adapters;
import android.content.Context;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

public class CustomGridCursorAdapter extends ResourceCursorAdapter {
    private Context mContext;
    TextView playlistImage;

    public CustomGridCursorAdapter(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView playlistName = (TextView)view.findViewById(R.id.grid_image);
        playlistName.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_NAME)));

        playlistName.setTag(cursor.getLong(cursor.getColumnIndex(DatabaseContract.PlaylistTable._ID)));
        playlistName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((MainActivity)v.getContext()).loadPlaylist(Long.parseLong(v.getTag().toString()));
            }
        });
    }
}