package com.example.buca.saxmusicplayer.activities;

import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.api.chartlyrics.ChartLyricsClient;
import com.example.buca.saxmusicplayer.util.DataHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Stefan on 04/05/2017.
 */

public class LyricsActivity extends AppCompatActivity {


    private String API_URL = "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?";

    private String songArtist;
    private String songTitle;

    private ChartLyricsClient client;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyrics);

        Toolbar toolbar = (Toolbar) findViewById(R.id.lyrics_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.lyrics);

        songArtist = DataHolder.getCurrentSong().getArtist();

        songTitle = DataHolder.getCurrentSong().getTitle();

        TextView twSongTitle = (TextView) findViewById(R.id.lyrics_act_song_title);

        TextView twLyricsText = (TextView) findViewById(R.id.lyrics_act_song_lyrics);

        //songArtist = "metallica";
        //songTitle = "unforgiven";

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            client = new ChartLyricsClient();

            String response = client.query(songArtist,songTitle);

            twLyricsText.setText(response);

            twSongTitle.setText(songArtist);

        }



    }


}
