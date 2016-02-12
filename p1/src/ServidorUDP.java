
import java.net.*;

/**
 * Implementa un servidor de eco usando UDP.
 *
 * @author d.mendez.alvarez@udc.es
 */
public class ServidorUDP {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("> Formato: ServidorUDP <puerto>");
            System.exit(-1);
        }

        DatagramSocket sDatagram = null;

        try {
            // Creamos el socket del servidor asociado a un puerto específico
            // (recibido en el primer argumento por linea de comandos)
            // Establecemos un timeout de 30 segs
            sDatagram = new DatagramSocket(Integer.parseInt(args[0]));
            sDatagram.setSoTimeout(30000);

            while (true) {
                // Preparamos un datagrama para recepción
                byte array[] = new byte[1024];
                DatagramPacket dgramRec = new DatagramPacket(array, array.length);

                // Recibimos el mensaje
                sDatagram.receive(dgramRec);
                System.out.println("> SERVIDOR: Recibido "
                        + new String(dgramRec.getData(), 0, dgramRec.getLength())
                        + " de " + dgramRec.getAddress().toString() + ":"
                        + dgramRec.getPort());

                // Preparamos el datagrama que vamos a enviar
                DatagramPacket dgramEnv = new DatagramPacket(dgramRec.getData(),
                        dgramRec.getLength(), dgramRec.getAddress(), dgramRec.getPort());

                // Enviamos el mensaje
                sDatagram.send(dgramEnv);
                System.out.println("> SERVIDOR: Enviando "
                        + new String(dgramEnv.getData()) + " a "
                        + dgramEnv.getAddress().toString() + ":"
                        + dgramEnv.getPort());
            }
        } catch (SocketTimeoutException e) {
            System.err.println("> 30 seg sin recibir nada.");
        } catch (Exception e) {
            System.err.println("> Error: " + e.getMessage());
            e.printStackTrace(System.err);
        } finally {
            // Cerramos el socket
            sDatagram.close();
        }
    }
}
