/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: Monitor.kt, v 1.0 2021/05/12 17:24 lacobo $
 * Universidad Ean (Bogotá - Colombia)
 * Grupo de Investigación Tecnológico ONTARE
 * Licenciado bajo el esquema Academic Free License version 2.1
 *
 * Proyecto PreventLink
 * Autor: Luis Cobo - 02/06/2021
 * Modificado por:
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package preventlink.mundo

import java.util.*
import kotlin.concurrent.fixedRateTimer

/**
 * Esta interface será utilizada cuando ocurre un TimeOut
 */

interface TagTimeout {
    fun onTimeout(tagsAusentesMin: Int, tagsAusentesMax: Int)
}

/**
 * Un monitor es un programa que cada segundo está pendiente de los EPP
 * y es un observador
 */
object Monitor {
    private val observadores: MutableList<TagTimeout> = mutableListOf()
    private var timer: Timer? = null

    /**
     * Agregar un nuevo observador a la lista
     */
    fun agregarObservador(observador: TagTimeout) {
        observadores.add(observador)
    }

    /**
     * Elimina un observador de la lista
     */
    fun eliminarObservador(observador: TagTimeout) {
        observadores.remove(observador)
    }

    /**
     * Disparar los observadores que están aquí registrados
     */
    fun generarEvento(tagsAusentesMin: Int, tagsAusentesMax: Int) {
        for (obs in observadores) {
            obs.onTimeout(tagsAusentesMin, tagsAusentesMax)
        }
    }

    /**
     * Inicia la tarea del monitor
     */
    fun iniciar() {
        val escenario = Escenario.instance()
        if (escenario != null) {
            timer = fixedRateTimer(escenario.monitorNombre,false, escenario.monitorDemoraInicial, escenario.monitorPeriodo) {
                Monitor.evaluarTiemposEPPs()
            }
        }
    }

    /**
     * Realiza la evaluacion de los tiempos de los diversos EPPs
     */
    private fun evaluarTiemposEPPs() {
        val escenario = Escenario.instance()
        if (escenario != null) {
            val conf = escenario.configuracionActual()
            if (conf != null) {
                var tagSupMax = 0
                var tagSupMin = 0
                for (tagId in conf.epps) {
                    val epp = escenario.epps[tagId]
                    if (epp != null) {
                        val t = epp.tiempoUltimaPresencia
                        Reporte.info("Tiempo para el tag $tagId = $t")
                        if (t >= conf.tiempoAusenciaMaximo) {
                            Reporte.info("Tiempo del EPP $tagId superado")
                            tagSupMax++
                        }
                        else if (epp.tiempoLocal == 0L || t >= conf.tiempoAusenciaMinimo) {
                            tagSupMin++
                        }
                    }
                }
                generarEvento(tagSupMin, tagSupMax)
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
            observadores.clear()
        }
    }

}