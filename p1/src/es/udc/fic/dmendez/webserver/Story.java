package es.udc.fic.dmendez.webserver;

import java.io.*;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Crowdstorying: many people continuing a story knowing only the last sentence.
 *
 * @author David Méndez (d.mendez.alvarez@udc.es)
 */
public class Story implements MiniServlet {

    private final File storyFile;
    private final File storyHTML;

    public Story() {
        storyFile = new File("private/story.txt");
        storyHTML = new File("www/story.html");
    }

    @Override
    public String doGet(Map<String, String> parameters) {
        PrintWriter out = null;
        BufferedReader in = null;
        String newLine = "";
        String story = "";
        String currentLine = "";

        try {
            newLine = URLDecoder.decode(parameters.get("line"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
        }

        try {
            out = new PrintWriter(new FileWriter(storyFile, true));
            out.println(newLine);
        } catch (FileNotFoundException e) {
            System.err.println("> Error: Could not load the file");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            if (out != null) {
                out.close();
            }
        }

        try {
            in = new BufferedReader(new FileReader(storyFile));
            while ((currentLine = in.readLine()) != null) {
                story += currentLine + "<br />";
            }
        } catch (FileNotFoundException e) {
            System.err.println("> Error: Could not load the file");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }

        modifyHTML(newLine);

        return "<html><head><title>Crowdstorying</title></head>"
                + "<body>" + story + "</body></html>";
    }

    private void modifyHTML(String newLine) {
        PrintWriter out = null;

        String newHTML = "<html><head><title>Crowdstorying</title></head><body>"
                + newLine + "<br /><form action='Story.do' method='get'><input "
                + "type='text' name='line' /><input type='submit' value='Contin"
                + "ue the story!'/></div></form><body></html>";

        try {
            out = new PrintWriter(new FileWriter(storyHTML));
            out.println(newHTML);
        } catch (FileNotFoundException e) {
            System.err.println("> Error: Could not load the file");
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
