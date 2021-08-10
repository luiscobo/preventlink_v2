/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: Lector.kt, v 1.0 2021/05/12 17:24 lacobo $
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

import com.impinj.octane.*
import java.lang.Exception

/**
 * Los lectores son los elementos que reciben la información de los
 * RFID cercano a las antenas.
 */
data class Lector(var IP: String,
                  var nombre: String,
                  var descripcion: String,
                  var periodoAutoInicio: Int,
                  var duracionAutoDetencion: Int,
                  var periodoKeepAlives: Int,
                  var estado: String)  {

    // -----------------------------------------------------------------
    // Atributos
    // -----------------------------------------------------------------

    // El diccionario de antenas
    private val antenas: HashMap<String, Antena> = HashMap()

    // Para determinar si está activo o no el lector dado
    private var activo: Boolean = false

    // El objeto de comunicación con el lector físico
    private var reader: ImpinjReader? = null

    // -----------------------------------------------------------------
    // Métodos
    // -----------------------------------------------------------------

    /**
     * Determinar si este lector es igual a otro
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Lector

        if (nombre != other.nombre) return false
        if (IP != other.IP) return false

        return true
    }

    /**
     * Determinar el código de hash de este objeto
     */
    override fun hashCode(): Int {
        var result = nombre.hashCode()
        result = 31 * result + IP.hashCode()
        return result
    }

    /**
     * Obtener la representación en String del objeto en cuestión
     */
    override fun toString(): String {
        return "Lector(IP='$IP', nombre='$nombre', descripcion='$descripcion')"
    }

    /**
     * Obtener la representación en HCL del objeto Lector
     */
    fun convertirHCL(builder: StringBuilder) {
        builder.appendLine("lector \"$nombre\" {")
        builder.appendLine("  ip = \"$IP\"")
        builder.appendLine("  descripcion = \"$descripcion\"")
        builder.appendLine("  periodoAutoInicio = $periodoAutoInicio")
        builder.appendLine("  duracionAutoDetencion = $duracionAutoDetencion")
        builder.appendLine("  periodoKeepAlives = $periodoKeepAlives")
        builder.appendLine("  estado = \"$estado\"")
        for (antena in antenas.values) {
            antena.convertirHCL(builder)
        }
        builder.appendLine("}")
    }

    /**
     * Desarrolla la conexión al lector remoto y realiza todos los procesos de configuración
     */
    fun activar(): Boolean {
        if (estado == "INACTIVO" || activo) {
            Reporte.error("Lector activo!")
            return false  // Si el estado no es activo, termine
        }

        try {
            val reader = ImpinjReader()

            // Conexión con el lector
            reader.connect(this.IP)

            // Configuracion
            val settings = reader.queryDefaultSettings()
            settings.report.includeAntennaPortNumber = true
            settings.report.includeFirstSeenTime = true
            settings.report.includeLastSeenTime = true
            settings.report.includeSeenCount = true
            settings.readerMode = ReaderMode.AutoSetDenseReader
            settings.searchMode = SearchMode.DualTarget
            settings.session = 2

            // Send a tag report every time the reader stops (period is over).
            settings.report.mode = ReportMode.BatchAfterStop;
            // Reading tags for 3 seconds every 4 seconds
            settings.autoStart.mode = AutoStartMode.Periodic
            settings.autoStart.periodInMs = this.periodoAutoInicio.toLong()
            settings.autoStop.mode = AutoStopMode.Duration
            settings.autoStop.durationInMs = this.duracionAutoDetencion.toLong()
            // Enable keepalives
            settings.keepalives.enabled = true
            settings.keepalives.periodInMs = this.periodoKeepAlives.toLong()
            // Enable link monitor mode.
            // If our application fails to reply to
            // five consecutive keepalive messages,
            // the reader will close the network connection.
            settings.keepalives.enableLinkMonitorMode = true
            settings.keepalives.linkDownThreshold = 2
            settings.holdReportsOnDisconnect = true

            // Trabajar con la antena # 1. Disable all others.
            settings.antennas.disableAll()
            for (antenaId in this.antenas.keys) {
                val antena = this.antenas[antenaId]!!
                if (antena.activa) {
                    val puertoAntena = antena.puerto
                    settings.antennas.enableById(shortArrayOf(puertoAntena))
                    settings.antennas.getAntenna(puertoAntena).isMaxTxPower = true
                    settings.antennas.getAntenna(puertoAntena).isMaxRxSensitivity = true
                    settings.antennas.getAntenna(puertoAntena).portNumber = puertoAntena
                    settings.antennas.getAntenna(puertoAntena).txPowerinDbm = antena.potencia
                }
            }
            // Aplicar la configuración
            reader.applySettings(settings)

            reader.tagReportListener = OnTagsReported()
            this.reader = reader
            this.activo = true
        }
        catch (ex: Exception) {
            Reporte.excepcion(ex)
            this.reader = null
            this.activo = false
            return false
        }

        return true
    }

    // Comienza la lectura de los datos enviados por los EPPs
    fun iniciarLectura() {
        if (!activo || this.reader == null) {
            return
        }

        try {
            this.reader!!.start()
        }
        catch (ex: Exception) {
            Reporte.excepcion(ex)
        }
    }

    // Finalizar la lectura y desconectar del lector
    fun finalizarLectura() {
        if (!activo || this.reader == null) {
            return
        }

        try {
            this.reader!!.stop()
            this.reader!!.disconnect()
            this.activo = false
            this.reader = null
        }
        catch (ex: Exception) {
            Reporte.excepcion(ex)
        }
    }

    // Objeto para realizar el proceso de serialización
    companion object {
        /**
         * Permite crear un lector con la información que se trae desde el archivo de
         * configuración en formato HCL
         */
        @JvmStatic
        fun leerDesdeConfiguracionHCL(nombreLector: String, lectorInfo: Map<String, Any?>): Lector? {
            if (lectorInfo.isNotEmpty()) {
                val direccionIP = lectorInfo["ip"]!!.toString()
                val descripcion = lectorInfo["descripcion"]!!.toString()
                val estado = lectorInfo["estado"]!!.toString().uppercase()
                val periodoAutoInicio = lectorInfo["periodoAutoInicio"]!!.toString().toDouble().toInt()
                val duracionAutoDetencion = lectorInfo["duracionAutoDetencion"]!!.toString().toDouble().toInt()
                val periodoKeepAlives = lectorInfo["periodoKeepAlives"]!!.toString().toDouble().toInt()
                val lector = Lector(direccionIP, nombreLector, descripcion, periodoAutoInicio, duracionAutoDetencion, periodoKeepAlives,estado)
                val antenasEntrada = lectorInfo["antena"] as Map<String, Any?>?
                if (antenasEntrada != null) {
                    for (antenaIdentificador in antenasEntrada.keys) {
                        val antenaInfo = antenasEntrada[antenaIdentificador] as Map<String, Any?>
                        val antena = Antena.leerDesdeConfiguracionHCL(antenaIdentificador, antenaInfo)
                        if (antena != null) {
                            antena.lector = lector
                            lector.antenas[antenaIdentificador] = antena
                        }
                    }
                }
                return lector
            }
            return null
        }
    }
}

class OnTagsReported : TagReportListener {
    override fun onTagReported(reader: ImpinjReader?, report: TagReport?) {
        val tags: List<Tag> = report!!.tags

        for (t in tags) {
            val tagID = t.epc.toHexString()
            val lectorIP = reader!!.address
            val tiempo = t.lastSeenTime.localDateTime.time
            val contador = t.tagSeenCount.toInt()

            Reporte.info("EPC $tagID de $lectorIP (Tiempo = $tiempo)")
            val escenario = Escenario.instance()
            if (escenario != null) {
                if (escenario.esValidaLaLectura(tagID, lectorIP)) {
                    val epp = escenario.epps[tagID]
                    epp?.registrarLectura(contador, tiempo)
                }
            }
        }
    }
}
