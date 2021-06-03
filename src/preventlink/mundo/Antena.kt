/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: Antena.kt, v 1.0 2021/05/12 17:24 lacobo $
 * Universidad Ean (Bogotá - Colombia)
 * Grupo de Investigación Tecnológico ONTARE
 * Licenciado bajo el esquema Academic Free License version 2.1
 *
 * Proyecto PreventLink
 * Autor: Luis Cobo - 12/05/2021
 * Modificado por:
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package preventlink.mundo

/**
 * Esta clase permite almacenar la información de una
 * antena que capta RFID
 */
data class Antena(var id: String,
                  var nombre: String,
                  var tipo: String,
                  var puerto: Short,
                  var potencia: Double,
                  var activa: Boolean) {

    // El lector al cual pertenece esta antena
    lateinit var lector: Lector

    // Esta función exporta la antena en formato HCL
    fun convertirHCL(builder: StringBuilder): Unit {
        builder.appendLine("  antena \"$id\" {")
        builder.appendLine("    nombre = \"$nombre\"")
        builder.appendLine("    tipo = \"$tipo\"")
        builder.appendLine("    puerto = $puerto")
        builder.appendLine("    potencia = $potencia")
        builder.appendLine("    activa = \"${if (activa) "SI" else "NO"}\"")
        builder.appendLine("  }")
    }

    companion object {
        /**
         * Permite obtener la información de la antena desde el archivo de configuación
         */
        @JvmStatic
        fun leerDesdeConfiguracionHCL(identificador: String, antenaInfo: Map<String,Any?>): Antena? {
            val nombre: String
            val tipo: String
            val puerto: String
            val potencia: String
            val activa: Boolean

            if (antenaInfo.isNotEmpty()) {
                nombre = antenaInfo["nombre"]!!.toString()
                tipo = antenaInfo["tipo"]!!.toString()
                puerto = antenaInfo["puerto"]!!.toString()
                potencia = antenaInfo["potencia"]!!.toString()
                activa = antenaInfo["activa"]!!.toString().uppercase() == "SI"
                return Antena(identificador, nombre, tipo,
                    puerto.toDouble().toInt().toShort(), potencia.toDouble(), activa)
            }
            return null
        }
    }
}