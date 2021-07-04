import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import org.apache.logging.log4j.*;
import org.trello4j.model.Card;

public class LED {

    private static final String LED_SCRIPTS_BASE_PATH = "c:\\Users\\alanb\\code\\trello\\device\\";
    private static final String LED_DEVICE_BASE_PATH = "e:\\";
    private String state = "";
    private Logger logger;
    private static LED instance;

    private LED() {
        logger = LogManager.getLogger();
    }

    public static LED getInstance() {
        if (instance == null) {
            instance = new LED();
        }
        return instance;
    }

    private void copyToDevice(String sourceFile) {
        try{
            Files.copy(
                new File(LED_SCRIPTS_BASE_PATH + sourceFile).toPath(),
                new File(LED_DEVICE_BASE_PATH + "code.py").toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
            logger.error("Unable to copy into the USB device");
            logger.error(e);
        }
    }

    public void startTimer(int minutes) {
        PrintWriter printWriter = null;
        try{
            // Read the timer, and write it to the device
            BufferedReader br = new BufferedReader(new FileReader(LED_SCRIPTS_BASE_PATH + "timer.py"));

            File file = new File (LED_DEVICE_BASE_PATH + "code.py");
            printWriter = new PrintWriter(file);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("REPLACE_TIME_HERE")) {
                    line = "TimeInSeconds = " + (minutes * 60);
                }

                printWriter.println(line);
            }
        } catch (Exception e) {
            logger.error(e);

        } finally {
            printWriter.close();

        }

        this.state = "timer";
    }

    public void turnOff() {
        String contents = "import time\n"
            + "import board\n"
            + "import neopixel\n"
            + "import adafruit_pypixelbuf\n"
        
            + "pixel_pin = board.A3\n"
            + "num_pixels = 30\n"
            + "pixels = neopixel.NeoPixel(pixel_pin, num_pixels, auto_write=False)\n"
            + "pixels.brightness = 0\n"
            + "pixels.show()\n";

        PrintWriter printWriter = null;
        try{
            File file = new File (LED_DEVICE_BASE_PATH + "code.py");
            printWriter = new PrintWriter(file);

            printWriter.println(contents);
        } catch (Exception e) {
            logger.error(e);

        } finally {
            printWriter.close();

        }

        this.state = "off";
    }

    public void changeLed(String state) {
        if (state != this.state) {
            switch (state) {
                case "off":
                    copyToDevice("off.py");
                break;

                case "pomodoro":
                    copyToDevice("code.py_pomodoro");
                break;

                case "rest":
                    copyToDevice("code.py_rest");
                break;

                case "standby":
                    copyToDevice("code.py_standby");
                break;
            }
        }
    }
}
