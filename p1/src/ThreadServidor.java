
import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Thread que atiende una conexión de un servidor TCP.
 *
 * @author d.mendez.alvarez@udc.es
 */
public class ThreadServidor extends Thread {

    Socket socket = null;

    public ThreadServidor(Socket s) {
        socket = s;
    }

    private String generaCabecera(Date fecha, File fichero) throws IOException {
        DateFormat rfc1123 = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

        String cabecera = "HTTP/1.0 200 OK\r\n";
        cabecera += "Date: " + rfc1123.format(fecha) + "\r\n";
        cabecera += "Server: deiv/0.1 (Unix)\r\n";
        cabecera += "Last-Modified: " + rfc1123.format(fichero.lastModified()) + "\r\n";
        cabecera += "Content-Length: " + fichero.length() + "\r\n";
        cabecera += "Content-Type: text/plain\r\n";
        cabecera += "\r\n";

        return cabecera;
    }

    @Override
    public void run() {
        try {
            // Establecemos el canal de entrada
            BufferedReader sEntrada = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // Establecemos el canal de salida
            DataOutputStream sSalida = new DataOutputStream(socket.
                    getOutputStream());

            // Recibimos la linea de peticion del cliente
            Date fechaPeticion = Calendar.getInstance().getTime();

            String peticion = sEntrada.readLine();
            String linea = peticion;
            String[] lineaPeticion = linea.split(" ");
            int metodo = 0;
            File fichero = null;

            // Recibe las lineas de cabecera
            while (!(linea = sEntrada.readLine()).equals("")) {
                peticion += "\r\n" + linea;
            }

            System.out.println("> PETICIÓN: " + peticion);

            // Detectamos el metodo (GET/HEAD)
            switch (lineaPeticion[0]) {
                case "GET":
                    metodo = 1;
                    break;
                case "HEAD":
                    metodo = 2;
                    break;
            }

            // Deteccion de errores
            if (lineaPeticion[1] != null) {
                fichero = new File("../../ficheros/", lineaPeticion[1]);

                if (!fichero.exists()) { // Error 404
                    return;
                }
            } else {
                return; // Falta el parametro "fichero"
            }

            // Generamos la salida en funcion del metodo
            switch (metodo) {
                case 0: // Metodo no soportado
                    return;

                case 1: // Metodo GET
                    sSalida.writeBytes(generaCabecera(fechaPeticion, fichero));
                    FileInputStream fileStream = new FileInputStream(fichero);
                    int b;

                    while ((b = fileStream.read()) != -1) {
                        sSalida.write(b);
                    }

                    fileStream.close();
                    sSalida.writeBytes("\r\n");
                    break;

                case 2: // Metodo HEAD
                    sSalida.writeBytes(generaCabecera(fechaPeticion, fichero));
                    break;
            }
            System.out.println("> ENVIADO: " + fichero.toString());

            // Cerramos los flujos
            sSalida.close();
            sEntrada.close();
        } catch (SocketTimeoutException e) {
            System.err.println("> 30 segs sin recibir nada");
        } catch (Exception e) {
            System.err.println("> ERROR: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
