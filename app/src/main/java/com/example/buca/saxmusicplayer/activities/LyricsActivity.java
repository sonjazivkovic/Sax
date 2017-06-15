package com.example.buca.saxmusicplayer.activities;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.api.chartlyrics.ChartLyricsClient;
import com.example.buca.saxmusicplayer.util.DataHolder;

/**
 * Created by Stefan on 04/05/2017.
 */

public class LyricsActivity extends AppCompatActivity {

    private TextView lyricsText;

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

        client = new ChartLyricsClient();


            songArtist = DataHolder.getCurrentSong().getArtist();

            songTitle = DataHolder.getCurrentSong().getTitle();

            TextView twSongTitle = (TextView) findViewById(R.id.lyrics_act_song_title);

            TextView twLyricsText = (TextView) findViewById(R.id.lyrics_act_song_lyrics);

        //pronalazenje lyrics-a
        if(!songArtist.equals("<unknown>") && !songTitle.equals("<unknown>")) {

            /**
             * Ispisuje autora i naziv pesme.
             */
            try {
                twSongTitle.setText(client.getSongLyrics(songArtist, songTitle).artist + " - " + client.getSongLyrics(songArtist, songTitle).title);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /**
             * Ispisuje tekst pesme.
             */
            try {
                twLyricsText.setText(client.getSongLyrics(songArtist, songTitle).lyrics);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            String[] parts = songTitle.split("-");

            try {
                twSongTitle.setText(songTitle);
            } catch (Exception e) {
                e.printStackTrace();
            }

            /**
             * Ispisuje tekst pesme.
             */
            try {
                twLyricsText.setText(client.getSongLyrics(parts[0], parts[1]).lyrics);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
