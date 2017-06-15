package com.example.buca.saxmusicplayer.api.chartlyrics;

/**
 * Created by stojan.mitric on 6/15/2017.
 *
 * ChartLyricsClient klasa za uspostavljanje konekcije ka ChartLyrics api -ju.
 */
import com.example.buca.saxmusicplayer.api.common.HttpGet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartLyricsClient {

    private static final String TAG = "ChartLyricsClient";
    private static final String BASE_URL = "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect";

    private static final Pattern PATTERN_LYRICS = Pattern.compile("<Lyric>(.*)</Lyric>", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern PATTERN_SONG = Pattern.compile("<LyricSong>(.*)</LyricSong>", Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern PATTERN_ARTIST = Pattern.compile("<LyricArtist>(.*)</LyricArtist>", Pattern.MULTILINE | Pattern.DOTALL);

    /**
     * getSongLyrics Metoda salje get request api-ju sa nazivom izvodjaca i nazivom pesme.
     */

    public static LyricsResponse getSongLyrics(String artist, String title) throws IOException, Exception {
        String lyricsXml = HttpGet.get(BASE_URL, "artist=" + URLEncoder.encode(artist, "UTF-8") + "&song=" + URLEncoder.encode(title, "UTF-8"), true);

        Matcher matcher_song = PATTERN_SONG.matcher(lyricsXml);
        Matcher matcher_artist = PATTERN_ARTIST.matcher(lyricsXml);
        Matcher matcher_lyrics = PATTERN_LYRICS.matcher(lyricsXml);

        LyricsResponse response = new LyricsResponse();

        if (matcher_lyrics.find()) {
            response.lyrics = matcher_lyrics.group(1);
        }

        if (matcher_artist.find()) {
            response.artist = matcher_artist.group(1);
        }

        if (matcher_song.find()) {
            response.title = matcher_song.group(1);
        }

        return response;
    }

    /**
     * Klasa koja sadrži podatke o autoru, nazivu i tekstu pesme.
     */

    public static class LyricsResponse {
        public String lyrics;
        public String artist;
        public String title;
    }

}
