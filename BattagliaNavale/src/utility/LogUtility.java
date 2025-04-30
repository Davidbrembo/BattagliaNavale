package utility;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.*;

public class LogUtility {

    private static Logger logger = Logger.getLogger("BattagliaNavaleLogger");

    static {
        try {
            // Crea automaticamente la cartella "log" se non esiste
            Path logDir = Path.of("log");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            // Configura il file di log
            FileHandler fileHandler = new FileHandler("log/battaglia_navale.log", true); // append = true
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            logger.setUseParentHandlers(true); // stampa anche in console

        } catch (IOException e) {
            System.out.println("Errore nella configurazione del logger: " + e.getMessage());
        }
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void error(String msg) {
        logger.severe(msg);
    }

    public static void warning(String msg) {
        logger.warning(msg);
    }
}