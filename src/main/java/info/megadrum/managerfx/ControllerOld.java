package info.megadrum.managerfx;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ControllerOld {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}