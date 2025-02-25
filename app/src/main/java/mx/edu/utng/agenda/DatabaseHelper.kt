package mx.edu.utng.agenda

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "AgendaDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE tareas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                titulo TEXT NOT NULL,
                descripcion TEXT,
                fecha TEXT NOT NULL,
                hora TEXT NOT NULL,
                prioridad TEXT NOT NULL,
                completada INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tareas")
        onCreate(db)
    }

    fun insertarTarea(tarea: Tarea): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", tarea.titulo)
            put("descripcion", tarea.descripcion)
            put("fecha", tarea.fecha)
            put("hora", tarea.hora)
            put("prioridad", tarea.prioridad)
            put("completada", if (tarea.completada) 1 else 0)
        }
        return db.insert("tareas", null, values)
    }

    fun obtenerTareas(): List<Tarea> {
        val db = readableDatabase
        val listaTareas = mutableListOf<Tarea>()
        val cursor = db.rawQuery("SELECT * FROM tareas", null)

        while (cursor.moveToNext()) {
            listaTareas.add(
                Tarea(
                    id = cursor.getInt(0),
                    titulo = cursor.getString(1),
                    descripcion = cursor.getString(2),
                    fecha = cursor.getString(3),
                    hora = cursor.getString(4),
                    prioridad = cursor.getString(5),
                    completada = cursor.getInt(6) == 1
                )
            )
        }
        cursor.close()
        return listaTareas
    }

    fun obtenerTareaPorId(id: Int): Tarea? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM tareas WHERE id = ?", arrayOf(id.toString()))

        return if (cursor.moveToFirst()) {
            val tarea = Tarea(
                id = cursor.getInt(0),
                titulo = cursor.getString(1),
                descripcion = cursor.getString(2),
                fecha = cursor.getString(3),
                hora = cursor.getString(4),
                prioridad = cursor.getString(5),
                completada = cursor.getInt(6) == 1
            )
            cursor.close()
            tarea
        } else {
            cursor.close()
            null
        }
    }

    fun actualizarTarea(tarea: Tarea): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("titulo", tarea.titulo)
            put("descripcion", tarea.descripcion)
            put("fecha", tarea.fecha)
            put("hora", tarea.hora)
            put("prioridad", tarea.prioridad)
            put("completada", if (tarea.completada) 1 else 0)
        }
        return db.update("tareas", values, "id = ?", arrayOf(tarea.id.toString()))
    }

    fun eliminarTarea(id: Int, context: Context) {
        val db = writableDatabase
        db.delete("tareas", "id = ?", arrayOf(id.toString()))
        db.close()

        // 🔹 Cancelar la notificación de la tarea eliminada
        cancelarNotificacion(id, context)
    }

    private fun cancelarNotificacion(tareaId: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TareaReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, tareaId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
