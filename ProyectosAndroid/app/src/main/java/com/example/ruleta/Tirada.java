package com.example.ruleta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class Tirada extends AppCompatActivity {
    private long DURACION_GIRO = 7000; // Duración del giro de la ruleta
    private ImageView ruleta;
    private EditText editTextApuesta;
    private int resultado;
    private int premioSeleccionado;
    private TextView txtTotalMonedas;

    private MediaPlayer au_giroRuleta, au_premio, au_perder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tirada);
        inicializarVista();

        au_giroRuleta = MediaPlayer.create(Tirada.this,R.raw.giro_ruleta);
        au_premio = MediaPlayer.create(Tirada.this,R.raw.premio);
        au_perder = MediaPlayer.create(Tirada.this,R.raw.perder);
    }

    private void inicializarVista() {
        // Recuperar el nombre de usuario de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", null);
        TextView textUser = findViewById(R.id.textUser);
        textUser.setText(String.valueOf(nombreUsuario));

        int monedasTotales = prefs.getInt(nombreUsuario + "_monedas", 100);

        ruleta = findViewById(R.id.Ruleta);
        editTextApuesta = findViewById(R.id.editTextNumber3);
        txtTotalMonedas = findViewById(R.id.txtMonedasTotales);
        txtTotalMonedas.setText(String.valueOf(monedasTotales));

        Button btnRetirarse = findViewById(R.id.btnRetirarse);
        btnRetirarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent para regresar al menú principal
                Intent menuIntent = new Intent(Tirada.this, Menu.class);

                // Limpia la pila de actividades y lleva al usuario al menú principal
                // Asegurándose de que solo una instancia de la actividad del menú esté en la pila
                menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(menuIntent);
            }
        });
        findViewById(R.id.btnTirar).setOnClickListener(v -> validarYgirarRuleta(nombreUsuario));
    }

    private void validarYgirarRuleta(String nombreUsuario) {
        String valorApuesta = editTextApuesta.getText().toString();
        if (!valorApuesta.isEmpty()) {
            try {
                int apuesta = Integer.parseInt(valorApuesta);
                int monedasTotales = obtenerMonedasTotales(nombreUsuario);
                if (apuesta >= 10 && apuesta <= monedasTotales) {
                    premioSeleccionado = new Random().nextInt(8) + 1;
                    girarRuleta(premioSeleccionado, nombreUsuario, apuesta);
                } else {
                    Toast.makeText(this, getString(R.string.txtiApuestaInvalida, monedasTotales), Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.txtiNumeroValido, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.txtiIntroduceValorApuesta, Toast.LENGTH_LONG).show();
        }
    }


    private void girarRuleta(int premioSeleccionado, String nombreUsuario, int apuesta) {
        int anguloPorPremio = 45; // Grados que ocupa cada premio en la ruleta
        int vueltaCompleta = 360; // Grados en una vuelta completa
        int vueltasExtra = 5; // Número de vueltas extra para añadir al giro

        int anguloFinal = (vueltasExtra * vueltaCompleta) + ((premioSeleccionado - 1) * anguloPorPremio);

        RotateAnimation rotateAnimation = new RotateAnimation(0, anguloFinal,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(DURACION_GIRO);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new LinearInterpolator());

        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (au_giroRuleta != null) {
                    au_giroRuleta.start(); // Iniciar la reproducción del sonido
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (au_giroRuleta != null) {
                    au_giroRuleta.pause(); // Pausar el sonido al finalizar la animación
                    au_giroRuleta.seekTo(0); // Opcional, reiniciar el sonido a su inicio
                }
                // Reproducir sonido de premio o pérdida
                if (premioSeleccionado == 1 || premioSeleccionado == 2 || premioSeleccionado == 5 || premioSeleccionado == 7) {
                    if (au_premio != null) {
                        au_premio.start();
                    }
                } else {
                    if (au_perder != null) {
                        au_perder.start();
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        calcularPremio(premioSeleccionado, nombreUsuario, apuesta);

                        String mensaje;
                        switch (premioSeleccionado) {
                            case 1: mensaje = getString(R.string.txtiEnhorabuena); break;
                            case 2: mensaje = getString(R.string.txtiBienHecho); break;
                            case 3: mensaje = getString(R.string.txtiMalaSuerte); break;
                            case 4: mensaje = getString(R.string.txtiQuiebra); break;
                            case 5: mensaje = getString(R.string.txtiNoEstaMal); break;
                            case 6: mensaje = getString(R.string.txtiPudoSerPeor); break;
                            case 7: mensaje = getString(R.string.txtiGenial); break;
                            case 8: mensaje = getString(R.string.txtiEsAlgo); break;
                            default: mensaje = getString(R.string.txtiAlgoHaIdoMal);
                        }

                        mostrarMensajeResultado(mensaje);
                        mostrarResultado(apuesta);
                    }
                }, 2000);
            }


            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        ruleta.startAnimation(rotateAnimation);
    }

    private void calcularPremio(int premioSeleccionado, String nombreUsuario, int apuesta) {
        double multiplicador = 0;
        switch (premioSeleccionado) {
            case 1:
                multiplicador = 10;
                break;
            case 2:
                multiplicador = 2;
                break;
            case 3:
                multiplicador = 0.2;
                break;
            case 4:
                multiplicador = 0;
                break;
            case 5:
                multiplicador = 1.5;
                break;
            case 6:
                multiplicador = 0.5;
                break;
            case 7:
                multiplicador = 5;
                break;
            case 8:
                multiplicador = 0.666;
                break;
            default:
        }
        resultado = (int) (apuesta * multiplicador) - apuesta;
        int monedasTotales = obtenerMonedasTotales(nombreUsuario) + resultado;
        actualizarMonedasTotales(nombreUsuario, monedasTotales);
    }

    private int obtenerMonedasTotales(String nombreUsuario) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        return prefs.getInt(nombreUsuario + "_monedas", 100);
    }

    private void actualizarMonedasTotales(String nombreUsuario, int nuevasMonedas) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(nombreUsuario + "_monedas", nuevasMonedas);
        editor.apply();
        txtTotalMonedas.setText(String.valueOf(nuevasMonedas));
    }

    private void mostrarMensajeResultado(String mensaje) {
        runOnUiThread(() -> Toast.makeText(Tirada.this, mensaje, Toast.LENGTH_SHORT).show());
    }

    private void mostrarResultado(int apuesta) {

        Intent intent = new Intent(Tirada.this, Resultado.class);
        intent.putExtra("monedasGanadas", resultado);
        intent.putExtra("monedasApostadas", apuesta);
        intent.putExtra("premioSeleccionado", premioSeleccionado);

        startActivity(intent);
    }
    @Override
    protected void onDestroy() {
        if (au_giroRuleta != null) {
            au_giroRuleta.release();
            au_giroRuleta = null;
        }
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar la música sólo si está habilitada en las preferencias
        if (PreferenciasUsuario.shouldPlayMusic(this)) {
            Intent intent = new Intent(this, MusicaFondo.class);
            intent.setAction(MusicaFondo.ACTION_PLAY);
            intent.putExtra(MusicaFondo.EXTRA_VOLUME, 0.3f);
            startService(intent);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Pausar la música cuando la aplicación esta en segundo plano
        Intent intent = new Intent(this, MusicaFondo.class);
        intent.setAction(MusicaFondo.ACTION_PAUSE);
        startService(intent);
    }

}