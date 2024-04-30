package com.example.ruleta;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Spinner;
import android.view.MotionEvent;
import java.util.Locale;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;

public class Opciones extends AppCompatActivity {
    private boolean userHasInteracted = false;
    private Switch switchMusica;
    private Button btnSelectMusica;
    private boolean isReiniciando = false;  // Variable para controlar reinicios

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);

        // Obtener referencias de la UI
        switchMusica = findViewById(R.id.switchMusic);
        btnSelectMusica = findViewById(R.id.btnSelectMusic);
        Button btnVolver = findViewById(R.id.btnVolver);

        // Configurar el estado inicial del Switch basado en la preferencia guardada
        switchMusica.setChecked(PreferenciasUsuario.shouldPlayMusic(this));

        // Manejar cambios en el Switch
        switchMusica.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenciasUsuario.saveMusicPreference(this, isChecked);
            Intent intent = new Intent(this, MusicaFondo.class);
            if (isChecked) {
                intent.setAction(MusicaFondo.ACTION_PLAY);
            } else {
                intent.setAction(MusicaFondo.ACTION_PAUSE);
            }
            startService(intent);
        });

        btnSelectMusica.setOnClickListener(v -> openFileChooser());
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(Opciones.this, MainActivity.class);
            startActivity(intent);
        });

        Button btnEnglish = findViewById(R.id.buttonEnglish);
        Button btnSpanish = findViewById(R.id.buttonSpanish);
        Button btnCatalan = findViewById(R.id.buttonCatalan);

        int color = Color.parseColor("#FFB800");
        btnEnglish.setBackgroundColor(color);
        btnSpanish.setBackgroundColor(color);
        btnCatalan.setBackgroundColor(color);

        // Set onClickListeners for buttons
        btnEnglish.setOnClickListener(v -> cambiarIdioma(new Locale("en")));
        btnSpanish.setOnClickListener(v -> cambiarIdioma(new Locale("es")));
        btnCatalan.setOnClickListener(v -> cambiarIdioma(new Locale("ca")));
    }

    private void cambiarIdioma(Locale locale) {
        if (isReiniciando) {
            return; // Si ya estamos reiniciando, no hacer nada
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        isReiniciando = true; // Marcar que estamos reiniciando
        Intent intent = new Intent(this, getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedMusicUri = data.getData();
            if (selectedMusicUri != null) {
                playMusicFromUri(selectedMusicUri);
            }
        }
    }

    private void playMusicFromUri(Uri musicUri) {
        Intent serviceIntent = new Intent(this, MusicaFondo.class);
        serviceIntent.setAction(MusicaFondo.ACTION_PLAY);
        serviceIntent.putExtra(MusicaFondo.EXTRA_MUSIC_URI, musicUri.toString());
        startService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar la música cuando la aplicación esta en segundo plano
        Intent intent = new Intent(this, MusicaFondo.class);
        intent.setAction(MusicaFondo.ACTION_PAUSE);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isReiniciando = false; // Resetear la bandera al reanudar la actividad
        // Reanudar la música sólo si está habilitada en las preferencias
        if (PreferenciasUsuario.shouldPlayMusic(this)) {
            Intent intent = new Intent(this, MusicaFondo.class);
            intent.setAction(MusicaFondo.ACTION_PLAY);
            intent.putExtra(MusicaFondo.EXTRA_VOLUME, 1.0f);
            startService(intent);
        }
    }
}
