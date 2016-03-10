package es.udc.fic.dmendez.webserver;

import java.util.Map;

/**
 * The servlet interface.
 *
 * @author David Méndez (d.mendez.alvarez@udc.es)
 */
public interface MiniServlet {

    public String doGet(Map<String, String> parameters) throws Exception;

}
