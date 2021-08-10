/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: SensorRemoto.kt, v 1.0 2021/05/12 17:24 lacobo $
 * Universidad Ean (Bogotá - Colombia)
 * Grupo de Investigación Tecnológico ONTARE
 * Licenciado bajo el esquema Academic Free License version 2.1
 *
 * Proyecto PreventLink
 * Autor: Luis Cobo - 23/07/2021
 * Modificado por:
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package preventlink.mundo

import preventlink.Utils

/**
 * Un sensor remoto es un objeto que está conectado a una elemento de protección y que permite determinar si estamos cerca
 * o lejos de ese elemento
 */
data class SensorRemoto(var identificador: String,
                        var ip: String,
                        var puerto: Int,
                        var activo: Boolean) {

    // Para almacenar el tiempo que se recibe del sensor
    private var elTiempo: Long = 0L

    val tiempo: Long
        get() = elTiempo

    // Permite guardar este objeto en el archivo de configuracion HCL
    fun convertirHCL(builder: StringBuilder) {
        val s = "\"${if (activo) "SI" else "NO"}\""
        with(builder) {
            appendLine("sensor \"$identificador\" {")
            appendLine("  ip = \"$ip\"")
            appendLine("  puerto = $puerto")
            appendLine("  activo = $s")
            appendLine("}")
        }
    }

    // Envía una señal al sensor remoto para saber el estado del mismo
    fun enviarSeñal() {
        if (activo) {
            var resp = Utils.obtenerEstado(ip, puerto);
            if (resp == null) {
                Reporte.error("Problemas de comunicación con el sensor remoto")
            }
            else {
                val pos = resp.indexOf(' ');
                if (pos > 0) {
                    elTiempo = resp.substring(pos + 1).toLong();
                    Reporte.info("Llegó tiempo = $elTiempo")
                }
                else {
                    Reporte.error("Respuesta incorrecta desde el sensor remoto")
                }
            }
        }
    }

    companion object {
        /**
         * Permite obtener la información de la antena desde el archivo de configuación
         */
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, sensorInfo: Map<String, Any?>): SensorRemoto? {
            if (sensorInfo.isNotEmpty()) {
                val ip = sensorInfo["ip"].toString()
                val puerto = (sensorInfo["puerto"] as Double).toInt()
                val activo = (sensorInfo["activo"].toString()) == "SI"
                return SensorRemoto(identificador, ip, puerto, activo)
            }
            return null
        }
    }
}
