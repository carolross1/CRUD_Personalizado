package mx.edu.utng.agenda


import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import mx.edu.utng.agenda.R
import mx.edu.utng.agenda.TareaAdapter
import mx.edu.utng.agenda.DatabaseHelper
import mx.edu.utng.agenda.Tarea

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tareaAdapter: TareaAdapter
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var searchView: SearchView
    private lateinit var btnAgregar: FloatingActionButton

    private var listaTareas: List<Tarea> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        btnAgregar = findViewById(R.id.btnAgregar)

        dbHelper = DatabaseHelper(this)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        tareaAdapter = TareaAdapter(listaTareas) { tarea ->
            val intent = Intent(this, DetalleTareaActivity::class.java)
            intent.putExtra("TAREA_ID", tarea.id)
            startActivity(intent)
        }
        recyclerView.adapter = tareaAdapter

        // Cargar tareas desde la base de datos
        cargarTareas()

        // Búsqueda en tiempo real
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtro = listaTareas.filter { tarea ->
                    tarea.titulo.contains(newText ?: "", ignoreCase = true)
                }
                tareaAdapter.actualizarLista(filtro)
                return true
            }
        })

        // Botón para agregar nueva tarea
        btnAgregar.setOnClickListener {
            val intent = Intent(this, EditarTareaActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarTareas() // Recargar lista al volver de otra pantalla
    }

    private fun cargarTareas() {
        listaTareas = dbHelper.obtenerTareas()
        tareaAdapter.actualizarLista(listaTareas)
    }
}
