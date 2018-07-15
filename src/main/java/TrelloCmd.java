import com.google.gson.*;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import org.apache.logging.log4j.*;
import org.trello4j.*;
import org.trello4j.model.*;

/**
 * Anything that has to do with reading or writing to
 * the terminal.
 *
 * */
public class TrelloCmd {
    private static TrelloClient tClient;
    // We should get this from TrelloClient, only trello client should
    // know about trello.com
    final static String url = "https://trello.com/1/authorize?key="+
                            TrelloClient.getKey()
                            +"&expiration=90days&name=trelloc&response_type=token&scope=read,write,account";

    static Logger log;

    // *************************************************************************
    // *        Trello Configuration Stuff
    // *************************************************************************
    private static void updateJsonToken() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SimpleSwingBrowser browser = new SimpleSwingBrowser();
                browser.setVisible(true);
                browser.loadURL(url);
            }
        });
    }

    public static void writeNewToken(String token) {
        tClient.writeTokenToConfig(token);
    }

    // *************************************************************************
    // *
    // *************************************************************************
    private static void showMyBoardsAndLists() throws Exception {
        for (Board b : tClient.getMyBoards()) {
            System.out.println("Board: " + b.getName() + " - " + b.getId());
            for (org.trello4j.model.List l : tClient.getListsFromBoard(b)) {
                System.out.println("    List: " + l.getName() + " - " + l.getId());
            }
        }
    }

    // *************************************************************************
    // *       Entry point & parsing command line
    // *************************************************************************
    public static void main(String[] args) {

        log = LogManager.getLogger();

        log.info ("/-------------------------------------------------\\");
        log.info ("|        Trello version " + About.Version + " started      |");
        log.info ("|                                                 |");
        log.info ("| Built: " + About.BuiltByUser + "(" + About.BuiltByMachine + ")");
        log.info ("|        " + About.DateTime + " ");
        log.info ("\\-------------------------------------------------/");
        tClient = TrelloClient.GetInstance();

        boolean bDontUpdate = false;
        for (String arg : args) {
            if (arg.equals("-noupdate")) {
                bDontUpdate = true;
                break;
            }
        }

        if (!bDontUpdate) {
            tClient.checkForSoftwareUpdate();
        }

        if (!tClient.configExist()) {
            log.info("Config file does not exist");

            // Create new empty file now.
            if (!tClient.newConfigFile()) {
                log.error("Could not create config file, ill just exit.");
                return;
            }

            updateJsonToken();
            return;
        }

        try {
            if (args.length > 0 && args[0].equals("-c")) {
                tClient.initialize(args[1]);
            } else {
                tClient.initialize();
            }

        } catch (Exception e) {
            log.error("Unable to initialize client. I'll try to update the config. The problem was:" + e);
            updateJsonToken();
            return;
        }

        if (args.length > 0 && args[0].equals("-?")) {
            String NEW_LINE = System.getProperty("line.separator");
            System.out.println(
                    "trello client 0.1" + NEW_LINE
                    + "-? this help " + NEW_LINE
                    + "-noupdate don't update" + NEW_LINE
                    + "-b show boards I have access to" + NEW_LINE 
                    + "-c <config> use this config file" + NEW_LINE );
            return;
        }

        if (args.length > 0 && args[0].equals("-b")) {
            try {
                showMyBoardsAndLists();
            } catch(Exception e) {
                log.error(e);
            }
            return;
        }

        boolean configHasLists = tClient.loadLists();

        if (!configHasLists) {
            new TrelloConfigurationWindow();
            //return;
        }

        // Start UI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UIServer.createAndShowGUI();
            }
        });

        log.info("Main thread ends");
    }

}

