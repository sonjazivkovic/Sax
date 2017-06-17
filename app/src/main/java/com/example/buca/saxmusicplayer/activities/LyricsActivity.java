package com.example.buca.saxmusicplayer.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
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


import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static android.os.Build.VERSION_CODES.M;
import static org.apache.commons.lang3.StringUtils.substringBetween;

/**
 * Created by Stefan on 04/05/2017.
 */

public class LyricsActivity extends AppCompatActivity {

    private String songArtist;
    private String songTitle;

    private ChartLyricsClient client;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        changeLang();
        setContentView(R.layout.lyrics);

        Toolbar toolbar = (Toolbar) findViewById(R.id.lyrics_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.lyrics);

        TextView twSongTitle = (TextView) findViewById(R.id.lyrics_act_song_title);

        TextView twLyricsText = (TextView) findViewById(R.id.lyrics_act_song_lyrics);

        songArtist = DataHolder.getCurrentSong().getArtist();

        songTitle = DataHolder.getCurrentSong().getTitle();

        if(songArtist.equals("<unknown>") && !songTitle.equals("<unknown>")){
            if(songTitle.contains("-")) {
                String parts[] = songTitle.split("-");

                songArtist = parts[0];
                songTitle = parts[1];
            } else {
                twLyricsText.setText(R.string.song_title_is_unknown);
            }
        } else {
            twLyricsText.setText(R.string.song_title_is_unknown);
        }

        if(songTitle.equals("<unknown>")) {
            twLyricsText.setText(R.string.song_title_is_unknown);
        }



        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

            client = new ChartLyricsClient();

            String response = client.query(songArtist,songTitle);

            String lyrics = StringUtils.substringBetween(response, "<Lyric>", "</Lyric>");

            twLyricsText.setText(lyrics);

            twSongTitle.setText(songArtist);

        }



    }

    public void changeLang() {
        SharedPreferences sp = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        String languageToLoad = sp.getString("language_preference", "en"); // your language
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}
