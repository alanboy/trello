
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

import org.apache.logging.log4j.*;

public class TrelloClient extends SwingWorker<Integer, Integer> {

    // @TODO put this in a configuration file somewhere:
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

    Logger log;

    private JsonObject jObject; // Store configuration object

    // *************************************************************************
    // *         Singleton Construction
    // *************************************************************************
    public static TrelloClient GetInstance() {
        if (null == trelloInstance) {
            trelloInstance = new TrelloClient();
        }
        return trelloInstance;
    }

    private TrelloClient() {
        log = LogManager.getLogger();
        log.debug("Building a new TrelloClient");
    }

    // *************************************************************************
    // *         Worker thread (this is a new thread)
    // *************************************************************************
    protected Integer doInBackground() throws Exception {

        log.info("Worker thread started");

        if (!isInitialized) {
            throw new Exception("Client has not been initialized.");
        }

        doWork();

        for (int nApiCalls = 0; ;nApiCalls++) {
            Thread.sleep(ONE_SECOND * 2);

            secondsSinceUpdate++;

            if (secondsSinceUpdate > UPDATE_INTERVAL) {
                doWork();
                secondsSinceUpdate = 0;
            }

            UIServer.updateTimes();
        }

        // unreachable code...
        // return null;
    }

    private void doWork() throws Exception {
        log.info(">>>>>>>>>>>>>>>>>>>>>>> Entering doWork()");

        // Iterate lists in configuration file
        for (int nCurrentList = 0; nCurrentList < configListArray.size(); nCurrentList++) {
            List<Card> listOfCardsInList = null;
            String listId = configListArray.get(nCurrentList).toString().replace('"', ' ').trim();

            try {
                log.info("getCardsByList() API CALL ...");
                listOfCardsInList = trello4jClient.getCardsByList(listId);
            } catch(Exception e) {
                log.info("[apicall] getCardsByList [FAILED]");
                log.info(e);
            }

            if (null == listOfCardsInList) {
                log.info("list `" + listId + "` does not exist or is invalid." );
                throw new Exception();
            }

            String idBoard;
            List<org.trello4j.model.List> listsInBoard = null;

            if (0 != listOfCardsInList.size()) {
                // @TODO Support 0 sized lists
                idBoard = listOfCardsInList.get(0).getIdBoard();
                listsInBoard = getListsInBoard(idBoard);
            } else {
                log.info("List `" + listId + "` has no cards!" );
            }

            UIServer.addList(listId, listOfCardsInList, listsInBoard);
        }

        UIServer.updateTimes();

        log.info(">>>>>>>>>>>>>>>>>>>>> Exiting doWork()");
    }

    public static String getKey() {
        return API_KEY;
    }

    public List<org.trello4j.model.List> getListsFromBoard(Board board) throws Exception {
        if (!isInitialized) {
            throw new Exception("Client has not been initialized.");
        }

        List<org.trello4j.model.List> ls = trello4jClient.getListByBoard(board.getId());

        return ls;
    }

    public List<Board> getMyBoards() throws Exception {
        if (!isInitialized) {
            throw new Exception("Client has not been initialized.");
        }

        List<Board> bs = trello4jClient.getBoardsByMember("my");

        if (0 == bs.size()) {
            log.info("you dont have access to any boards?");
        }

        return bs;
    }

    public void stopForAWhile() throws Exception {
        UIServer.setVisible(false);
        Thread.sleep(FIVE_MINUTES);
        UIServer.setVisible(true);
    }

    public void updateOnce() throws Exception {
        doWork();
    }

    public List<org.trello4j.model.List> getListsInBoard(String idBoard) {
        return trello4jClient.getListByBoard(idBoard);
    }

    public void archiveCard(Card c) { // throws Exception
        if (!isInitialized) {
           // throw new Exception("Client has not been initialized.");
           return;
        }

        trello4jClient.closeCard(c.getId());
    }

    // Look for a list that is called, "Done" and move cCard to that list
    public void moveCardToList(Card cCard, String sListId) { // throws Exception
        if (!isInitialized) {
           // throw new Exception("Client has not been initialized.");
           return;
        }

        trello4jClient.moveCard(cCard.getId(), sListId);
        log.info("Move card " + cCard.getId() + " to list " + sListId);
    }

    public void newCardToList(String sListId, String newCardTitle) throws Exception {
        if (!isInitialized) {
            throw new Exception("Client has not been initialized.");
        }

        Card c = trello4jClient.createCard(sListId, newCardTitle, null);
        log.info("New card in list " + sListId
                   + " " + ((null == c) ? "Failed." : "Succeded"));

    }

    public void initialize(String configPath) throws Exception {
        explicitConfigLocation = configPath;
        initialize();
    }

    public void initialize() throws Exception {
        if (isInitialized) {
            throw new Exception("Client has already been initialized");
        }

        String configJson = readConfig();
        boolean canContinue = false;

        try {
            canContinue = parseConfig(configJson);
        } catch (com.google.gson.JsonSyntaxException jse) {
            //@TODO json is bad, lets back it up and start a new one
            throw new Exception("json is bad");
        }

        if (!canContinue) {
            throw new Exception("Unable to stat trello client, something related to the configuration");
        }

        String userTokenFromConfig = configUserToken.substring(1, configUserToken.length()-1);
        trello4jClient = new TrelloImpl(API_KEY, userTokenFromConfig);

        if (null == trello4jClient) {
            log.error("trello4jClient returned null");
        }

        Member m = trello4jClient.getMember("my");

        if (null == m) {
            log.error("Initialize failed while getting current user.");
            throw new Exception("Unable to stat trello client.");
        }

        isInitialized = true;
        log.info("trello client started for username " + m.getUsername());
    }

