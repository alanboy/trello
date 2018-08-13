
import java.awt.*;
import java.awt.Desktop;
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
    private Card trelloCard;
    private Logger log;
    private boolean cardOpened = false;
    private static final int ONE_SECOND = 1000;
    private static ArrayList<CardButton> buttonsInApplication;
    private static boolean Instance = false;
    static int foo = 0;

    CardButton(Card c) {
        this.trelloCard = c;
        this.log = LogManager.getLogger();

        log.info("Creating CardButton for card id = "+ c.getId() +"");

        this.setMargin(new Insets(0, 0, 0, 0));

        this.setContentAreaFilled(false);
        this.setFocusPainted(false);
        this.setToolTipText(c.getName() + "\n" + c.getDesc());
        this.setOpaque(true);

        // this works
        // this.setPreferredSize(new Dimension(500, 50));

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
                System.out.println("------------ static refresh ---------------------------");
                for(CardButton card : buttonsInApplication) {
                    //System.out.println("  ->" + card.trelloCard.getName().substring(0, 10) );
                    card.updateText();
                }
            } catch (InterruptedException ie) {
                System.out.println("exception");
            }
        }
    }

    public void updateCard(Card c) {
        this.trelloCard = c;
        updateText();
    }

    public void setOpened(boolean opened) {
        cardOpened = opened;
        System.out.println(this.hashCode()  + "is now opened= " + cardOpened);
    }

    public boolean getOpened() {
        return cardOpened;
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

        String description = ""; //trelloCard.getDesc();
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

        String html = "";
        if (cardOpened) {

            //this.setSize(new Dimension(500, 50));
            this.setPreferredSize(new Dimension(500, 50));

            html =  "<html><font color=\"" + titleColor + "\">OPENED" + trelloCard.getName() + "</font>"
                +  " <font color=\"" + timeColor + "\">"
                + (days > 0 ? days + "d " : "")
                + (hours < 10 ? "0" : "") + hours + ":"
                + (minutes < 10 ? "0" : "") + minutes + ":"
                + (seconds < 10 ? "0" : "") + seconds
                + "</font><br>"
                + " " + trelloCard.getDesc()
                + "</html>";
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

