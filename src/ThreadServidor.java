
import java.net.*;
import java.io.*;

/**
 * Thread que atiende una conexión de un servidor de eco.
 *
 * @author d.mendez.alvarez@udc.es
 */
public class ThreadServidor extends Thread {

    Socket socket = null;

    public ThreadServidor(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        try {
            // Establecemos el canal de entrada
            BufferedReader sEntrada = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // Establecemos el canal de salida
            PrintWriter sSalida = new PrintWriter(socket.getOutputStream(), true);

            // Recibimos el mensaje del cliente
            String mensaje = sEntrada.readLine();
            System.out.println("> SERVIDOR: Recibido " + mensaje);

            // Enviamos el eco al cliente
            System.out.println("> SERVIDOR: Enviando " + mensaje);
            sSalida.println(mensaje);

            // Cerramos los flujos
            sSalida.close();
            sEntrada.close();
        } catch (SocketTimeoutException e) {
            System.err.println("> 30 segs sin recibir nada");
        } catch (Exception e) {
            System.err.println("> Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
