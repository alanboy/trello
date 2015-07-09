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

public class UIServer {
    static JDialog frame;

    public static void createAndShowGUI() {
        frame = new JDialog();
        frame.setLocation(200, System.getProperty("os.name").toLowerCase().startsWith("mac") ? 25 : 0 );
        frame.setResizable(true);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setOpacity(0.9f);

        FlowLayout experimentLayout = new java.awt.FlowLayout();
        frame.setLayout(experimentLayout);

        try {
            // Start TrelloClient WorkerThread, this call is non-blocking
            TrelloClient.GetInstance().execute();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void clearLists() {
        frame.getContentPane().removeAll();
    }

    public static void addList(
            final String sListId, 
            final List<Card> listOfCards,
            List<org.trello4j.model.List> listsInBoard) {

        JPanel f = new ListPanel(sListId, listOfCards, listsInBoard);

        System.out.println("about to add list " + sListId + " to frame" );
        frame.getContentPane().add(f);
        frame.pack();
        frame.setVisible(true);
        System.out.println("Done adding Jpanel to the frame for list " + sListId);
    }


    public static void updateTimes()
    {
        for(Component c : frame.getContentPane().getComponents())
        {
            ((ListPanel)c).updateTimes();
        }
    }

    public static void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}

