import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;
import java.net.URI;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import org.apache.logging.log4j.*;
import org.trello4j.*;
import org.trello4j.model.*;
import org.trello4j.model.Card;

// Has a DefaultListModel<Cards> that holds all the cards that are to be shown
// You can then add custom actions to DefaultListModel<>
//
//     ListPanelCellRenderer
//     ListPanelMouseAdapter
//
public class ListPanel extends JList<Card> {

    private HashMap<String, Color> cardColors = null;
    private HashSet<Long> userModifiedCards = null;
    private List<Card> cards;
    private List<org.trello4j.model.List> listsInBoard;
    private Logger log;
    private final String sListId;
    public DefaultListModel<Card> listModel;
    protected HashMap<String, CardButton> cardButtons;

    ListPanel(final String sListId, final List<Card> cards, List<org.trello4j.model.List> listsInBoard) {
        super();

        this.sListId = sListId;
        this.cards = cards;
        this.listsInBoard = listsInBoard;
        this.log = LogManager.getLogger();
        this.cardButtons = new HashMap<String, CardButton> ();

        log.info("Creating new ListPanel:" + sListId);

        userModifiedCards = new HashSet<Long>();

        this.listModel = new DefaultListModel<Card>();
        this.setModel(this.listModel);

        ListPanelMouseAdapter mouseAdapter = new ListPanelMouseAdapter(this, this.listModel);
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);

        this.setCellRenderer(new ListPanelCellRenderer(this));

