package com.example.ruleta;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;

public class MusicaFondo extends Service {
    private MediaPlayer player;
    private AudioManager audioManager;
    private boolean isPausedInCall = false; // Flag para controlar la pausa por llamada

    public static final String ACTION_PAUSE = "com.example.ruleta.PAUSE";
    public static final String ACTION_PLAY = "com.example.ruleta.PLAY";
    public static final String ACTION_SET_VOLUME = "com.example.ruleta.SET_VOLUME";
    public static final String EXTRA_VOLUME = "VOLUME";
    public static final String EXTRA_MUSIC_URI = "MUSIC_URI";

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                // Se pierde el enfoque de audio de forma prolongada
                stopPlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Se pierde el enfoque de audio de forma temporal
                if (player != null && player.isPlaying()) {
                    player.pause();
                    isPausedInCall = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Permitir continuar reproduciendo pero con volumen reducido
                if (player != null && player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                // Se recupera el enfoque de audio después de una pérdida temporal
                if (player != null && isPausedInCall) {
                    player.setVolume(1.0f, 1.0f);
                    player.start();
                    isPausedInCall = false;
                }
                break;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, R.raw.musica_fondo);
        player.setLooping(true);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Solicitar audio focus
        int audioFocusResult = audioManager.requestAudioFocus(audioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // No se ganó el foco de audio, no reproducir música
            return START_NOT_STICKY;
        }

        if (intent != null) {
            handleIntent(intent);
        }
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (ACTION_PLAY.equals(action)) {
            playMusic(intent);
        } else if (ACTION_PAUSE.equals(action)) {
            pauseMusic();
        } else if (ACTION_SET_VOLUME.equals(action)) {
            setVolume(intent);
        }
    }

    private void playMusic(Intent intent) {
        if (intent.hasExtra(EXTRA_MUSIC_URI)) {
            Uri musicUri = Uri.parse(intent.getStringExtra(EXTRA_MUSIC_URI));
            prepareMediaPlayerFromUri(musicUri);
        } else if (player == null) {
            player = MediaPlayer.create(this, R.raw.musica_fondo);
            player.setLooping(true);
        }
        if (!player.isPlaying()) {
            player.start();
        }
        adjustVolume(intent);
    }
    private void adjustVolume(Intent intent) {
        if (intent.hasExtra(EXTRA_VOLUME)) {
            float volume = intent.getFloatExtra(EXTRA_VOLUME, 1.0f);
            player.setVolume(volume, volume);
        }
    }

    private void pauseMusic() {
        if (player != null && player.isPlaying()) {
            player.pause();
        }
    }

    private void setVolume(Intent intent) {
        if (player != null && intent.hasExtra(EXTRA_VOLUME)) {
            float volume = intent.getFloatExtra(EXTRA_VOLUME, 1.0f);
            player.setVolume(volume, volume);
        }
    }

    private void prepareMediaPlayerFromUri(Uri musicUri) {
        try {
            player.reset();
            player.setDataSource(getApplicationContext(), musicUri);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e("MusicaFondo", "Error setting data source", e);
        }
    }

    private void stopPlayer() {
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
            player.release();
            player = null;
        }
        audioManager.abandonAudioFocus(audioFocusChangeListener);
    }

    @Override
    public void onDestroy() {
        stopPlayer();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
