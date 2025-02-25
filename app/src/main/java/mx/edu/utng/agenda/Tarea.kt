package mx.edu.utng.agenda


data class Tarea(
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fecha: String,  // Formato: "YYYY-MM-DD"
    val hora: String,   // Formato: "HH:MM"
    val prioridad: String, // Baja, Media, Alta
    val completada: Boolean
)
