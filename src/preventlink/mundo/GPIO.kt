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
           var rele1: Int,
           var rele2: Int,
           var rele3: Int,
           var activo: Boolean) {

    // Permite guardar este objeto en el archivo de configuracion HCL
    fun convertirHCL(builder: StringBuilder) {
        val s = if (activo) "SI" else "NO"
        with(builder) {
            appendLine("GPIO \"$identificador\" {")
            appendLine("  ip = \"$ip\"")
            appendLine("  puertoConexion = $puertoConexion")
            appendLine("  puertoVerde = $verde" )
            appendLine("  puertoAmbar = $ambar")
            appendLine("  puertoRojo = $rojo")
            appendLine("  puertoRele1 = $rele1")
            appendLine("  puertoRele2 = $rele2")
            appendLine("  puertoRele3 = $rele3")
            appendLine("  activo = \"$s\"")
            appendLine("}")
        }
    }

    /**
     * Permite enviar una señal de encendido al dispositivo conectado
     * al puerto dado. Retorna el estado anterior del puerto.
     */
    fun encender(puerto: Int): Boolean? {
        if (activo) {
            return Utils.enviarComandoGPIO(ip, puertoConexion, puerto, true)
        }
        return null
    }

    /**
     * Permite enviar una señal de apagado al dispositivo conectado al
     * puerto dado. Retorna el estado anterior del puerto
     */
    fun apagar(puerto: Int): Boolean? {
        if (activo) {
            return Utils.enviarComandoGPIO(ip, puertoConexion, puerto, false)
        }
        return null
    }

    fun encenderRojo() = encender(rojo)
    fun apagarRojo() = apagar(rojo)
    fun encenderAmbar() = encender(ambar)
    fun apagarAmbar() = apagar(ambar)
    fun encenderVerde() = encender(verde)
    fun apagarVerde() = apagar(verde)
    fun encenderRele1() = encender(rele1)
    fun apagarRele1() = apagar(rele1)
    fun encenderRele2() = encender(rele2)
    fun apagarRele2() = apagar(rele2)
    fun encenderRele3() = encender(rele3)
    fun apagarRele3() = apagar(rele3)

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
                val puertoRele1 = (gpioInfo["puertoRele1"] as Double).toInt()
                val puertoRele2 = (gpioInfo["puertoRele2"] as Double).toInt()
                val puertoRele3 = (gpioInfo["puertoRele3"] as Double).toInt()
                val activo = gpioInfo["activo"].toString() == "SI"

                return GPIO(identificador, ip, puertoConexion, puertoVerde, puertoAmbar, puertoRojo, puertoRele1, puertoRele2, puertoRele3, activo)
            }
            return null
        }
    }

    /**
     * Inica la simulación del GPIO
     */
    fun iniciar() {
        if (!activo) {
            return
        }
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

            encenderRele1()
            Thread.sleep(50)
            encenderRele2()
            Thread.sleep(50)
            encenderRele3()
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

            apagarRele1()
            Thread.sleep(50)

            apagarRele2()
            Thread.sleep(50)

            apagarRele3()
            Thread.sleep(50)
        }
        catch (ex: Exception) {
            Reporte.excepcion(ex)
        }
    }
}