package preventlink.tests

import preventlink.mundo.Escenario
import preventlink.mundo.Monitor

fun main() {
    println("Iniciando la monitorizacion")
    Monitor.iniciar()
    println("Entre ENTER para finalizar")
    readLine()!!
    Monitor.finalizar()
    println("Fin!")

}