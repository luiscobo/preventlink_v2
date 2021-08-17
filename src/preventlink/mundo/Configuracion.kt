/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: Configuracion.kt, v 1.0 2021/05/12 17:24 lacobo $
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

import java.util.ArrayList

/**
 * Una configuración indica una forma de acceder a un conjunto de
 * elementos que trabajan juntos
 */
data class Configuracion(var identificador: String,
                         var tiempoAusenciaMinimo: Long,
                         var tiempoAusenciaMaximo: Long,
                         var monitores: ArrayList<String>,
                         var lectorID: String,
                         var sensores: ArrayList<String>) {

    // Convierte esta configuración al formato HCL
    fun convertirHCL(builder: StringBuilder) {
        fun fromListToString(lst: ArrayList<String>): String {
            var s = "["

            for (i in 0 until lst.size) {
                s += '"' + lst[i] + '"'
                if (i != lst.size - 1) {
                    s += ", "
                }
            }
            s += "]"
            return s
        }
        with(builder) {
            appendLine("configuracion \"$identificador\" {")
            appendLine("  tiempoDeAusenciaMinimo = $tiempoAusenciaMinimo")
            appendLine("  tiempoDeAusenciaMaximo = $tiempoAusenciaMaximo")
            appendLine("  monitores = \"${fromListToString(monitores)}\"")
            appendLine("  lector = \"$lectorID\"")
            appendLine("  sensores = ${fromListToString(sensores)}")
            appendLine("}")
        }
    }

    // Cantidad de sensores
    fun numSensores() = sensores.size

    // Cantidad de monitores
    fun numMonitores() = monitores.size

    companion object {
        /**
         * Esta función crea una configuración desde el archivo de HCL
         */
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, configInfo: Map<String, Any?>): Configuracion? {
            if (configInfo.isNotEmpty()) {
                val tiempoAusenciaMinimo = configInfo["tiempoDeAusenciaMinimo"]!!.toString().toDouble().toLong()
                val tiempoAusenciaMaximo = configInfo["tiempoDeAusenciaMaximo"]!!.toString().toDouble().toLong()
                val lectorID = configInfo["lector"].toString()
                val monitores = configInfo["monitores"] as ArrayList<String>
                val sensors = configInfo["sensores"] as ArrayList<String>
                return Configuracion(identificador, tiempoAusenciaMinimo, tiempoAusenciaMaximo, monitores, lectorID, sensors)
            }
            return null
        }
    }
}