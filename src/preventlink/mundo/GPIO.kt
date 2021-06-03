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

import preventlink.Utils
import java.lang.Exception
import javax.swing.plaf.TableHeaderUI

/**
 * Un GPIO es un objeto que permite conectarse con la máquina y generar
 * alarmas al respecto
 */
class GPIO(var identificador: String,
           var ip: String,
           var puertoConexion: Int,
           var verde: Int,
           var ambar: Int,
           var rojo: Int,
           val rele: Int) {

    // Permite guardar este objeto en el archivo de configuracion HCL
    fun convertirHCL(builder: StringBuilder) {
        with(builder) {
            appendLine("GPIO \"$identificador\" {")
            appendLine("  ip = \"$ip\"")
            appendLine("  puertoConexion = $puertoConexion")
            appendLine("  puertoVerde = $verde" )
            appendLine("  puertoAmbar = $ambar")
            appendLine("  puertoRojo = $rojo")
            appendLine("  puertoRele = $rele")
            appendLine("}")
        }
    }

    /**
     * Permite enviar una señal de encendido al dispositivo conectado
     * al puerto dado. Retorna el estado anterior del puerto.
     */
    fun encender(puerto: Int): Boolean? {
        return Utils.enviarComandoGPIO(ip, puertoConexion, puerto, true)
    }

    /**
     * Permite enviar una señal de apagado al dispositivo conectado al
     * puerto dado. Retorna el estado anterior del puerto
     */
    fun apagar(puerto: Int): Boolean? {
        return Utils.enviarComandoGPIO(ip, puertoConexion, puerto, false)
    }

    fun encenderRojo() = encender(rojo)
    fun apagarRojo() = apagar(rojo)
    fun encenderAmbar() = encender(ambar)
    fun apagarAmbar() = apagar(ambar)
    fun encenderVerde() = encender(verde)
    fun apagarVerde() = apagar(verde)
    fun encenderRele() = encender(rele)
    fun apagarRele() = apagar(rele)

    companion object {
        /**
         * Permite obtener la información de la antena desde el archivo de configuación
         */
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, gpioInfo: Map<String, Any?>): GPIO? {
            if (gpioInfo.isNotEmpty()) {
                val ip = gpioInfo["ip"].toString()
                val puertoConexion = (gpioInfo["puertoConexion"] as Double).toInt()
                val puertoVerde = (gpioInfo["puertoVerde"] as Double).toInt()
                val puertoAmbar = (gpioInfo["puertoAmbar"] as Double).toInt()
                val puertoRojo = (gpioInfo["puertoRojo"] as Double).toInt()
                val puertoRele = (gpioInfo["puertoRele"] as Double).toInt()

                return GPIO(identificador, ip, puertoConexion, puertoVerde, puertoAmbar, puertoRojo, puertoRele)
            }
            return null
        }
    }

    /**
     * Inica la simulación del GPIO
     */
    fun iniciar() {
        try {
            encenderVerde()
            Thread.sleep(50)

            encenderAmbar()
            Thread.sleep(50)

            encenderRojo()
            Thread.sleep(50)

            apagarRojo()
            Thread.sleep(50)

            apagarAmbar()
            Thread.sleep(50)

            apagarVerde()
            Thread.sleep(50)

            encenderRele()
            Thread.sleep(50)
        }
        catch (ex: Exception) {
            Reporte.excepcion(ex)
        }
    }

    /**
     * Apaga la máquina y  lo que tiene que ver con el gpio
     */
    fun finalizar() {
        try {
            apagarRojo()
            Thread.sleep(300)

            apagarAmbar()
            Thread.sleep(300)

            apagarVerde()
            Thread.sleep(300)

            apagarRele()
        }
        catch (ex: Exception) {
            Reporte.excepcion(ex)
        }
    }
}