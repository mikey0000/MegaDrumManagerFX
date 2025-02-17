package info.megadrum.managerfx.ui;

import java.io.File;
import java.io.IOException;

import info.megadrum.managerfx.data.ConfigOptions;
import info.megadrum.managerfx.data.FileManager;
import info.megadrum.managerfx.midi.MidiController;
import info.megadrum.managerfx.utils.Constants;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UIUpgrade {
    private final Stage window;
    public final static int UPGRADE_SUCCESS = 0;
    public final static int UPGRADE_NOT_PERFORMED = 1;
    public final static int UPGRADE_FAILED = 2;
    private int upgradeResult = UPGRADE_NOT_PERFORMED;
    private boolean upgradeIsInProgress = false;
    private final Label textAreaInstruction;
    private final Button buttonStart, buttonCancel, buttonClose, buttonOpen;
    private final TextField textFieldFileName;
    private final ProgressBar progressBar;
    private final Label labelResult;
    private final MidiController midiController;
    private File file;
    private final FileManager fileManager;
    private final ConfigOptions configOptions;

    public UIUpgrade(MidiController controller, FileManager fm, ConfigOptions config) {
        configOptions = config;
        fileManager = fm;
        midiController = controller;
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("MegaDrum Firmware Upgrade");
        window.setOnCloseRequest(e -> {
            e.consume();
            closeWindow();
        });
        window.getIcons().add(new Image("/icon_256x256.png"));

        textAreaInstruction = new Label();
        textAreaInstruction.setFont(new Font(16));
        textAreaInstruction.setPadding(new Insets(2, 2, 2, 2));
        textAreaInstruction.setMinHeight(400);
        textAreaInstruction.setMaxHeight(400);
        VBox vBoxTop = new VBox();
        vBoxTop.setPadding(new Insets(2, 2, 2, 2));
        vBoxTop.setAlignment(Pos.TOP_CENTER);
        vBoxTop.getChildren().add(textAreaInstruction);

        textFieldFileName = new TextField();
        textFieldFileName.setMinWidth(450);
        textFieldFileName.setOnKeyReleased(_ -> textFieldChanged());
        buttonOpen = new Button("Open");
        buttonOpen.setOnAction(_ -> selectFile());
        HBox hBoxFileSelection = new HBox();
        hBoxFileSelection.setAlignment(Pos.CENTER);
        hBoxFileSelection.getChildren().addAll(new Label("MegaDrum firmware:"), textFieldFileName, buttonOpen);

        vBoxTop.getChildren().add(hBoxFileSelection);

        progressBar = new ProgressBar();
        progressBar.setPadding(new Insets(2, 0, 2, 0));
        progressBar.prefWidthProperty().bind(vBoxTop.widthProperty().subtract(10));
        vBoxTop.getChildren().add(progressBar);
        labelResult = new Label();
        labelResult.setFont(new Font(16));
        labelResult.setPadding(new Insets(2, 0, 10, 0));
        vBoxTop.getChildren().add(labelResult);

        buttonStart = new Button("Start");
        buttonStart.setOnAction(_ -> startUpgrade());
        buttonCancel = new Button("Cancel");
        buttonCancel.setOnAction(_ -> midiController.cancelUpgrade());
        buttonClose = new Button("Close");
        buttonClose.setOnAction(_ -> closeWindow());
        HBox hBoxButtons = new HBox();
        hBoxButtons.setAlignment(Pos.CENTER);
        hBoxButtons.getChildren().addAll(buttonStart, new Label("    "), buttonCancel, new Label("    "), buttonClose);

        vBoxTop.getChildren().add(hBoxButtons);
        window.setScene(new Scene(vBoxTop));
    }

    private void setLabelResult(String s, int error) {
        String fontColor = "blue";
        if (s.isEmpty()) {
            labelResult.setText("");
        } else {
            if (error > 0) {
                fontColor = "red";
            } else {
                if (error < 0) {
                    fontColor = "orange";
                } else {
                    fontColor = "green";
                }
            }
            labelResult.setText(s);
        }
        labelResult.setStyle("-fx-text-fill: " + fontColor);
    }

    public void show() {
        progressBar.progressProperty().unbind();
        midiController.openMidi(configOptions.MidiInName, configOptions.MidiOutName, "");
        midiController.setInFirmwareUpgrade(true);
        if (configOptions.mcuType > 2) {
            textAreaInstruction.setText(Constants.UPGRADE_INSTRUCTION_ARM);
        } else {
            textAreaInstruction.setText(Constants.UPGRADE_INSTRUCTION_ATMEGA);
        }
        upgradeResult = UPGRADE_NOT_PERFORMED;
        window.setResizable(false);
        setButtons(true);
        setLabelResult("", 0);
        progressBar.setProgress(0);
        window.showAndWait();
    }

    public int getUpgradeResult() {
        return upgradeResult;
    }

    private void closeWindow() {
        if (!upgradeIsInProgress) {
            midiController.setInFirmwareUpgrade(false);
            window.close();
        }
    }

    private void setButtons(boolean state) {
        textFieldFileName.setDisable(!state);
        buttonStart.setDisable(!state);
        buttonClose.setDisable(!state);
        buttonCancel.setDisable(state);
        buttonOpen.setDisable(!state);
    }

    private void startUpgrade() {
        if (file != null) {
            upgradeIsInProgress = true;
            setLabelResult("Upgrade is in progress", -1);
            Task<Integer> taskUpgrade;
            try {
                setButtons(false);
                taskUpgrade = midiController.doFirmwareUpgrade(file, configOptions.mcuType, progressBar);
                taskUpgrade.setOnSucceeded(_ -> upgradeFinished());
                new Thread(taskUpgrade).start();
            } catch (IOException e1) {
                e1.printStackTrace();
                setButtons(true);
                progressBar.progressProperty().unbind();
                upgradeIsInProgress = false;
                upgradeResult = UPGRADE_FAILED;
            }
        } else {
            setLabelResult("Incorrect or no file specified", 1);
        }
    }

    private void upgradeFinished() {
        upgradeIsInProgress = false;
        progressBar.progressProperty().unbind();
        setButtons(true);
        if (midiController.getUpgradeError() > 0) {
            upgradeResult = UPGRADE_FAILED;
        } else {
            upgradeResult = UPGRADE_SUCCESS;
            progressBar.setProgress(1);
        }
        setLabelResult(midiController.getUpgradeString(), midiController.getUpgradeError());
    }

    private void selectFile() {
        setLabelResult("", 0);
        file = fileManager.selectFirmwareFile(configOptions);
        if (file != null) {
            textFieldFileName.setText(file.getAbsolutePath());
            buttonStart.setDisable(false);
        } else {
            textFieldFileName.setText("");
        }
        window.toFront();
    }

    private void textFieldChanged() {
        file = new File(textFieldFileName.getText());
        buttonStart.setDisable(!file.isFile());
    }
}
