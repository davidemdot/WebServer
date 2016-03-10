package es.udc.fic.dmendez.webserver;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Manages the different connections to the multithreaded web server.
 *
 * @author David Méndez (d.mendez.alvarez@udc.es)
 */
public class ServerThread extends Thread {

    private static final String VERSION = "deiv/1.0";
    private static final DateFormat RFC1123 = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
    private static final int TIMEOUT = 60000; // 1 min

    private final Configuration config;
    private final Socket socket;
    private final InetAddress ip;
    private final String directory_index;
    private final String directory;
    private final boolean allow;
    private final String logs_directory;
    private final PrintWriter accessLog;
    private final PrintWriter errorLog;

    public ServerThread(Configuration c, Socket s) throws IOException {
        config = c;
        socket = s;
        ip = socket.getInetAddress();
        directory_index = config.getDirectoryIndex();
        directory = config.getDirectory();
        allow = config.getAllow();
        logs_directory = config.getLogsDirectory();

        File access = new File(logs_directory, "access.log");
        if (!access.exists()) {
            access.createNewFile();
        }
        accessLog = new PrintWriter(new FileWriter(access, true));

        File error = new File(logs_directory, "error.log");
        if (!error.exists()) {
            error.createNewFile();
        }
        errorLog = new PrintWriter(new FileWriter(error, true));
    }

    private void toAccessLog(Date time, String request, String msg, long size) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
        String accessLine = String.format("[%s] [%s] [%s] [%s] [%d bytes]",
                df.format(time), ip.toString(), request, msg, size);

