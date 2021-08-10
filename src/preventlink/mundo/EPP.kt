/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: EPP.kt, v 1.0 2021/05/12 17:24 lacobo $
 * Universidad Ean (Bogotá - Colombia)
 * Grupo de Investigación Tecnológico ONTARE
 * Licenciado bajo el esquema Academic Free License version 2.1
 *
 * Proyecto PreventLink
 * Autor: Luis Cobo - 23/05/2021
 * Modificado por:
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package preventlink.mundo

import java.time.Instant

/**
 * Esta clase representa la información de los EPP (Elementos de Protección Personal)
 */
class EPP (var identificador: String,
           var codigo: String,
           var nombre: String,
           var descripcion: String,
           var esVerificacion: Boolean,
           var activo: Boolean) {

    // Con este atributo podemos saber cuando se obtuvo por última vez este EPP
    private var _tiempo: Long = 0L
    var tiempoLocal: Long = 0L
    private var _contador: Int = 0
    val tiempo: Long
        get() = _tiempo
    val contador: Int
        get() = _contador

    /**
     * Esta función permite saber cuanto tiempo ha pasado desde la última vez que lo vimos
     */
    val tiempoUltimaPresencia: Long
        get() {
            val escenario = Escenario.instance()
            if (escenario != null) {
                val ahora = Instant.now().toEpochMilli()
                if (escenario.configuracionActual() != null) {
                    return  ahora - tiempoLocal
                }
            }
            return 0L
        }

    // Permite guardar este objeto en el archivo de configuracion HCL
    fun convertirHCL(builder: StringBuilder) {
        val res = if (esVerificacion) "SI" else "NO"
        val act = if (activo) "SI" else "NO"
        with(builder) {
            appendLine("epp \"$identificador\" {")
            appendLine("  codigo = \"$codigo\"")
            appendLine("  nombre = \"$nombre\"")
            appendLine("  descripcion = \"$descripcion\"")
            appendLine("  esVerificacion = \"$res\"")
            appendLine("  activo = \"$act\"")
            appendLine("}")
        }
    }

    /**
     * Esta operación permite registrar una nueva lectura
     */
    fun registrarLectura(contador: Int, tiempo: Long) {
        tiempoLocal = Instant.now().toEpochMilli()
        Reporte.info("Tiempo local = $tiempoLocal")
        _tiempo = tiempo
        _contador += contador
    }

    companion object {
        // Permite obtener la información de un EPP del archivo de configuración
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, eppInfo: Map<String,Any?>): EPP? {
            val codigo: String
            val nombre: String
            val descripcion: String
            val esVerificacion: Boolean

            if (eppInfo.isNotEmpty()) {
                codigo = eppInfo["codigo"].toString()
                nombre = eppInfo["nombre"].toString()
                descripcion = eppInfo["descripcion"].toString()
                esVerificacion = eppInfo["esVerificacion"].toString().uppercase() == "SI"
                val activo = eppInfo["activo"].toString().uppercase() == "SI"
                return EPP(identificador, codigo, nombre, descripcion, esVerificacion, activo)
            }
            return null

        }
    }

}