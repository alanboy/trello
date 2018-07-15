import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import org.apache.logging.log4j.*;

// For openning the desktop browser
import java.awt.Desktop;
import java.net.URI;
import java.net.*;
import java.io.IOException;

// Trello4j stuff
import org.trello4j.model.Card;

class CardButton extends JButton {
    private Card trelloCard;
    private Logger log;

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
    }

    public void updateCard(Card c) {
        this.trelloCard = c;
        updateText();
    }

    private void updateText() {

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

        String html =  "<html><font color=\"" + titleColor + "\">" + title + "</font>"
            +  " <font color=\"" + timeColor + "\">" 
            + (days > 0 ? days + "d " : "")
            + (hours < 10 ? "0" : "") + hours + ":"
            + (minutes < 10 ? "0" : "") + minutes + ":"
            + (seconds < 10 ? "0" : "") + seconds
            + "</font>"
            + "<br>"
            + trelloCard.getDesc()
            + "</html>";

        this.setText(html);
    }
}

