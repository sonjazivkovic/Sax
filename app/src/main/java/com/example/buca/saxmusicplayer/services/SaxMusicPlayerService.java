package com.example.buca.saxmusicplayer.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.buca.saxmusicplayer.MainActivity;
import com.example.buca.saxmusicplayer.R;
import com.example.buca.saxmusicplayer.util.DataHolder;

import java.io.IOException;

import static com.example.buca.saxmusicplayer.R.string.welcome_text;

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

    public static final String ACTION_PLAY = "com.example.buca.saxmusicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.buca.saxmusicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.buca.saxmusicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.buca.saxmusicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.buca.saxmusicplayer.ACTION_STOP";

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;

    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 1234;

    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initPlayer();
        requestAudioFocus();
        callStateListener();

    }

    /*Ova metoda se poziva kad aktivnost posalje zahtev servisu za pokretanje.
      Inicijalizujemo media sesiju i plejer
    */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }

        }
        /*Ovde izvrsavamo zadatu akciju pritiskom na odredjeno dugmence*/
        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
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
        removeNotification();
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
        createNotification(PlaybackStatus.PLAYING);

    }

    public void pause(){
        player.pause();
        createNotification(PlaybackStatus.PAUSED);
    }

    public void resume(){
        player.start();
        createNotification(PlaybackStatus.PLAYING);
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

    /*Kreiramo notifikaciju, i pozivamo je pri prvom pustanju pesme*/
    public void createNotification(PlaybackStatus playbackStatus) {
        PendingIntent play_pauseAction = null;
        //inicijalizujemo ikonicu dugmenceta za pustanje pesme na notifikaciji na 'pause'
        int notificationAction = android.R.drawable.ic_media_pause;
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //i saljemo u playbackAction argument 1, sto oznacava akciju za pauzu
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            //ako je pauzirana pesma onda stavljamo ikonicu dugmenceta na 'play'
            notificationAction = android.R.drawable.ic_media_play;
            //i saljemo u playbackAction argument 0, sto oznacava akciju za play
            play_pauseAction = playbackAction(0);
        }
        //Stilizujemo notifikaciju, ubacujemo ikonicu aplikacije, dugmice za upravljanje, i akcije koje se dogode na njihov klik
        NotificationCompat.Builder mBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.mptrames)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2))
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.welcome_text))
                //playbackAction arument 3 oznacava pustanje prethodne pesme
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                //playbackAction arument 2 oznacava pustanje sledece pesme
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        Intent intent = new Intent(this, MainActivity.class);
        //pritiskom na notifikaciju vracamo se u Main aktivnost
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, mBuilder.build());
    }


    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, SaxMusicPlayerService.class);
        switch(actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Sledeca pesma
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Prethodna pesma
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    //Inicijalizujemo media sesiju da bi se sinhronizovale komande sa notifikacije i u samoj app
    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return;

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
      //  transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        /*Ovde bi trebalo da se ispisuju podaci o pesmi na notifikaciji*/
       // updateSongInfo();

    }

    /*U zavisnoti od toga koji smo argument prosledili, ovde odredjujemo koja ce se akcija dogoditi pritiskom
    na dugmence na notifikaciji
   */
    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            if(DataHolder.getResetAndPrepare()) {
                this.play();
            }else{
                this.resume();
            }
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            this.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
           this.fastForward();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
           this.backForward();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
          // this.stop();
        }
    }

    /*Ovom metodom uklanjamo notifikaciju, a pozivamo je na samo gasenje aplikacije*/
    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


}
