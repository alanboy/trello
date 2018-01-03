import org.trello4j.model.Card;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.event.*;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import java.net.URI;
import java.net.*;
import java.io.IOException;
import org.apache.logging.log4j.*;

class ContainterPanel extends JPanel {

    private String listId;
    private ListPanel listPanel;
    private CardButton button;
    private JButton minimizeList;
    private boolean isDroppedDown;

    ContainterPanel(final String sListId, final List<Card> cards, List<org.trello4j.model.List> listsInBoard) {
        super();

        listId = sListId;
        isDroppedDown = false;

        listPanel = new ListPanel(sListId, cards, listsInBoard);
        listPanel.setVisible(isDroppedDown);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (listPanel.listModel.size() > 0) {
            Card topcard = listPanel.listModel.elementAt(0);
            button = new CardButton(topcard);
            button.setLayout(new BoxLayout(button, BoxLayout.Y_AXIS));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setVisible(!isDroppedDown);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    isDroppedDown = true;
                    updateVisibilityOfElements  ();
                }
            });
            this.add(button);
        }

        minimizeList = new JButton("^^^^^^^^");
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
        this.add(listPanel);

        this.add(minimizeList);
    }

    private void updateVisibilityOfElements() {

        // top card button
        if (!isDroppedDown) {
            button.updateCard(listPanel.listModel.elementAt(0));
        }
        button.setVisible(!isDroppedDown);

        //  list of cards
        listPanel.setVisible(isDroppedDown);
        listPanel.getParent().repaint();

        // show the minime button
        minimizeList.setVisible(isDroppedDown);

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
