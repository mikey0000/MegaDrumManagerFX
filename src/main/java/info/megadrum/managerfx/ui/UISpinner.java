package info.megadrum.managerfx.ui;

import java.util.Timer;
import java.util.TimerTask;

import info.megadrum.managerfx.utils.Constants;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class UISpinner extends UIControl {
    private SpinnerFast<Integer> spinnerFast;
    private int minValue;
    private Integer maxValue;
    private int spinnerType = Constants.FX_SPINNER_TYPE_STANDARD;
    private int step;
    private SpinnerValueFactory<Integer> valueFactory;
    private boolean changedByEdit = false;
    private int changedByEditTimers = 0;

    public UISpinner(String labelText, Integer min, Integer max, Integer initial, Integer s, Boolean showCopyButton) {
        super(labelText, showCopyButton);
        init(min, max, initial, s);
    }

    private void init(Integer min, Integer max, Integer initial, Integer s) {
        minValue = min;
        maxValue = max;
        intValue = initial;
        valueType = Constants.VALUE_TYPE_INT;

        step = s;
        valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, intValue, step);

        spinnerFast = new SpinnerFast<>();
        spinnerFast.setValueFactory(valueFactory);
        spinnerFast.setEditable(true);
        spinnerFast.getEditor().textProperty().addListener(new ChangeListener<>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (changedFromSet > 0) {
                    changedFromSet--;
                } else {
                    if (!newValue.matches("\\d*")) {
                        spinnerFast.getEditor().setText(String.valueOf(intValue));
                    } else {
                        if (newValue.matches("")) {
                            spinnerFast.getEditor().setText(String.valueOf(intValue));
                        } else {
                            if (intValue != Integer.parseInt(newValue)) {
                                intValue = (Integer.parseInt(newValue) / step) * step;
                                if (spinnerType == Constants.FX_SPINNER_TYPE_STANDARD) {
                                    if ((intValue >= minValue) & (intValue <= maxValue)) {
                                        fireControlChangeEvent(new ControlChangeEvent(this), 0);
                                        changedByEdit = true;
                                        changedByEditTimers++;
                                        new Timer().schedule(new TimerTask() {

                                            @Override
                                            public void run() {
                                                Platform.runLater(() -> {
                                                    if (changedByEditTimers == 1) {
                                                        changedByEdit = false;
                                                        if (syncState != Constants.SYNC_STATE_UNKNOWN) {
                                                            if (intValue == mdIntValue) {
                                                                setSyncState(Constants.SYNC_STATE_SYNCED);
                                                            } else {
                                                                setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
                                                            }
                                                        }
                                                        if (!Integer.valueOf(spinnerFast.getEditor().getText()).equals(intValue)) {
                                                            int cursor = spinnerFast.getEditor().getCaretPosition();
                                                            spinnerFast.getEditor().setText(String.valueOf(intValue));
                                                            spinnerFast.getEditor().positionCaret(cursor);
                                                        }
                                                    }
                                                    changedByEditTimers--;
                                                });
                                            }
                                        }, 1500);
                                    }
                                }

                            }
                        }
                    }
                }
                if (syncState != Constants.SYNC_STATE_UNKNOWN) {
                    if (intValue == mdIntValue) {
                        setSyncState(Constants.SYNC_STATE_SYNCED);
                    } else {
                        setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
                    }
                }
            }
        });

        HBox layout = new HBox();
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(spinnerFast);
        initControl(layout);
    }

    private void resizeFont(double h) {
        int l = maxValue.toString().length();
        double ll = (16 / (16 + (double) l));
        double we;
        if (spinnerType == Constants.FX_SPINNER_TYPE_SYSEX) {
            we = 16.0;
        } else {
            we = h * 2.2 * ll * 0.3;
        }
        spinnerFast.getEditor().setFont(new Font(we));
    }

    @Override
    public void respondToResize(double w, double h) {
        super.respondToResize(w, h);
        double spinnerButtonsFontSize = h * 0.3;
        double width = h * 3.0;
        spinnerFast.setMinHeight(h);
        spinnerFast.setMaxHeight(h);
        switch (spinnerType) {
            case Constants.FX_SPINNER_TYPE_SYSEX:
                width = w * 0.15;
                break;
            case Constants.FX_SPINNER_TYPE_STANDARD:
            default:
                break;
        }
        spinnerFast.setMaxWidth(width);
        spinnerFast.setMinWidth(width);
        spinnerFast.setStyle("-fx-font-size: " + spinnerButtonsFontSize + "pt");
        resizeFont(h);
    }

    public void uiCtlSetValue(int n, boolean setFromSysex) {
        if ((intValue / step) != (n / step)) {
            changedFromSet++;
            intValue = n;
        }
        if (setFromSysex) {
            setSyncState(Constants.SYNC_STATE_SYNCED);
            mdIntValue = n;
        } else {
            updateSyncStateConditional();
        }
        if (!changedByEdit) {
            valueFactory.setValue(n);
            spinnerFast.getEditor().setText(String.valueOf(intValue));
        }
        changedByEdit = false;
        resizeFont(spinnerFast.getHeight());
    }

    public Integer uiCtlGetValue() {
        return intValue;
    }

    public void setSpinnerType(Integer type) {
        spinnerType = type;
    }

}
