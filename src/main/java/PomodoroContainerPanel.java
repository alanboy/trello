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

class PomodoroContainerPanel extends JPanel {

    private CardButton topCardButton;
    private JButton minimizeList;
    private String listId;
    private static final int ONE_SECOND = 1000;
    private int seconds;
    private Card card;

    PomodoroContainerPanel(Card card, CardButton cardButton) {
        super();

        seconds = 0;
        topCardButton = cardButton;
        this.card = card;

        minimizeList = new JButton("");
        minimizeList.setLayout(new BoxLayout(minimizeList, BoxLayout.Y_AXIS));
        minimizeList.setAlignmentX(Component.CENTER_ALIGNMENT);

        PomodoroMouseAdapter mouseAdapter = new PomodoroMouseAdapter();
        minimizeList.addMouseListener(mouseAdapter);


        minimizeList.setMargin(new Insets(0, 0, 0, 0));
        minimizeList.setContentAreaFilled(false);
        minimizeList.setFocusPainted(false);
        minimizeList.setOpaque(true);

        minimizeList.setFont(new Font("Arial", Font.PLAIN, 30));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(minimizeList);

        new Thread(() -> updateTimersOnCards()).start();
    }

    private void updateTimersOnCards() {
        while(true) {
            try {
                updateVisibilityOfElements();
                Thread.sleep(ONE_SECOND);
                seconds++;
            } catch (InterruptedException ie) {

            }
        }
    }

    private void updateVisibilityOfElements() {
        int timeLeft = (25 * 60) - seconds;
        int minutesLeft = timeLeft / 60;
        int secondsLeft = timeLeft % 60;

        String minutesLeftString = (minutesLeft <= 9 ? "0" : "") + minutesLeft;
        String secondsLeftString = (secondsLeft <= 9 ? "0" : "") + secondsLeft;

        String text = minutesLeftString + ":" + secondsLeftString
                + " - "
                + card.getName();

        minimizeList.setText(text);

        JDialog topFrame = (JDialog)SwingUtilities.windowForComponent(this);
        if (topFrame != null)
            topFrame.pack();
    }

    class PomodoroMouseAdapter extends MouseInputAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }

            // Create contextual menu
            JPopupMenu buttonPopUp = new JPopupMenu();

            JMenuItem startPomodoroTimer = new JMenuItem("End pomodoro timer");
            startPomodoroTimer.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ev) {
                    UIServer.endPomodoroTimerForCard();
                }
            });

            JMenuItem exitMenu = new JMenuItem("Exit");
            exitMenu.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    // This might not get flushed to log:
                    //log.info("Exiting per user request");
                    System.exit(0);
                }
            });


            // Add menus
            buttonPopUp.add(startPomodoroTimer);
            buttonPopUp.add(exitMenu);

            buttonPopUp.show(e.getComponent(), e.getX(), e.getY()); //and show the menu
        }
    }
}
