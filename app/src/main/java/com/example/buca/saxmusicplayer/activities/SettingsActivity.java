package com.example.buca.saxmusicplayer.activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.providers.SongProvider;
import com.example.buca.saxmusicplayer.services.SaxMusicPlayerService;
import com.example.buca.saxmusicplayer.util.DataHolder;
import com.example.buca.saxmusicplayer.util.DatabaseContract;


/**
 * Created by Stefan on 14/05/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    private SaxMusicPlayerService saxMusicPlayerService;
    private boolean serviceBound = false;
    private AlertDialog scanDialog;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /*kada se aktivnost poveze sa servisom postavljamo referencu ka servisu kako bi mogli koristiti njegove metode*/
            SaxMusicPlayerService.SaxMusicPlayerBinder binder = (SaxMusicPlayerService.SaxMusicPlayerBinder) service;
            saxMusicPlayerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.settings);

        //Display the fragment as the main content
        SettingsFragment sf = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.preference_fragment_container, sf).commit();
        getFragmentManager().executePendingTransactions();

        ListPreference lp = (ListPreference) sf.findPreference("language_preference");
        lp.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Intent mainIntent = new Intent(MainActivity.Broadcast_UPDATE_UI_MAIN_ACTIVITY);
                mainIntent.putExtra(MainActivity.Broadcast_RESET_MAIN_ACTIVITY, true);
                sendBroadcast(mainIntent);
                finish();
                return true;
            }
        });

        Preference scanDevice = sf.findPreference("scan_device");
        scanDevice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(preference.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(preference.getContext());
                }
                scanDialog = builder.setTitle(R.string.scan_device)
                        .setMessage(R.string.long_operation)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new LongOperation().execute("scan_device");
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create();
                scanDialog.show();
                return true;
            }
        });

        Preference cleanDevice = sf.findPreference("clean_device");
        cleanDevice.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!serviceBound) {
            Intent playMusicIntent = new Intent(this, SaxMusicPlayerService.class);
            bindService(playMusicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        if(scanDialog != null)
            scanDialog.dismiss();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            /*Izabran jezik ce biti smesten u Shared preference fajl*/
            PreferenceManager pm = getPreferenceManager();
            pm.setSharedPreferencesName("language_preference");
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    private class LongOperation extends AsyncTask<String, Void, String>{

        private ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            if(params[0].equals("scan_device"))
                scanDeviceFunc();
            else
                cleanDeviceFunc();
            return null;
        }

        @Override
        protected void onPreExecute() {
            //dijalog sa progres barom
            progressDialog = new ProgressDialog(SettingsActivity.this);
            progressDialog.setTitle(R.string.please_wait);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
            //sprecavamo korisnika da klikce po aplikaciji dok se ne zavrsi skeniranje
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //privremeno disablujemo rotaciju
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }

        @Override
        protected void onPostExecute(String result) {
            //kada se zavrsi operacija sklanjamo progres bar i pustamo korisnika da klikce po ekranu
            progressDialog.dismiss();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            //ponovo palimo rotaciju
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        private void scanDeviceFunc(){
            ContentResolver musicResolver = getContentResolver();
            Uri songsUri = SongProvider.CONTENT_URI_SONGS;
            Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.YEAR};
            String selection = MediaStore.Audio.Media.DURATION + " > 150000";
            Cursor musicCursor = musicResolver.query(musicUri, projection, selection, null, null);
            boolean newSongsAvailable = false;

            if(musicCursor!=null && musicCursor.moveToFirst()){
                do {
                    //saving to db
                    String[] selectionArgs = {musicCursor.getString(0)};
                    Cursor songExistInDb = musicResolver.query(songsUri, null, DatabaseContract.SongTable.COLUMN_PATH + " = ?", selectionArgs, null, null);
                    if(songExistInDb == null || !songExistInDb.moveToFirst()) {
                        ContentValues cv = new ContentValues();
                        cv.put(DatabaseContract.SongTable.COLUMN_PATH, musicCursor.getString(0));
                        cv.put(DatabaseContract.SongTable.COLUMN_TITLE, musicCursor.getString(1));
                        cv.put(DatabaseContract.SongTable.COLUMN_ARTIST, musicCursor.getString(2));
                        cv.put(DatabaseContract.SongTable.COLUMN_ALBUM, musicCursor.getString(3));
                        cv.put(DatabaseContract.SongTable.COLUMN_YEAR, musicCursor.getInt(4));
                        musicResolver.insert(songsUri, cv);
                        newSongsAvailable = true;
                    }
                }
                while (musicCursor.moveToNext());
            }
            musicCursor.close();
            //ako je inicijalno bila ucitana lista svih pesama ponovo je ucitavamo u suprotnom nema potrebe zato sto nove pesme sigurno ne postoje u nekoj od korisnickih plejlista
            if(newSongsAvailable && DataHolder.getActivePlaylistId() == -1)
                saxMusicPlayerService.loadNewPlaylist(-1);
        }

        private void cleanDeviceFunc(){

        }
    }

}
