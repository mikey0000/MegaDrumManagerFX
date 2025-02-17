package info.megadrum.managerfx.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.data.ConfigOptions;
import info.megadrum.managerfx.midi.MidiRescanEvent;
import info.megadrum.managerfx.midi.MidiRescanEventListener;
import info.megadrum.managerfx.utils.Constants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UIOptions {
    private final Stage window;
    private final Scene scene;
    private final UICheckBox uiCheckBoxSamePort;
    private final UIComboBox uiComboBoxMidiIn;
    private final UIComboBox uiComboBoxMidiOut;
    private final UIComboBox uiComboBoxChainId;
    private final UICheckBox uiCheckBoxEnableMidiThru;
    private final UIComboBox uiComboBoxMidiThru;
    private final UICheckBox uiCheckBoxInitPortsStartup;
    private final UISpinner uiSpinnerSysexTimeout;
    private final HBox okCloseButtonsLayout;
    private final TabPane optionsTabs;

    private final UICheckBox uiCheckBoxSaveOnExit;
    private final UICheckBox uiCheckBoxShowAdvanced;

    private final ConfigOptions configOptions;
    private Boolean closedWithOk;

    private final ArrayList<UIControl> allMidiControls;
    private final ArrayList<UIControl> allMiscControls;

    protected EventListenerList listenerList = new EventListenerList();

    public void addMidiRescanEventListener(MidiRescanEventListener listener) {
        listenerList.add(MidiRescanEventListener.class, listener);
    }

    protected void fireMidiRescanEvent(MidiRescanEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == MidiRescanEventListener.class) {
                ((MidiRescanEventListener) listeners[i + 1]).midiRescanEventOccurred(evt);
            }
        }
    }

    public UIOptions(ConfigOptions config) {
        configOptions = config;
        window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Options");
        window.getIcons().add(new Image("/icon_256x256.png"));

        optionsTabs = new TabPane();
        optionsTabs.setTabMaxHeight(20);
        optionsTabs.setTabMinHeight(20);

        Tab midiTab = new Tab("MIDI");
        midiTab.setClosable(false);
        Tab miscTab = new Tab("Misc");
        miscTab.setClosable(false);
        optionsTabs.getTabs().addAll(midiTab, miscTab);
        VBox midiLayout = new VBox();
        VBox miscLayout = new VBox();

        midiTab.setContent(midiLayout);
        miscTab.setContent(miscLayout);

        allMidiControls = new ArrayList<>();
        allMiscControls = new ArrayList<>();
        uiCheckBoxSamePort = new UICheckBox("Use same In/Out", false);
        uiCheckBoxSamePort.setIgnoreSyncState();
        uiCheckBoxSamePort.addListener((_, _, newValue) -> setSameMidiInOut(newValue));
        allMidiControls.add(uiCheckBoxSamePort);

        uiComboBoxMidiIn = new UIComboBox("MIDI In", false);
        uiComboBoxMidiIn.enableComboBoxWide();
        uiComboBoxMidiIn.addListener((_, _, _) -> setSameMidiInOut(uiCheckBoxSamePort.uiCtlIsSelected()));

        allMidiControls.add(uiComboBoxMidiIn);

        uiComboBoxMidiOut = new UIComboBox("MIDI Out", false);
        uiComboBoxMidiOut.enableComboBoxWide();
        allMidiControls.add(uiComboBoxMidiOut);

        uiComboBoxChainId = new UIComboBox("MegaDrum Chain Id", false);
        allMidiControls.add(uiComboBoxChainId);

        uiCheckBoxEnableMidiThru = new UICheckBox("Enable MIDI Thru", false);
        uiCheckBoxEnableMidiThru.setIgnoreSyncState();
        allMidiControls.add(uiCheckBoxEnableMidiThru);

        uiComboBoxMidiThru = new UIComboBox("MIDI Thru", false);
        uiComboBoxMidiThru.enableComboBoxWide();
        allMidiControls.add(uiComboBoxMidiThru);

        uiCheckBoxInitPortsStartup = new UICheckBox("Init Ports on Startup", false);
        uiCheckBoxInitPortsStartup.setIgnoreSyncState();
        allMidiControls.add(uiCheckBoxInitPortsStartup);

        uiSpinnerSysexTimeout = new UISpinner("Sysex Timeout", 10, 100, 30, 1, false);
        uiSpinnerSysexTimeout.setSpinnerType(Constants.FX_SPINNER_TYPE_SYSEX);
        allMidiControls.add(uiSpinnerSysexTimeout);

        for (UIControl allMidiControl : allMidiControls) {
            midiLayout.getChildren().add(allMidiControl.getUI());
        }

        Button buttonRescanPort = new Button("Rescan MIDI ports");
        buttonRescanPort.setOnAction(_ -> fireMidiRescanEvent(new MidiRescanEvent(this)));
        midiLayout.getChildren().add(buttonRescanPort);
        midiLayout.setAlignment(Pos.TOP_CENTER);

        uiCheckBoxSaveOnExit = new UICheckBox("Save Options on Exit", false);
        uiCheckBoxSaveOnExit.setIgnoreSyncState();
        allMiscControls.add(uiCheckBoxSaveOnExit);

        uiCheckBoxShowAdvanced = new UICheckBox("Show Advanced Settings", false);
        uiCheckBoxShowAdvanced.setIgnoreSyncState();
        allMiscControls.add(uiCheckBoxShowAdvanced);

        for (UIControl allMiscControl : allMiscControls) {
            miscLayout.getChildren().add(allMiscControl.getUI());
        }
        miscLayout.setAlignment(Pos.TOP_CENTER);


        Button okButton = new Button("Ok");
        okButton.setOnAction(_ -> okAndClose());

        Label labelSpacer = new Label("   ");

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(_ -> window.close());

        okCloseButtonsLayout = new HBox();
        okCloseButtonsLayout.getChildren().addAll(okButton, labelSpacer, cancelButton);
        okCloseButtonsLayout.setAlignment(Pos.CENTER_RIGHT);

        VBox layout = new VBox();
        layout.getChildren().add(optionsTabs);
        layout.getChildren().add(okCloseButtonsLayout);
        layout.setAlignment(Pos.TOP_CENTER);

        okCloseButtonsLayout.setPadding(new Insets(5, 20, 10, 20));
        scene = new Scene(layout);
        scene.widthProperty().addListener((_, _, _) -> respondToResize(scene.getWidth(), scene.getHeight()));

        scene.heightProperty().addListener((_, _, _) -> respondToResize(scene.getWidth(), scene.getHeight()));

        uiComboBoxChainId.uiCtlSetValuesArray(Arrays.asList("0", "1", "2", "3"));
        window.setScene(scene);
        window.setMinWidth(600);
        window.setMaxWidth(600);
        window.setMinHeight(380);
        window.setMaxHeight(380);
    }

    public void show() {
        closedWithOk = false;
        window.showAndWait();
    }

    public void okAndClose() {
        configOptions.useSamePort = uiCheckBoxSamePort.uiCtlIsSelected();
        configOptions.useThruPort = uiCheckBoxEnableMidiThru.uiCtlIsSelected();
        configOptions.autoOpenPorts = uiCheckBoxInitPortsStartup.uiCtlIsSelected();
        configOptions.sysexDelay = uiSpinnerSysexTimeout.uiCtlGetValue();
        configOptions.MidiInName = uiComboBoxMidiIn.uiCtlGetSelected();
        configOptions.MidiOutName = uiComboBoxMidiOut.uiCtlGetSelected();
        configOptions.MidiThruName = uiComboBoxMidiThru.uiCtlGetSelected();
        configOptions.chainId = Integer.parseInt(uiComboBoxChainId.uiCtlGetSelected());
        configOptions.saveOnExit = uiCheckBoxSaveOnExit.uiCtlIsSelected();
        configOptions.showAdvancedSettings = uiCheckBoxShowAdvanced.uiCtlIsSelected();
        closedWithOk = true;
        window.close();
    }

    public boolean getClosedWithOk() {
        return closedWithOk;
    }

    public void setMidiInList(List<String> list) {
        uiComboBoxMidiIn.uiCtlSetValuesArray(list);
    }

    public void setMidiOutList(List<String> list) {
        uiComboBoxMidiOut.uiCtlSetValuesArray(list);
    }

    public void setMidiThruList(List<String> list) {
        uiComboBoxMidiThru.uiCtlSetValuesArray(list);
    }

    private void setSameMidiInOut(Boolean same) {
        if (same) {
            uiComboBoxMidiOut.uiCtlSetValue(uiComboBoxMidiIn.uiCtlGetSelected());
            uiComboBoxMidiOut.uiCtlSetDisable(true);
        } else {
            uiComboBoxMidiOut.uiCtlSetValue(configOptions.MidiOutName);
            uiComboBoxMidiOut.uiCtlSetDisable(false);
        }
    }

    public void updateControls() {
        uiCheckBoxSamePort.uiCtlSetValue(configOptions.useSamePort, true);
        uiCheckBoxEnableMidiThru.uiCtlSetValue(configOptions.useThruPort, true);
        uiCheckBoxInitPortsStartup.uiCtlSetValue(configOptions.autoOpenPorts, true);
        uiCheckBoxSaveOnExit.uiCtlSetValue(configOptions.saveOnExit, true);
        uiCheckBoxShowAdvanced.uiCtlSetValue(configOptions.showAdvancedSettings, true);
        uiSpinnerSysexTimeout.uiCtlSetValue(configOptions.sysexDelay, true);
        uiComboBoxMidiIn.uiCtlSetValue(configOptions.MidiInName);
        setSameMidiInOut(configOptions.useSamePort);
        uiComboBoxMidiThru.uiCtlSetValue(configOptions.MidiThruName);
        uiComboBoxChainId.uiCtlSetValue(String.valueOf(configOptions.chainId));
    }

    public void respondToResize(Double w, Double h) {
        optionsTabs.setMinHeight(h - okCloseButtonsLayout.getHeight());
        optionsTabs.setMaxHeight(h - okCloseButtonsLayout.getHeight());
        optionsTabs.setMinWidth(w);
        optionsTabs.setMaxWidth(w);
        for (UIControl allMidiControl : allMidiControls) {
            allMidiControl.respondToResize(w * 0.85, h * 0.09);
        }
        for (UIControl allMiscControl : allMiscControls) {
            allMiscControl.respondToResize(w * 0.85, h * 0.09);
        }
    }
}
