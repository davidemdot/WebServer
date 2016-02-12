
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Copia un fichero utilizando flujos de datos.
 *
 * @author d.mendez.alvarez@udc.es
 */
public class Copy {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("> Formato: java Copy <fichero origen> <fichero destino>");
            System.exit(-1);
        }

        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(args[0]);
            out = new FileOutputStream(args[1]);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
