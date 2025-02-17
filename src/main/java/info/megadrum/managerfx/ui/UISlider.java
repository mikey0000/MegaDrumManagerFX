package info.megadrum.managerfx.ui;

import info.megadrum.managerfx.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;


public class UISlider extends UIControl {
    private Slider uiSlider;

    public UISlider(String labelText, int min, int max, int initial, boolean showCopyButton) {
        super(labelText, showCopyButton);
        init(min, max, initial);
    }

    private void init(int min, int max, int initial) {
        intValue = initial;
        valueType = Constants.VALUE_TYPE_INT;

        uiSlider = new Slider(min, max, intValue);
        uiSlider.valueProperty().addListener(new ChangeListener<>() {
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                if (changedFromSet > 0) {
                    changedFromSet--;
                } else {
                    if (intValue != new_val.intValue()) {
                        intValue = new_val.intValue();
                        fireControlChangeEvent(new ControlChangeEvent(this), 0);
                        if (syncState != Constants.SYNC_STATE_UNKNOWN) {
                            setSyncState((intValue == mdIntValue) ? Constants.SYNC_STATE_SYNCED : Constants.SYNC_STATE_NOT_SYNCED);
                        }
                    }
                }
            }
        });

        uiSlider.setShowTickMarks(true);
        uiSlider.setMajorTickUnit(16f);
        uiSlider.setBlockIncrement(1.0f);
        uiSlider.setMinorTickCount(16);

        HBox layout = new HBox();
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(uiSlider);
        initControl(layout);
    }

    @Override
    public void respondToResize(double w, double h) {
        double width = w * 0.48;
        double sliderFontSize = (h * 0.5 > 8) ? 8 : h * 0.5;
        super.respondToResize(w, h);
        uiSlider.setMinHeight(h);
        uiSlider.setMaxHeight(h);
        uiSlider.setMaxWidth(width);
        uiSlider.setMinWidth(width);
        uiSlider.setStyle("-fx-font-size: " + sliderFontSize + "pt");
    }

    public void uiCtlSetValue(int n, boolean setFromSysex) {
        if (intValue != n) {
            changedFromSet++;
            intValue = n;
        }
        if (setFromSysex) {
            setSyncState(Constants.SYNC_STATE_SYNCED);
            mdIntValue = n;
        } else {
            updateSyncStateConditional();
        }
        uiSlider.setValue(intValue);
    }

    public int uiCtlGetValue() {
        return (int) uiSlider.getValue();
    }
}
