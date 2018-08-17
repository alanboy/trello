
import com.google.gson.*;
import java.awt.*;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import org.apache.logging.log4j.*;
import org.trello4j.*;
import org.trello4j.model.*;

public class UIServer {
    public static JDialog trelloFrame;
    public static JDialog pomodoroFrame;
    static Logger log;

    static {
        log = LogManager.getLogger();
    }

    // *************************************************************************
    // *        Setting up the trelloFrame
    // *************************************************************************
    public static void createAndShowGUI() {
        log.debug("Created GUI on Event Dispatcher Thread: " + SwingUtilities.isEventDispatchThread());

        trelloFrame = new JDialog();

        GridBagLayout experimentLayout = new GridBagLayout();
        trelloFrame.setLayout(experimentLayout);

        trelloFrame.setLocation(200, System.getProperty("os.name").toLowerCase().startsWith("mac") ? 25 : 0 );
        trelloFrame.setUndecorated(true);
        trelloFrame.setAlwaysOnTop(true);
        trelloFrame.setFocusableWindowState(false);

        // Make it invisible
        trelloFrame.getRootPane().setOpaque(false);
        trelloFrame.getContentPane().setBackground(new Color(0, 0, 0, 0));
        trelloFrame.setBackground(new Color(0, 0, 0, 0));

        trelloFrame.setType(Window.Type.UTILITY);

        JButton mainMenu = new MainMenuButton();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;

        trelloFrame.add(mainMenu, c);

        // If you dont do this, the main thread will die and so will
        // the entire application.
        trelloFrame.pack();
        trelloFrame.setVisible(true);

        log.debug("Created UI. Freame object is " + trelloFrame );

        try {
            // Start TrelloClient WorkerThread, this call is non-blocking
            TrelloClient.GetInstance().execute();

        } catch (Throwable t) {
            log.info(t);
        }

        log.debug("createAndShowGUI thread ends");
    }

    static void endPomodoroTimerForCard() {
        pomodoroFrame.setVisible(false);
        trelloFrame.setVisible(true);
    }

    static void startPomodoroTimerForCard(Card card, CardButton cardButton) {
        log.debug("Created GUI on Event Dispatcher Thread: " + SwingUtilities.isEventDispatchThread());

        pomodoroFrame = new JDialog();

        GridBagLayout experimentLayout = new GridBagLayout();
        pomodoroFrame.setLayout(experimentLayout);

        pomodoroFrame.setLocation(200, System.getProperty("os.name").toLowerCase().startsWith("mac") ? 25 : 0 );
        pomodoroFrame.setUndecorated(true);
        pomodoroFrame.setAlwaysOnTop(true);
        pomodoroFrame.setFocusableWindowState(false);

        // Make it invisible
        pomodoroFrame.getRootPane().setOpaque(false);
        pomodoroFrame.getContentPane().setBackground(new Color(0, 0, 0, 0));
        pomodoroFrame.setBackground(new Color(0, 0, 0, 0));

        pomodoroFrame.setType(Window.Type.UTILITY);

        JButton mainMenu = new MainMenuButton();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;

        pomodoroFrame.add(mainMenu, c);

        // If you dont do this, the main thread will die and so will
        // the entire application.
        pomodoroFrame.pack();
        pomodoroFrame.setVisible(true);

        trelloFrame.setVisible(false);

        log.debug("Created UI. Freame object is " + pomodoroFrame );

        PomodoroContainerPanel cp = new PomodoroContainerPanel(card, cardButton);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.PAGE_START;
        pomodoroFrame.add(cp, c2);

        log.info("Done adding Jpanel to the pomodoroFrame for list " );
        pomodoroFrame.pack();

        log.debug("createAndShowGUI thread ends");
    }

    static void addList(final String sListId, final List<Card> listOfCards, List<org.trello4j.model.List> listsInBoard) {
        log.debug("Adding list " + sListId);
        ListPanel existingList = getListPanelForList(sListId);
        if (existingList == null) {
            // Add new ListPanel to the trelloFrame
            ContainerPanel cp = new ContainerPanel(sListId, listOfCards, listsInBoard);

            // Add ListPanel (JPanel) to main Window (JDialog)
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.PAGE_START;
            trelloFrame.add(cp, c);

            log.info("Done adding Jpanel to the trelloFrame for list " + sListId);
            trelloFrame.pack();

        } else {
            // Merge into existing List
            log.info("about to add list " + sListId + " to an existing list" );
            existingList.mergeCards(listOfCards);
        }
    }

    public static ListPanel getListPanelForList(String sListId) {
        for(Component c : trelloFrame.getContentPane().getComponents()) {
            if (c instanceof ContainerPanel) {
                if (((ContainerPanel)c).getListId().equals(sListId)) {
                    return ((ContainerPanel)c).getListPanel();
                }
            }
        }
        return null;
    }

    public static void moveWindowTo(Point position) {
        trelloFrame.setLocation((int)position.getX(), (int)position.getY());
    }

    public static void setVisible(boolean visible) {
        trelloFrame.setVisible(visible);
    }
}

