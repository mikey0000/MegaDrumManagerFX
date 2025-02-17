package info.megadrum.managerfx.ui;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.utils.Constants;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class UIControl extends Control implements UIControlInterface {

    @Override
    protected Skin<?> createDefaultSkin() {
        return super.createDefaultSkin();
    }

    protected GridPane layout;
    protected Label label;
    protected Pane rootPane;
    protected int intValue = 0;
    protected int mdIntValue = 0;
    protected int syncState = Constants.SYNC_STATE_UNKNOWN;
    protected int changedFromSet = 0;
    protected boolean booleanValue = false;
    protected boolean mdBooleanValue = false;
    protected int valueType = Constants.VALUE_TYPE_BOOLEAN;
    protected Pane uiControl;
    protected boolean copyButtonShown;
    protected Double padding = 2.0;
    private Button buttonCopy;
    private Double labelWidthMultiplier = 0.4;
    private Double controlWidthMultiplier;
    protected final static boolean debugSizes = false;
    private int valueId = -1;
    private boolean copyPressed = false;
    private boolean advancedSetting = false;

    protected EventListenerList listenerList = new EventListenerList();

    public void addControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.add(ControlChangeEventListener.class, listener);
    }

    public void removeControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.remove(ControlChangeEventListener.class, listener);
    }

    protected void fireControlChangeEvent(ControlChangeEvent evt, int parameter) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ControlChangeEventListener.class) {
                if (label.getText().equals("Name")) {
                    ((ControlChangeEventListener) listeners[i + 1]).controlChangeEventOccurred(evt, Constants.CONTROL_CHANGE_EVENT_NAME);
                } else {
                    ((ControlChangeEventListener) listeners[i + 1]).controlChangeEventOccurred(evt, parameter);
                }
            }
        }
    }

    public UIControl(boolean showCopyButton) {
        copyButtonShown = showCopyButton;
        init("Unknown");
    }

    public UIControl(String labelText, boolean showCopyButton) {
        copyButtonShown = showCopyButton;
        init(labelText);
    }

    private void init(String labelText) {
        rootPane = new Pane();
        layout = new GridPane();

        rootPane.getChildren().add(layout);
        layout.setPadding(new Insets(padding, padding, padding, padding));
        layout.setHgap(5);

        label = new Label();
        label.setText(labelText);
        GridPane.setConstraints(label, 0, 0);
        GridPane.setHalignment(label, HPos.RIGHT);
        GridPane.setValignment(label, VPos.CENTER);
        layout.getChildren().add(label);

        buttonCopy = new Button("c");
        GridPane.setConstraints(buttonCopy, 2, 0);
        GridPane.setHalignment(buttonCopy, HPos.RIGHT);
        GridPane.setValignment(buttonCopy, VPos.CENTER);
        buttonCopy.setVisible(copyButtonShown);
        buttonCopy.setOnAction(_ -> {
            copyPressed = true;
            fireControlChangeEvent(new ControlChangeEvent(this), 0);
        });
        buttonCopy.setTooltip(new Tooltip("Copy this Input setting to all Inputs"));
        buttonCopy.getTooltip().setFont(new Font(14));


        if (debugSizes) {
            layout.setStyle("-fx-background-color: grey");
        }
        layout.getChildren().add(buttonCopy);
        setLabelWidthMultiplier(labelWidthMultiplier);
    }

    public Node getUI() {
        return layout;
    }

    public void initControl(Pane uiCtrl) {
        uiControl = uiCtrl;
        GridPane.setConstraints(uiControl, 1, 0);
        GridPane.setHalignment(uiControl, HPos.LEFT);
        GridPane.setValignment(uiControl, VPos.CENTER);
        layout.getChildren().add(uiControl);
        if (debugSizes) {
            uiControl.setStyle("-fx-background-color: green");
        }
        GridPane.setHgrow(uiControl, Priority.ALWAYS);
    }

    @Override
    public void setSyncState(int state) {
        syncState = state;
        Color color = switch (state) {
            case Constants.SYNC_STATE_UNKNOWN -> Constants.SYNC_STATE_UNKNOWN_COLOR;
            case Constants.SYNC_STATE_NOT_SYNCED -> Constants.SYNC_STATE_NOT_SYNCED_COLOR;
            default -> Constants.SYNC_STATE_SYNCED_COLOR;
        };

        label.setTextFill(color);
    }

    public void updateSyncStateConditional() {
        if (syncState != Constants.SYNC_STATE_UNKNOWN) {
            updateSyncState();
        }
    }

    public void updateSyncState() {
        boolean valuesAreEqual = false;
        switch (valueType) {
            case Constants.VALUE_TYPE_BOOLEAN:
                valuesAreEqual = (booleanValue == mdBooleanValue);
                break;
            case Constants.VALUE_TYPE_INT:
                valuesAreEqual = (intValue == mdIntValue);
                break;
            default:
                break;
        }
        if (valuesAreEqual) {
            setSyncState(Constants.SYNC_STATE_SYNCED);
        } else {
            setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
        }
    }

    public void uiCtlSetMdValue(Object value) {
        switch (valueType) {
            case Constants.VALUE_TYPE_BOOLEAN:
                mdBooleanValue = (boolean) value;
                break;
            case Constants.VALUE_TYPE_INT:
                mdIntValue = (int) value;
                break;
            default:
                break;
        }
        setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
    }

    public void setLabelWidthMultiplier(Double mul) {
        labelWidthMultiplier = mul;
        controlWidthMultiplier = 1.0 - mul - 0.05;
    }

    public void respondToResize(double w, double h) {
        double wCl0, wCl1, wCl2;
        wCl0 = (w - padding * 2) * labelWidthMultiplier;
        if (copyButtonShown) {
            wCl1 = (w - padding * 2) * controlWidthMultiplier * 0.8;
            wCl2 = (w - padding * 2) * 0.1;
        } else {
            wCl1 = (w - padding * 2) * controlWidthMultiplier;
            wCl2 = 0.0;
        }
        layout.getColumnConstraints().clear();
        layout.getColumnConstraints().add(new ColumnConstraints(wCl0));
        layout.getColumnConstraints().add(new ColumnConstraints(wCl1));
        layout.getColumnConstraints().add(new ColumnConstraints(wCl2));
        buttonCopy.setMaxHeight(h / 1.2);
        buttonCopy.setMinHeight(h / 1.2);
        buttonCopy.setMaxWidth(h / 1.2);
        buttonCopy.setMinWidth(h / 1.2);
        buttonCopy.setFont(new Font(h / 2.5));
        layout.getRowConstraints().clear();
        layout.getRowConstraints().add(new RowConstraints(h - padding * 2 - 1));
        label.setFont(new Font(h * 0.5));
    }

    public void uiCtlSetDisable(boolean disable) {
        layout.setDisable(disable);
    }

    public void setValueId(int value) {
        valueId = value;
    }

    public int getValueId() {
        return valueId;
    }

    public boolean isCopyPressed() {
        return copyPressed;
    }

    public void resetCopyPressed() {
        copyPressed = false;
    }

    public void setButtonCopyToolTip(String tt) {
        buttonCopy.getTooltip().setText(tt);
    }

    public void setLabelText(String text) {
        label.setText(text);
    }

    public void setControlTooltip(Tooltip tooltip) {
        Tooltip.install(uiControl, tooltip);
    }

    public void setAdvancedSetting(boolean advanced) {
        advancedSetting = advanced;
    }

    public boolean isAdvancedSetting() {
        return advancedSetting;
    }
}
