package com.example.buca.saxmusicplayer.api.common;

/**
 * Created by stojan.mitric on 6/15/2017.
 */

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP GET helper klasa
 */
public class HttpGet {

    private static final String TAG = "HttpGet";

    /**
     * Dobavlja podatke sa prosledjenog URL-a.
     * @param inUrl Prosledjeni URL.
     * @param query Prosledjeni upit. '?' + parametri se dodaje automatski, vrednosti parametara moraju biti encoded.
     * @return Vraća podatke iz prosleđenog URL-a.
     */
    public static String get(String inUrl, String query, boolean cached)
            throws IOException, Exception {
        return new String(getBytes(inUrl, query, cached));
    }

    /**
     * Dobavlja podatke sa prosledjenog URL-a.
     * @param inUrl Prosledjeni URL.
     * @param query Prosledjeni upit. '?' + parametri se dodaje automatski, vrednosti parametara moraju biti encoded.
     * @return Vraća niz bajtova iz prosleđenog URL-a.
     */
    public static byte[] getBytes(String inUrl, String query, boolean cached)
            throws IOException, Exception {
        final String formattedUrl = inUrl + (query.isEmpty() ? "" : ("?" + query));

        Log.d(TAG, "Formatted URL: " + formattedUrl);

        URL url = new URL(formattedUrl);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("User-Agent","OmniMusic/1.0-dev (http://www.omnirom.org)");
        urlConnection.setUseCaches(cached);
        urlConnection.setInstanceFollowRedirects(true);
        int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
        urlConnection.addRequestProperty("Cache-Control", "max-stale=" + maxStale);
        try {
            final int status = urlConnection.getResponseCode();
            // parsiranje statusa
            if (status == HttpURLConnection.HTTP_OK) {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                int contentLength = urlConnection.getContentLength();
                if (contentLength <= 0) {
                    // alociranje content lengtha 100KB.
                    contentLength = 100 * 1024;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream(contentLength);
                byte[] buffer = new byte[2048];
                BufferedInputStream bis = new BufferedInputStream(in);
                int read;

                //hedlovanje statusa responsa
                while ((read = bis.read(buffer, 0, buffer.length)) > 0) {
                    baos.write(buffer, 0, read);
                }
                return baos.toByteArray();
            } else if (status == HttpURLConnection.HTTP_NOT_FOUND) {
                // 404
                return new byte[]{};
            } else if (status == HttpURLConnection.HTTP_FORBIDDEN) {
                return new byte[]{};
            } else if (status == HttpURLConnection.HTTP_UNAVAILABLE) {
                throw new Exception();
            } else if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == 307 /* HTTP/1.1 TEMPORARY REDIRECT */
                    || status == HttpURLConnection.HTTP_SEE_OTHER) {
                // u slučaju redirekcije, prati novi URL
                final String followUrl = urlConnection.getHeaderField("Location");
                Log.e(TAG, "Redirected to: " +  followUrl);
                return getBytes(followUrl, "", cached);
            } else {
                Log.e(TAG, "Error when fetching: " + formattedUrl + " (" + urlConnection.getResponseCode() + ")");
                return new byte[]{};
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}