        update();
    }

    private boolean syncLocalPositionChangesToServer() {
        boolean changesMade = false;

        // O(n^2) alg, change this if it becomes a problem:
        for (int i = 0; i < this.listModel.getSize(); i++) {
            Card localCard = this.listModel.elementAt(i);

            if (!userModifiedCards.contains(localCard.getIdShort())) {
                continue;
            }

            for (int j = 0; j < cards.size(); j++) {
                Card serverCard = cards.get(j);

                if ((localCard.getIdShort().equals(serverCard.getIdShort()))
                        && (localCard.getPos() != serverCard.getPos())) {

                    changesMade = true;

                    log.info("Updating position of card");
                    TrelloClient.GetInstance().moveCardToPos(localCard, localCard.getPos());

                    userModifiedCards.remove(localCard.getIdShort());
                }
            }
        }

        return changesMade;
    }

    public void update() {
        log.info("Updating ListPanel");

        // In case some of the local cards have different position, update server
        // and update again.
        boolean changesMade = syncLocalPositionChangesToServer();
        if (changesMade) {
            log.info("Changes made, re-updating...");
            try {
                TrelloClient.GetInstance().updateOnce();
            }catch (Exception e ) {
                log.error(e);
            }

            return;
        }

        // Calculate oldest 5 cards
        log.info("Calculating oldest cards");
        PriorityQueue<CardComparable> queue = new PriorityQueue<CardComparable>();
        for (Card c : cards) {
            queue.add(new CardComparable(c));
        }

        cardColors = new HashMap<String, Color> ();
        try{
            int i = 5;
            while (i-- > 0) {
                CardComparable cc = queue.poll();
                Color bgColor = null;
                switch(i) {
                    case 1: bgColor = Color.decode("0x0766ff"); break;
                    case 2: bgColor = Color.decode("0x1997ff"); break;
                    case 3: bgColor = Color.decode("0xffc526"); break;
                    case 4: bgColor = Color.decode("0xff910c"); break; // Oldest
                }

                if (bgColor != null)
                    cardColors.put(cc.trelloCard.getId(), bgColor);
            }
        } catch (Exception e) {
            log.error("cant determine order, not fatal: " + e);
        }

        // Add the new cards to the list model so that JList gets reflected
        this.listModel.removeAllElements();
        for (Card c : cards) {
            this.listModel.addElement(c);
        }

        JDialog topFrame = (JDialog)SwingUtilities.windowForComponent(this);
        if (topFrame != null)
            topFrame.pack();
    }

    public List<org.trello4j.model.List> getListsInBoard() {
        return this.listsInBoard;
    }

    public String getListId() {
        return sListId;
    }

    // Merge current Cards with the ones in the supplied
    public void mergeCards(List<Card> incomingCards) {
        log.debug("Merging cards into existing list");

        // @TODO: Actually merge cards
        this.cards = incomingCards;

        update();
    }

    class CardComparable implements Comparable<CardComparable> {
        public Card trelloCard;
        public Long creationTime;

        public CardComparable(Card card) {
            this.trelloCard = card;
            this.creationTime = Long.parseLong(card.getId().substring(0,8), 16);
        }

        public int compareTo(CardComparable right) {
            return creationTime.compareTo(right.creationTime);
        }
    }

    class ListPanelCellRenderer implements ListCellRenderer {
        private ListPanel listPanelRef;

        public ListPanelCellRenderer(ListPanel listPanel) {
            listPanelRef = listPanel;
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Card trelloCard = (Card)value;

            if (!listPanelRef.cardButtons.containsKey(trelloCard.getId())) {
                CardButton cardButton = new CardButton(trelloCard);
                listPanelRef.cardButtons.put(trelloCard.getId(), cardButton);
            }

            CardButton cardButton = listPanelRef.cardButtons.get(trelloCard.getId());

            if (cardColors.containsKey(trelloCard.getId())) {
                cardButton.setBackground(cardColors.get(trelloCard.getId()));
            }

            return cardButton;
        }
    }

    class ListPanelMouseAdapter extends MouseInputAdapter {
        private ListPanel myList;
        private boolean mouseDragging = false;
        private int dragSourceIndex;
        private DefaultListModel<Card> myListModel;

        public ListPanelMouseAdapter(ListPanel list, DefaultListModel<Card> listModel) {
            this.myList = list;
            this.myListModel = listModel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }

            if (e.getClickCount() != 2) {
                return;
            }

            Card selectedCard = myListModel.get(myList.locationToIndex(e.getPoint()));

            if (myList.cardButtons.containsKey(selectedCard.getId())) {
                CardButton cardButton = myList.cardButtons.get(selectedCard.getId());
                cardButton.setOpened(!cardButton.getOpened());
                cardButton.updateText();

                myList.update();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (myList.getSelectedIndex() >= 0) {
                    Card clickedEl = myListModel.get(myList.getSelectedIndex());
                    dragSourceIndex = myList.getSelectedIndex();
                    mouseDragging = true;
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            mouseDragging = false;

            if (!e.isPopupTrigger()) {
                return;
            }

            Card selectedCard = myListModel.get(myList.locationToIndex(e.getPoint()));

            // Create contextual menu
            JPopupMenu buttonPopUp = new JPopupMenu();

            JMenu moveToListMenu = new JMenu("Move to ...");
            for (org.trello4j.model.List listInBoard : myList.getListsInBoard()) {

                // dont move to the current list
                if (selectedCard.getIdList().equals(listInBoard.getId())) {
                    continue;
                }

                JMenuItem menuForList = new JMenuItem(listInBoard.getName());
                MoveToListAction f = new MoveToListAction(selectedCard, listInBoard.getId());
                menuForList.addActionListener(f);
                moveToListMenu.add(menuForList);
            }

            // Move to other boards, super slow!!
            // JMenu moveToOtherBoard = new JMenu("Other board");

            // try {
            //     for (Board board : TrelloClient.GetInstance().getMyBoards()) {

            //         JMenu boardName = new JMenu(board.getName());

            //         for (org.trello4j.model.List listInBoard : TrelloClient.GetInstance().getListsFromBoard(board)) {
            //             JMenuItem menuForList = new JMenuItem(listInBoard.getName());
            //             MoveToListAction f = new MoveToListAction(selectedCard, listInBoard.getId());
            //             menuForList.addActionListener(f);
            //             boardName.add(menuForList);
            //         }

            //         moveToOtherBoard.add(boardName);
            //     }
            // } catch (Exception ex) {
            //     log.error(ex);
            // }

            // moveToListMenu.add(moveToOtherBoard);

            JMenuItem refreshMenu = new JMenuItem("Refresh");
            refreshMenu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev) {
                    try {
                        TrelloClient.GetInstance().updateOnce();
                    } catch(Exception ex) {
                        log.error(ex);
                    }
                }
            });

            JMenuItem addCommentMenu = new JMenuItem("Add comment");
            addCommentMenu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev) {
                    String response = JOptionPane.showInputDialog("Whats the comment?");

                    try {
                        TrelloClient.GetInstance().newCommentToCard(selectedCard.getId(), response);
                        TrelloClient.GetInstance().updateOnce();
                    } catch(Exception ex) {
                        log.error(ex);
                    }
                }
            });

            JMenuItem startPomodoroTimer = new JMenuItem("Start pomodoro timer");
            startPomodoroTimer.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev) {
                    CardButton cardButton = myList.cardButtons.get(selectedCard.getId());
                    UIServer.startPomodoroTimerForCard(selectedCard, cardButton);
                }
            });

            JMenuItem copyToClipboardMenu = new JMenuItem("Copy to clipboard");
            copyToClipboardMenu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev) {

                    CardButton cardButton = myList.cardButtons.get(selectedCard.getId());
                    cardButton.copyContentsToClipboard();
                }
            });

            JMenuItem newCardMenu = new JMenuItem("New card");
            newCardMenu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev) {
                    String newCardTitle = JOptionPane.showInputDialog("New card");
                    try {
                        TrelloClient.GetInstance().newCardToList(sListId, newCardTitle);
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
                            Desktop.getDesktop().browse(new URI("https://trello.com/c/"+ selectedCard.getId()));
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
                public void actionPerformed(ActionEvent e) {
                    // This might not get flushed to log:
                    log.info("Exiting per user request");
                    System.exit(0);
                }
            });

            JMenuItem archiveCardMenu = new JMenuItem("Archive card");
            archiveCardMenu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    int response = JOptionPane.showConfirmDialog(null, "Archive?", "Are you sure you want to archive " + selectedCard.getName(), JOptionPane.YES_NO_OPTION);

                    if (response != 0 /*YES*/) {
                        return;
                    }

                    try {
                        TrelloClient.GetInstance().archiveCard(selectedCard);
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
            buttonPopUp.add(addCommentMenu);
            buttonPopUp.add(startPomodoroTimer);
            buttonPopUp.add(copyToClipboardMenu);
            buttonPopUp.add(archiveCardMenu);
            buttonPopUp.addSeparator();
            buttonPopUp.add(hideForAWhile);
            buttonPopUp.add(refreshMenu);
            buttonPopUp.add(exitMenu);

            myList.setSelectedIndex(myList.locationToIndex(e.getPoint())); //select the item
            buttonPopUp.show(myList, e.getX(), e.getY()); //and show the menu
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (mouseDragging) {
                int currentIndex = myList.locationToIndex(e.getPoint());
                if (currentIndex != dragSourceIndex) {

                    int dragTargetIndex = myList.getSelectedIndex();

                    Card dragElement = myListModel.get(dragSourceIndex);

                    myListModel.remove(dragSourceIndex);
                    myListModel.add(dragTargetIndex, dragElement);

                    double newPos = 0.0;
                    if (dragTargetIndex == 0) {
                        // Moving card to the topS
                        Card after  = myListModel.get(dragTargetIndex + 1);
                        newPos = (after.getPos()) / 2;

                    } else if (dragTargetIndex == (myListModel.getSize() - 1)) {
                        // Moving card to the bottom of the list
                        Card before = myListModel.get(dragTargetIndex - 1);
                        newPos = (before.getPos()) + 1;
                    } else {
                        Card before = myListModel.get(dragTargetIndex - 1);
                        Card after  = myListModel.get(dragTargetIndex + 1);
                        newPos = (after.getPos() + before.getPos()) / 2;
                    }

                    dragElement.setPos(newPos);
                    userModifiedCards.add(dragElement.getIdShort());
                    dragSourceIndex = currentIndex;
                }
            }
        }
    }

    class MoveToListAction implements ActionListener {
        private String sListId;
        private Card cCard;

        public MoveToListAction(Card card, String list) {
            this.cCard = card;
            this.sListId = list;
        }

        public void actionPerformed(ActionEvent ev) {
            try {
                TrelloClient.GetInstance().moveCardToList(cCard, sListId);
                TrelloClient.GetInstance().updateOnce();
            }catch(Exception ex) {
                log.error(ex);
            }
        }
    }
}

