package com.example.ruleta;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", "");
        TextView textUser = findViewById(R.id.textUser);
        textUser.setText(String.valueOf(nombreUsuario));

        // Recupera el valor actual de monedasTotales para el usuario o usa 100 como predeterminado
        int monedasTotales = prefs.getInt(nombreUsuario + "_monedas", 100);

        // Actualiza las monedasTotales
        TextView txtMonedasTotales = findViewById(R.id.txtMonedasTotales);
        if (txtMonedasTotales != null) {
            txtMonedasTotales.setText(String.valueOf(monedasTotales));
        }

        Button btnSalir = findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, MainActivity.class);
            startActivity(intent);
        });

        Button btnIniciar = findViewById(R.id.btnIniciarPartida);
        btnIniciar.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, Tirada.class);
            startActivity(intent);
        });

        Button btnReiniciarMonedas = findViewById(R.id.btnReiniciarMonedas);
        btnReiniciarMonedas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoConfirmacion();
            }
        });

        Button btnVerResultados = findViewById(R.id.btnResultados);
        btnVerResultados.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abre la actividad HistorialTiradasActivity
                Intent intent = new Intent(Menu.this, HistorialTiradasActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar la música cuando la aplicación esta en segundo plano
        Intent intent = new Intent(this, MusicaFondo.class);
        intent.setAction(MusicaFondo.ACTION_PAUSE);
        startService(intent);
    }


    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.txteTituloReiniciarMonedas))
                .setMessage(getString(R.string.txteMensajeReiniciarMonedas))
                .setPositiveButton(getString(R.string.txteAceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reiniciarMonedas();
                    }
                })
                .setNegativeButton(getString(R.string.txteCancelar), null)
                .show();
    }

    private void reiniciarMonedas() {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String nombreUsuario = prefs.getString("nombreUsuario", "");
        if (!nombreUsuario.isEmpty()) {
            editor.putInt(nombreUsuario + "_monedas", 100);
            editor.apply();
            Toast.makeText(this, getString(R.string.txteMonedasReiniciadas), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.txteErrorReiniciarMonedas), Toast.LENGTH_SHORT).show();
        }
        TextView txtMonedasTotales = findViewById(R.id.txtMonedasTotales);
        txtMonedasTotales.setText(String.valueOf(100));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar la música sólo si está habilitada en las preferencias
        if (PreferenciasUsuario.shouldPlayMusic(this)) {
            Intent intent = new Intent(this, MusicaFondo.class);
            intent.setAction(MusicaFondo.ACTION_PLAY);
            intent.putExtra(MusicaFondo.EXTRA_VOLUME, 1.0f);
            startService(intent);
        }
        // Actualizar las monedas totales al volver de la actividad Tirada
        actualizarMonedasTotales();
    }

    private void actualizarMonedasTotales() {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", null);
        int monedasTotales = prefs.getInt(nombreUsuario + "_monedas", 100); // Usa 100 como valor por defecto
        TextView txtTotalMonedas = findViewById(R.id.txtMonedasTotales);
        txtTotalMonedas.setText(String.valueOf(monedasTotales));
    }
}
