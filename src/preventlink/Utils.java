/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * $Id: EPP.kt, v 1.0 2021/05/12 17:24 lacobo $
 * Universidad Ean (Bogotá - Colombia)
 * Grupo de Investigación Tecnológico ONTARE
 * Licenciado bajo el esquema Academic Free License version 2.1
 * <p>
 * Proyecto PreventLink
 * Autor: Luis Cobo - 23/05/2021
 * Modificado por:
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package preventlink;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

/**
 * En esta clase tendremos varias funciones de utilidad para el resto
 * de partes de la aplicación.
 */
public class Utils {
    private static final String USER_AGENT = "Mozilla/5.0";

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

    public static String obtenerEstado(String direccionIP, int puerto) {
        try {
            String url = String.format("http://%s:%d/status", direccionIP, puerto);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            //System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // print result
                return response.toString();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
