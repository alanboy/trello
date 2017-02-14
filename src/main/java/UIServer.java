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
    static JDialog frame;
    static Logger log;

    static {
        log = LogManager.getLogger();
    }

    // *************************************************************************
    // *        Setting up the frame
    // *************************************************************************
    public static void createAndShowGUI() {

        log.debug("Created GUI on EDT? " + SwingUtilities.isEventDispatchThread());

        frame = new JDialog();

        FlowLayout experimentLayout = new FlowLayout();
        frame.setLayout(experimentLayout);

        frame.setLocation(200, System.getProperty("os.name").toLowerCase().startsWith("mac") ? 25 : 0 );
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setFocusableWindowState(false);
        frame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.RED));
        frame.getContentPane().setBackground(Color.decode("0xe2e4e6"));
        frame.setType(Window.Type.UTILITY);

        JButton mainMenu = new MainMenuButton();
        frame.add(mainMenu);

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
        }

        log.debug("createAndShowGUI thread ends");
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

    public static ListPanel getList(String sListId) {
        for(Component c : frame.getContentPane().getComponents()) {
            if (c instanceof ListPanel) {
                ListPanel lp = ((ListPanel)c);
                if (lp.getListId().equals(sListId)) {
                    return lp;
                }
            }
        }
        return null;
    }

    public static void updateTimes() {
        for(Component c : frame.getContentPane().getComponents()) {
            if (c instanceof ListPanel) {
                ((ListPanel)c).updateTimes();
            }
        }

        frame.pack();
        frame.setVisible(true);
    }

    public static void moveWindowTo(Point position) {
        log.info("Moving window to ... " + (int)position.getX() +","+(int)position.getY());
        frame.setLocation((int)position.getX(), (int)position.getY());
    }

    public static void clearLists() {
        frame.getContentPane().removeAll();
    }

    public static void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}

