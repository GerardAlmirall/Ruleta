package com.example.ruleta.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.ruleta.TiradaClase;

public class DBconexion extends SQLiteOpenHelper {
    private static final String DB_NAME = "dbruleta";
    private static final int DB_VERSION = 1;

    public DBconexion(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear las tablas
        String CREAR_TBL_USUARIO = "CREATE TABLE Usuario (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombreUsuario TEXT," +
                "monedasTotales INTEGER)";
        db.execSQL(CREAR_TBL_USUARIO);

        String CREAR_TBL_TIRADA = "CREATE TABLE Tirada (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "resultado INTEGER," +
                "premioSeleccionado INTEGER," +
                "apuesta INTEGER," +
                "usuarioId INTEGER," +
                "monedasTotales INTEGER," +
                "FOREIGN KEY(usuarioId) REFERENCES Usuario(id))";
        db.execSQL(CREAR_TBL_TIRADA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Manejar actualizaciones de la base de datos si es necesario
    }
    public long insertarTirada(TiradaClase nuevaTirada) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("resultado", nuevaTirada.getResultado());
        values.put("premioSeleccionado", nuevaTirada.getPremioSeleccionado());
        values.put("apuesta", nuevaTirada.getApuesta());
        values.put("usuarioId", nuevaTirada.getUsuarioid());
        values.put("monedasTotales", nuevaTirada.getMonedasTotales());

        // Insertar la nueva tirada en la tabla Tirada
        long tiradaId = db.insert("Tirada", null, values);

        if (tiradaId != -1) {
            // Si la inserción en la tabla Tirada fue exitosa, actualizar las monedas totales del usuario en la tabla Usuario
            ContentValues updateValues = new ContentValues();
            updateValues.put("monedasTotales", nuevaTirada.getMonedasTotales());
            int rowsAffected = db.update("Usuario", updateValues, "id = ?", new String[]{String.valueOf(nuevaTirada.getUsuarioid())});
            if (rowsAffected <= 0) {
                // Si no se actualizó ninguna fila en la tabla Usuario, registrar un error
                Log.e("DBconexion", "Error al actualizar las monedas totales del usuario en la tabla Usuario.");
            }
        } else {
            // Si la inserción en la tabla Tirada falló, registrar un error
            Log.e("DBconexion", "Error al insertar nueva tirada en la tabla Tirada.");
        }

        // Cerrar la conexión a la base de datos
        db.close();

        // Devolver el ID de la nueva tirada (puede ser -1 si la inserción falló)
        return tiradaId;
    }



    // Método para obtener las monedas totales del usuario
    private int obtenerMonedasTotalesDelUsuario(String nombreUsuario) {
        SQLiteDatabase db = this.getReadableDatabase();
        int monedasTotales = 0;

        // Realizar una consulta a la base de datos para obtener las monedas totales del usuario
        Cursor cursor = db.query("Usuario", new String[]{"monedasTotales"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);
        if (cursor.moveToFirst()) {
            int monedasTotalesIndex = cursor.getColumnIndex("monedasTotales");
            if (monedasTotalesIndex != -1) {
                monedasTotales = cursor.getInt(monedasTotalesIndex);
            } else {
                // Manejar el caso en que la columna "monedasTotales" no exista
                Log.e("DBconexion", "La columna 'monedasTotales' no fue encontrada en el resultado de la consulta.");
            }
        } else {
            // Manejar el caso en que no se encuentre el usuario
            Log.e("DBconexion", "Usuario no encontrado: " + nombreUsuario);
        }

        cursor.close(); // Asegúrate de cerrar el cursor después de usarlo
        db.close(); // Cierra la base de datos para liberar recursos

        return monedasTotales;
    }

    public SQLiteDatabase obtenerDatabase() {
        return this.getReadableDatabase();
    }
}
