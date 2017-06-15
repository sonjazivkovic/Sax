package com.example.buca.saxmusicplayer.api.chartlyrics;

/**
 * Created by stojan.mitric on 6/15/2017.
 *
 * ChartLyricsClient klasa za uspostavljanje konekcije ka ChartLyrics api -ju.
 */
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChartLyricsClient {

    private String API_URL = "http://api.chartlyrics.com/apiv1.asmx/SearchLyricDirect?";

    public String query( String artist, String title) {
        try {

            URL url = new URL(API_URL + "artist=" + artist + "&song=" + title);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                String response = stringBuilder.toString();
                return response;
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return "Unable to find lyrics";
        }
    }

}
