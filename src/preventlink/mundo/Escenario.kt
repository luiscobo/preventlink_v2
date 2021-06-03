/**~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: Escenario.kt, v 1.0 2021/05/12 17:24 lacobo $
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

import com.bertramlabs.plugins.hcl4j.HCLParser
import java.io.File

/**
 * Un escenario contiene lo necesario para trabajar con una configuración
 * en una instalación dada
 */
data class Escenario(val nombreArchivo: String) {
    // Atributos del escenario

    var nombre: String = ""

    var lectores: HashMap<String, Lector> = HashMap()

    var maquinas: HashMap<String, Maquina> = HashMap()

    var gpios: HashMap<String, GPIO> = HashMap()

    var epps: HashMap<String, EPP> = HashMap()

    var configuraciones: HashMap<String, Configuracion> = HashMap()

    var monitorNombre: String = "MonitorPreventLink"

    var monitorDemoraInicial: Long = 1000L

    var monitorPeriodo: Long = 1000L

    private var configuracionActiva: String = ""

    val estaVacio: Boolean
        get() = configuracionActiva == ""

    // Métodos de los escenarios

    init {
        val archivo: File = File(nombreArchivo)
        val res = HCLParser().parse(archivo, "UTF-8")
        if (res != null) {
            // Primero los lectores
            val lectorInfoLista = res["lector"] as Map<String, Any?>?
            if (lectorInfoLista != null) {
                for (lectorNombre in lectorInfoLista.keys) {
                    val lectorInfo = lectorInfoLista[lectorNombre] as Map<String, Any?>
                    val lector = Lector.leerDesdeConfiguracionHCL(lectorNombre, lectorInfo)
                    if (lector != null) {
                        this.lectores[lectorNombre] = lector
                    }
                }
            }
            // Ahora vamos con los GPIOs
            val gpioInfoLista = res["gpio"] as Map<String, Any?>?
            if (gpioInfoLista != null) {
                for (gpioID in gpioInfoLista.keys) {
                    val gpioInfo = gpioInfoLista[gpioID] as Map<String, Any?>
                    val gpio = GPIO.leerDesdeConfiguracionHCL(gpioID, gpioInfo)
                    if (gpio != null) {
                        this.gpios[gpioID] = gpio
                    }
                }
            }
            // Ahora vamos con las máquinas
            val maquinaInfoLista = res["maquina"] as Map<String, Any?>?
            if (maquinaInfoLista != null) {
                for (maquinaID in maquinaInfoLista.keys) {
                    val maquinaInfo = maquinaInfoLista[maquinaID] as Map<String, Any?>
                    val maquina = Maquina.leerDesdeConfiguracionHCL(maquinaID, maquinaInfo)
                    if (maquina != null) {
                        this.maquinas[maquinaID] = maquina
                    }
                }
            }
            // Ahora vienen los EPPs
            val eppInfoLista = res["epp"] as Map<String, Any?>?
            if (eppInfoLista != null) {
                for (eppID in eppInfoLista.keys) {
                    val eppInfo = eppInfoLista[eppID] as Map<String, Any?>
                    val epp = EPP.leerDesdeConfiguracionHCL(eppID, eppInfo)
                    if (epp != null) {
                        this.epps[eppID] = epp
                    }
                }
            }
            // Finalmente nos quedamos con las configuraciones
            val configInfoLista = res["configuracion"] as Map<String, Any?>?
            if (configInfoLista != null) {
                for (confID in configInfoLista.keys) {
                    val configInfo = configInfoLista[confID] as Map<String, Any?>
                    val config = Configuracion.leerDesdeConfiguracionHCL(confID, configInfo)
                    if (config != null) {
                        this.configuraciones[confID] = config
                    }
                }
            }
            // Y ahora obtenemos los datos adicionales
            if (res["configuracionActiva"] != null) {
                this.configuracionActiva = res["configuracionActiva"].toString()
            }
            if (res["escenario"] != null) {
                this.nombre = res["escenario"].toString()
            }
            if (res["monitor"] != null) {
                val monitorInfo = res["monitor"] as Map<String, Any?>
                if (monitorInfo["nombre"] != null) {
                    monitorNombre = monitorInfo["nombre"]!!.toString()
                }
                if (monitorInfo["esperaInicial"] != null) {
                    monitorDemoraInicial = monitorInfo["esperaInicial"]!!.toString().toDouble().toLong()
                }
                if (monitorInfo["periodo"] != null) {
                    monitorPeriodo = monitorInfo["periodo"]!!.toString().toDouble().toLong()
                }
            }
        }
    }

    /**
     * Este método obtiene la configuración en HCL del escenario
     */
    fun convertirHCL(): String {
        val builder = StringBuilder()

        // Los elementos principales
        builder.appendLine("escenario = \"$nombre\"")
        builder.appendLine("configuracionActiva = \"$configuracionActiva\"")
        // Ahora vienen los lectores
        for (lector in lectores.values) {
            lector.convertirHCL(builder)
        }
        // Ahora vienen los GPIOs
        for (gpio in gpios.values) {
            gpio.convertirHCL(builder)
        }
        // Ahora vienen las máquinas
        for (maquina in maquinas.values) {
            maquina.convertirHCL(builder)
        }
        // Ahora vienen los EPPs
        for (epp in epps.values) {
            epp.convertirHCL(builder)
        }
        // Finalmente las configuraciones
        for (config in configuraciones.values) {
            config.convertirHCL(builder)
        }
        // Retornamos
        return builder.toString()
    }

    /**
     * Este es el singleton de la clase Escenario
     */
    companion object {
        @Volatile
        @JvmStatic
        private var INSTANCE: Escenario? = null

        fun instance(archivo: String = "./resource/configuracion.tf"): Escenario {
            if (INSTANCE == null) {
                INSTANCE = Escenario(archivo)
            }
            return INSTANCE!!
        }
    }

    /**
     * Permite obtener la configuración activa y actual del archivo de escenarios
     */
    fun configuracionActual(): Configuracion? {
        if (estaVacio) {
            return null
        }

        return configuraciones[configuracionActiva]
    }

    /**
     * Permite obtener el lector de la configuración actual
     */
    fun lectorConfiguracionActual(): Lector? {
        val conf = configuracionActual()
        if (conf != null) {
            return lectores[conf.lectorID]
        }
        return null
    }

    /**
     * Permite obtener el gpio de la configuracion actual
     */
    fun gpioConfiguracionActual(): GPIO? {
        val conf = configuracionActual()
        if (conf != null) {
            return gpios[conf.gpioID]
        }
        return null
    }

    /**
     * Permite obtener la máquina de la configuracion actual
     */
    fun maquinaConfiguracionActual(): Maquina? {
        val conf = configuracionActual()
        if (conf != null) {
            return maquinas[conf.maquinaID]
        }
        return null
    }

    /**
     * Llegó una lectura, hay que validar que la EPP y la IP están correctas
     */
    fun esValidaLaLectura(tagId: String, lectorDireccionIP: String): Boolean {
        val conf = configuracionActual() ?: return false
        val lector = lectorConfiguracionActual() ?: return false
        if (lector.IP == lectorDireccionIP) {
            if (tagId in conf.epps) {
                return true
            }
        }
        return false
    }


}

