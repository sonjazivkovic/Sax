package com.example.buca.saxmusicplayer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.util.DataHolder;

import java.io.IOException;

/**
 * Created by Stefan on 04/06/2017.
 */

public class SaxMusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private IBinder iBinder = new SaxMusicPlayerBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        DataHolder.nextSong();
        play();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        Intent intent = new Intent(MainActivity.Broadcast_SONG_META_DATA);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player.isPlaying())
            player.stop();
        player.release();
        player = null;
    }

    public boolean isPlaying(){
        return player.isPlaying();
    }

    public void play(){
        player.reset();
        try {
            player.setDataSource(DataHolder.getCurrentSong().getPathToFile());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("FILE NOT FOUND", "Song is not on the path specified!", e);
        }
        player.prepareAsync();
    }

    public void pause(){
        player.pause();
    }

    public void resume(){
        player.start();
    }

    /*ff i bf prvo proveravaju da li je u toku reprodukcija, ukoliko nije samo se menja redosled pesme i postavlja se flag da treba resetovati plejer za pustanje nove*/
    public void fastForward(){
        DataHolder.nextSong();
        if(player.isPlaying())
            play();
        else
            DataHolder.setResetAndPrepare(true);
    }

    public void backForward(){
        DataHolder.previousSong();
        if(player.isPlaying())
            play();
        else
            DataHolder.setResetAndPrepare(true);
    }

    /*premotavanje stopira reprodukciju pa se zbog toga poziva start odmah nakon premotavanja*/
    public void seekTo(int seekPosition){
        player.seekTo(seekPosition);
        player.start();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }

    public class SaxMusicPlayerBinder extends Binder {
        public SaxMusicPlayerService getService(){
            return SaxMusicPlayerService.this;
        }
    }
}
