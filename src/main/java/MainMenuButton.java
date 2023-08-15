import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.List;
import org.trello4j.model.*;
import org.trello4j.model.Card;
import org.apache.logging.log4j.*;

import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class MainMenuButton extends JButton {
    Logger log;
    private boolean isMovingWindow;

    MainMenuButton() {
        this.log = LogManager.getLogger();

        log.info("Creating MainMenuButton");

        this.setMargin(new Insets(0, 0, 0, 0));
        this.setFocusPainted(false);
        this.isMovingWindow = false;

        this.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent ev) {
                if (!ev.isPopupTrigger()) {
                    Point currPosition = MouseInfo.getPointerInfo().getLocation();
                    UIServer.moveWindowTo(currPosition);
                    PomodoroContainerPanel.moveWindowTo(currPosition);
                }
            }

            public void mousePressed(MouseEvent ev) {
                //if (!ev.isPopupTrigger()) {
                //    Point currPosition = MouseInfo.getPointerInfo().getLocation();
                //    UIServer.moveWindowTo(currPosition);
                //}
            }
        });

        JMenuItem openBugInGithub = new JMenuItem("Write json");
        openBugInGithub.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                Macropad.getInstance().Write();
            }
        });

        JMenuItem checkForUpdates = new JMenuItem("Check for updates");
        checkForUpdates.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                // TrelloClient to check for updateded binary in github
                // https://raw.githubusercontent.com/alanboy/trello/master/dist/latest/version.json
                // Download & patch
                // Restart this thing
            }
        });

        JMenuItem openConfiguration = new JMenuItem("Configuration...");
        openConfiguration.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                new TrelloConfigurationWindow();
            }
        });

        JMenuItem exitMenu = new JMenuItem("Exit");
        exitMenu.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Add menus
        // Contextual menu
        JPopupMenu buttonPopUp = new JPopupMenu();
        buttonPopUp.add(openConfiguration);
        buttonPopUp.add(openBugInGithub);
        buttonPopUp.add(checkForUpdates);
        buttonPopUp.add(exitMenu);
        this.setComponentPopupMenu(buttonPopUp);

        this.setText("T");
    }
}

