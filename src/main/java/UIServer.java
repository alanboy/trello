
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
    private static int nListsToDisplay;


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

        // Start TrelloClient WorkerThread
        try
        {
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

    public static void update(int n, Card c)
    {
        JButton b = (JButton)frame.getContentPane().getComponent(n);
        b.setText(c.getName());
        frame.pack();
        frame.setVisible(true);
    }

    private static void createNewList()
    {
        JButton button = new JButton("");
        button.setForeground(Color.blue);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        //button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //button.setBorderPainted(false);

        button.addMouseListener(new MouseAdapter(){
                public void mouseReleased(MouseEvent ev) {
                    if (ev.isPopupTrigger())
                    {
                        System.out.println("right released clicked!");
                    }
                    else
                    {
                        System.out.println("left? released clicked!");
                    }
                }
            });

        frame.getContentPane().add(button);
    }

    public static void setNumberOfLists(int n)
    {
        frame.getContentPane().removeAll();

        while (n-- > 0)
        {
            createNewList();
        }

        nListsToDisplay = n;
    }
}

