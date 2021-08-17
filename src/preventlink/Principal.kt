/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: Principal.kt, v 1.0 2021/05/12 17:24 lacobo $
 * Universidad Ean (Bogotá - Colombia)
 * Grupo de Investigación Tecnológico ONTARE
 * Licenciado bajo el esquema Academic Free License version 2.1
 *
 * Proyecto PreventLink
 * Autor: Luis Cobo - 23/05/2021
 * Modificado por:
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package preventlink

import preventlink.mundo.*

/**
 * Programa principal de la aplicación
 */
fun main() {
    val escenario = Escenario.instance()
    val configuracion = escenario.configuracionActual()

    println("================PREVENTLINK====================")

    if (escenario != null) {
        val lector: Lector? = escenario.lectorConfiguracionActual()
        if (lector != null) {
            Reporte.info("Conectandose al lector en la dirección ${lector.IP}")
            if (lector.activar()) {
                Reporte.info("Iniciando lectura de datos")
                lector.iniciarLectura()
            }
        } else {
            Reporte.error("No hay lector de RFID configurado")
        }

        // Ahora que el lector arrancó, iniciamos el monitor
        Reporte.info("Iniciando monitorización")
        // Iniciar los monitores
        iniciarMonitores()

        // Arrancamos el comunicador
        Reporte.info("Iniciando comunicación con sensores")
        ComunicadorSensores.iniciar()

        println("Entre ENTER para finalizar")
        readLine()!!

        // Finalizamos el comunicador
        Reporte.info("Terminando el comunicador con los sensores")
        ComunicadorSensores.finalizar()

        // Finalizamos el monitor
        Reporte.info("Finalizando monitorización")
        finalizarMonitores()

        // Finalizamos el lector
        if (lector != null && lector.estado == "ACTIVO") {
            Reporte.info("Finalizando lectura de tags")
            lector.finalizarLectura()
        }
    }

    println("Fin!")
}

/**
 * Esta clases permiten pasar de una etapa a otra con los lectores
 */
class Etapa1(val monitor: Monitor): TagTimeout {

    override fun onTimeout(tagsAusentesMin: Int, tagsAusentesMax: Int) {
        if (monitor.estado == 1) {
            val maquina = monitor.maquinaMonitor()
            if (maquina != null && maquina.estado == "OK") {
                if (maquina.encendida) {
                    return
                }
            }
            Reporte.info("ETAPA1 del monitor ${monitor.identificador}")
            // Si están todos los tags presente
            if (tagsAusentesMin == 0 && tagsAusentesMax == 0) {
                // El GPIO habilita el sistema de encendido
                if (maquina != null && maquina.estado == "OK") {
                    maquina.encender()
                }
                // Se genera el reporte
                Reporte.info("Máquina ${maquina?.nombre} encedida")
                monitor.estado = 2
            }
        }
    }
}

class Etapa2(val monitor: Monitor): TagTimeout {

    override fun onTimeout(tagsAusentesMin: Int, tagsAusentesMax: Int) {
        val maquina = monitor.maquinaMonitor()
        if (monitor.estado == 2) {
            if (maquina != null && maquina.estado == "OK")  {
                if (!maquina.encendida) {
                    // Maquina apagada? Salir
                    return
                }
            }
            Reporte.info("ETAPA2 del monitor ${monitor.identificador}")
            if (tagsAusentesMax > 0) {
                Reporte.info("Máquina ${maquina?.nombre} fin alarma sonora")
                if (maquina != null && maquina.estado == "OK") {
                    maquina.alarmaSonora(false)
                }
                monitor.estado = 3
            }
            else if (tagsAusentesMin > 0) {
                Reporte.info("Máquina ${maquina?.nombre} inicio alarma sonora")
                if (maquina != null && maquina.estado == "OK") {
                    maquina.alarmaSonora(true)
                }
                monitor.estado = 3
            }
        }
    }
}

class Etapa3(val monitor: Monitor): TagTimeout {

    override fun onTimeout(tagsAusentesMin: Int, tagsAusentesMax: Int) {
        val maquina = monitor.maquinaMonitor()

        if (monitor.estado == 3) {
            if (maquina != null && maquina.estado == "OK") {
                if (!maquina.encendida) {
                    return
                }
            }
            Reporte.info("ETAPA3 del monitor ${monitor.identificador}")
            if (tagsAusentesMax > 0) {
                if (maquina != null && maquina.estado == "OK") {
                    // Apagamos la alarma sonora
                    maquina.alarmaSonora(false)
                    // Apagamos la máquina
                    maquina.apagar()
                    // Generamos el reporte
                    Reporte.info("Máquina ${maquina.nombre} apagada")
                }
                monitor.estado = 1
            }
            else if (tagsAusentesMin == 0) {
                if (maquina != null && maquina.estado == "OK") {
                    // Apagamos la alarma sonora
                    maquina.alarmaSonora(false)
                    // Generamos reporte
                    Reporte.info("Máquina ${maquina.nombre} fin alarma sonora")
                    // Pasamos al estado anterior
                }
                monitor.estado = 2
            }
        }
    }
}

/**
 * Iniciamos los monitores del escenario actual
 */
fun iniciarMonitores() {
    val escenario = Escenario.instance()
    val configuracion = escenario.configuracionActual()
    if (configuracion != null) {
        for (monitorId in configuracion.monitores) {
            val m = escenario.monitor(monitorId)
            if (m != null && m.activo) {
                m.iniciar()
            }
        }
    }
}

fun finalizarMonitores() {
    val escenario = Escenario.instance()
    val configuracion = escenario.configuracionActual()
    if (configuracion != null) {
        for (monitorId in configuracion.monitores) {
            val m = escenario.monitor(monitorId)
            if (m != null && m.activo) {
                m.finalizar()
            }
        }
    }
}
