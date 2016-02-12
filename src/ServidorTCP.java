
import java.net.*;
import java.io.*;

/**
 * Implementa un servidor multihilo de eco usando TCP.
 *
 * @author d.mendez.alvarez@udc.es
 */
public class ServidorTCP {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("> Formato: ServidorTCP <puerto>");
            System.exit(-1);
        }

        ServerSocket sServidor = null;

        try {
            // Creamos el socket del servidor asociado a un puerto específico
            // (recibido en el primer argumento por linea de comandos)
            // Establecemos un timeout de 30 segs
            sServidor = new ServerSocket(Integer.parseInt(args[0]));
            sServidor.setSoTimeout(30000);

            while (true) {
                // Esperamos posibles conexiones
                Socket sCliente = sServidor.accept();
                System.out.println("> SERVIDOR: Conexion establecida con "
                        + sCliente.getInetAddress().toString() + ":"
                        + sCliente.getPort());

                // Creamos un objeto ThreadServidor, pasándole la nueva conexión
                // Iniciamos su ejecución con el método start()
                ThreadServidor thread = new ThreadServidor(sCliente);
                thread.start();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("> 30 segs sin recibir nada");
        } catch (Exception e) {
            System.err.println("> Error: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            //Cerramos el socket
            try {
                sServidor.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
