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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

inline fun <reified T> logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

/**
 * Permite generar los reportes que podrán ser observados posteriormente
 */
class Reporte {


    companion object {
        // El objeto a través del cual guardamos los datos en el reporte
        private val logger = logger<Reporte>()

        fun error(mensaje: String) {
            logger.error(mensaje)
            println("ERROR: $mensaje")
        }

        fun excepcion(ex: Exception) {
            logger.error(ex.toString())
        }

        fun info(mensaje: String) {
            logger.info(mensaje)
            println("INFO: $mensaje")
        }
        fun adv(mensaje: String) = logger.warn(mensaje)
    }
}