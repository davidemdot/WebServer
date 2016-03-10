package es.udc.fic.dmendez.webserver;

import java.io.*;
import java.net.*;

/**
 * A tiny java web server.
 *
 * @author David Méndez (d.mendez.alvarez@udc.es)
 */
public class WebServer {

    private static final int TIMEOUT = 600000; // 10 min
    private final Configuration config;
    private final int serverPort;
    private ServerSocket serverSocket = null;

    // Creates the server socket on a specific port
    public WebServer(int port) {
        config = new Configuration("private/config.properties");

        // If there are no arguments, uses the default port
        serverPort = (port == -1) ? config.getPort() : port;

        try {
            serverSocket = new ServerSocket(serverPort);
            serverSocket.setSoTimeout(TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    // Waits for new connections
    private void listen() {
        try {
            while (true) {
                System.out.println("> Waiting for connections...");
                Socket clientSocket = serverSocket.accept();
                System.out.printf("> Established connection with %s:%d\n",
                        clientSocket.getInetAddress().toString(),
                        clientSocket.getPort());

                // Passes the connection to a new thread and starts it
                ServerThread thread = new ServerThread(config, clientSocket);
                thread.start();
            }
        } catch (SocketTimeoutException e) {
            System.err.println("> Webserver timeout");
        } catch (Exception e) {
            System.err.println("> Error: " + e.getMessage());
        } finally {
            close();
        }
    }

    // Closes the socket
    private void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        int port = (args.length == 0) ? -1 : Integer.parseInt(args[0]);
        new WebServer(port).listen();
    }
}
