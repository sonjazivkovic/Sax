package com.example.buca.saxmusicplayer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.util.DataHolder;

import java.io.IOException;

/**
 * Created by Stefan on 04/06/2017.
 */

public class SaxMusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer player;
    private IBinder iBinder = new SaxMusicPlayerBinder();
    private AudioManager audioManager;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private boolean callOngoing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
        requestAudioFocus();
        callStateListener();
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
        DataHolder.setResetAndPrepare(false);
        Intent intent = new Intent(MainActivity.Broadcast_UPDATE_UI_MAIN_ACTIVITY);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player.isPlaying())
            player.stop();
        player.release();
        player = null;
        removeAudioFocus();
    }

    /*kada neka druga aplikacija zatrazi da pusta muziku moramo pauzirati ili zaustaviti nas plejer*/
    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange){
            /*kada dobijemo fokus pustamo plejer da svira*/
            case AudioManager.AUDIOFOCUS_GAIN:
                if(!player.isPlaying()){
                    player.start();
                }
                player.setVolume(1.0f, 1.0f);
                break;
            /*ako izgubimo fokus na duze vreme, koristi se neka druga aplikacija za muziku npr*/
            case AudioManager.AUDIOFOCUS_LOSS:
                if(player.isPlaying()) {
                    player.stop();
                    DataHolder.setResetAndPrepare(true);
                    Intent intent = new Intent(MainActivity.Broadcast_UPDATE_UI_MAIN_ACTIVITY);
                    intent.putExtra(MainActivity.Broadcast_RESET_SEEK_BAR, true);
                    sendBroadcast(intent);
                }
            /*ako izgubimo fokus na krace vreme*/
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if(player.isPlaying())
                    player.pause();
                break;
            /*ako izgubimo fokus na krace vreme, ali ne toliko bitno pa mozemo nastaviti da pustamo muziku sa nizim tonom*/
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(player.isPlaying())
                    player.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private void requestAudioFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    private void removeAudioFocus(){
        audioManager.abandonAudioFocus(this);
    }

    private void callStateListener(){
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state){
                    //postoji bar jedan poziv (zvoni telefon, poziv u toku, na cekanju,...)
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        //zvoni telefon - zaustavi reprodukciju
                    case TelephonyManager.CALL_STATE_RINGING:
                        player.pause();
                        callOngoing = true;
                        break;
                    //nema poziva - nastavi reprodukciju
                    case TelephonyManager.CALL_STATE_IDLE:
                        if(callOngoing) {
                            player.start();
                            callOngoing = false;
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void initPlayer(){
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
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
        DataHolder.setResetAndPrepare(true);
        if(player.isPlaying())
            play();
        else{
            Intent intent = new Intent(MainActivity.Broadcast_UPDATE_UI_MAIN_ACTIVITY);
            intent.putExtra(MainActivity.Broadcast_RESET_SEEK_BAR, true);
            sendBroadcast(intent);
        }
    }

    public void backForward(){
        DataHolder.previousSong();
        DataHolder.setResetAndPrepare(true);
        if(player.isPlaying())
            play();
        else{
            Intent intent = new Intent(MainActivity.Broadcast_UPDATE_UI_MAIN_ACTIVITY);
            intent.putExtra(MainActivity.Broadcast_RESET_SEEK_BAR, true);
            sendBroadcast(intent);
        }
    }

    /*premotavanje stopira reprodukciju pa se zbog toga poziva start odmah nakon premotavanja*/
    public void seekTo(int seekPosition){
        player.seekTo(seekPosition);
        player.start();
    }

    public int getDuration(){
        if(DataHolder.getResetAndPrepare())
            return -1;
        else
            return player.getDuration();
    }

    public int getCurrentPosition(){
        if(DataHolder.getResetAndPrepare())
            return -1;
        else
            return player.getCurrentPosition();
    }

    public class SaxMusicPlayerBinder extends Binder {
        public SaxMusicPlayerService getService(){
            return SaxMusicPlayerService.this;
        }
    }
}