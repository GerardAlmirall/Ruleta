package com.example.ruleta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ruleta.DB.DBconexion;
import java.io.OutputStream;
import android.os.AsyncTask;


public class Resultado extends AppCompatActivity {
    private DBconexion dbConexion;
    private class SaveImageTask extends AsyncTask<Bitmap, Void, String> {
        private Context context;

        SaveImageTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                return context.getString(R.string.txthImagenGuardada);
            } catch (Exception e) {
                return context.getString(R.string.txthErrorGuardarImagen) + e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_resultado);
        dbConexion = new DBconexion(this);
        // Recupera datos pasados de la actividad Tirada
        Intent intent = getIntent();
        int monedasGanadas = intent.getIntExtra("monedasGanadas", 0);
        int monedasApostadas = intent.getIntExtra("monedasApostadas", 10);

        // Recupera el nombre de usuario de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", "");
        long usuarioId = obtenerUsuarioIdPorNombre(nombreUsuario);
        TextView textUser = findViewById(R.id.textUser);
        textUser.setText(String.valueOf(nombreUsuario));
        int monedasTotales = prefs.getInt(nombreUsuario + "_monedas", 100);
        TiradaClase nuevaTirada = new TiradaClase(
                0, // Pasamos 0 si el ID es autoincrementable
                monedasGanadas,
                intent.getIntExtra("premioSeleccionado", 0),
                intent.getIntExtra("monedasApostadas", 0),
                usuarioId,
                monedasTotales
        );

        // Guarda la nueva tirada en la base de datos
        DBconexion dbConexion = new DBconexion(this);
        dbConexion.insertarTirada(nuevaTirada);

        // Configura los elementos de la UI con los datos recuperados
        TextView txtGanado = findViewById(R.id.txtGanado);
        TextView txtHasGanado = findViewById(R.id.txtHasGanado);
        TextView txtHasApostado = findViewById(R.id.txtHasApostado);
        TextView txtMonedasApostadas = findViewById(R.id.txtMonedasApostadas);
        TextView txtTotalMonedas2 = findViewById(R.id.txtTotalMonedas2);
        TextView txtTotalMonedas = findViewById(R.id.txtMonedasTotales);

        txtGanado.setText(String.valueOf(monedasGanadas));
        txtMonedasApostadas.setText(String.valueOf(monedasApostadas));
        txtTotalMonedas2.setText(String.valueOf(monedasTotales));
        txtTotalMonedas.setText(String.valueOf(monedasTotales));

        if (monedasGanadas > 0) {
            txtHasGanado.setText(getString(R.string.txthHasGanado));

        } else {
            txtHasGanado.setText(getString(R.string.txthHasPerdido));

        }

        Button btnRetirarse = findViewById(R.id.btnRetirarse);
        btnRetirarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent para regresar al menú principal
                Intent menuIntent = new Intent(Resultado.this, Menu.class);

                // Limpia la pila de actividades y lleva al usuario al menú principal
                menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(menuIntent);
            }
        });

        //Botón para volver a tirar
        Button btnVolverTirar = findViewById(R.id.btnVolverTirar);
        btnVolverTirar.setOnClickListener(v -> {
            // Iniciar la actividad Tirada de nuevo
            Intent tiradaIntent = new Intent(Resultado.this, Tirada.class);
            startActivity(tiradaIntent);
        });
    }
    public long obtenerUsuarioIdPorNombre(String nombreUsuario) {
        SQLiteDatabase db = dbConexion.getReadableDatabase();
        Cursor cursor = db.query("Usuario", new String[]{"id"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("id");
            if (columnIndex != -1) {
                long id = cursor.getLong(columnIndex);
                cursor.close();
                return id;
            }
        }
        cursor.close();
        return -1;
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reanuda la música sólo si está habilitada en las preferencias
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
        // Pausa la música cuando la aplicación esta en segundo plano
        Intent intent = new Intent(this, MusicaFondo.class);
        intent.setAction(MusicaFondo.ACTION_PAUSE);
        startService(intent);
    }
    public void captureAndSaveDisplay() {

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);


        new SaveImageTask(this).execute(bitmap);
    }
    private boolean isScreenshotTaken = false;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isScreenshotTaken) {
            Intent intent = getIntent();
            int monedasGanadas = intent.getIntExtra("monedasGanadas", 0);
            if (monedasGanadas > 0) {
                captureAndSaveDisplay();
            }
            isScreenshotTaken = true;
        }
    }

}
