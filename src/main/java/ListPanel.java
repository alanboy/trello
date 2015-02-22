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
    private int MAX_CHARACTERS_IN_TITLE = 16;

    private void toggleDropDown()
    {
        isDroppedDown = !isDroppedDown;
        int i= 0;
        for(Component c : this.getComponents())
        {
            // Don't toggle top 2 components
            if (i++ < 2) continue;
            c.setVisible(isDroppedDown);
            //c.setSize(0,0);
        }
        //this.getTopLevelAncestor()
        //JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        JFrame topFrame = (JFrame) SwingUtilities.windowForComponent(this);
        topFrame.pack();
        //topFrame.setLocation(200, 100);
        //topFrame.repaint();
    }

    private void addCardButton(Card c, boolean bStartAsInvisible)
    {
        // Define button and tool tip text
        JButton button = new JButton("");

        button.setText(
                c.getName().length() > MAX_CHARACTERS_IN_TITLE
                ? c.getName().substring(0, MAX_CHARACTERS_IN_TITLE) + "..."
                : c.getName());

        button.setForeground(Color.blue);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        if (c.getDesc().trim().length() > 0) {
            button.setToolTipText(c.getDesc());
        }

        // Contextual menu
        JPopupMenu buttonPopUp = new JPopupMenu();
        JMenuItem newCardMenu = new JMenuItem("New card");
        JMenuItem showNextCardMenu = new JMenuItem("Archive card");
        JMenuItem configurationMenu = new JMenuItem("Configuration ");
        JMenuItem exitMenu = new JMenuItem("Exit");

        newCardMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev)
            {
                String newCardTitle = JOptionPane.showInputDialog("New card");
                try {
                    TrelloClient.GetInstance().newCardToList(sListId, newCardTitle);
                }catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }
        });

        showNextCardMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("archiving card");
                TrelloClient.GetInstance().archiveCard(cards.get(0));
            }
        });

        buttonPopUp.add(newCardMenu);
        buttonPopUp.add(showNextCardMenu);
        buttonPopUp.addSeparator();
        buttonPopUp.add(configurationMenu);
        buttonPopUp.add(exitMenu);
        button.setComponentPopupMenu(buttonPopUp);

        // Start as invisible
        button.setVisible(!bStartAsInvisible);
        this.add(button);
    }

    private void addDropdownButton()
    {
        JButton button = new JButton("");

        button.setText("^");
        button.setForeground(Color.RED);
        button.setContentAreaFilled(false);
        button.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent ev) {
                if (!ev.isPopupTrigger()) {
                    toggleDropDown();
                }
            }
        });
        button.setFocusPainted(false);
        this.add(button);
    }

    ListPanel(final String sListId, final List<Card> cards)
    {
        this.sListId = sListId;
        this.cards = cards;

        int i = 0;
        boolean bFirstCard = true;
        for (Card c : cards)
        {
            if (bFirstCard)
            {
                bFirstCard = false;
                addCardButton(c, /*bStartInvisible*/ false);
                addDropdownButton();
            }
            else
            {
                addCardButton(c, /*bStartInvisible*/ true);
            }
        }

        //this.setLayout(new GridLayout(0,1));
        this.setLayout(new InvisibleGridLayout(0,1));
         this.setAlignmentY(Component.TOP_ALIGNMENT);
        //this.setAlignmentY(0);
        System.out.println("done adding cards to list");
    }
}