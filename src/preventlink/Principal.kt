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

    println("================PREVENTLINK====================")

    if (escenario != null) {
        val lector: Lector? = escenario.lectorConfiguracionActual()
        val maquina = escenario.maquinaConfiguracionActual()
        if (lector != null) {
            Reporte.info("Conectandose al lector en la dirección ${lector.IP}")
            if (lector.activar()) {
                Reporte.info("Iniciando lectura de datos")
                lector.iniciarLectura()
            }
        }
        else {
            Reporte.error("No hay lector de RFID configurado")
        }

        // Ahora que el lector arrancó, iniciamos el monitor
        Reporte.info("Iniciando monitorización")
        estado = 1
        Monitor.agregarObservador(etapa1)
        Monitor.agregarObservador(etapa2)
        Monitor.agregarObservador(etapa3)
        Monitor.iniciar()

        // Arrancamos el comunicador
        Reporte.info("Iniciando comunicación con sensores")
        ComunicadorSensores.iniciar()

        // Ahora iniciamos la máquina
        if (maquina != null && maquina.estado == "OK") {
            Reporte.info("Iniciando la máquina ${maquina.nombre}")
            maquina.apagar(final = true)
        }
        else {
            Reporte.error("No hay máquina configurada!")
        }

        println("Entre ENTER para finalizar")
        readLine()!!

        // Finalizamos el comunicador
        Reporte.info("Terminando el comunicador con los sensores")
        ComunicadorSensores.finalizar()

        // Finalizamos el monitor
        Reporte.info("Finalizando monitorización")
        Monitor.finalizar()

        // Finalizamos el lector
        if (lector != null && lector.estado == "ACTIVO") {
            Reporte.info("Finalizando lectura de tags")
            lector.finalizarLectura()
        }

        // Finalizamos la máquina
        if (maquina != null && maquina.estado == "OK") {
            Reporte.info("Finalizando la máquina ${maquina.nombre}")
            maquina.apagar(final = true)
        }
        else {
            Reporte.error("No hay máquina configurada!")
        }
    }

    println("Fin!")
}

/**
 * Esta variable me indica en que estado me encuentro
 */
var estado: Int = 0

object etapa1: TagTimeout {
    override fun onTimeout(tagsAusentesMin: Int, tagsAusentesMax: Int) {
        val escenario = Escenario.instance()
        val conf = escenario.configuracionActual() ?: return
        val maquina = escenario.maquinaConfiguracionActual() ?: return

        if (estado == 1) {
            if (maquina.encendida) {
                return
            }
            Reporte.info("ETAPA1")
            // Si están todos los tags presente
            if (tagsAusentesMin == 0 && tagsAusentesMax == 0) {
                // El GPIO habilita el sistema de encendido
                maquina.encender()
                // Se genera el reporte
                Reporte.info("Máquina ${maquina.nombre} encedida")
                estado = 2
            }
        }
    }
}

object etapa2: TagTimeout {
    override fun onTimeout(tagsAusentesMin: Int, tagsAusentesMax: Int) {
        val escenario = Escenario.instance()
        val conf = escenario.configuracionActual() ?: return
        val maquina = escenario.maquinaConfiguracionActual() ?: return

        if (estado == 2) {
            if (!maquina.encendida) {
                // Maquina apagada? Salir
                return
            }
            Reporte.info("ETAPA2")
            if (tagsAusentesMax > 0) {
                Reporte.info("Máquina ${maquina.nombre} fin alarma sonora")
                maquina.alarmaSonora(false)
                estado = 3
            }
            else if (tagsAusentesMin > 0) {
                Reporte.info("Máquina ${maquina.nombre} inicio alarma sonora")
                maquina.alarmaSonora(true)
                estado = 3
            }
        }
    }
}

object etapa3: TagTimeout {
    override fun onTimeout(tagsAusentesMin: Int, tagsAusentesMax: Int) {
        val escenario = Escenario.instance()
        val conf = escenario.configuracionActual() ?: return
        val maquina = escenario.maquinaConfiguracionActual() ?: return

        if (estado == 3) {
            if (!maquina.encendida) {
                return
            }
            Reporte.info("ETAPA3")
            if (tagsAusentesMax > 0) {
                // Apagamos la alarma sonora
                maquina.alarmaSonora(false)
                // Apagamos la máquina
                maquina.apagar()
                // Generamos el reporte
                Reporte.info("Máquina ${maquina.nombre} apagada")
                estado = 1
            }
            else if (tagsAusentesMin == 0) {
                // Apagamos la alarma sonora
                maquina.alarmaSonora(false)
                // Generamos reporte
                Reporte.info("Máquina ${maquina.nombre} fin alarma sonora")
                // Pasamos al estado anterior
                estado = 2

            }
        }
    }
}
