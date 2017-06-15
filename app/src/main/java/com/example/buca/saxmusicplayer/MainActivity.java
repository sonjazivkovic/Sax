package com.example.buca.saxmusicplayer;

import com.example.buca.saxmusicplayer.activities.AboutActivity;
import com.example.buca.saxmusicplayer.activities.DetailsAndRatingActivity;
import com.example.buca.saxmusicplayer.activities.LyricsActivity;
import com.example.buca.saxmusicplayer.activities.SettingsActivity;
import com.example.buca.saxmusicplayer.services.SaxMusicPlayerService;
import com.example.buca.saxmusicplayer.util.DataHolder;
import com.example.buca.saxmusicplayer.util.MathUtil;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String[] menuItems;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private SaxMusicPlayerService saxMusicPlayerService;
    private boolean serviceBound = false;
    public static String Broadcast_UPDATE_UI_MAIN_ACTIVITY = "com.example.buca.saxmusicplayer.broadcast.UPDATE_UI_MAIN_ACTIVITY";
    public static String Broadcast_RESET_SEEK_BAR = "com.example.buca.saxmusicplayer.broadcast.RESET_SEEK_BAR";
    public static String Broadcast_RESET_MAIN_ACTIVITY = "com.example.buca.saxmusicplayer.broadcast.RESET_MAIN_ACTIVITY";
    public static String Broadcast_UPDATE_SONG_INFO = "com.example.buca.saxmusicplayer.broadcast.UPDATE_SONG_INFO";
    public static String Broadcast_INIT_SEEK_BAR = "com.example.buca.saxmusicplayer.broadcast.INIT_SEEK_BAR";
    public static String Broadcast_SONG_PAUSE = "com.example.buca.saxmusicplayer.broadcast.SONG_PAUSE";
    public static String Broadcast_SONG_RESUME = "com.example.buca.saxmusicplayer.broadcast.SONG_RESUME";
    private Handler runnableHandler = new Handler();
    private TextView movingTimeText;
    private TextView endTimeText;
    private SeekBar seekBar;
    private ImageButton playPause;
    private ImageButton playNextSong;
    private ImageButton playPrevSong;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE_PHONE = 123;

    private GridView gridView;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /*kada se aktivnost poveze sa servisom postavljamo referencu ka servisu kako bi mogli koristiti njegove metode*/
            SaxMusicPlayerService.SaxMusicPlayerBinder binder = (SaxMusicPlayerService.SaxMusicPlayerBinder) service;
            saxMusicPlayerService = binder.getService();
            serviceBound = true;

            /*ukoliko se u plejeru nalazi pesma treba inicijalizovati seekbar - ovo je zbog toga sto se restartuje aktivnost pri rotaciji ekrana, da se ne izgubi seekbar progres*/
            if(!DataHolder.getResetAndPrepare()) {
                initSeekBar(saxMusicPlayerService.isPlaying());
            }
            //isto i za dugme, moramo ovde proveriti zato sto pre ovoga ne postoji saxMusicPlayerService
            if(saxMusicPlayerService.isPlaying()) {
                playPause.setImageResource(R.drawable.main_pause_icon);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private BroadcastReceiver uiUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(Broadcast_RESET_SEEK_BAR, false)){
                resetSeekBar();
            }
            if(intent.getBooleanExtra(Broadcast_RESET_MAIN_ACTIVITY, false)) {
                MainActivity.this.recreate();
            }
            if(intent.getBooleanExtra(Broadcast_UPDATE_SONG_INFO, false)){
                setSongName();
            }
            if(intent.getBooleanExtra(Broadcast_SONG_PAUSE, false)){
                runnableHandler.removeCallbacks(updateSongTime);
                playPause.setImageResource(R.drawable.play);
            }
            if(intent.getBooleanExtra(Broadcast_SONG_RESUME, false)){
                initSeekBar(true);
                playPause.setImageResource(R.drawable.main_pause_icon);
            }
        }
    };

    private Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
            int movingTime = saxMusicPlayerService.getCurrentPosition();
            int endTime = saxMusicPlayerService.getDuration();
            if(movingTime != -1 && endTime != -1) {
                movingTimeText.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) movingTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) movingTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) movingTime))));
                SeekBar sb = (SeekBar) findViewById(R.id.seekBar);
                sb.setProgress(MathUtil.getPercentage(movingTime, endTime));
            }
            runnableHandler.postDelayed(this, 1000);
            Log.e("Hello","From the other thread");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        changeLang();
        setContentView(R.layout.activity_main);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        menuItems = new String[] { getString(R.string.all_songs), getString(R.string.default_playlist), getString(R.string.choose_playlist) };
        drawerList = (ListView) findViewById(R.id.left_drawer);
        drawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItems));

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View view) {
                super.onDrawerOpened(view);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.app_name);

        movingTimeText = (TextView)findViewById(R.id.movingTime);
        endTimeText = (TextView)findViewById(R.id.endTime);
        seekBar = (SeekBar)findViewById(R.id.seekBar);

        playPause = (ImageButton)findViewById(R.id.button_play_pause);

        playNextSong = (ImageButton)findViewById(R.id.button_next);
        playPrevSong = (ImageButton)findViewById(R.id.button_prev);
        playPause.setOnClickListener(
                new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         play_pause();
                     }
                 }
        );

        playNextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saxMusicPlayerService.fastForward();
            }
        });

        playPrevSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saxMusicPlayerService.backForward();
            }
        });

        /*provera da li postoji pesma u plejeru, ukoliko ne postoji onemoguciti kornisniku da klikce po seekbaru*/
        if(DataHolder.getResetAndPrepare())
            seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                saxMusicPlayerService.pause();
                playPause.setEnabled(false);
                playNextSong.setEnabled(false);
                playPrevSong.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int endTime = saxMusicPlayerService.getDuration();
                saxMusicPlayerService.seekTo(MathUtil.getNumberFromPercentage(seekBar.getProgress(), endTime));
                playPause.setEnabled(true);
                playNextSong.setEnabled(true);
                playPrevSong.setEnabled(true);
            }
        });

        setSongName();
        registerUiUpdateReceiver();
    }

    public void play_pause() {
        if(DataHolder.getResetAndPrepare()) {
            saxMusicPlayerService.play();
        }else{
            if(saxMusicPlayerService.isPlaying()) {
                saxMusicPlayerService.pause();
            } else {
                saxMusicPlayerService.resume();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!serviceBound) {
            Intent playMusicIntent = new Intent(this, SaxMusicPlayerService.class);
            startService(playMusicIntent);
            bindService(playMusicIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()){
            case R.id.action_lyrics:
                Intent lyricsIntent = new Intent(MainActivity.this, LyricsActivity.class);
                startActivity(lyricsIntent);
                return true;
            case R.id.action_rating_details:
                Intent detailsIntent = new Intent(MainActivity.this, DetailsAndRatingActivity.class);
                startActivity(detailsIntent);
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*proverava se da li se aktivnost unistava ili se samo menja konfiguracija*/
        if(isFinishing() && serviceBound) {
            unbindService(serviceConnection);
            saxMusicPlayerService.stopSelf();
        }else if(isChangingConfigurations() && serviceBound){
            unbindService(serviceConnection);
        }
        unregisterReceiver(uiUpdateReceiver);
        runnableHandler.removeCallbacks(updateSongTime);
    }

    private void initSeekBar(boolean runUIUpdateThread) {
        int movingTime = saxMusicPlayerService.getCurrentPosition();
        int endTime = saxMusicPlayerService.getDuration();
        if(movingTime != -1 && endTime != -1) {
            movingTimeText.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) movingTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) movingTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) movingTime))));
            endTimeText.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) endTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) endTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) endTime))));
            seekBar.setMax(MathUtil.getPercentage(endTime, endTime));
            seekBar.setProgress(MathUtil.getPercentage(movingTime, endTime));
            seekBar.setEnabled(true);
        }
        if(runUIUpdateThread) {
            runnableHandler.removeCallbacks(updateSongTime);
            runnableHandler.postDelayed(updateSongTime, 1000);
        }
    }

    /*Ovde citamo iz Shared preferences fajla koji je jezik izabran od strane korisnika, i koristimo taj jezik pri pokretanju Main aktivnosti*/
    public void changeLang() {
        SharedPreferences sp = getSharedPreferences("language_preference", MODE_PRIVATE);
        String languageToLoad = sp.getString("language_preference", "en"); // your language
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

    }

    private void resetSeekBar() {
        runnableHandler.removeCallbacks(updateSongTime);
        movingTimeText.setText("00:00");
        endTimeText.setText("00:00");
        seekBar.setProgress(0);
        seekBar.setEnabled(false);
    }

    private void registerUiUpdateReceiver(){
        IntentFilter filter = new IntentFilter(Broadcast_UPDATE_UI_MAIN_ACTIVITY);
        registerReceiver(uiUpdateReceiver, filter);
    }

    /*Ovim proveravamo da li uredjaj dozvoljava citanje sa eksterne kartice i stanja telefona nasoj aplikaciji*/
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                    MY_PERMISSIONS_REQUEST_READ_STORAGE_PHONE);
                 }
    }

    /*Ovde proveravamo da li smo dobili dozvolu, za sada sam samo napisala poruku i za slucaj da jesmo i za slucaj da nismo*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE_PHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.read_storage_phone_approved), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.read_storage_phone_denied), Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    private void setSongName() {

        TextView tv1 = (TextView)findViewById(R.id.songName);

        tv1.setText(DataHolder.getCurrentSong().getTitle());
    }

}