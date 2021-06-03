package preventlink.tests

import preventlink.Utils
import preventlink.mundo.Escenario

fun main() {
    val escenario = Escenario.instance()

    if (escenario != null) {
        val gpio = escenario.gpioConfiguracionActual()
        println("Encender GPIO")
        gpio!!.iniciar()

        Thread.sleep(2000)
        println("Apagar GPIO")
        gpio!!.finalizar()


    }
}