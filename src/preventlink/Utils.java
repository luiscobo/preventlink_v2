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
package preventlink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * En esta clase tendremos varias funciones de utilidad para el resto
 * de partes de la aplicación.
 */
public class Utils {
    /**
     * Permite enviar un determinado comando al GPIO que se encuentra en la dirección
     * IP, al puerto dado, y el valor booleano puede ser true para enviar una señal
     * de encendido o false para enviar una señal de apagado
     * @param direccionIP dirección IP del GPIO
     * @param puerto el puerto dado
     * @param comando encender o apagar
     * @return el resultado obtenido
     */
    public static Boolean enviarComandoGPIO(String direccionIP, int puertoComunicacion, int puerto, boolean comando) {
        Socket s = null;
        try {
            s = new Socket(direccionIP, puertoComunicacion);

            OutputStream out = s.getOutputStream();
            InputStream in = s.getInputStream();

            String request = String.format("""
                    GET /?op=output&data=port.%d&data=state.%d\r
                    Accept: */*\r
                    Host: %s\r
                    Connection: Close\r
                    \r
                    """, puerto, comando ? 1 : 0, direccionIP);

            out.write(request.getBytes());

            StringBuffer response = new StringBuffer();
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer, 0, 4096)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    response.append((char) buffer[i]);
                }
            }

            s.close();

            String resp = response.toString();
            if (resp.length() > 0) {
                int ini = resp.indexOf("<html>");
                int fin = resp.indexOf("</html>");
                resp = resp.substring(ini + 8, fin - 2);
                return resp.endsWith("1");
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
