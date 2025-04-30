package utility;

import java.util.logging.*;

public class LogUtility {

    private static Logger logger = Logger.getLogger("BattagliaNavaleLogger");

    static {
        logger.setUseParentHandlers(false); // Disattiva i gestori predefiniti

        // Crea un ConsoleHandler per stampare solo su console
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(consoleHandler);

        logger.setLevel(Level.ALL);
        consoleHandler.setLevel(Level.ALL);
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
