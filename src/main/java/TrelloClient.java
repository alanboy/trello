
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
import org.trello4j.model.Action;

public class TrelloClient extends SwingWorker<Integer, Integer>
{
    private static final String API_KEY = "c67c1cdec3b70b84a052b4d085c15eb1";
    private static final int MAX_API_CALLS = 1;
    private static final int UPDATE_INTERVAL = 1000 * 60 * 60; // 1 Hour

    private JsonArray configListArray;
    private String configUserToken;
    private Trello trello4jClient;
    private boolean isInitialized;
    private static TrelloClient trelloInstance;
    private String explicitConfigLocation;

    private TrelloClient()
    {
    }

    public static String getKey()
    {
        return API_KEY;
    }

    public List<org.trello4j.model.List> getListsFromBoard(Board board) throws Exception
    {
        if (!isInitialized)
        {
            throw new Exception("Client has not been initialized.");
        }

        List<org.trello4j.model.List> ls = trello4jClient.getListByBoard(board.getId());

        return ls;
    }

    public List<Board> getMyBoards() throws Exception
    {
        if (!isInitialized)
        {
            throw new Exception("Client has not been initialized.");
        }

        List<Board> bs = trello4jClient.getBoardsByMember("my");

        if (0 == bs.size())
        {
            System.out.println("you dont have access to any boards?");
        }

        return bs;
    }

    public static TrelloClient GetInstance()
    {
        if (null == trelloInstance)
        {
            trelloInstance = new TrelloClient();
        }

        return trelloInstance;
    }

    protected Integer doInBackground() throws Exception
    {
        if (!isInitialized)
        {
            throw new Exception("Client has not been initialized.");
        }

        for (int nApiCalls = 0; ;nApiCalls++)
        {
            doWork();

            Thread.sleep(UPDATE_INTERVAL);
        }
    }

    public void updateOnce() throws Exception
    {
        doWork();
    }

    // Get all cards in list. Add them to UIServer.
    private void doWork() throws Exception
    {

        UIServer.clearLists();

        for (int nCurrentList = 0; nCurrentList < configListArray.size(); nCurrentList++)
        {
            List<Card> bs2 = null;
            System.out.println("[apicall] getCardsByList");
            String listId = configListArray.get(nCurrentList).toString().replace('"', ' ').trim();

            try
            {
                bs2 = trello4jClient.getCardsByList(listId);
            }
            catch(Exception e)
            {
                System.out.println(e);
            }

            if (null == bs2)
            {
                System.out.println("list `" + listId + "` does not exist or is invalid." );
                throw new Exception();
            }

            if (0 == bs2.size())
            {
                System.out.println("list `" + listId + "` has no cards!" );
                throw new Exception();
            }

            String idBoard;
            List<org.trello4j.model.List> l = null;

            if (bs2.size() != 0)
            {
                idBoard = bs2.get(0).getIdBoard();

                l = getListsInBoard(idBoard);

            }

            UIServer.addList(listId, bs2, l);
        }
    }

    public List<org.trello4j.model.List> getListsInBoard(String idBoard)
    {
        List<org.trello4j.model.List> l = trello4jClient.getListByBoard(idBoard, null);

        return l;
    }

    public void archiveCard(Card c) // throws Exception
    {
        if (!isInitialized)
        {
           // throw new Exception("Client has not been initialized.");
           return;
        }

        trello4jClient.closeCard(c.getId());

    }

    // Look for a list that is called, "Done" and move cCard to that list
    public void moveCardToList(Card cCard, String sListId) // throws Exception
    {
        if (!isInitialized)
        {
           // throw new Exception("Client has not been initialized.");
           return;
        }

        // search for card called "done"

        trello4jClient.moveCard(cCard.getId(), sListId);
        System.out.println(
                "Move card " + cCard.getId() + " to list " + sListId
               );

    }

    public void newCardToList(String sListId, String newCardTitle) throws Exception
    {
        if (!isInitialized)
        {
            throw new Exception("Client has not been initialized.");
        }

        Card c = trello4jClient.createCard(sListId, newCardTitle, null);
        System.out.println(
                "New card in list " + sListId
               + " " + ((null == c) ? "Failed." : "Succeded"));

    }

    public void initialize(String configPath) throws Exception
    {
        explicitConfigLocation = configPath;
        initialize();
    }

    public void initialize() throws Exception
    {
        if (isInitialized)
        {
            throw new Exception("Client has already been initialized");
        }

        boolean canContinue = readConfig();

        if (!canContinue)
        {
            throw new Exception("Unable to stat trello client, something related to the configuration");
        }

        String userTokenFromConfig = configUserToken.substring(1, configUserToken.length()-1);

        trello4jClient = new TrelloImpl(API_KEY, userTokenFromConfig);

        if (null == trello4jClient)
        {
            System.out.println("trello4jClient returned null");
        }

        Member m = trello4jClient.getMember("my");

        if (null == m)
        {
            System.out.println("initialize failed while getting current user.");
            throw new Exception("Unable to stat trello client.");
        }

        isInitialized = true;

        System.out.println("trello client started for username " + m.getUsername());
    }

    /**
      * returns true if the configuration has the minimun necessary info
      *                   to continue execution of the client.
      **/
    private boolean parseConfig(String s)
    {
        JsonElement jElement = new JsonParser().parse(s);
        JsonObject  jObject = jElement.getAsJsonObject();

        // Search for usertoken
        configUserToken = jObject.get("usertoken").toString();
        if (null == configUserToken)
        {
            System.out.println("config.json is missing the usertoken");
            return false;
        }

        // Search for at lest one list
        configListArray = jObject.getAsJsonArray("lists");

        if (null == configListArray || configListArray.size() == 0)
        {
            System.out.println("config.json is missing at least 1 list");
            return false;
        }

        return true;
    }

    public void writeConfig(String token, String lists)
    {
    
    }

    public boolean configExist()
    {
        String config = (explicitConfigLocation == null) ? System.getProperty("user.home") + "/trello.json"
                            :explicitConfigLocation;

        return (new File(config).isFile());
    }

    private boolean readConfig()
    {

        String config = (explicitConfigLocation == null) ? System.getProperty("user.home") + "/trello.json"
                            :explicitConfigLocation;
        String json = "";
        try
        {
            BufferedReader br;
            br = new BufferedReader(new FileReader(config));

            String s;

            while((s = br.readLine()) != null) json += s;

        }
        catch(IOException ioe)
        {
            System.out.println("Problem reading your config (" + config + ") :" + ioe);
            return false;
        }

        return parseConfig(json);
    }

}

