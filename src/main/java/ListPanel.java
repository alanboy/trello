import org.trello4j.model.Card;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

class ListPanel extends JPanel
{
    private List<Card> cards;
    private final String sListId;
    private boolean isDroppedDown = false;
    private List<org.trello4j.model.List>  listsInBoard;

    private void toggleDropDown()
    {
        isDroppedDown = !isDroppedDown;
        int i= 0;
        for(Component c : this.getComponents())
        {
            // Don't toggle first component
            if (i++ < 1) continue;
            c.setVisible(isDroppedDown);
        }

        JDialog topFrame = (JDialog) SwingUtilities.windowForComponent(this);
        topFrame.pack();
    }

    public void updateTimes()
    {
        for(Component c : this.getComponents())
        {
            CardButton card = (CardButton)c;

            card.update();
        }
    }

    private void addCardButton(final Card c, boolean bStartAsInvisible)
    {
        CardButton button = new CardButton(c);
        button.setMargin(new Insets(0, 0, 0, 0));

        button.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent ev) {
                if (!ev.isPopupTrigger()) {
                    toggleDropDown();
                }
            }
        });

        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        button.setToolTipText(c.getName() + "\n" + c.getDesc());

        // Contextual menu
        JPopupMenu buttonPopUp = new JPopupMenu();

        JMenu moveToListMenu = new JMenu("Move to ...");
        for (org.trello4j.model.List listInBoard : listsInBoard) {
            JMenuItem menuForList = new JMenuItem(listInBoard.getName());
            MoveToListAction f = new MoveToListAction(c, listInBoard.getId());
            menuForList.addActionListener(f);
            moveToListMenu.add(menuForList);
        }

        JMenuItem refreshMenu = new JMenuItem("Refresh");
        refreshMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                try
                {
                    TrelloClient.GetInstance().updateOnce();
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }
        });

        JMenuItem newCardMenu = new JMenuItem("New card");
        newCardMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                String newCardTitle = JOptionPane.showInputDialog("New card");
                try
                {
                    TrelloClient.GetInstance().newCardToList(sListId, newCardTitle);
                    TrelloClient.GetInstance().updateOnce();
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
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
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    TrelloClient.GetInstance().archiveCard(c);
                    TrelloClient.GetInstance().updateOnce();
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }
        });

        // Add menus
        buttonPopUp.add(newCardMenu);
        buttonPopUp.add(moveToListMenu);
        buttonPopUp.add(refreshMenu);
        buttonPopUp.add(archiveCardMenu);
        buttonPopUp.addSeparator();
        buttonPopUp.add(exitMenu);
        button.setComponentPopupMenu(buttonPopUp);

        // Start as invisible
        button.setVisible(!bStartAsInvisible);
        this.add(button);
    }

    ListPanel(final String sListId, final List<Card> cards, List<org.trello4j.model.List> listsInBoard)
    {
        this.sListId = sListId;
        this.cards = cards;
        this.listsInBoard = listsInBoard;

        int i = 0;
        boolean bFirstCard = true;
        for (Card c : cards)
        {
            if (bFirstCard)
            {
                bFirstCard = false;
                addCardButton(c, /*bStartInvisible*/ false);
            }
            else
            {
                addCardButton(c, /*bStartInvisible*/ true);
            }
        }

        this.setLayout(new InvisibleGridLayout(0,1));
        this.setAlignmentY(Component.TOP_ALIGNMENT);
        System.out.println("done adding cards to list");
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
            System.out.println("moving to " + sListId);
            TrelloClient.GetInstance().moveCardToList(cCard, sListId);
            TrelloClient.GetInstance().updateOnce();
        }catch(Exception ex) {
            System.out.println(ex);
        }
    }
}
