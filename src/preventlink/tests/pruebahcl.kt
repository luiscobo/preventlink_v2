package preventlink.tests

import preventlink.mundo.Escenario
import preventlink.mundo.Lector
import preventlink.mundo.Reporte
import java.time.Instant
import java.util.*

fun main() {
    val escenario = Escenario.instance()

    if (escenario != null) {
        val lector: Lector? = escenario.lectorConfiguracionActual()
        if (lector != null) {
            Reporte.info("Conectandose al lector en la dirección ${lector.IP}")
            if (lector.activar()) {
                Reporte.info("Iniciando lectura de datos")
                lector.iniciarLectura()
            }

            print("Presione ENTER para finalizar")
            readLine()!!

            Reporte.info("Finalizando lectura")
            lector.finalizarLectura()

            val ttiempo = Instant.now().toEpochMilli()
            val escenario = Escenario.instance()
            if (escenario != null) {
                val conf = escenario.configuracionActual()
                if (conf != null) {
                    for (tagId in conf.epps) {
                        val epp = escenario.epps[tagId]
                        if (epp != null) {
                            println("${epp.identificador} - ${epp.tiempoLocal} - $ttiempo")
                            val difer: Double = (ttiempo - epp.tiempoLocal) / 1000.0
                            println("    Diferencia $difer")
                        }
                    }
                }
            }
            print("Fin!")
        }
    }
}