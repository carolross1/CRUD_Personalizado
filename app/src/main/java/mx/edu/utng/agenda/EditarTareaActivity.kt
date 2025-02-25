package mx.edu.utng.agenda

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class EditarTareaActivity : AppCompatActivity() {

    private lateinit var edtTitulo: EditText
    private lateinit var edtDescripcion: EditText
    private lateinit var btnFecha: Button
    private lateinit var btnHora: Button
    private lateinit var spnPrioridad: Spinner
    private lateinit var checkCompletada: CheckBox
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnBack: ImageView

    private lateinit var dbHelper: DatabaseHelper
    private var tareaId: Int = -1
    private var fechaSeleccionada: String = ""
    private var horaSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_tarea)

        // Referencias a la UI
        edtTitulo = findViewById(R.id.edtTitulo)
        edtDescripcion = findViewById(R.id.edtDescripcion)
        btnFecha = findViewById(R.id.btnFecha)
        btnHora = findViewById(R.id.btnHora)
        spnPrioridad = findViewById(R.id.spnPrioridad)
        checkCompletada = findViewById(R.id.checkCompletada)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnBack = findViewById(R.id.btnBack)

        dbHelper = DatabaseHelper(this)

        // Configurar Spinner con texto oscuro
        val prioridades = arrayOf("Baja", "Media", "Alta")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, prioridades) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(resources.getColor(R.color.purple_500, theme))
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(resources.getColor(R.color.purple_500, theme))
                return view
            }
        }
        spnPrioridad.adapter = adapter

        // Cargar tarea si es edición
        tareaId = intent.getIntExtra("TAREA_ID", -1)
        if (tareaId != -1) {
            cargarTarea()
        }

        // Botones de acciones
        btnFecha.setOnClickListener { mostrarSelectorFecha() }
        btnHora.setOnClickListener { mostrarSelectorHora() }
        btnGuardar.setOnClickListener { guardarTarea() }
        btnEliminar.setOnClickListener { eliminarTarea() }
        btnBack.setOnClickListener { finish() }
    }

    private fun cargarTarea() {
        val tarea = dbHelper.obtenerTareaPorId(tareaId)
        tarea?.let {
            edtTitulo.setText(it.titulo)
            edtDescripcion.setText(it.descripcion)
            btnFecha.text = it.fecha
            btnHora.text = it.hora
            fechaSeleccionada = it.fecha
            horaSeleccionada = it.hora
            spnPrioridad.setSelection(
                when (it.prioridad) {
                    "Baja" -> 0
                    "Media" -> 1
                    "Alta" -> 2
                    else -> 0
                }
            )
            checkCompletada.isChecked = it.completada
        }
    }

    // Mostramos el selector de fecha usando nuestros estilos personalizados (con botones Aceptar y Cancelar)
    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            ContextThemeWrapper(this, R.style.CustomDatePickerDialog),
            { _, year, month, day ->
                fechaSeleccionada = "$day/${month + 1}/$year"
                btnFecha.text = fechaSeleccionada
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )
        // Los botones Aceptar y Cancelar se muestran por defecto
        datePickerDialog.show()
    }

    // Mostramos el selector de hora usando nuestros estilos personalizados (con botones Aceptar y Cancelar)
    private fun mostrarSelectorHora() {
        val calendario = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            ContextThemeWrapper(this, R.style.CustomTimePicker),
            { _, hour, minute ->
                horaSeleccionada = String.format("%02d:%02d", hour, minute)
                btnHora.text = horaSeleccionada
            },
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE),
            true
        )
        // Los botones Aceptar y Cancelar se muestran por defecto
        timePickerDialog.show()
    }

    private fun guardarTarea() {
        val titulo = edtTitulo.text.toString().trim()
        val descripcion = edtDescripcion.text.toString().trim()
        val prioridad = spnPrioridad.selectedItem.toString()
        val completada = checkCompletada.isChecked

        if (titulo.isEmpty() || descripcion.isEmpty() || fechaSeleccionada.isEmpty() || horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val tarea = Tarea(
            tareaId,
            titulo,
            descripcion,
            fechaSeleccionada,
            horaSeleccionada,
            prioridad,
            completada
        )

        if (tareaId == -1) {
            tareaId = dbHelper.insertarTarea(tarea).toInt()
            programarNotificacion(tarea.copy(id = tareaId))
            Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.actualizarTarea(tarea)
            programarNotificacion(tarea)
            Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun eliminarTarea() {
        if (tareaId != -1) {
            cancelarNotificacion(tareaId)
            dbHelper.eliminarTarea(tareaId, this)
            Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun cancelarNotificacion(tareaId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TareaReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, tareaId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun programarNotificacion(tarea: Tarea) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
                Toast.makeText(
                    this,
                    "Habilita el permiso de alarmas exactas en Configuración",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaHora = formatoFecha.parse("${tarea.fecha} ${tarea.hora}")

        if (fechaHora != null) {
            val calendar = Calendar.getInstance()
            calendar.time = fechaHora

            val intent = Intent(this, TareaReceiver::class.java).apply {
                putExtra("TAREA_ID", tarea.id)
                putExtra("TITULO", tarea.titulo)
                putExtra("DESCRIPCION", tarea.descripcion)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this, tarea.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}
