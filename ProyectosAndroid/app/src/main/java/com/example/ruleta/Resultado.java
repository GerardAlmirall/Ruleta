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
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import android.provider.CalendarContract;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Resultado extends AppCompatActivity {
    private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID = 1234;
    private static final int REQUEST_CODE_PERMISSIONS = 123;
    private static final String[] NECESSARY_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
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

    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> missingPermissions = new ArrayList<>();
            for (String permission : NECESSARY_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(permission);
                }
            }

            if (!missingPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    // Method invoked when permissions are granted or denied
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    String messageGranted = getString(R.string.txtRSiPermiso, permissions[i]);
                    Log.d("Permisos", messageGranted);
                    Toast.makeText(this, messageGranted, Toast.LENGTH_SHORT).show();
                } else {
                    String messageDenied = getString(R.string.txtRNoPermiso, permissions[i]);
                    Log.e("PermisosError", messageDenied);
                    Toast.makeText(this, messageDenied, Toast.LENGTH_SHORT).show();
                }
            }
        }
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

        // Recupera el nombreUsuario de Preferencias
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", "");
    }
    private void guardarEventoEnCalendario(String nombreUsuario, int monedasGanadas) {
        long currentTimeMillis = System.currentTimeMillis();
        long calID = obtenerIdCalendarioPorDefecto();

        if (calID == -1) {
            Log.e("CalendarioError", "No se ha localizado ningún ID válido");
            Toast.makeText(this, "No valid calendar found", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, currentTimeMillis);
        values.put(CalendarContract.Events.DTEND, currentTimeMillis + 3600000); // 1 hour later
        values.put(CalendarContract.Events.TITLE, getString(R.string.event_title, nombreUsuario));
        values.put(CalendarContract.Events.DESCRIPTION, getString(R.string.event_description, nombreUsuario, monedasGanadas));
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

        Uri uri = getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri != null) {
            Log.d("Calendario", "Evento añadido al calendario con URI: " + uri.toString());
            Toast.makeText(this, R.string.event_added_to_calendar, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("CalendarioError", "Error añadiendo evento al calendario");
            Toast.makeText(this, R.string.error_adding_event_to_calendar, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Intenta obtener el ID del calendario por defecto.
     */
    private long obtenerIdCalendarioPorDefecto() {
        Cursor cursor = getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                new String[]{CalendarContract.Calendars._ID},
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                CalendarContract.Calendars._ID + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return id;
        }
        return -1; // No se ha localizado calendario
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
                // Recupera el nombreUsuario de Preferencias
                SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
                String nombreUsuario = prefs.getString("nombreUsuario", "");

                // Comprueba si se tienen permisos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    // Intenta añadir evento
                    guardarEventoEnCalendario(nombreUsuario, monedasGanadas);
                } else {
                    // Permiso denegado
                    Toast.makeText(this, "WRITE_CALENDAR permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            isScreenshotTaken = true;
        }
    }
}
