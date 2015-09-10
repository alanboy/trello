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
    private final int MAX_CHARACTERS_IN_TITLE = 24;
    private final long creationTime;
    Logger log;
    ListPanel parentListPanel;

    CardButton(Card c, ListPanel parentPanel) {
        this.trelloCard = c;
        this.parentListPanel = parentPanel;
        this.log = LogManager.getLogger();

        log.info("Creating CardButton for card id = "+ c.getId() +"");

        this.setMargin(new Insets(0, 0, 0, 0));

        this.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent ev) {
                if (!ev.isPopupTrigger()) {
                    parentListPanel.toggleDropDown();
                }
            }
        });

        this.setContentAreaFilled(false);
        this.setFocusPainted(false);
        this.setToolTipText(c.getName() + "\n" + c.getDesc());

        // Contextual menu
        JPopupMenu buttonPopUp = new JPopupMenu();

        JMenu moveToListMenu = new JMenu("Move to ...");
        for (org.trello4j.model.List listInBoard : parentListPanel.getListsInBoard()) {

            // dont move to the current list
            if (c.getIdList().equals(listInBoard.getId())) {
                continue;
            }

            JMenuItem menuForList = new JMenuItem(listInBoard.getName());
            MoveToListAction f = new MoveToListAction(c, listInBoard.getId());
            menuForList.addActionListener(f);
            moveToListMenu.add(menuForList);
        }

        JMenuItem refreshMenu = new JMenuItem("Refresh");
        refreshMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                try
                {
                    TrelloClient.GetInstance().updateOnce();
                }
                catch(Exception ex)
                {
                    log.error(ex);
                }
            }
        });

        JMenuItem newCardMenu = new JMenuItem("New card");
        newCardMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                String newCardTitle = JOptionPane.showInputDialog("New card");
                try {
                    TrelloClient.GetInstance().newCardToList(parentListPanel.getListId(), newCardTitle);
                    TrelloClient.GetInstance().updateOnce();
                } catch(Exception ex) {
                    log.error(ex);
                }
            }
        });

        JMenuItem openBoardInBrowser = new JMenuItem("Open in browser");
        openBoardInBrowser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                if(Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://trello.com/c/"+ trelloCard.getId()));
                    } catch (URISyntaxException e) {
                        log.error(e);
                    } catch (IOException ioe) {
                        log.error(ioe);
                    }
                }
            }
        });

        JMenuItem hideForAWhile = new JMenuItem("Hide 5min");
        hideForAWhile.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    TrelloClient.GetInstance().stopForAWhile();
                } catch(Exception ex) {
                    log.info(ex);
                }
            }
        });

        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });

        JMenuItem archiveCardMenu = new JMenuItem("Archive card");
        archiveCardMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(null,
                    "Archive?", "Are you sure you want to archive " + trelloCard.getName(), JOptionPane.YES_NO_OPTION);

                if (response != 0) { // 0 means YES
                    return;
                }

                try {
                    TrelloClient.GetInstance().archiveCard(trelloCard);
                    TrelloClient.GetInstance().updateOnce();
                } catch(Exception ex) {
                    log.error(ex);
                }
            }
        });

        // Add menus
        buttonPopUp.add(newCardMenu);
        buttonPopUp.add(openBoardInBrowser);
        buttonPopUp.add(moveToListMenu);
        buttonPopUp.add(archiveCardMenu);
        buttonPopUp.addSeparator();
        buttonPopUp.add(hideForAWhile);
        buttonPopUp.add(refreshMenu);
        buttonPopUp.add(exitMenu);
        this.setComponentPopupMenu(buttonPopUp);

        // The first 8 character of the card are the unix timestamp of creation
        creationTime = Long.parseLong(this.trelloCard.getId().substring(0,8), 16);

        update();
    }

    public Long getId() {
        return trelloCard.getIdShort();
    }

    public long getCreationTime() {
        return creationTime;
    }

    void update() {

        String title = trelloCard.getName();

//        StackTraceElement[] cause = Thread.currentThread().getStackTrace();
//        for (StackTraceElement f : cause)
//        {
//            System.out.print(f.getMethodName() + "->");
//        }
//        System.out.println();
//
//        if (title.length() > MAX_CHARACTERS_IN_TITLE) {
//            title = title.substring(0, MAX_CHARACTERS_IN_TITLE) + "...";
//        }

        long unixTime = System.currentTimeMillis() / 1000L;

        int init = (int)(unixTime - creationTime);
        int days = (int)(Math.floor(init / 86400));
        int hours = (int)(Math.floor(init / 3600) % 24);
        int minutes = (int)Math.floor((init / 60) % 60);
        int seconds = init % 60;

        // Color palete http://paletton.com/#uid=33L100kllllA7corxgSf9pO8Yui

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
            + "</font></html>";

        this.setText(html);
    }
}

