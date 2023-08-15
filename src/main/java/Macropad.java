import com.google.gson.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import org.apache.logging.log4j.*;
import org.trello4j.model.Card;


public class Macropad {
    //private static final String Macropad_SCRIPTS_BASE_PATH = "c:\\Users\\alanb\\code\\trello\\device\\";
    //private static final String Macropad_DEVICE_BASE_PATH = "e:\\";
    //private String state = "";
    private Logger logger;
    private static Macropad instance;

    private Macropad() {
        logger = LogManager.getLogger();
    }

    public static Macropad getInstance() {
        if (instance == null) {
            instance = new Macropad();
        }
        return instance;
    }

    public void Write() {

        PrintWriter printWriter = null;
        try{
            List<TrelloCardsWithInfo> results = null;
            results = TrelloClient.GetInstance().getBoardListCardHierarchy();

            //try {
            for (TrelloCardsWithInfo info : results) {
                logger.info("=====>Board name " + info.getBoard().getName());
                logger.info("=====>list " + info.getList().getName());

                //log.info("-->" +  b.getName());
                //l.getName(), l.getId(), listIsEnabled
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            //Gson gson = new Gson();

            String jsonified = gson.toJson(results);
            logger.info(jsonified);


            File file = new File("d:\\data.json");
            printWriter = new PrintWriter(file);

            String line;
            //while ((line = br.readLine()) != null) {
            //    if (line.contains("REPLACE_TIME_HERE")) {
            //        line = "TimeInSeconds = " + (minutes * 60);
            //    }

            //    printWriter.println(line);
            //}
            printWriter.println(jsonified);
        } catch (Exception e) {
            logger.error(e);
            //System.out.println(e);

        } finally {
            printWriter.close();

        }
    }

}
