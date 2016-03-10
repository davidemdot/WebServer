package es.udc.fic.dmendez.webserver;

import java.io.*;
import java.util.Properties;

/**
 * Deals with the configuration file of the web server.
 *
 * @author David Méndez (d.mendez.alvarez@udc.es)
 */
public class Configuration {

    private final Properties properties;

    public Configuration(String path) {
        Properties prop = new Properties();
        InputStream file = null;

        try {
            file = new FileInputStream(path);
            prop.load(file);
        } catch (FileNotFoundException e) {
            System.err.println("> Error: Could not load the configuration file");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

        properties = prop;
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty("PORT"));
    }

    public String getDirectoryIndex() {
        return properties.getProperty("DIRECTORY_INDEX");
    }

    public String getDirectory() {
        return properties.getProperty("DIRECTORY");
    }

    public boolean getAllow() {
        return "YES".equals(properties.getProperty("ALLOW"));
    }

    public String getLogsDirectory() {
        return properties.getProperty("LOGS_DIRECTORY");
    }
}
