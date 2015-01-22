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

/**
 * Anything that has to do with reading or writing to
 * the terminal.
 *
 *
 * */
public class TrelloCmd
{
    private static TrelloClient tClient;

    public static void main(String[] args)
    {
        tClient = TrelloClient.GetInstance();

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
                    + "-b show boards i have access to" + NEW_LINE );
            return;
        }

        if (args.length > 0 && args[0].equals("-b"))
        {
            try
            {
                showMyBoardsAndLists();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            return;
        }

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                UIServer.createAndShowGUI();
            }
        });
    }

    private static void showMyBoardsAndLists() throws Exception
    {
        for (Board b : tClient.getMyBoards())
        {
            System.out.println("Board: " + b.getName() + " - " + b.getId());
            for (org.trello4j.model.List l : tClient.getListsFromBoard(b))
            {
                System.out.println("    List: " + l.getName() + " - " + l.getId());
            }
            System.out.println();
        }
    }
}

