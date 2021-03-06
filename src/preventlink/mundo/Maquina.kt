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

/**
 * Una máquina es un objeto que representa un elemento
 * siendo monitoreado por un EPP y controlado por un GPIO
 */
class Maquina(var identificador: String,
              var tipo: String,
              var nombre: String,
              var estado: String,  // Indica si está activa o inactiva
              var descripcion: String,
              var gpio: String) {

    // Para saber si la máquina está encendida o apagada
    private var _encendida: Boolean = false
    val encendida: Boolean
        get() = _encendida


    // Esta función exporta la máquina al formato HCL
    fun convertirHCL(builder: StringBuilder) {
        with(builder) {
            appendLine("maquina \"$identificador\" {")
            appendLine("  tipo = \"$tipo\"")
            appendLine("  nombre = \"$nombre\"")
            appendLine("  estado = \"$estado\"")
            appendLine("  descripcion = \"$descripcion\"")
            appendLine("  gpio = \"$gpio\"")
            appendLine("}")
        }
    }

    companion object {
        // Esta función obtiene la información de las máquinas a partir del archivo de configuración
        /**
         * Permite obtener la información de la antena desde el archivo de configuación
         */
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, maquinaInfo: Map<String, Any?>): Maquina? {
            val tipo: String
            val nombre: String
            val estado: String
            val descripcion: String
            val gpio: String

            if (maquinaInfo.isNotEmpty()) {
                tipo = maquinaInfo["tipo"].toString()
                nombre = maquinaInfo["nombre"].toString()
                estado = maquinaInfo["estado"].toString()
                descripcion = maquinaInfo["descripcion"].toString()
                gpio = maquinaInfo["gpio"].toString()
                return Maquina(identificador, tipo, nombre, estado, descripcion, gpio)
            }
            return null
        }
    }

    /**
     * Enciende la máquina
     */
    fun encender() {
        if (estado != "OK") {
            return  // No se puede tocar la maquina en estado inactiva
        }

        if (_encendida) {
            return
        }

        val escenario = Escenario.instance()
        if (escenario != null) {
            val gpio = escenario.gpios[this.gpio]
            if (gpio != null) {
                gpio!!.iniciar()
                gpio!!.encenderVerde()
                _encendida = true
                Reporte.info("Máquina $nombre encendida")
            }
        }
    }

    /**
     * Apagar la máquina
     */
    fun apagar(final: Boolean = false) {
        if (estado != "OK") {
            return
        }

        if (!final && !_encendida) {
            return
        }

        val escenario = Escenario.instance()
        if (escenario != null) {
            val gpio = escenario.gpios[this.gpio]
            if (gpio != null) {
                gpio.finalizar()
                if (!final) {
                    gpio.encenderRojo()
                }
                _encendida = false
                Reporte.info("Maquina $nombre apagada")
            }
        }
    }

    /**
     * Alarma sonora
     */
    fun alarmaSonora(encender: Boolean) {
        if (estado != "OK") {
            return
        }
        if (!_encendida) {
            return
        }
        val escenario = Escenario.instance()
        if (escenario != null) {
            val gpio = escenario.gpios[this.gpio]
            if (gpio != null) {
                if (encender) {
                    gpio.encenderAmbar()
                }
                else {
                    gpio.apagarAmbar()
                }

                Reporte.info("Alarma sonora en $nombre")
            }
        }
    }
}