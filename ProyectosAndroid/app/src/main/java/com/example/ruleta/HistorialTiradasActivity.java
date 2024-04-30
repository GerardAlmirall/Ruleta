package com.example.ruleta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ruleta.DB.DBmanager;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class HistorialTiradasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistorialTiradasAdapter adapter;
    private DBmanager dbManager;

    private TextView txtTotalMonedas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_tiradas);
        inicializarVista();

        // Inicializar DBmanager
        dbManager = new DBmanager(this);

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener historial de tiradas del usuario actual
        long usuarioId = obtenerIdUsuarioActual();

        dbManager.obtenerHistorialDeUsuario(usuarioId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tiradas -> {
                    int cantidadTiradas = tiradas.size();
                    String mensaje = getString(R.string.txtaTiradas, cantidadTiradas);
                    Log.d("HistorialTiradas", mensaje);
                    if (!tiradas.isEmpty()) {
                        // Mostrar historial de tiradas en el RecyclerView
                        adapter = new HistorialTiradasAdapter(tiradas);
                        recyclerView.setAdapter(adapter);
                        recyclerView.setVisibility(View.VISIBLE); // Asegurar que el RecyclerView sea visible
                    } else {
                        // Mostrar un mensaje indicando que no hay historial disponible
                        Toast.makeText(this, getString(R.string.txtaHistorial), Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    Log.e("HistorialTiradas", "Error al obtener el historial de tiradas: " + throwable.getMessage());
                    // Manejar errores durante la obtención del historial de tiradas
                    Toast.makeText(this, getString(R.string.txtaError), Toast.LENGTH_SHORT).show();
                    throwable.printStackTrace(); // Imprimir el stack trace del error
                });
    }

    private void inicializarVista() {
        // Recuperar el nombre de usuario de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", null);
        TextView textUser = findViewById(R.id.textUser);
        textUser.setText(String.valueOf(nombreUsuario));

        int monedasTotales = prefs.getInt(nombreUsuario + "_monedas", 100);

        txtTotalMonedas = findViewById(R.id.txtMonedasTotales);
        txtTotalMonedas.setText(String.valueOf(monedasTotales));

        Button btnRetirarse = findViewById(R.id.btnRetirarse);
        btnRetirarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent para regresar al menú principal
                Intent menuIntent = new Intent(HistorialTiradasActivity.this, Menu.class);

                // Limpia la pila de actividades y lleva al usuario al menú principal
                menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(menuIntent);
            }
        });
    }

    // Método para obtener el ID del usuario actual
    private long obtenerIdUsuarioActual() {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        long userId = prefs.getLong("usuarioId", 0);
        Log.d("HistorialTiradasActivity", "Recuperado userId de SharedPreferences: " + userId);
        return userId;
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
        // Reanudar la música sólo si está habilitada en las preferencias
        if (PreferenciasUsuario.shouldPlayMusic(this)) {
            Intent intent = new Intent(this, MusicaFondo.class);
            intent.setAction(MusicaFondo.ACTION_PLAY);
            intent.putExtra(MusicaFondo.EXTRA_VOLUME, 1.0f);
            startService(intent);
        }
    }
}
