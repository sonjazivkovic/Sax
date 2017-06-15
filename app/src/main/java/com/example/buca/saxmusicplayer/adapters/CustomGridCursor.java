package com.example.buca.saxmusicplayer.adapters;
import android.content.ContentResolver;
import android.content.Context;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.util.DatabaseContract;

public class CustomGridCursor extends ResourceCursorAdapter {
    private Context mContext;

    public CustomGridCursor(Context context, int layout, Cursor c, int flags) {
        super(context, layout, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView playlistName = (TextView)view.findViewById(R.id.grid_text);
        playlistName.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.PlaylistTable.COLUMN_NAME)));

        ImageView playlistImage = (ImageView)view.findViewById(R.id.grid_image);
    }
}