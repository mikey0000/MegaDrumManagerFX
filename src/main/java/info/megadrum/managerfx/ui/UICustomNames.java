package info.megadrum.managerfx.ui;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.data.ConfigCustomName;
import info.megadrum.managerfx.utils.Constants;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class UICustomNames {
    private final VBox vBox;
    private final HBox toolBarTop;
    private final GridPane gridPane;
    private final Button buttonGetAll;
    private final Button buttonSendAll;
    private final Button buttonLoadAll;
    private final Button buttonSaveAll;

    private final ComboBox<String> comboBoxCustomNamesCount;
    private final ArrayList<Label> allLabels;
    private final ArrayList<TextField> allTextFields;
    private final ArrayList<Button> allGetButtons;
    private final ArrayList<Button> allSendButtons;
    private final int[] allSyncStates;
    private final String[] allCustomNames;
    private final String[] allMdCustomNames;
    private final int[] nameChangedFromSet;


    protected EventListenerList listenerList = new EventListenerList();

    public void addControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.add(ControlChangeEventListener.class, listener);
    }

    public void removeControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.remove(ControlChangeEventListener.class, listener);
    }

    protected void fireControlChangeEvent(ControlChangeEvent evt, Integer parameter) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ControlChangeEventListener.class) {
                ((ControlChangeEventListener) listeners[i + 1]).controlChangeEventOccurred(evt, parameter);
            }
        }
    }

    public UICustomNames() {
        Label labelCustomNamesCount = new Label("Custom Names:");
        comboBoxCustomNamesCount = new ComboBox<>();
        comboBoxCustomNamesCount.getItems().clear();
        comboBoxCustomNamesCount.getItems().addAll(Arrays.asList("2", "16", "32"));
        comboBoxCustomNamesCount.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                int start = switch (comboBoxCustomNamesCount.getSelectionModel().getSelectedIndex()) {
                    case 0 -> 2;
                    case 1 -> 16;
                    default -> 32;
                };
                for (int i = 0; i < Constants.CUSTOM_NAMES_MAX; i++) {
                    boolean visible = i < start;
                    allLabels.get(i).setVisible(visible);
                    allTextFields.get(i).setVisible(visible);
                    allGetButtons.get(i).setVisible(visible);
                    allSendButtons.get(i).setVisible(visible);
                }
            }
        });
        toolBarTop = new HBox();
        toolBarTop.setAlignment(Pos.CENTER_LEFT);
        buttonGetAll = new Button("GetAll");
        buttonSendAll = new Button("SendAll");
        buttonLoadAll = new Button("LoadAll");
        buttonSaveAll = new Button("SaveAll");
        toolBarTop.getChildren().addAll(labelCustomNamesCount, comboBoxCustomNamesCount, buttonGetAll, buttonSendAll, new Separator(Orientation.VERTICAL), buttonLoadAll, buttonSaveAll);

        vBox = new VBox(1);
        vBox.setStyle("-fx-padding: 0.0em 0.0em 0.2em 0.0em");
        vBox.getChildren().addAll(toolBarTop);
        allLabels = new ArrayList<>();
        allTextFields = new ArrayList<>();
        allGetButtons = new ArrayList<>();
        allSendButtons = new ArrayList<>();
        allSyncStates = new int[Constants.CUSTOM_NAMES_MAX];
        nameChangedFromSet = new int[Constants.CUSTOM_NAMES_MAX];
        allCustomNames = new String[Constants.CUSTOM_NAMES_MAX];
        allMdCustomNames = new String[Constants.CUSTOM_NAMES_MAX];
        gridPane = new GridPane();
        gridPane.getColumnConstraints().add(new ColumnConstraints(1));
        gridPane.getColumnConstraints().add(new ColumnConstraints(45));
        gridPane.getColumnConstraints().add(new ColumnConstraints(110));
        gridPane.getColumnConstraints().add(new ColumnConstraints(1));
        gridPane.getColumnConstraints().add(new ColumnConstraints(36));
        gridPane.getColumnConstraints().add(new ColumnConstraints(1));
        gridPane.getColumnConstraints().add(new ColumnConstraints(36));

        for (int i = 0; i < Constants.CUSTOM_NAMES_MAX; i++) {
            nameChangedFromSet[i] = 0;
            final int iFinal = i;
            allLabels.add(new Label("Name " + (i + 1) + ":"));
            GridPane.setConstraints(allLabels.get(i), 1, i);
            GridPane.setHalignment(allLabels.get(i), HPos.CENTER);
            GridPane.setValignment(allLabels.get(i), VPos.CENTER);
            gridPane.getChildren().add(allLabels.get(i));

            allTextFields.add(new TextField());
            GridPane.setConstraints(allTextFields.get(i), 2, i);
            GridPane.setHalignment(allTextFields.get(i), HPos.CENTER);
            GridPane.setValignment(allTextFields.get(i), VPos.CENTER);
            gridPane.getChildren().add(allTextFields.get(i));
            allTextFields.get(i).textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                    if (nameChangedFromSet[iFinal] > 0) {
                        nameChangedFromSet[iFinal] = 0;
                        allCustomNames[iFinal] = newValue;
                    } else {
                        if (allTextFields.get(iFinal).getText().length() > 8) {
                            int pos = allTextFields.get(iFinal).getCaretPosition();

                            String text = allTextFields.get(iFinal).getText();
                            text = text.trim();
                            text += "        ";
                            text = text.substring(0, 8);
                            text = text.trim();
                            allTextFields.get(iFinal).setText(text);
                            allCustomNames[iFinal] = text;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    allTextFields.get(iFinal).positionCaret(pos + 1);
                                }
                            });
                        } else {
                            allCustomNames[iFinal] = allTextFields.get(iFinal).getText();
                        }
                        fireControlChangeEvent(new ControlChangeEvent(this), Constants.CUSTOM_NAME_CHANGE_TEXT_START + iFinal);
                        testSyncState(iFinal);
                    }
                }
            });

            allGetButtons.add(new Button("Get"));
            GridPane.setConstraints(allGetButtons.get(i), 4, i);
            GridPane.setHalignment(allGetButtons.get(i), HPos.CENTER);
            GridPane.setValignment(allGetButtons.get(i), VPos.CENTER);
            gridPane.getChildren().add(allGetButtons.get(i));
            allGetButtons.get(i).setOnAction(e -> {
                fireControlChangeEvent(new ControlChangeEvent(this), Constants.CUSTOM_NAME_CHANGE_GET_START + iFinal);
            });

            allSendButtons.add(new Button("Send"));
            GridPane.setConstraints(allSendButtons.get(i), 6, i);
            GridPane.setHalignment(allSendButtons.get(i), HPos.CENTER);
            GridPane.setValignment(allSendButtons.get(i), VPos.CENTER);
            gridPane.getChildren().add(allSendButtons.get(i));
            allSendButtons.get(i).setOnAction(e -> {
                fireControlChangeEvent(new ControlChangeEvent(this), Constants.CUSTOM_NAME_CHANGE_SEND_START + iFinal);
            });

            setSyncState(Constants.SYNC_STATE_UNKNOWN, i);
        }
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(gridPane);
        vBox.getChildren().add(scrollPane);
        comboBoxCustomNamesCount.getSelectionModel().select(0);

    }

    public Node getUI() {
        return vBox;
    }

    public void respondToResize(Double w, Double h, Double controlW, Double controlH) {
        double buttonFontSize = controlH * Constants.FX_BUTTONS_FONT_SCALE;
        if (buttonFontSize > Constants.FX_BUTTONS_FONT_MAX_SIZE) {
            buttonFontSize = Constants.FX_BUTTONS_FONT_MAX_SIZE;
        }
        double customNamesFontHeight = controlH * 0.28;
        toolBarTop.setStyle("-fx-font-size: " + buttonFontSize + "pt");
        vBox.setStyle("-fx-font-size: " + customNamesFontHeight + "pt");
        gridPane.getColumnConstraints().remove(1);
        gridPane.getColumnConstraints().add(1, new ColumnConstraints(controlH * 2.5));
        gridPane.getColumnConstraints().remove(2);
        gridPane.getColumnConstraints().add(2, new ColumnConstraints(controlH * 2.7));
        gridPane.getColumnConstraints().remove(4);
        gridPane.getColumnConstraints().add(4, new ColumnConstraints(controlH * 1.7));
        gridPane.getColumnConstraints().remove(6);
        gridPane.getColumnConstraints().add(6, new ColumnConstraints(controlH * 1.7));
        toolBarTop.setMaxHeight(controlH);
        toolBarTop.setMaxWidth(vBox.getWidth() * 0.99);
    }

    public void setCustomName(ConfigCustomName config, int id, Boolean setFromSysex) {
        nameChangedFromSet[id] = 1;
        allTextFields.get(id).setText(config.name);
        if (setFromSysex) {
            setSyncState(Constants.SYNC_STATE_SYNCED, id);
            allMdCustomNames[id] = config.name;
        } else {
            testSyncState(id);
        }
    }

    private void testSyncState(int id) {
        if (allSyncStates[id] != Constants.SYNC_STATE_UNKNOWN) {
            if (allCustomNames[id].equals(allMdCustomNames[id])) {
                setSyncState(Constants.SYNC_STATE_SYNCED, id);
            } else {
                setSyncState(Constants.SYNC_STATE_NOT_SYNCED, id);
            }
        }
    }

    public void getCustomName(ConfigCustomName config, int id) {
        config.name = allCustomNames[id];
    }

    public Button getButtonGetAll() {
        return buttonGetAll;
    }

    public Button getButtonSendAll() {
        return buttonSendAll;
    }

    public Button getButtonLoadAll() {
        return buttonLoadAll;
    }

    public Button getButtonSaveAll() {
        return buttonSaveAll;
    }


    public ComboBox<String> getComboBoxCustomNamesCount() {
        return comboBoxCustomNamesCount;
    }

    public void setAllStateUnknown() {
        for (int i = 0; i < Constants.CUSTOM_NAMES_MAX; i++) {
            setSyncState(Constants.SYNC_STATE_UNKNOWN, i);
        }
    }

    public void setSyncState(int state, Integer namePointer) {
        allSyncStates[namePointer] = state;
        Color color = switch (state) {
            case Constants.SYNC_STATE_UNKNOWN -> Constants.SYNC_STATE_UNKNOWN_COLOR;
            case Constants.SYNC_STATE_NOT_SYNCED -> Constants.SYNC_STATE_NOT_SYNCED_COLOR;
            default -> Constants.SYNC_STATE_SYNCED_COLOR;
        };
        allLabels.get(namePointer).setTextFill(color);
    }
}
