package info.megadrum.managerfx.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class UIMidiLog extends UIPanel implements PanelInterface {
    private final TabPane tabPane;
    private final Tab tabVisual;
    private final Tab tabRaw;
    private final MidiLevelBarsPanel panelVisual;
    private final MidiRaw panelRaw;

    public UIMidiLog(String title) {
        super(title, false);
        tabPane = new TabPane();
        tabVisual = new Tab("Visual MIDI");
        tabVisual.setClosable(false);
        tabRaw = new Tab("Raw MIDI");
        tabRaw.setClosable(false);
        panelVisual = new MidiLevelBarsPanel();
        tabVisual.setContent(panelVisual);
        panelRaw = new MidiRaw();
        tabRaw.setContent(panelRaw);
        tabPane.getTabs().addAll(tabVisual, tabRaw);
        vBoxAll.getChildren().add(tabPane);
        setDetached(false);
        vBoxAll.setMaxHeight(440);
    }

    public void respondToResizeDetached() {
        double w = windowDetached.getScene().getWidth();
        double h = windowDetached.getScene().getHeight();
        respondToResize(w, h - 5, w, h * 0.1);
    }

    public void respondToResize(double w, double h, double cW, double cH) {
        super.respondToResize(w, h, cW, cH);
        titledPane.setWidth(w);
        vBoxAll.setMaxSize(w, h);
        vBoxAll.setMinSize(w, h);
        tabVisual.setStyle("-fx-font-size: " + tabsFontSize + "pt");
        tabRaw.setStyle("-fx-font-size: " + tabsFontSize + "pt");
        tabPane.setStyle("-fx-padding: " + tabHeaderPadding + "em 0.0em 0.0em 0.0em; -fx-tab-max-height:" + tabHeaderHeight + "pt;-fx-tab-min-height:" + tabHeaderHeight + "pt;");

        panelVisual.respondToResize(w, h * 0.965 - 5);
        panelRaw.respondToResize(w, h * 0.965 - 5);
    }

    public void addNewPositional(Integer pos) {
        panelVisual.addNewPositional(pos);
    }

    public void addNewMidiData(int type, int note, int level) {
        panelVisual.addNewBarData(type, note, level);
    }

    public void setHiHatLevel(int level) {
        panelVisual.setHiHatLevel(level);
    }

    public void addRawMidi(byte[] buffer) {
        panelRaw.addRawMidi(buffer);
    }

    @Override
    public int getVerticalControlsCount() {
        return 25;
    }
}