    /**
      * returns true if the configuration has the minimun necessary info
      *                   to continue execution of the client.
      **/
    private boolean parseConfig(String s) {
        JsonElement jElement = new JsonParser().parse(s);
        jObject = jElement.getAsJsonObject();

        // Search for usertoken
        configUserToken = jObject.get("usertoken").toString();
        if (null == configUserToken)
        {
            log.info("config.json is missing the usertoken");
            return false;
        }

        return true;
    }

    public boolean loadLists() {
        if (!isInitialized) {
            return false;
        }

        // Search for at lest one list
        configListArray = jObject.getAsJsonArray("lists");

        if (null == configListArray || configListArray.size() == 0) {
            log.info("config.json is missing at least 1 list");
            return false;
        }

        return true;
    }

    public boolean configExist() {
        String config = (explicitConfigLocation == null) ? 
                            System.getProperty("user.home") + java.io.File.separator + "trello.json"
                            : explicitConfigLocation;

        boolean bFoundConfig = new File(config).isFile();

        log.info("Configuration JSON"
                    + (bFoundConfig ? "" : " NOT ") 
                    +"found at : " + config);

        return bFoundConfig;
    }

    public boolean newConfigFile() {
        if (!configExist()) {
            try {
                PrintWriter pWriter = new PrintWriter(System.getProperty("user.home") + java.io.File.separator + "trello.json");
                pWriter.write("{}");
                pWriter.flush();
                pWriter.close();

                log.info("Created new config file.");

            } catch (IOException ioe) {
                log.error("Unable to create new config file: " + ioe);
                return false;
            }
        }
        return true;
    }

    public String getListsInConfig() {

        if (configListArray == null) {
            return "";
        }

        String output = "";
        // Iterate lists in configuration file
        for (int nCurrentList = 0; nCurrentList < configListArray.size(); nCurrentList++) {
            String listId = configListArray.get(nCurrentList).toString().replace('"', ' ').trim();
            output += listId + ";";

        }
        return output;
    }

    public boolean removeListFromConfig(String listId) {

        log.info("Removing list "+listId+" from configuration file");

        if (!configExist()) {
            log.error("Config file does not exist");
            return false;
        }

        JsonElement jElement = new JsonParser().parse(readConfig());

        JsonObject jObject = jElement.getAsJsonObject();

        JsonArray configListArray = jObject.getAsJsonArray("lists");

        if (configListArray == null) {
            configListArray = new JsonArray();
        }

        JsonPrimitive toDelete = new JsonPrimitive(listId);

        int i = 0;
        for (JsonElement el : configListArray) {
            if (el.equals(toDelete)) {
                configListArray.remove(i);
            } else {
                i++;
            }
        }

        jObject.add("lists", configListArray);

        String explicitConfigLocation = null;
        String configPath = (explicitConfigLocation == null) ? System.getProperty("user.home") + "/trello.json"
                            :explicitConfigLocation;

        try {
            PrintWriter pw = new PrintWriter(configPath);
            pw.println(jObject.toString());
            pw.flush();
            pw.close();
        } catch (FileNotFoundException fnfe) {
            log.error("Failed to write" + fnfe);
            return false;
        }

        return true;
    }

    public boolean addListToConfig(String listId) {

        log.info("Writing list to configuration file");

        if (!configExist()) {
            log.error("Config file does not exist");
            return false;
        }

        JsonElement jElement = new JsonParser().parse(readConfig());

        JsonObject jObject = jElement.getAsJsonObject();

        JsonArray configListArray = jObject.getAsJsonArray("lists");

        if (configListArray == null) {
            configListArray = new JsonArray();
        }

        configListArray.add(new JsonPrimitive(listId));

        jObject.add("lists", configListArray);

        String explicitConfigLocation = null;
        String configPath = (explicitConfigLocation == null) ? System.getProperty("user.home") + "/trello.json"
                            :explicitConfigLocation;

        try {
            PrintWriter pw = new PrintWriter(configPath);
            pw.println(jObject.toString());
            pw.flush();
            pw.close();
        } catch (FileNotFoundException fnfe) {
            log.error("Failed to write" + fnfe);
            return false;
        }

        return true;
    }

    public boolean writeTokenToConfig(String token) {
        log.info("Writing token to configuration file");

        if (!configExist()) {
            log.error("Config file does not exist");
            return false;
        }

        String config = readConfig();

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
            log.error("Failed to write" + fnfe);
            return false;
        }

        return true;
    }

    private String readConfig() {
        String config = (explicitConfigLocation == null) ? System.getProperty("user.home") + "/trello.json"
                            : explicitConfigLocation;

        String json = "";
        try {
            BufferedReader br;
            br = new BufferedReader(new FileReader(config));

            String s;
            while((s = br.readLine()) != null) json += s;

        } catch(IOException ioe) {
            log.error("Problem reading your config (" + config + ") :" + ioe);
            return null;
        }

        return json;
    }

    public void checkForSoftwareUpdate() {

        final String versionUrl = "https://github.com/alanboy/trello/raw/master/dist/latest/version.json";
        final File runningBinPath = new File(TrelloClient.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        final String binUrl = "https://github.com/alanboy/trello/raw/master/dist/latest/trello-0.0.2.jar";

        // Check for version.txt and compare it to my known version.
        log.info("Current running directory: " + runningBinPath);
        log.info("Checking for updates in " + binUrl);

        try {
            HttpClient.RequestBinToFile(binUrl, "latest-trello.jar");

            log.info("Update successful, saved to latest-trello.jar.");

        } catch (NullPointerException npe) {
            log.error("Unable to download new version:" + npe);

        } catch (Exception e) {
            log.error("Unable to download new version:" + e);
        }

    }
}

