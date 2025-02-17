package info.megadrum.managerfx.ui;

import java.util.List;

import info.megadrum.managerfx.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

public class UIComboBox extends UIControl {
    private ComboBox<String> comboBox;
    private boolean comboBoxWide = false;
    private int maxStringLength;

    public UIComboBox(String labelText, boolean showCopyButton) {
        super(labelText, showCopyButton);
        init();
    }

    private void init() {
        valueType = Constants.VALUE_TYPE_INT;
        comboBox = new ComboBox<>();
        comboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (changedFromSet > 0) {
                    changedFromSet--;
                } else {
                    int newIntValue = comboBox.getSelectionModel().getSelectedIndex();
                    if (newIntValue > -1) {
                        if (intValue != newIntValue) {
                            intValue = newIntValue;
                            fireControlChangeEvent(new ControlChangeEvent(this), 0);
                            if (syncState != Constants.SYNC_STATE_UNKNOWN) {
                                if (intValue == mdIntValue) {
                                    setSyncState(Constants.SYNC_STATE_SYNCED);
                                } else {
                                    setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
                                }
                            }
                        }
                    }
                }
            }
        });

        comboBox.setEditable(false);
        HBox layout = new HBox();
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(comboBox);
        initControl(layout);
    }

    @Override
    public void respondToResize(double w, double h) {
        double width;
        super.respondToResize(w, h);
        comboBox.setMinHeight(h);
        comboBox.setMaxHeight(h);
        if (comboBoxWide) {
            width = w * 0.67;
        } else {
            width = w * 0.50;
        }
        comboBox.setMinWidth(width);
        comboBox.setMaxWidth(width);
        double fontSize;
        if (maxStringLength > 16) {
            fontSize = h * 0.26;
        } else if (maxStringLength > 8) {
            fontSize = h * 0.31;
        } else {
            fontSize = h * 0.39;
        }
        comboBox.setStyle("-fx-font-size: " + fontSize + "pt");
    }

    public void uiCtlSetValuesArray(List<String> list) {
        maxStringLength = 0;
        for (String string : list) {
            if (string.length() > maxStringLength) {
                maxStringLength = string.length();
            }
        }
        int s = comboBox.getSelectionModel().getSelectedIndex();
        comboBox.getItems().clear();
        comboBox.getItems().addAll(list);
        comboBox.getSelectionModel().select(s);
    }

    public void uiCtlSetValue(int n, boolean setFromSysex) {
        if (intValue != n) {
            changedFromSet = 1;
            intValue = n;
        }
        if (setFromSysex) {
            setSyncState(Constants.SYNC_STATE_SYNCED);
            mdIntValue = n;
        } else {
            updateSyncStateConditional();
        }
        comboBox.getSelectionModel().select(n);
    }

    public int uiCtlGetValue() {
        return intValue;
    }

    public void uiCtlSetValue(String value) {
        comboBox.setValue(value);
    }

    public String uiCtlGetSelected() {
        return comboBox.getValue();
    }

    public void addListener(ChangeListener<String> listener) {
        comboBox.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    public void uiCtlSetDisable(boolean state) {
        comboBox.setDisable(state);
    }

    public void enableComboBoxWide() {
        comboBoxWide = true;
    }
}
