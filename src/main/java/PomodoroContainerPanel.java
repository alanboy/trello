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

    private CardButton topCardButton;
    private JButton minimizeList;
    private String listId;
    private static final int ONE_SECOND = 1000;
    private static final int HALF_SECOND = 500;
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
            changeLed ("pomodoro");
            TrelloClient.GetInstance().newCommentToCard(
                card.getId(),
                "New pomodoro started in " + InetAddress.getLocalHost().getHostName());
        } catch(Exception ex) {
            //log.error(ex);
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

    private String state = "";
    private void changeLed(String state) {
        try{

            if (state != this.state) {
                switch (state) {
                    case "pomodoro":
                        Files.copy(
                            new File("c:\\Users\\alanb\\code\\trello\\device\\code.py_pomodoro").toPath(),
                            new File("e:\\code.py").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    break;

                    case "rest":
                        Files.copy(
                            new File("c:\\Users\\alanb\\code\\trello\\device\\code.py_rest").toPath(),
                            new File("e:\\code.py").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    break;

                    case "standby":
                        Files.copy(
                            new File("c:\\Users\\alanb\\code\\trello\\device\\code.py_standby").toPath(),
                            new File("e:\\code.py").toPath(), StandardCopyOption.REPLACE_EXISTING);
                    break;
                }
            }

            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String text = date.format(formatter);

            //LocalDate parsedDate = LocalDate.parse(text, formatter);
            if (state == "pomodoro") {
                String content = "\n" + text + "\n";
                content += "    +25m #pomodoro " + card.getName() + "\n";
                //String content =  "{ \"timestamp\": \"" + now  + "\", \"event\": \""+ state +"\", " +  "\"id\": \"" + card.getId() + "\" ," +  "\"title\": \"" + card.getName() + "\" }, \n";

                Files.write( Paths.get("C:\\Users\\alanb\\activity.txt"),content.getBytes(), StandardOpenOption.APPEND);
            }

        } catch (IOException ioe) {
            System.out.println(ioe);
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

    private static Point windowPosition;

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

                        changeLed ("rest");
                        TrelloClient.GetInstance().newCommentToCard(
                            card.getId(),
                            "New pomodoro finished in " + InetAddress.getLocalHost().getHostName());
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
