import javax.swing.*;
import org.trello4j.model.Card;

class CardButton extends JButton
{
    private Card trelloCard;
    private final int MAX_CHARACTERS_IN_TITLE = 24;
    private final long creationTime;


    CardButton(Card trelloCard)
    {
        this.trelloCard = trelloCard;

        // The first 8 character of the card are the unix timestamp of creation
        creationTime = Long.parseLong(trelloCard.getId().substring(0,8), 16);

        update();
    }

    public long getCreationTime()
    {
        return creationTime;
    }

    void update()
    {
        String title = trelloCard.getName();

        if (title.length() > MAX_CHARACTERS_IN_TITLE)
        {
            title = title.substring(0, MAX_CHARACTERS_IN_TITLE) + "...";
        }

        long unixTime = System.currentTimeMillis() / 1000L;

        int init = (int)(unixTime - creationTime);
        int days = (int)(Math.floor(init / 86400));
        int hours = (int)(Math.floor(init / 3600) % 24);
        int minutes = (int)Math.floor((init / 60) % 60);
        int seconds = init % 60;

        // Color palete http://paletton.com/#uid=33L100kllllA7corxgSf9pO8Yui

        String titleColor = "061842";
        String timeColor = "2E4172";

        if (days > 5)
        {
            timeColor = "82121D";
        }
        else if (days > 2)
        {
            timeColor = "A2A838";
        }

        String html =  "<html><font color=\"" + titleColor + "\">" + title + "</font>"
            +  " <font color=\"" + timeColor + "\">" 
            + (days > 0 ? days + "d " : "")
            + (hours < 10 ? "0" : "") + hours + ":"
            + (minutes < 10 ? "0" : "") + minutes + ":"
            + (seconds < 10 ? "0" : "") + seconds
            + "</font></html>";

        this.setText(html);
    }
}

