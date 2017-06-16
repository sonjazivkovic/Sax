package com.example.buca.saxmusicplayer.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.api.chartlyrics.ChartLyricsClient;
import com.example.buca.saxmusicplayer.util.DataHolder;

import org.apache.commons.lang3.StringUtils;

import java.net.URL;

/**
 * Created by Stefan on 04/05/2017.
 */

public class DetailsAndRatingActivity extends AppCompatActivity {

    private ChartLyricsClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_and_rating);

        Toolbar toolbar = (Toolbar) findViewById(R.id.details_rating_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.details_rating);

        TextView tvSongTitle = (TextView) findViewById(R.id.det_rat_act_song_title);

        TextView tvDetails = (TextView) findViewById(R.id.det_rat_act_song_details);

        ImageView ivAlbumCover = (ImageView) findViewById(R.id.det_rat_album_cover);

        String songArtist = DataHolder.getCurrentSong().getArtist();

        String songTitle = DataHolder.getCurrentSong().getTitle();

        if(songArtist.equals("<unknown>") && !songTitle.equals("<unknown>")){
            if(songTitle.contains("-")) {
                String parts[] = songTitle.split("-");

                songArtist = parts[0];
                songTitle = parts[1];
            } else {
                tvSongTitle.setText(R.string.song_title_is_not_valid);
            }
        } else {
            tvSongTitle.setText(R.string.song_title_is_unknown);
        }


        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            client = new ChartLyricsClient();

            String response = client.query(songArtist,songTitle);

            String songAlbumCover = StringUtils.substringBetween(response, "<LyricCovertArtUrl>", "</LyricCovertArtUrl>");
            String songRank = StringUtils.substringBetween(response, "<LyricRank>", "</LyricRank>");
            String artist = StringUtils.substringBetween(response, "<LyricArtist>", "</LyricArtist>");
            String song = StringUtils.substringBetween(response, "<LyricSong>", "</LyricSong>");

            String details = ("Song name: " + song + "\n" + "Song artist: " + artist + "\n" + "Song rank:" + songRank + "\n");

            tvDetails.setText(details);

                try {
                    URL imgUrl = new URL(songAlbumCover);
                    Bitmap malbumCover = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
                    ivAlbumCover.setImageBitmap(malbumCover);
                }
                catch(Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                }


        }



    }
}
