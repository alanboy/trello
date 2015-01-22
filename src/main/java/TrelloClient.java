
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

public class TrelloClient extends SwingWorker<Integer, Integer>
{
    private static final String API_KEY = "c67c1cdec3b70b84a052b4d085c15eb1";
    private static final int MAX_API_CALLS = 1;
    private static final int UPDATE_INTERVAL = 1000 * 60 * 60; // 1 Hour

    private JsonArray configListArray;
    private String configUserToken;
    private Trello trello4jClient;
    private boolean isInitialized;

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

    private void doWork() throws Exception
    {
        for (int a = 0; a < configListArray.size(); a++)
        {
            List<Card> bs2 = null;
            System.out.println("[apicall] getCardsByList");
            String listId = configListArray.get(a).toString().replace('"', ' ').trim();

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

            for (Card b : bs2)
            {
                System.out.println("name:" + b.getName());
            }

            // Update the UI
            UIServer.update(a, bs2.get(0));
        }

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
            throw new Exception("Unable to stat trello client.");
        }


        String user = configUserToken.substring(1, configUserToken.length()-1);
        System.out.println("using user " + user);

        trello4jClient = new TrelloImpl(API_KEY, user);

        if (null == trello4jClient)
        {
            System.out.println("trello4jClient returned null");
        }

        Member m = trello4jClient.getMember("my");

        if (null == m)
        {
            System.out.println("get member failed, is token valid?");
            throw new Exception("Unable to stat trello client.");
        }

        isInitialized = true;

        System.out.println(m.getUsername());

        System.out.println("trello client started");

        //verify configUserToken is valid
    }

    public void showMyProfile() throws Exception
    {
        if (!isInitialized)
        {
            throw new Exception("Client has not been initialized.");
        }

        Member m = trello4jClient.getMember("my");
    }

    public void showMyBoards() throws Exception
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

        for (Board b : bs)
        {
            System.out.println("id:" +   b.getId());
            System.out.println("desc:" + b.getDesc());
            System.out.println("name:" + b.getName());
        }
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

    private boolean readConfig()
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader("config.json"));
            String json = "";
            String s;

            while((s = br.readLine()) != null) json += s;

            return parseConfig(json);
        }
        catch(IOException ioe)
        {
            System.out.println("IO error. Does config.json exist?");
            return false;
        }

    }

}

