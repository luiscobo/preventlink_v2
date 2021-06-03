escenario = "Escenario de prueba"

monitor {
  nombre = "MonitorPrincipal"
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
    activa = "NO"
  }
}

configuracion "prueba-23-05-2021" {
  lector = "lector1"
  tiempoDeAusenciaMinimo = 10000   # En milisegundos
  tiempoDeAusenciaMaximo = 15000   # Tiempo en milisegundos
  maquina = "torno01"
  gpio = "gpioPrincipal"
  epps = ["AD29140012199986600000AE", "AD29140012191B88640000A0"]
}

configuracionActiva = "prueba-23-05-2021"

gpio "gpioPrincipal" {
  ip = "192.168.1.50"
  puertoConexion = 80
  puertoVerde = 9
  puertoAmbar = 8
  puertoRojo = 7
  puertoRele = 2
}

epp "AD29140012191B88640000A0" {
  codigo = "AD29140012191B88640000A0"
  nombre = "casco"
  descripcion = "Este es el identificador del casco"
  esVerificacion = "SI"
}

epp "AD29140012199986600000AE" {
  codigo = "AD29140012199986600000AE"
  nombre = "gafas"
  descripcion = "Este el RFID para las gafas que lleva el operador"
  esVerificacion = "SI"
}

maquina "torno01" {
  tipo = "Maquinaria de laboratorio"
  nombre = "Torno"
  estado = "OK"
  descripcion = "El torno del laboratorio"
}
