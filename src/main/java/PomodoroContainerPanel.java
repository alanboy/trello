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
import java.io.File;
import java.nio.file.Files;
import java.nio.file.*;
import java.io.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.io.FileWriter;
import java.time.LocalDate;

class PomodoroContainerPanel extends JPanel {

    private static Point windowPosition;
    private static final int HALF_SECOND = 500;
    private static final int ONE_SECOND = 1000;
    private Card card;
    private CardButton topCardButton;
    private JButton minimizeList;
    private String listId;
    private int seconds;
    Logger logger;

    PomodoroContainerPanel(Card card, CardButton cardButton) {
        super();

        logger = LogManager.getLogger();

        seconds = 0;
        topCardButton = cardButton;
        this.card = card;

        minimizeList = new JButton("");
        minimizeList.setLayout(new BoxLayout(minimizeList, BoxLayout.Y_AXIS));
        minimizeList.setAlignmentX(Component.CENTER_ALIGNMENT);

        PomodoroMouseAdapter mouseAdapter = new PomodoroMouseAdapter(card);
        minimizeList.addMouseListener(mouseAdapter);

        minimizeList.setMargin(new Insets(0, 0, 0, 0));
        minimizeList.setContentAreaFilled(false);
        minimizeList.setFocusPainted(false);
        minimizeList.setOpaque(true);

        minimizeList.setFont(new Font("Arial", Font.PLAIN, 30));

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(minimizeList);

        try {
            String response = JOptionPane.showInputDialog("What do you want to accomplish during the next 25 minutes?");
            LED.getInstance().changeLed("pomodoro");
            writePomodoroActivity(response);

            TrelloClient.GetInstance().newCommentToCard(
                card.getId(),
                "Pomodoro started  @ " + InetAddress.getLocalHost().getHostName() + ":" + response);
        } catch(Exception ex) {
            logger.error(ex);
        }

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

    // Write the activity on the Pomodoro activity file
    private void writePomodoroActivity(String details) {
        String activityFileName = "C:\\Users\\alanb\\activity.txt";

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String text = date.format(formatter);

        String content = "\n" + text + "\n";
        content += "    +25m #pomodoro - " + card.getName() + " - " + details + "\n";

        try {
            File activityFile = new File(activityFileName);
            if (!activityFile.exists()) {
                return;
            }

            Files.write(
                Paths.get(activityFileName),
                content.getBytes(),
                StandardOpenOption.APPEND);

        } catch (Exception e) {
            logger.error(e);
        }
    }

    private void updateVisibilityOfElements() throws InterruptedException {
        int timeLeft = (25 * 60) - seconds;
        int minutesLeft = timeLeft / 60;
        int secondsLeft = timeLeft % 60;

        if (timeLeft <= 0) {
            minimizeList.setBackground(Color.decode("0xfea56e"));
            Thread.sleep(HALF_SECOND);
            minimizeList.setBackground(Color.decode("0xfc6161"));
            return;
        }

        switch (timeLeft) {
            case 20*60: minimizeList.setBackground(Color.decode("0xfff4d9")); break;
            case 15*60: minimizeList.setBackground(Color.decode("0xffd794")); break;
            case 10*60: minimizeList.setBackground(Color.decode("0xfea56e")); break;
            case 5*60: minimizeList.setBackground(Color.decode("0xfc6161")); break;
        }

        String minutesLeftString = (minutesLeft <= 9 ? "0" : "") + minutesLeft;
        String secondsLeftString = (secondsLeft <= 9 ? "0" : "") + secondsLeft;

        String text = minutesLeftString + ":" + secondsLeftString
                + " - "
                + card.getName();

        minimizeList.setText(text);

        JDialog topFrame = (JDialog)SwingUtilities.windowForComponent(this);
        if (topFrame != null)
            topFrame.pack();

        if (topFrame != null && windowPosition != null) {
            topFrame.setLocation((int)windowPosition.getX(), (int)windowPosition.getY());
        }
    }

    public static void moveWindowTo(Point position) {
        windowPosition = position;
    }

    class PomodoroMouseAdapter extends MouseInputAdapter {

        private Card card;
        PomodoroMouseAdapter(Card card) {
            this.card = card;
        }

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

                    try {
                        LED.getInstance().changeLed("rest");
                        TrelloClient.GetInstance().newCommentToCard(
                            card.getId(),
                            "Pomodoro finished @ " + InetAddress.getLocalHost().getHostName());
                    } catch(Exception ex) {
                        //log.error(ex);
                    }

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
