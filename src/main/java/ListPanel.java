import org.trello4j.model.Card;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import org.apache.logging.log4j.*;

class ListPanel extends JPanel {
    private List<Card> cards;
    private final String sListId;
    private boolean isDroppedDown = false;
    private List<org.trello4j.model.List>  listsInBoard;
    Logger log;

    ListPanel(final String sListId, final List<Card> cards, List<org.trello4j.model.List> listsInBoard) {
        this.sListId = sListId;
        this.cards = cards;
        this.listsInBoard = listsInBoard;
        this.log = LogManager.getLogger();

        log.info("Creating new ListPanel:" + sListId);

        update();
    }

    private void update() {

        log.debug("Updating list cards/UI");

        // @TODO: Actually merge cards
        this.removeAll();

        boolean bFirstCard = true;
        for (Card c : cards) {
            CardButton cardButton = new CardButton(c, this);

            boolean bVisible = bFirstCard || isDroppedDown;
            cardButton.setVisible(bVisible);

            if (bFirstCard) {
                bFirstCard = false;
            }

            // Add CardButton (JButton) to ListPanel (JPanel)
            this.add(cardButton);
        }

        this.setLayout(new InvisibleGridLayout(0,1));
        this.setAlignmentY(Component.TOP_ALIGNMENT);
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

    public void toggleDropDown() {
        isDroppedDown = !isDroppedDown;
        int i= 0;
        for(Component c : this.getComponents()) {
            // Don't toggle first component
            if (i++ < 1) continue;
            c.setVisible(isDroppedDown);
        }

        JFrame topFrame = (JFrame) SwingUtilities.windowForComponent(this);
        topFrame.pack();
    }

    public void updateTimes() {
        for (Component c : this.getComponents()) {
            CardButton card = (CardButton)c;
            card.update();
        }
    }
}

class MoveToListAction implements ActionListener{
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
            //log.error(ex);
        }
    }
}
