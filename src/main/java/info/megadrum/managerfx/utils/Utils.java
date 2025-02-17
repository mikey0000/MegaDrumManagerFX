package info.megadrum.managerfx.utils;

import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.web.WebView;

public class Utils {

    public static void delayMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Utils.show_error("Unrecoverable timer error. Exiting.\n(" + e.getMessage() + ")");
            System.exit(1);
        }
    }

    public static void show_error(String msg) {
        WebView webView = new WebView();
        webView.getEngine().loadContent(msg);
        webView.setPrefSize(300, 120);

        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText("Error!");
        alert.getDialogPane().setContent(webView);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(alert::showAndWait);
            }
        }, 10);
    }

    public static byte[] byte2sysex(byte b) {
        byte[] result = new byte[2];
        result[0] = (byte) ((b & 0xf0) >> 4);
        result[1] = (byte) (b & 0x0f);
        return result;
    }

    public static byte[] short2sysex(short s) {
        byte[] result = new byte[4];
        result[0] = (byte) ((s & 0x00f0) >> 4);
        result[1] = (byte) (s & 0x000f);
        result[2] = (byte) ((s & 0xf000) >> 12);
        result[3] = (byte) ((s & 0x0f00) >> 8);
        return result;
    }

    public static byte sysex2byte(byte[] sx) {
        return (byte) (((sx[0] & 0x0f) << 4) | (sx[1] & 0x0f));
    }

    public static short sysex2short(byte[] sx) {
        return (short) (((sx[0] & 0x0f) << 4) | (sx[1] & 0x0f) | ((sx[2] & 0x0f) << 12) | ((sx[3] & 0x0f) << 8));
    }

    public static int validateInt(int value, int min, int max, int fallBack) {
        if ((value >= min) && (value <= max)) {
            return value;
        } else {
            return fallBack;
        }
    }

    public static Double validateDouble(Double value, Double min, Double max, Double fallBack) {
        if ((value >= min) && (value <= max)) {
            return value;
        } else {
            return fallBack;
        }
    }
}
