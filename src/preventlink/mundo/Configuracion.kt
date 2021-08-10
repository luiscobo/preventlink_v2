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
                         var gpioID: String,
                         var tiempoAusenciaMinimo: Long,
                         var tiempoAusenciaMaximo: Long,
                         var maquinaID: String,
                         var lectorID: String,
                         var epps: ArrayList<String>,
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
            appendLine("  gpio = \"$gpioID\"")
            appendLine("  tiempoDeAusenciaMinimo = $tiempoAusenciaMinimo")
            appendLine("  tiempoDeAusenciaMaximo = $tiempoAusenciaMaximo")
            appendLine("  maquina = \"$maquinaID\"")
            appendLine("  lector = \"$lectorID\"")
            appendLine("  epps = ${fromListToString(epps)}")
            appendLine("  sensores = ${fromListToString(sensores)}")
            appendLine("}")
        }
    }

    // Numero de EPPS
    fun numeroEPPS() = epps.size

    // Cantidad de sensores
    fun numSensores() = sensores.size

    companion object {
        /**
         * Esta función crea una configuración desde el archivo de HCL
         */
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, configInfo: Map<String, Any?>): Configuracion? {
            if (configInfo.isNotEmpty()) {
                val gpioID = configInfo["gpio"].toString()
                val tiempoAusenciaMinimo = configInfo["tiempoDeAusenciaMinimo"]!!.toString().toDouble().toLong()
                val tiempoAusenciaMaximo = configInfo["tiempoDeAusenciaMaximo"]!!.toString().toDouble().toLong()
                val maquinaID = configInfo["maquina"].toString()
                val lectorID = configInfo["lector"].toString()
                val epps = configInfo["epps"] as ArrayList<String>
                val sensors = configInfo["sensores"] as ArrayList<String>
                return Configuracion(identificador, gpioID, tiempoAusenciaMinimo, tiempoAusenciaMaximo, maquinaID, lectorID, epps, sensors)
            }
            return null
        }
    }
}