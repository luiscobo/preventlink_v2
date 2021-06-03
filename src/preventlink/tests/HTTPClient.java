package preventlink.tests;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HTTPClient {
    public static void HTTPRequest(String ipAddress) throws Exception {
        Socket s = new Socket(ipAddress, 80);

        OutputStream out = s.getOutputStream();
        InputStream in = s.getInputStream();

        String request = "GET /?op=output&data=port.7&data=state.0\r\n" +
                "Accept: */*\r\n" +
                "Host: " + ipAddress + "\r\n" +
                "Connection: Close\r\n\r\n";

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

        System.out.println(response);
    }

    public static void main(String[] args) throws Exception {
        HTTPRequest("192.168.1.50");
    }
}
