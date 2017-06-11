package com.example.buca.saxmusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.buca.saxmusicplayer.activities.DetailsAndRatingActivity;
import com.example.buca.saxmusicplayer.activities.LyricsActivity;
import com.example.buca.saxmusicplayer.activities.SettingsActivity;
import com.example.buca.saxmusicplayer.services.SaxMusicPlayerService;
import com.example.buca.saxmusicplayer.util.DataHolder;
import com.example.buca.saxmusicplayer.util.MathUtil;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String[] menuItems;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private SaxMusicPlayerService saxMusicPlayerService;
    private boolean serviceBound = false;
    public static String Broadcast_SONG_META_DATA = "com.example.buca.saxmusicplayer.broadcast.SONG_META_DATA";
    public static String Broadcast_RESET_SEEK_BAR = "com.example.buca.saxmusicplayer.broadcast.RESET_SEEK_BAR";
    private Handler runnableHandler = new Handler();
    private TextView movingTimeText;
    private TextView endTimeText;
    private SeekBar seekBar;
    private ImageButton playPause;
    private ImageButton playNextSong;
    private ImageButton playPrevSong;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /*kada se aktivnost poveze sa servisom postavljamo referencu ka servisu kako bi mogli koristiti njegove metode*/
            SaxMusicPlayerService.SaxMusicPlayerBinder binder = (SaxMusicPlayerService.SaxMusicPlayerBinder) service;
            saxMusicPlayerService = binder.getService();
            serviceBound = true;

            /*ukoliko se u plejeru nalazi pesma treba inicijalizovati seekbar - ovo je zbog toga sto se restartuje aktivnost pri rotaciji ekrana, da se ne izgubi seekbar progres*/
            if(!DataHolder.getResetAndPrepare())
                initSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private BroadcastReceiver songMetaDataUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra(Broadcast_RESET_SEEK_BAR, false))
                resetSeekBar(false);
            else
                initSeekBar();
        }
    };

    private Runnable updateSongTime = new Runnable() {
        @Override
        public void run() {
            int movingTime = saxMusicPlayerService.getCurrentPosition();
            int endTime = saxMusicPlayerService.getDuration();
            movingTimeText.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long)movingTime),
                    TimeUnit.MILLISECONDS.toSeconds((long)movingTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)movingTime))));
            SeekBar sb = (SeekBar)findViewById(R.id.seekBar);
            sb.setProgress(MathUtil.getPercentage(movingTime,endTime));
            runnableHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                         if(DataHolder.getResetAndPrepare()) {
                             saxMusicPlayerService.play();
                             DataHolder.setResetAndPrepare(false);
                         }else{
                             if(saxMusicPlayerService.isPlaying())
                                 saxMusicPlayerService.pause();
                             else
                                 saxMusicPlayerService.resume();
                         }
                     }
                 }
        );

        playNextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSeekBar(saxMusicPlayerService.isPlaying());
                saxMusicPlayerService.fastForward();
            }
        });

        playPrevSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetSeekBar(saxMusicPlayerService.isPlaying());
                saxMusicPlayerService.backForward();
            }
        });

        /*provera da li postoji pesma u plejeru, ukoliko ne postoji onemoguciti kornisniku da klikce po seekbaru*/
        if(DataHolder.getResetAndPrepare())
            seekBar.setEnabled(false);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    int endTime = saxMusicPlayerService.getDuration();
                    saxMusicPlayerService.seekTo(MathUtil.getNumberFromPercentage(progress, endTime));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                saxMusicPlayerService.pause();
                runnableHandler.removeCallbacks(updateSongTime);
                playPause.setEnabled(false);
                playNextSong.setEnabled(false);
                playPrevSong.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int endTime = saxMusicPlayerService.getDuration();
                saxMusicPlayerService.seekTo(MathUtil.getNumberFromPercentage(seekBar.getProgress(), endTime));
                runnableHandler.postDelayed(updateSongTime, 1000);
                playPause.setEnabled(true);
                playNextSong.setEnabled(true);
                playPrevSong.setEnabled(true);
            }
        });

        registerSongMetaDataUpdateReceiver();
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
        unregisterReceiver(songMetaDataUpdateReceiver);
        runnableHandler.removeCallbacks(updateSongTime);
    }

    private void initSeekBar() {
        int movingTime = saxMusicPlayerService.getCurrentPosition();
        int endTime = saxMusicPlayerService.getDuration();
        movingTimeText.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long)movingTime),
                TimeUnit.MILLISECONDS.toSeconds((long)movingTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)movingTime))));
        endTimeText.setText(String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long)endTime),
                TimeUnit.MILLISECONDS.toSeconds((long)endTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)endTime))));
        seekBar.setMax(MathUtil.getPercentage(endTime, endTime));
        seekBar.setProgress(MathUtil.getPercentage(movingTime, endTime));
        seekBar.setEnabled(true);
        runnableHandler.removeCallbacks(updateSongTime);
        runnableHandler.postDelayed(updateSongTime, 1000);
    }

    private void resetSeekBar(boolean isPlaying){
        movingTimeText.setText("00:00");
        endTimeText.setText("00:00");
        seekBar.setProgress(0);
        if(!isPlaying)
            seekBar.setEnabled(false);
        runnableHandler.removeCallbacks(updateSongTime);
    }

    private void registerSongMetaDataUpdateReceiver(){
        IntentFilter filter = new IntentFilter(Broadcast_SONG_META_DATA);
        registerReceiver(songMetaDataUpdateReceiver, filter);
    }
}