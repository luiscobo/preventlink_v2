/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: ComunicadorSensores.kt, v 1.0 2021/05/12 17:24 lacobo $
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

import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Este objeto tiene un timer que se comunica con los sensores remotos
 */
object ComunicadorSensores {
    private var timer: Timer? = null

    /**
     * Inicia la tarea del comunicador
     */
    fun iniciar() {
        val escenario = Escenario.instance()
        if (escenario != null) {
            timer = fixedRateTimer(escenario.comunicadorNombre,false, escenario.comunicadorDemoraInicial, escenario.comunicadorPeriodo) {
                comunicar()
            }
        }
    }

    /**
     * Envía una señal a cada sensor remoto para saber
     */
    private fun comunicar() {
        val escenario = Escenario.instance()
        if (escenario != null) {
            val conf = escenario.configuracionActual()
            if (conf != null) {
                val sensorIDs = conf.sensores
                for (ident in sensorIDs) {
                    val sensor = escenario.sensores[ident]
                    if (sensor != null && sensor.activo) {
                        sensor.enviarSeñal()
                    }
                }
            }
        }
    }

    /**
     * Finaliza la monitorización de eventos
     */
    fun finalizar() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }
}