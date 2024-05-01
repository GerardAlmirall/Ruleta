package com.example.ruleta.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import com.example.ruleta.TiradaClase;



import io.reactivex.rxjava3.core.Observable;

public class DBmanager {
    public DBconexion dbConexion;

    public DBmanager(Context context) {
        this.dbConexion = new DBconexion(context);
    }

    public long verificarEInsertarUsuario(String nombreUsuario, int monedasTotales, String ubicacion) {
        SQLiteDatabase db = dbConexion.getWritableDatabase();

        // Intenta encontrar el usuario por su nombre
        Cursor cursor = db.query("Usuario", new String[]{"id"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);

        long id;
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id"); // Obtén el índice de la columna
            if (idIndex != -1) { // Verifica que el índice sea válido
                id = cursor.getLong(idIndex); // Usa el índice para obtener el valor de la columna
                Log.d("DBmanager", "Usuario existente encontrado con ID: " + id);

                // Actualizar las monedas totales del usuario
                ContentValues updateValues = new ContentValues();
                updateValues.put("monedasTotales", monedasTotales);
                db.update("Usuario", updateValues, "id = ?", new String[]{String.valueOf(id)});
            } else {
                // Maneja el caso en que la columna "id" no exista, si es necesario
                Log.e("DBmanager", "La columna 'id' no fue encontrada en el resultado de la consulta.");
                throw new IllegalStateException("La columna 'id' no fue encontrada en el resultado de la consulta.");
            }
        } else {
            // Si el usuario no existe, inserta uno nuevo
            Log.d("DBmanager", "Usuario no encontrado, procediendo a insertar nuevo usuario: " + nombreUsuario);
            ContentValues values = new ContentValues();
            values.put("nombreUsuario", nombreUsuario);
            values.put("monedasTotales", monedasTotales); // Valor de monedas totales proporcionado

            id = db.insert("Usuario", null, values);

            if (id == -1) {
                Log.e("DBmanager", "Error al insertar nuevo usuario: " + nombreUsuario);
            } else {
                Log.d("DBmanager", "Nuevo usuario insertado correctamente con ID: " + id);
            }
        }

        cursor.close(); // Asegúrate de cerrar el cursor después de usarlo
        return id;
    }
   /*
public List<Usuario> obtenerUsuariosOrdenadosPorMonedas() {
    List<Usuario> usuarios = new ArrayList<>();
    SQLiteDatabase db = dbConexion.getReadableDatabase();

    String[] projection = {
            "id",
            "nombreUsuario",
            "monedasTotales"
    };

    String orderBy = "monedasTotales DESC"; // Ordenar por monedasTotales de forma descendente

    Cursor cursor = db.query(
            "Usuario",
            projection,
            null,
            null,
            null,
            null,
            orderBy);

    while (cursor.moveToNext()) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        String nombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("nombreUsuario"));
        int monedasTotales = cursor.getInt(cursor.getColumnIndexOrThrow("monedasTotales"));

        usuarios.add(new Usuario((int)id, nombreUsuario, monedasTotales));
    }
    cursor.close();

    return usuarios;
}
*/



    public Observable<List<TiradaClase>> obtenerHistorialDeUsuario(long usuarioId) {
        return Observable.create(emitter -> {
            List<TiradaClase> historial = new ArrayList<>();
            SQLiteDatabase db = dbConexion.getReadableDatabase();

            String[] projection = {
                    "id",
                    "resultado",
                    "premioSeleccionado",
                    "apuesta",
                    "usuarioId",
                    "monedasTotales"
            };

            String selection = "usuarioId = ?";
            String[] selectionArgs = {String.valueOf(usuarioId)};
            Log.d("DBmanager", "Iniciando consulta de historial para usuarioId: " + usuarioId);
            Cursor cursor = db.query(
                    "Tirada",
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int resultado = cursor.getInt(cursor.getColumnIndexOrThrow("resultado"));
                int premioSeleccionado = cursor.getInt(cursor.getColumnIndexOrThrow("premioSeleccionado"));
                int apuesta = cursor.getInt(cursor.getColumnIndexOrThrow("apuesta"));
                long userId = cursor.getLong(cursor.getColumnIndexOrThrow("usuarioId"));
                int monedasTotales = cursor.getInt(cursor.getColumnIndexOrThrow("monedasTotales"));

                historial.add(new TiradaClase(id, resultado, premioSeleccionado, apuesta, userId, monedasTotales));
                Log.d("DBmanager", "Tirada recuperada: Id=" + id + ", Resultado=" + resultado + ", PremioSeleccionado=" + premioSeleccionado + ", Apuesta=" + apuesta + ", UsuarioId=" + userId + ", MonedasTotales=" + monedasTotales);
            }
            cursor.close();

            if (!emitter.isDisposed()) {
                emitter.onNext(historial);
                emitter.onComplete();
            }
        });
    }
}
