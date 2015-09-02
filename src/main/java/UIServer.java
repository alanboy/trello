import com.google.gson.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.trello4j.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import org.trello4j.model.*;
import java.awt.*;

import org.apache.logging.log4j.*;

public class UIServer {
    static JFrame frame;
    static Logger log;

    static {
        log = LogManager.getLogger();
    }

    // *************************************************************************
    // *        Setting up the frame
    // *************************************************************************
    public static void createAndShowGUI() {


        log.debug("Created GUI on EDT? "+
                SwingUtilities.isEventDispatchThread());

        frame = new JFrame("Swing Paint Demo");

        FlowLayout experimentLayout = new java.awt.FlowLayout();
        frame.setLayout(experimentLayout);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocation(200, System.getProperty("os.name").toLowerCase().startsWith("mac") ? 25 : 0 );
        frame.setResizable(true);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setOpacity(0.9f);

        JButton jb = new JButton("..");
        frame.add(jb);

        // If you dont do this, the main thread will die and so will
        // the entire application.
        frame.pack();
        frame.setVisible(true);

        log.debug("Created UI. Freame object is " + frame );

        try {
            // Start TrelloClient WorkerThread, this call is non-blocking
            TrelloClient.GetInstance().execute();
        } catch (Throwable t) {
            log.info(t);
            //System.exit(1);
        }

        log.info("createAndShowGUI thread ends");
    }

    public static void addList(
            final String sListId,
            final List<Card> listOfCards,
            List<org.trello4j.model.List> listsInBoard) {

        log.debug("Adding list " + sListId);

        ListPanel existingList = getList(sListId);

        if (existingList == null) {
            // Add new ListPanel to the frame
            JPanel f = new ListPanel(sListId, listOfCards, listsInBoard);

            // Add ListPanel (JPanel) to main Window (JDialog)
            frame.add(f);
            log.info("Done adding Jpanel to the frame for list " + sListId);

            frame.pack();

        } else {
            // Merge into existing List
            log.info("about to add list " + sListId + " to an existing list" );
            existingList.mergeCards(listOfCards);
        }
    }

    // This guy introduced the whole thread problem!!!!!
    public static ListPanel getList(String sListId) {
        //for (Component c : frame.getComponents()) {
        //    ListPanel lp = ((ListPanel)c);
        //    if (lp.getListId().equals(sListId)) {
        //        return lp;
        //    }
        //}
        return null;
    }

    public static void updateTimes() {
        log.debug("updating times...");
        for(Component c : frame.getContentPane().getComponents()) {

            if (c instanceof ListPanel) {
                log.debug("updating times c...");
                ((ListPanel)c).updateTimes();
            }
        }
        frame.pack();
        frame.setVisible(true);
    }


    // *************************************************************************
    // *         Nothing has been refactore from here down
    // *************************************************************************
    public static void clearLists() {
        frame.getContentPane().removeAll();
    }

    public static void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}

