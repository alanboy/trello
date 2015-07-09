
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
    private static final int UPDATE_INTERVAL = 60 * 15; // 15 minutes
    private static final int ONE_SECOND = 1000;
    private static final int FIVE_MINUTES = ONE_SECOND * 60 * 5;

    private JsonArray configListArray;
    private String configUserToken;
    private Trello trello4jClient;
    private boolean isInitialized;
    private static TrelloClient trelloInstance;
    private String explicitConfigLocation;
    private long secondsSinceUpdate = 0;

    private JsonObject jObject; // Store configuration object

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

        doWork();

        for (int nApiCalls = 0; ;nApiCalls++)
        {
            Thread.sleep(ONE_SECOND);

            secondsSinceUpdate++;

            if (secondsSinceUpdate > UPDATE_INTERVAL)
            {
                doWork();
                secondsSinceUpdate = 0;
            }

            UIServer.updateTimes();
        }
    }

    public void stopForAWhile() throws Exception
    {
        UIServer.setVisible(false);
        Thread.sleep(FIVE_MINUTES);
        UIServer.setVisible(true);
    }

    public void updateOnce() throws Exception
    {
        doWork();
    }

    // Get all cards in list. Add them to UIServer.
    private void doWork() throws Exception
    {
        System.out.println("Entering doWork()");

        UIServer.clearLists();

        for (int nCurrentList = 0; nCurrentList < configListArray.size(); nCurrentList++)
        {
            List<Card> bs2 = null;
            String listId = configListArray.get(nCurrentList).toString().replace('"', ' ').trim();

            try
            {
                bs2 = trello4jClient.getCardsByList(listId);
                System.out.println("[apicall] getCardsByList [OK]");
            }
            catch(Exception e)
            {
                System.out.println("[apicall] getCardsByList [FAILED]");
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

        System.out.println("Exiting doWork()");
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

        String configJson = readConfig();
        boolean canContinue = false;

        try
        {
            canContinue = parseConfig(configJson);
        }
        catch (com.google.gson.JsonSyntaxException jse)
        {
            //@TODO json is bad, lets back it up and start a new one
            throw new Exception("json is bad");
        }

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
        jObject = jElement.getAsJsonObject();

        // Search for usertoken
        configUserToken = jObject.get("usertoken").toString();
        if (null == configUserToken)
        {
            System.out.println("config.json is missing the usertoken");
            return false;
        }

        return true;
    }

    public boolean loadLists()
    {
        if (!isInitialized)
        {
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

    public boolean configExist()
    {
        String config = (explicitConfigLocation == null) ? System.getProperty("user.home") + "/trello.json"
                            :explicitConfigLocation;

        System.out.println("Found configuration at:" + config);

        return (new File(config).isFile());
    }

    public boolean writeTokenToConfig(String token)
    {
        if (!configExist())
        {
            System.out.println("config does not exist");
			return false;
        }

        String config = readConfig();

        System.out.println(config);

        JsonElement jElement = new JsonParser().parse(config);

        JsonObject jObject = jElement.getAsJsonObject();

        jObject.addProperty("usertoken", token);

        String explicitConfigLocation = null;
        String configPath = (explicitConfigLocation == null) ? System.getProperty("user.home") + "/trello.json"
                            :explicitConfigLocation;

        try {
            PrintWriter pw = new PrintWriter(configPath);
            pw.println(jObject.toString());
            pw.flush();
            pw.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe);
        }

        return true;
    }

    private String readConfig()
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
            return null;
        }

        return json;
    }

}

