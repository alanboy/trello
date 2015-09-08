import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.trello4j.model.Card;
import org.apache.logging.log4j.*;

class MainMenuButton extends JButton {
    Logger log;
    private boolean isMovingWindow;

    MainMenuButton() {
        this.log = LogManager.getLogger();

        log.info("Creating MainMenuButton");

        this.setMargin(new Insets(0, 0, 0, 0));
        this.setContentAreaFilled(false);
        this.setFocusPainted(false);
        this.isMovingWindow = false;

        this.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent ev) {
                if (!ev.isPopupTrigger()) {
                    Point currPosition = MouseInfo.getPointerInfo().getLocation();
                    UIServer.moveWindowTo(currPosition);
                }
            }

            public void mousePressed(MouseEvent ev) {
                if (!ev.isPopupTrigger()) {
                    Point currPosition = MouseInfo.getPointerInfo().getLocation();
                    UIServer.moveWindowTo(currPosition);
                }
            }
        });

        // Contextual menu
        JPopupMenu buttonPopUp = new JPopupMenu();

        JMenuItem refreshMenu = new JMenuItem("Refresh");
        refreshMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
            }
        });

        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Add menus
        buttonPopUp.addSeparator();
        buttonPopUp.add(refreshMenu);
        buttonPopUp.add(exitMenu);
        this.setComponentPopupMenu(buttonPopUp);

        this.setText("T");
    }
}

