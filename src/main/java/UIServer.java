
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
    public static JDialog frame;
    static Logger log;

    static {
        log = LogManager.getLogger();
    }

    // *************************************************************************
    // *        Setting up the frame
    // *************************************************************************
    public static void createAndShowGUI() {
        log.debug("Created GUI on Event Dispatcher Thread: " + SwingUtilities.isEventDispatchThread());

        frame = new JDialog();

        GridBagLayout experimentLayout = new GridBagLayout();
        frame.setLayout(experimentLayout);

        frame.setLocation(200, System.getProperty("os.name").toLowerCase().startsWith("mac") ? 25 : 0 );
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setFocusableWindowState(false);

        // Make it invisible
        frame.getRootPane().setOpaque(false);
        frame.getContentPane().setBackground(new Color(0, 0, 0, 0));
        frame.setBackground(new Color(0, 0, 0, 0));

        frame.setType(Window.Type.UTILITY);

        JButton mainMenu = new MainMenuButton();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.PAGE_START;

        frame.add(mainMenu, c);

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

    static void addList(final String sListId, final List<Card> listOfCards, List<org.trello4j.model.List> listsInBoard) {
        log.debug("Adding list " + sListId);
        ListPanel existingList = getListPanelForList(sListId);
        if (existingList == null) {
            // Add new ListPanel to the frame
            ContainerPanel cp = new ContainerPanel(sListId, listOfCards, listsInBoard);

            // Add ListPanel (JPanel) to main Window (JDialog)
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.PAGE_START;
            frame.add(cp, c);

            log.info("Done adding Jpanel to the frame for list " + sListId);
            frame.pack();

        } else {
            // Merge into existing List
            log.info("about to add list " + sListId + " to an existing list" );
            existingList.mergeCards(listOfCards);
        }
    }

    public static ListPanel getListPanelForList(String sListId) {
        for(Component c : frame.getContentPane().getComponents()) {
            if (c instanceof ContainerPanel) {
                if (((ContainerPanel)c).getListId().equals(sListId)) {
                    return ((ContainerPanel)c).getListPanel();
                }
            }
        }
        return null;
    }

    public static void moveWindowTo(Point position) {
        frame.setLocation((int)position.getX(), (int)position.getY());
    }

    public static void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}

