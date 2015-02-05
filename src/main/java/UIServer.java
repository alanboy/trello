
import com.google.gson.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import org.trello4j.*;
import org.trello4j.model.*;

public class UIServer
{
    private static JFrame frame;
    private static int nNumberOfLists = 0;

    public static void createAndShowGUI()
    {
        // Create and set up the window.
        frame = new JFrame("Trello Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocation(1000,0);
        frame.setResizable(true);
        frame.setUndecorated(true);
        frame.setAlwaysOnTop( true );

        // This works on windows, not on Ubuntu+XFCE
        frame.setOpacity(0.9f);

        java.awt.FlowLayout experimentLayout = new java.awt.FlowLayout();
        frame.setLayout(experimentLayout);


        frame.pack();
        frame.setVisible(true);

        try
        {
            // Start TrelloClient WorkerThread,
            // this call is non-blocking
            TrelloClient.GetInstance().execute();
        }
        catch (Exception e)
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void doneUpdating()
    {
        frame.pack();
        frame.setVisible(true);
    }

    public static void updateNewList(final String sListId, List<Card> listOfCards)
    {
        if (0 == listOfCards.size())
        {
           return;
        }

        nNumberOfLists++;

        JButton button = new JButton("");
        button.setText(listOfCards.get(0).getName());
        button.setForeground(Color.blue);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        JPopupMenu buttonPopUp = new JPopupMenu();
        JMenuItem newCardMenu = new JMenuItem("New card");
        JMenuItem showNextCardMenu = new JMenuItem("Next card");
        JMenuItem configurationMenu = new JMenuItem("Configuration ");
        JMenuItem exitMenu = new JMenuItem("Exit");

        newCardMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                String newCardTitle = JOptionPane.showInputDialog("New card");
                try {
                    TrelloClient.GetInstance().newCardToList(sListId, newCardTitle);
                }catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }
        });

        showNextCardMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("archiving card");
                //TrelloClient.GetInstance().archiveCard(listOfCards.get(0));
            }
        });

        buttonPopUp.add(newCardMenu);
        buttonPopUp.add(showNextCardMenu);
        buttonPopUp.addSeparator();
        buttonPopUp.add(configurationMenu);
        buttonPopUp.add(exitMenu);
        button.setComponentPopupMenu(buttonPopUp);

        frame.getContentPane().add(button);
    }

    public static void clearLists()
    {
        frame.getContentPane().removeAll();
        nNumberOfLists = 0;
    }
}

