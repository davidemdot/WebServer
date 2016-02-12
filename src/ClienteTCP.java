


import java.net.*;
import java.io.*;

/**
 * Implementa un cliente de eco usando TCP.
 *
 * @author d.mendez.alvarez@udc.es
 */
public class ClienteTCP {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Formato: ClienteTCP <maquina> <puerto> <mensaje>");
            System.exit(-1);
        }

        Socket socket = null;

        try {
            // Obtenemos la dirección IP del servidor
            InetAddress dirServidor = InetAddress.getByName(args[0]);
            // Obtenemos el puerto del servidor
            int puertoServidor = Integer.parseInt(args[1]);
            // Obtenemos el mensaje
            String mensaje = args[2];

            // Creamos el socket y establecemos la conexión con el servidor
            socket = new Socket(dirServidor, puertoServidor);
            System.out.println("CLIENTE: Conexion establecida con "
                    + dirServidor.toString() + " al puerto " + puertoServidor);

            // Establecemos un timeout de 30 segs
            socket.setSoTimeout(30000);

            // Establecemos el canal de entrada
            BufferedReader sEntrada = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // Establecemos el canal de salida
            PrintWriter sSalida = new PrintWriter(socket.getOutputStream(), true);

            // Enviamos el mensaje al servidor
            System.out.println("CLIENTE: Enviando " + mensaje);
            sSalida.println(mensaje);

            // Recibimos la respuesta del servidor
            String recibido = sEntrada.readLine();
            System.out.println("CLIENTE: Recibido " + recibido);

            // Cerramos los flujos y el socket para liberar la conexión
            sSalida.close();
            sEntrada.close();
        } catch (SocketTimeoutException e) {
            System.err.println("30 segs sin recibir nada");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
