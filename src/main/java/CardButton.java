
import java.awt.*;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;
import java.net.URI;
import javax.swing.*;
import org.apache.logging.log4j.*;
import org.trello4j.model.Card;
import java.util.ArrayList;

class CardButton extends JButton {
    private ArrayList<String> comments = null;
    private Card trelloCard;
    private Logger log;
    private boolean cardOpened = false;
    private boolean commentsLoaded = false;
    private static ArrayList<CardButton> buttonsInApplication;
    private static boolean Instance = false;
    private static final int ONE_SECOND = 1000;

    CardButton(Card c) {
        this.trelloCard = c;
        this.log = LogManager.getLogger();

        log.info("Creating CardButton for card id = "+ c.getId() +"");

        this.setMargin(new Insets(0, 0, 0, 0));

        this.setContentAreaFilled(false);
        this.setFocusPainted(false);
        this.setToolTipText(c.getName() + "\n" + c.getDesc());
        this.setOpaque(true);

        updateText();

        Init(this);
    }

    private static void Init(CardButton thisCard) {
        if (!Instance) {
            Instance = true;
            buttonsInApplication = new ArrayList<CardButton>();
            new Thread(() -> updateTimersOnCards()).start();
        }

        buttonsInApplication.add(thisCard);
    }

    private static void updateTimersOnCards() {
        while(true) {
            try {
                Thread.sleep(ONE_SECOND * 5);
                for(CardButton card : buttonsInApplication) {
                    card.updateText();
                }
            } catch (InterruptedException ie) {
            }
        }
    }

    public void updateCard(Card c) {
        this.trelloCard = c;
        updateText();
    }

    public void setOpened(boolean opened) {
        cardOpened = opened;
    }

    public boolean getOpened() {
        return cardOpened;
    }

    public void copyContentsToClipboard() {

        String text = trelloCard.getName() + " " + trelloCard.getDesc();

        for (String comment : comments) {
            text += comment + "; ";
        }

        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public void updateText() {
        String shortTitle;
        String title = trelloCard.getName();
        long unixTime = System.currentTimeMillis() / 1000L;

        // The first 8 character of the card are the unix timestamp of creation
        long creationTime = Long.parseLong(trelloCard.getId().substring(0,8), 16);

        int init = (int)(unixTime - creationTime);
        int days = (int)(Math.floor(init / 86400));
        int hours = (int)(Math.floor(init / 3600) % 24);
        int minutes = (int)Math.floor((init / 60) % 60);
        int seconds = init % 60;

        String description = "";
        if (title.length() > 40) {
            shortTitle = title.substring(0, 40);
        } else {
            shortTitle = title;
        }

        String titleColor = "061842";
        String timeColor = "2E4172";

        if (days > 5) {
            timeColor = "82121D";
        } else if (days > 2) {
            timeColor = "A2A838";
        }

        if (!commentsLoaded) {
            comments = TrelloClient.GetInstance().getComments(trelloCard);
            commentsLoaded = true;
        }

        String html = "";
        if (cardOpened) {
            this.setPreferredSize(new Dimension(500, 50));

            html =  "<html><font color=\"" + titleColor + "\">" + trelloCard.getName() + "</font>"
                +  " <font color=\"" + timeColor + "\">"
                + (days > 0 ? days + "d " : "")
                + (hours < 10 ? "0" : "") + hours + ":"
                + (minutes < 10 ? "0" : "") + minutes + ":"
                + (seconds < 10 ? "0" : "") + seconds
                + "</font><br>"
                + " " + trelloCard.getDesc();

            for (String comment : comments) {
                html += comment + "<br>";
            }

            html += "</html>";
        } else {

            this.setPreferredSize(null);

            html =  "<html><font color=\"" + titleColor + "\">" + shortTitle + "</font>"
                +  " <font color=\"" + timeColor + "\">"
                + (days > 0 ? days + "d " : "")
                + (hours < 10 ? "0" : "") + hours + ":"
                + (minutes < 10 ? "0" : "") + minutes + ":"
                + (seconds < 10 ? "0" : "") + seconds
                + "</font>"
                + "</html>";
        }

        this.setText(html);
    }
}

