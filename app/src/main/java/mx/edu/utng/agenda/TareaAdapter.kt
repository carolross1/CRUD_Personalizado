package mx.edu.utng.agenda


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.utng.agenda.R
import mx.edu.utng.agenda.Tarea

class TareaAdapter(
    private var listaTareas: List<Tarea>,
    private val onItemClick: (Tarea) -> Unit
) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    class TareaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTitulo)
        val txtFechaHora: TextView = view.findViewById(R.id.txtFechaHora)
        val txtPrioridad: TextView = view.findViewById(R.id.txtPrioridad)
        val checkCompletada: CheckBox = view.findViewById(R.id.checkCompletada)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = listaTareas[position]
        holder.txtTitulo.text = tarea.titulo
        holder.txtFechaHora.text = "${tarea.fecha} - ${tarea.hora}"
        holder.txtPrioridad.text = "Prioridad: ${tarea.prioridad}"
        holder.checkCompletada.isChecked = tarea.completada

        // Al hacer clic en la tarea, se abrirá la pantalla de detalles
        holder.itemView.setOnClickListener { onItemClick(tarea) }
    }

    override fun getItemCount(): Int = listaTareas.size

    // Método para actualizar la lista de tareas
    fun actualizarLista(nuevaLista: List<Tarea>) {
        listaTareas = nuevaLista
        notifyDataSetChanged()
    }
}
