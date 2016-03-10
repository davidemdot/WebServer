package es.udc.fic.dmendez.webserver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

/**
 * Resources for the web server.
 *
 * @author David Méndez (d.mendez.alvarez@udc.es)
 */
public class ServerUtils {

    protected static String processDynRequest(String className,
            Map<String, String> parameters) throws Exception {

        MiniServlet servlet;
        Class<?> instance;

        instance = Class.forName("es.udc.fic.dmendez.webserver." + className);
        servlet = (MiniServlet) instance.newInstance();

        return servlet.doGet(parameters);
    }

    protected static String getFileType(String ext) {
        switch (ext) {
            case "gif":
                return "image/gif";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "htm":
            case "html":
                return "text/html";
            case "txt":
                return "text/plain";
            case "ico":
                return "image/x-icon";
            case "css":
                return "text/css";
            case "js":
                return "text/javascript";
            default:
                return "application/octet-stream";
        }
    }

    protected static String listDir(String path, int index) throws IOException {

        Comparator sort = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                File f1 = (File) o1;
                File f2 = (File) o2;
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.compareTo(f2);
                }
            }
        };

        File dir = new File(path);
        if (!dir.isDirectory() || !dir.exists()) {
            throw new IOException("> Error: The folder doesn't exist");
        }

        String folder = dir.getPath().substring(index) + "/";
        String folderParent = dir.getParent().substring(index) + "/";

        File[] files = dir.listFiles();
        Arrays.sort(files, sort);

        String listHTML = String.format("<html><head><title>Welcome to %1$s</ti"
                + "tle><link rel='stylesheet' href='/list/style.css' /></head><"
                + "body><h1>You are in %1$s</h1><div class='wrapper'><pre><hr /"
                + "><br /><img src='/list/folder-parent.png' /> <a href='%2$s'>"
                + "Parent directory</a><br />", folder, folderParent);

        if (files.length == 0) {
            listHTML += "This directory is empty.";
        } else {
            for (File file : files) {
                if (file.isDirectory()) {
                    listHTML += String.format("<img src='/list/folder.png' /> "
                            + "<a href='%1$s%2$s'>%2$s</a><br />", folder,
                            file.getName());
                } else {
                    String[] fileName = file.getName().split("\\.", 2);
                    String ext = (fileName.length == 2) ? fileName[1] : "";

                    String icon = "default";
                    switch (ext) {
                        case "mp3":
                            icon = "audio";
                            break;
                        case "doc":
                            icon = "doc";
                            break;
                        case "htm":
                        case "html":
                            icon = "html";
                            break;
                        case "gif":
                        case "jpg":
                        case "jpeg":
                        case "png":
                        case "ico":
                            icon = "image";
                            break;
                        case "pdf":
                            icon = "pdf";
                            break;
                        case "js":
                        case "css":
                        case "txt":
                            icon = "text";
                            break;
                        case "avi":
                        case "mp4":
                            icon = "video";
                            break;
                        case "zip":
                        case "rar":
                        case "tar.gz":
                            icon = "zip";
                            break;
                    }

                    listHTML += String.format("<img src='/list/%1$s.png' /> <a "
                            + "href='%2$s%3$s'>%3$s</a> - %4$d bytes - %5$s<br "
                            + "/>", icon, folder, file.getName(), file.length(),
                            new Date(file.lastModified()));
                }
            }
        }
        listHTML += "</pre></div></body></html>\r\n";
        return listHTML;
    }
}