        accessLog.println(accessLine);
        System.out.println("> Access log: " + accessLine);
    }

    private void toErrorLog(Date time, String request, String msg) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
        String errorLine = String.format("[%s] [%s] [%s] [error: %s]",
                df.format(time), ip.toString(), request, msg);

        errorLog.println(errorLine);
        System.out.println("> Error log: " + errorLine);
    }

    private String errorPage(String request, int code, String msg) throws
            IOException {
        Date time = Calendar.getInstance().getTime();

        String errorHTML = String.format("<html><head><title>Error %1$s</title>"
                + "</head><body><img style='float: left; vertical-align: middle"
                + "' src='/doh.png' /><h1 style='font-weight: bold; font-family"
                + ": serif; text-align: center; font-size: 70px'><br /><br />ER"
                + "ROR %1$s:<br />%2$s</h1></body></html>\r\n", code, msg);

        toErrorLog(time, request, msg);

        return getHeader(time, code, request, null, null, errorHTML.length())
                + errorHTML;
    }

    private String dynPage(String servletName, String args) {
        String dynHTML = null;
        String[] parameters = args.split("&");

        Map<String, String> parametersMap = new HashMap<>();
        for (String p : parameters) {
            String[] keyValue = p.split("=", 2);
            parametersMap.put(keyValue[0], keyValue[1]);
        }

        try {
            dynHTML = ServerUtils.processDynRequest(servletName, parametersMap);
        } catch (Exception e) {
            System.err.println("> Dynamic web page error: " + e.getMessage());
        }

        return dynHTML;
    }

    private String getHeader(Date time, int code, String request, File file,
            String ext, long size) throws IOException {

        // Default values for dynamic and error pages
        String fileExt = "html";
        long fileSize = size;
        long lastModified = time.getTime();

        if (file != null) {
            fileExt = ext;
            fileSize = file.length();
            lastModified = file.lastModified();
        }

        String header = "HTTP/1.1 ";
        switch (code) {
            case 200:
                header += "200 OK";
                toAccessLog(time, request, header, fileSize);
                break;
            case 304:
                header += "304 Not Modified";
                toAccessLog(time, request, header, fileSize);
                break;
            case 400:
                header += "400 Bad Request";
                break;
            case 403:
                header += "403 Forbidden";
                break;
            case 404:
                header += "404 Not Found";
                break;
            default:
                header += "501 Not Implemented";
                break;
        }

        header += "\r\n";
        header += "Date: " + RFC1123.format(time) + "\r\n";
        header += "Server: " + VERSION + "\r\n";
        header += "Content-Length: " + fileSize + "\r\n";
        header += "Content-Type: " + ServerUtils.getFileType(fileExt) + "\r\n";
        header += "Last-Modified: " + RFC1123.format(lastModified) + "\r\n";
        header += "\r\n";

        return header;
    }

    @Override
    public void run() {

        BufferedReader in = null;
        DataOutputStream out = null;

        try {
            // Sets persistent connections
            socket.setKeepAlive(true);
            socket.setSoTimeout(TIMEOUT);

            while (true) {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new DataOutputStream(socket.getOutputStream());

                Date time = Calendar.getInstance().getTime();

                // Checking request line
                String request = in.readLine();
                String[] requestSplitted = request.split(" ");

                if (requestSplitted.length < 2) {
                    out.writeBytes(errorPage(request, 400, "Bad Request"));
                    continue;
                }

                // Other header lines
                Date ifModifiedSince = null;
                String line = ".";
                while (!(line = in.readLine()).equals("")) {
                    if (line.startsWith("If-Modified-Since")) {
                        int index = line.indexOf(':');
                        String header = line.substring(index + 7);
                        DateFormat RFC1123b
                                = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z");
                        try {
                            ifModifiedSince = RFC1123b.parse(header);
                        } catch (ParseException e) {
                            e.printStackTrace(System.err);
                        }
                    }
                }

                // HTTP method
                int method = 0;
                switch (requestSplitted[0]) {
                    case "HEAD":
                        method = 1;
                        break;
                    case "GET":
                        method = 2;
                        break;
                    default:
                        out.writeBytes(errorPage(request, 400, "Bad Request"));
                        continue;
                }

                // Dealing with the file
                File file = new File(directory, requestSplitted[1]);

                if (file.isDirectory()) {
                    File indexFile = new File(file, directory_index);

                    if (indexFile.exists()) {
                        file = indexFile;
                    } else if (allow) {
                        String listHTML = ServerUtils.listDir(file.getPath(),
                                directory.length());
                        out.writeBytes(getHeader(time, 200, request, null, "",
                                listHTML.length()));
                        out.writeBytes(listHTML);
                        continue;
                    } else {
                        out.writeBytes(errorPage(request, 403, "Forbidden"));
                        continue;
                    }
                }

                String[] fileName = file.getName().split("\\.", 2);

                // Dynamic pages
                if (fileName.length == 2 && fileName[1].startsWith("do?")) {
                    String dynHTML = dynPage(fileName[0], fileName[1].
                            substring(3));
                    out.writeBytes(getHeader(time, 200, request, null, "",
                            dynHTML.length()));
                    out.writeBytes(dynHTML);
                    continue;
                }

                if (!file.exists()) {
                    out.writeBytes(errorPage(request, 404, "Not Found"));
                    continue;
                }

                // Checks the If-Modified-Since header
                if (ifModifiedSince != null
                        && ifModifiedSince.getTime() <= file.lastModified()) {
                    method = 3;
                }

                switch (method) {
                    case 1: // HEAD
                        out.writeBytes(getHeader(time, 200, request, file,
                                fileName[1], 0));
                        break;
                    case 2: // GET
                        out.writeBytes(getHeader(time, 200, request, file,
                                fileName[1], 0));
                        FileInputStream fileStream = new FileInputStream(file);
                        int b;
                        while ((b = fileStream.read()) != -1) {
                            out.write(b);
                        }
                        fileStream.close();
                        out.writeBytes("\r\n");
                        break;
                    case 3: // Not modified
                        out.writeBytes(getHeader(time, 304, request, file,
                                fileName[1], 0));
                        break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.err.println("> Thread timeout");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            accessLog.close();
            errorLog.close();
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
