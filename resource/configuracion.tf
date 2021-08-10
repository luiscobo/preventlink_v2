escenario = "Escenario de prueba"

monitor {
  nombre = "MonitorPrincipal"
  esperaInicial = 1000.0
  periodo = 1000.0
}

comunicador {
  nombre = "ComunicadorSensores"
  esperaInicial = 1000.0
  periodo = 1000.0
}

lector "lector1" {
  ip = "192.168.1.51"
  nombre = "lector_principal"
  descripcion = "La antena que está al lado de la mia"
  estado = "ACTIVO"
  periodoAutoInicio = 2000
  duracionAutoDetencion = 500
  periodoKeepAlives = 15000
  antena "antena1" {
    nombre = "Antena Derecha"
    tipo = "Bidireccional"
    puerto = 1
    potencia = 25.0
    activa = "SI"
  }
  antena "antena2" {
    nombre = "Antena Derecha"
    tipo = "Bidireccional"
    puerto = 2
    potencia = 25.0
    activa = "SI"
  }
}

configuracion "prueba-23-05-2021" {
  lector = "lector1"
  tiempoDeAusenciaMinimo = 10000   # En milisegundos
  tiempoDeAusenciaMaximo = 15000   # Tiempo en milisegundos
  maquina = "torno01"
  gpio = "gpioPrincipal"
  epps = ["AD29140012199986600000AE", "AD2914001218B18763000096", "AD291400121911866600009F"]
  sensores = ["careta"]
}

configuracionActiva = "prueba-23-05-2021"

gpio "gpioPrincipal" {
  ip = "192.168.1.50"
  puertoConexion = 80
  puertoVerde = 7
  puertoAmbar = 8
  puertoRojo = 9
  puertoRele1 = 3
  puertoRele2 = 5
  puertoRele3 = 6
  activo = "SI"
}

epp "AD29140012199986600000AE" {
  codigo = "AD29140012199986600000AE"
  nombre = "casco"
  descripcion = "Este es el identificador del casco"
  esVerificacion = "SI"
  activo = "SI"
}

epp "AD291400121A2986650000BC" {
  codigo = "AD291400121A2986650000BC"
  nombre = "gafas"
  descripcion = "Este el RFID para las gafas que lleva el operador"
  esVerificacion = "SI"
  activo = "SI"
}

epp "CCCCFB63AC1F3841EC880467" {
  codigo = "CCCCFB63AC1F3841EC880467"
  nombre = "tapabocas"
  descripcion = "Este el RFID para las gafas que lleva el operador"
  esVerificacion = "SI"
  activo = "SI"
}

epp "AD2914001218B18763000096" {
  codigo = "AD2914001218B18763000096"
  nombre = "tapabocas"
  descripcion = "Este el RFID para las gafas que lleva el operador"
  esVerificacion = "SI"
  activo = "SI"
}

epp "AD291400121911866600009F" {
  codigo = "AD291400121911866600009F"
  nombre = "tapabocas"
  descripcion = "Este el RFID para las gafas que lleva el operador"
  esVerificacion = "SI"
  activo = "SI"
}



maquina "torno01" {
  tipo = "Maquinaria de laboratorio"
  nombre = "Torno"
  estado = "OK"
  descripcion = "El torno del laboratorio"
}

sensor "careta" {
  ip = "192.168.0.29"
  puerto = 80
  activo = "NO"
}
