import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import org.apache.logging.log4j.*;
import org.trello4j.model.Card;

class ContainerPanel extends JPanel {

    private CardButton topCardButton;
    private JButton minimizeList;
    private ListPanel listPanel;
    private String listId;
    private boolean isDroppedDown;
    private static final int ONE_SECOND = 1000;

    // Wraps the ListPanel which contains the cards
    // It also contains the top button and the minimize button
    ContainerPanel(final String sListId, final List<Card> cards, List<org.trello4j.model.List> listsInBoard) {
        super();

        listId = sListId;
        isDroppedDown = false;

        listPanel = new ListPanel(sListId, cards, listsInBoard);
        listPanel.setVisible(isDroppedDown);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (listPanel.listModel.size() > 0) {
            Card topcard = listPanel.listModel.elementAt(0);
            topCardButton = new CardButton(topcard);
            topCardButton.setLayout(new BoxLayout(topCardButton, BoxLayout.Y_AXIS));
            topCardButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            topCardButton.setVisible(!isDroppedDown);
            topCardButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    isDroppedDown = true;
                    updateVisibilityOfElements  ();
                }
            });
            this.add(topCardButton);
        }

        minimizeList = new JButton("^^^ close ^^^^");
        minimizeList.setVisible(isDroppedDown);
        minimizeList.setLayout(new BoxLayout(minimizeList, BoxLayout.Y_AXIS));
        minimizeList.setAlignmentX(Component.CENTER_ALIGNMENT);
        minimizeList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                isDroppedDown = false;
                updateVisibilityOfElements  ();
            }
        });

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(minimizeList);
        this.add(listPanel);

        new Thread(() -> updateTimersOnCards()).start();
    }

    private void updateTimersOnCards() {
        while(true) {
            try {
                Thread.sleep(ONE_SECOND);
                updateVisibilityOfElements();
            } catch (InterruptedException ie) {

            }
        }
    }

    private void updateVisibilityOfElements() {
        // top card topCardButton
        if (!isDroppedDown) {
            topCardButton.updateCard(listPanel.listModel.elementAt(0));
        }

        topCardButton.setVisible(!isDroppedDown);
        minimizeList.setVisible(isDroppedDown);

        //  list of cards
        listPanel.setVisible(isDroppedDown);
        listPanel.getParent().repaint();

        JDialog topFrame = (JDialog)SwingUtilities.windowForComponent(listPanel);
        if (topFrame != null)
            topFrame.pack();
    }

    public ListPanel getListPanel() {
        return listPanel;
    }

    public String getListId() {
        return listId;
    }
}
