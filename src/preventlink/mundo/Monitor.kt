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

import preventlink.Etapa1
import preventlink.Etapa2
import preventlink.Etapa3
import java.util.*
import kotlin.collections.ArrayList
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
class Monitor(
    var identificador: String,
    var nombre: String,
    var esperaInicial: Long,
    var periodo: Long,
    var epps: ArrayList<String>,
    var maquina: String,
    var activo: Boolean
) {

    /**
     * Atributos
     */
    private val observadores: MutableList<TagTimeout> = mutableListOf()
    private var timer: Timer? = null

    var estado = 0

    private fun q(str: String) = "\"$str\""

    private fun fromListToString(lst: ArrayList<String>): String {
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

    /**
     * Métodos
     */
    // Esta función exporta el monitor al formato HCL
    fun convertirHCL(builder: StringBuilder) {
        val act = if (activo) "SI" else "NO"
        with (builder) {
            appendLine("monitor ${q(identificador)} {")
            appendLine("  nombre = ${q(nombre)}")
            appendLine("  esperaInicial = ${esperaInicial.toDouble()}")
            appendLine("  periodo = ${periodo.toDouble()}")
            appendLine("  epps = ${q(fromListToString(epps))}")
            appendLine("  maquina = ${q(maquina)}")
            appendLine("  activo = ${q(act)}")
            appendLine("}")
        }
    }

    /**
     * Permite obtener la máquina asociada a este monitor
     */
    fun maquinaMonitor(): Maquina? {
        val escenario = Escenario.instance()
        return escenario.maquina(this.maquina)
    }

    /**
     * Este objeto permite obtener la información del monitor desde la configuración
     */
    companion object {
        // Permite obtener la información del monitor desde el archivo de configuración
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, monitorInfo: Map<String, Any?>): Monitor? {
            val nombre: String
            val esperaInicial: Long
            val periodo: Long
            val epps: ArrayList<String>
            val maquina: String
            val activo: Boolean

            if (monitorInfo.isNotEmpty()) {
                nombre = monitorInfo["nombre"].toString()
                esperaInicial = monitorInfo["esperaInicial"].toString().toDouble().toLong()
                periodo = monitorInfo["periodo"].toString().toDouble().toLong()
                epps = monitorInfo["epps"] as ArrayList<String>
                maquina = monitorInfo["maquina"].toString()
                activo = monitorInfo["activo"].toString().uppercase() == "SI"
                return Monitor(identificador, nombre, esperaInicial, periodo, epps, maquina, activo)
            }
            return null
        }
    }

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

        // El estado inicial es 1
        estado = 1

        // Creamos las diversas etapas del monitor
        this.agregarObservador(Etapa1(this))
        this.agregarObservador(Etapa2(this))
        this.agregarObservador(Etapa3(this))

        // Iniciamos el timer
        timer = fixedRateTimer(this.nombre, false, this.esperaInicial.toLong(), this.periodo.toLong()) {
            evaluarTiemposEPPs()
        }

        // Iniciamos la máquina asociada
        this.iniciarMaquina()
    }

    /**
     * Inicia la máquina asocidad a este monitor
     */
    fun iniciarMaquina() {
        val machine = this.maquinaMonitor()

        if (machine != null && machine.estado == "OK") {
            Reporte.info("Iniciando la máquina ${machine.nombre}")
            machine.apagar(final = true)
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

                // Primero los tags
                for (tagId in this.epps) {
                    val epp = escenario.epps[tagId]
                    if (epp != null && epp.activo) {
                        val t = epp.tiempoUltimaPresencia
                        Reporte.info("Tiempo para el tag $tagId = $t")
                        if (t >= conf.tiempoAusenciaMaximo) {
                            Reporte.info("  Tiempo del EPP $tagId superado")
                            tagSupMax++
                        } else if (epp.tiempoLocal == 0L || t >= conf.tiempoAusenciaMinimo) {
                            tagSupMin++
                        }
                    }
                }
                // Ahora los sensores remotos
                for (sensorId in conf.sensores) {
                    val sensor = escenario.sensores[sensorId]
                    if (sensor != null && sensor.activo) {
                        val t = sensor.tiempo
                        Reporte.info("Tiempo del sensor ${sensorId} = $t")
                        if (t >= conf.tiempoAusenciaMaximo) {
                            Reporte.info("Tiempo del EPP $sensorId superado")
                            tagSupMax++
                        }
                        else if (t >= conf.tiempoAusenciaMinimo) {
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
            apagarMaquina()
        }
    }

    private fun apagarMaquina() {
        val maq = this.maquinaMonitor()
        if (maq != null && maq.estado == "OK") {
            Reporte.info("Finalizando la máquina ${maq.nombre}")
            maq.apagar(final = true)
        }
    }
}