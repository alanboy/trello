
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

class UIServer
{
    private static JFrame frame;
    private static TrelloClient tClient;

    public static void main(String[] args)
    {
        tClient = new TrelloClient();

        try
        {
            tClient.initialize();
        }
        catch (Exception e)
        {
            System.out.println(e);
            return;
        }

        if (args.length > 0 && args[0].equals("-?"))
        {

            String NEW_LINE = System.getProperty("line.separator");
            System.out.println(
                    "trello client 0.1" + NEW_LINE
                    + "-? this help " + NEW_LINE
                    + "-b show boards i have access to" + NEW_LINE
                );
            return;
        }

        if (args.length > 0 && args[0].equals("-b"))
        {
            try
            {
                tClient.showMyBoards();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            return;
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI()
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

        JButton button = new JButton("this is a long text");
        button.setForeground(Color.blue);
        //button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        //button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent ev) {
                    if (ev.isPopupTrigger()) {
                    System.out.println("right pressed clicked!");
                    //menu.show(ev.getComponent(), ev.getX(), ev.getY());
                    }
                }

                public void mouseReleased(MouseEvent ev) {
                    if (ev.isPopupTrigger()) {
                    System.out.println("right released clicked!");
                    //menu.show(ev.getComponent(), ev.getX(), ev.getY());
                    }
                }

                public void mouseClicked(MouseEvent ev) {
                    System.out.println("left clicked!");
                }
            });

        frame.getContentPane().add(button);

        JButton b2 = new JButton("dos");
        frame.getContentPane().add(b2);

        java.awt.FlowLayout experimentLayout = new java.awt.FlowLayout();
        frame.setLayout(experimentLayout);

        // Display the window.
        frame.pack();
        frame.setVisible(true);

        // Start TrelloClient WorkerThread
        try
        {
            tClient.execute();
        }
        catch (Exception e)
        {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void update(int n, Card c)
    {
        JButton b = (JButton)frame.getContentPane().getComponent(n);
        b.setText(c.getName());
        frame.pack();
        frame.setVisible(true);
    }
}

