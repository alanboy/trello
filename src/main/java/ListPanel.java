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
    private int MAX_CHARACTERS_IN_TITLE = 24;
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

    private void addCardButton(final Card c, boolean bStartAsInvisible)
    {
        String title = c.getName().length() > MAX_CHARACTERS_IN_TITLE
                ? c.getName().substring(0, MAX_CHARACTERS_IN_TITLE) + "..."
                : c.getName();

        // The first 8 character of the card are the unix timestamp of creation
        long unixTime = System.currentTimeMillis() / 1000L;
        long timestamp = Long.parseLong(c.getId().substring(0,8), 16);
        int hoursago = (int)(unixTime - timestamp)/60/60;
        int daysago = hoursago/24;

        // TODO: color depending on the number of days
        String html =  "<html><font color=blue>" + title + "</font>"
            +  " <font color=red>" 
            + ((daysago == 0) ? hoursago + "h" : daysago + "d")
            + "</font></html>";

        JButton button = new JButton(html);
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


        JMenu moveToDoneMenu = new JMenu("Move to ...");
        int i = 0;
        for (org.trello4j.model.List listInBoard : listsInBoard)
        {
            JMenuItem menuForList = new JMenuItem(listInBoard.getName(), i++);

            foo f = new foo();
            f.sListId = listInBoard.getId();
            f.c = c;
            menuForList.addActionListener(f);

            moveToDoneMenu.add(menuForList);
        }

        JMenuItem refreshMenu = new JMenuItem("Refresh");
        refreshMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                try {
                    TrelloClient.GetInstance().updateOnce();
                }catch(Exception ex)
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
                try {
                    TrelloClient.GetInstance().newCardToList(sListId, newCardTitle);
                    TrelloClient.GetInstance().updateOnce();
                }catch(Exception ex)
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
                System.out.println("archiving card");
                TrelloClient.GetInstance().archiveCard(c);
            }
        });

        // Add menus
        buttonPopUp.add(newCardMenu);
        buttonPopUp.add(moveToDoneMenu);
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
class foo implements ActionListener{
    public String sListId;
    public Card c;

    public void actionPerformed(ActionEvent ev)
    {
        try {
            System.out.println("moving to " + sListId);
            TrelloClient.GetInstance().moveCardToList(c, sListId);
            TrelloClient.GetInstance().updateOnce();
        }catch(Exception ex)
        {
            System.out.println(ex);
        }
    }

}
