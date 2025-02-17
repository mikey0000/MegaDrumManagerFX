package info.megadrum.managerfx.ui;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.data.ConfigCustomName;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class UIPadsExtra extends UIPanel implements PanelInterface {
    private final TabPane tabPane;
    private final Tab tabCurves;
    private final Tab tabCustomNames;
    private final UICurves uiCurves;
    private final UICustomNames uiCustomNames;

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
                ((ControlChangeEventListener) listeners[i + 1]).controlChangeEventOccurred(evt, parameter);
            }
        }
    }

    public UIPadsExtra(String title) {
        super(title, false);
        tabPane = new TabPane();
        tabCurves = new Tab("Custom Curves");
        tabCurves.setClosable(false);
        tabCustomNames = new Tab("Custom Names");
        tabCustomNames.setClosable(false);

        uiCurves = new UICurves();
        uiCurves.addControlChangeEventListener(new ControlChangeEventListener() {
            @Override
            public void controlChangeEventOccurred(ControlChangeEvent evt, int parameter) {
                fireControlChangeEvent(new ControlChangeEvent(this), parameter);
            }
        });
        tabCurves.setContent(uiCurves.getUI());
        uiCustomNames = new UICustomNames();
        uiCustomNames.addControlChangeEventListener(new ControlChangeEventListener() {
            @Override
            public void controlChangeEventOccurred(ControlChangeEvent evt, int parameter) {
                fireControlChangeEvent(new ControlChangeEvent(this), parameter);
            }
        });
        tabCustomNames.setContent(uiCustomNames.getUI());
        tabPane.getTabs().addAll(tabCurves, tabCustomNames);
        vBoxAll.getChildren().add(tabPane);
        setDetached(false);
    }

    public void respondToResizeDetached() {
        double w = windowDetached.getScene().getWidth();
        double h = windowDetached.getScene().getHeight();
        double controlW = w * 0.5;
        double controlH = h * 0.05;

        respondToResize(w, h + controlH, controlW, controlH);
    }

    public void respondToResize(double w, double h, double cW, double cH) {
        super.respondToResize(w, h, cW, cH);
        if (detached) {
            vBoxAll.setMaxWidth(w);
            titledPane.setWidth(w);
        } else {
            vBoxAll.setMaxWidth(340);
            vBoxAll.setMinWidth(340);
            titledPane.setWidth(340.0);
        }
        tabCurves.setStyle("-fx-font-size: " + tabsFontSize + "pt");
        tabCustomNames.setStyle("-fx-font-size: " + tabsFontSize + "pt");
        tabPane.setStyle("-fx-padding: " + tabHeaderPadding + "em 0.0em 0.0em 0.0em; -fx-tab-max-height:" + tabHeaderHeight + "pt;-fx-tab-min-height:" + tabHeaderHeight + "pt;");

        vBoxAll.setMinHeight(h - controlH * 1.02);
        vBoxAll.setMaxHeight(h - controlH * 1.02);
        uiCurves.respondToResize(w, h, 340 * 0.4, controlH);
        uiCustomNames.respondToResize(w, h, 340 * 0.4, controlH);
    }

    public void setAllCustomNamesStatesUnknown() {
        uiCustomNames.setAllStateUnknown();
    }

    public void setCustomName(ConfigCustomName config, int id, boolean setFromSysex) {
        uiCustomNames.setCustomName(config, id, setFromSysex);
    }

    public void getCustomName(ConfigCustomName config, int id) {
        uiCustomNames.getCustomName(config, id);
    }

    public Button getCustomNamesButtonGetAll() {
        return uiCustomNames.getButtonGetAll();
    }

    public Button getCustomNamesButtonSendAll() {
        return uiCustomNames.getButtonSendAll();
    }

    public Button getCustomNamesButtonLoadAll() {
        return uiCustomNames.getButtonLoadAll();
    }

    public Button getCustomNamesButtonSaveAll() {
        return uiCustomNames.getButtonSaveAll();
    }

    public ComboBox<String> getComboBoxCustomNamesCount() {
        return uiCustomNames.getComboBoxCustomNamesCount();
    }

    public void setYvalues(int[] values, boolean setFromSysex) {
        uiCurves.setYvalues(values, setFromSysex);
    }

    public void setMdYvalues(int[] values) {
        uiCurves.setMdYvalues(values);
    }

    public void getYvalues(int[] values) {
        uiCurves.getYvalues(values);
    }

    public void setCurveSysexReceived(boolean received) {
        uiCurves.setSysexReceived(received);
    }

    public void testCurveSyncState() {
        uiCurves.testSyncState();
    }

    public Button getCurvesButtonGet() {
        return uiCurves.getButtonGet();
    }

    public Button getCurvesButtonSend() {
        return uiCurves.getButtonSend();
    }

    public Button getCurvesButtonGetAll() {
        return uiCurves.getButtonGetAll();
    }

    public Button getCurvesButtonSendAll() {
        return uiCurves.getButtonSendAll();
    }

    public Button getCurvesButtonLoad() {
        return uiCurves.getButtonLoad();
    }

    public Button getCurvesButtonSave() {
        return uiCurves.getButtonSave();
    }

    public ComboBox<String> getCurvesComboBox() {
        return uiCurves.getComboBoxCurve();
    }

    public Button getCurvesButtonFirst() {
        return uiCurves.getButtonFirst();
    }

    public Button getCurvesButtonPrev() {
        return uiCurves.getButtonPrev();
    }

    public Button getCurvesButtonNext() {
        return uiCurves.getButtonNext();
    }

    public Button getCurvesButtonLast() {
        return uiCurves.getButtonLast();
    }

    @Override
    public int getVerticalControlsCount() {
        return 22;
    }
}
