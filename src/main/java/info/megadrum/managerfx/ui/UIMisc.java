package info.megadrum.managerfx.ui;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.data.ConfigMisc;
import info.megadrum.managerfx.utils.Constants;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class UIMisc extends UIPanel implements PanelInterface {
    private final HBox toolBar;

    private final UISpinner uiSpinnerNoteOffDelay;
    private final UISpinner uiSpinnerPressrollTimeout;
    private final UISpinner uiSpinnerLatency;
    private final UISpinner uiSpinnerNotesOctaveShift;
    private final UISpinner uiSpinnerNoiseFilter;
    private final UICheckBox uiCheckBoxBigVUmeter;
    private final UICheckBox uiCheckBoxBigVUsplit;
    private final UICheckBox uiCheckBoxBigVUQuickAccess;
    private final UICheckBox uiCheckBoxAltFalseTrSupp;
    private final UICheckBox uiCheckBoxInputsPriority;
    private final UICheckBox uiCheckBoxUnknownSetting;
    private final UICheckBox uiCheckBoxMIDIThru;
    private final UICheckBox uiCheckBoxSendTriggeredIn;
    private final UICheckBox uiCheckBoxAltNoteChoking;
    private final ArrayList<UIControl> allControls;
    private final Tooltip tooltipUnused;
    private final Tooltip tooltipAllGainsLow;
    private final Tooltip tooltipAltSampling;
    private final Tooltip tooltipUnknown;

    protected EventListenerList listenerList = new EventListenerList();

    public void addControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.add(ControlChangeEventListener.class, listener);
    }

    public void removeControlChangeEventListener(ControlChangeEventListener listener) {
        listenerList.remove(ControlChangeEventListener.class, listener);
    }

    protected void fireControlChangeEvent(ControlChangeEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ControlChangeEventListener.class) {
                ((ControlChangeEventListener) listeners[i + 1]).controlChangeEventOccurred(evt, 0);
            }
        }
    }

    public UIMisc(String title) {
        super(title, false);
        allControls = new ArrayList<>();
        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.getChildren().addAll(buttonGet, buttonSend, new Separator(Orientation.VERTICAL), buttonLoad, buttonSave);
        toolBar.setStyle("-fx-padding: 0.1em 0.0em 0.2em 0.01em");


        vBoxAll.getChildren().add(toolBar);

        uiSpinnerNoteOffDelay = new UISpinner("Note Off Delay", 20, 2000, 200, 20, false);
        allControls.add(uiSpinnerNoteOffDelay);

        uiSpinnerPressrollTimeout = new UISpinner("Pressroll Timeout", 0, 2000, 10, 10, false);
        uiSpinnerPressrollTimeout.setAdvancedSetting(true);
        allControls.add(uiSpinnerPressrollTimeout);

        uiSpinnerLatency = new UISpinner("Latency", 10, 100, 15, 1, false);
        allControls.add(uiSpinnerLatency);

        uiSpinnerNotesOctaveShift = new UISpinner("Notes Octave Shift", 0, 2, 2, 1, false);
        allControls.add(uiSpinnerNotesOctaveShift);

        uiSpinnerNoiseFilter = new UISpinner("Noise Filter Level", 0, 9, 5, 1, false);
        uiSpinnerNoiseFilter.setAdvancedSetting(true);
        allControls.add(uiSpinnerNoiseFilter);

        uiCheckBoxBigVUmeter = new UICheckBox("Big VU meter", false);
        uiCheckBoxBigVUmeter.setAdvancedSetting(true);
        allControls.add(uiCheckBoxBigVUmeter);

        uiCheckBoxBigVUsplit = new UICheckBox("Big VU split", false);
        uiCheckBoxBigVUsplit.setAdvancedSetting(true);
        allControls.add(uiCheckBoxBigVUsplit);

        uiCheckBoxBigVUQuickAccess = new UICheckBox("Quick Access", false);
        uiCheckBoxBigVUQuickAccess.setAdvancedSetting(true);
        allControls.add(uiCheckBoxBigVUQuickAccess);

        uiCheckBoxAltFalseTrSupp = new UICheckBox("AltFalseTrSupp", false);
        allControls.add(uiCheckBoxAltFalseTrSupp);

        uiCheckBoxInputsPriority = new UICheckBox("Inputs Priority", false);
        uiCheckBoxInputsPriority.setAdvancedSetting(true);
        allControls.add(uiCheckBoxInputsPriority);

        uiCheckBoxUnknownSetting = new UICheckBox("Unknown", false);
        uiCheckBoxUnknownSetting.setAdvancedSetting(true);
        allControls.add(uiCheckBoxUnknownSetting);
        tooltipUnused = new Tooltip("This setting is unused on this MegaDrum hardware version");
        tooltipAllGainsLow = new Tooltip("When enabled, it will disable all individual input Gain levels\nand make Gain even lower than 'Gain Level' 0.\nIt could be used if all your pads are 'hot' and to get\na better dynamic range with such pads.");
        tooltipAltSampling = new Tooltip("When enabled, MegaDrum uses a new sampling algorithm\nwhich can reduce signal noise\nand improve sensitivity.");
        tooltipUnknown = new Tooltip("This setting function depends on the type of MegaDrum MCU");
        Tooltip tooltipNoiseFilter = new Tooltip("This setting allows to reduce inputs noise\nand improve sensibility.\nAvailable only on STM32F205 based Megadrum.");
        uiSpinnerNoiseFilter.setControlTooltip(tooltipNoiseFilter);

        uiCheckBoxMIDIThru = new UICheckBox("MIDI Thru", false);
        uiCheckBoxMIDIThru.setAdvancedSetting(true);
        allControls.add(uiCheckBoxMIDIThru);

        uiCheckBoxSendTriggeredIn = new UICheckBox("Send TriggeredIn", false);
        allControls.add(uiCheckBoxSendTriggeredIn);

        uiCheckBoxAltNoteChoking = new UICheckBox("AltNote Chokng", false);
        uiCheckBoxAltNoteChoking.setAdvancedSetting(true);
        allControls.add(uiCheckBoxAltNoteChoking);


        vBoxAll.getChildren().add(paneAll);
        for (UIControl allControl : allControls) {
            verticalControlsCount++;
            if (!allControl.isAdvancedSetting()) {
                verticalControlsCountWithoutAdvanced++;
            }
            allControl.setLabelWidthMultiplier(Constants.FX_MISC_LABEL_WIDTH_MUL);
            allControl.addControlChangeEventListener(new ControlChangeEventListener() {

                @Override
                public void controlChangeEventOccurred(ControlChangeEvent evt, int parameter) {
                    fireControlChangeEvent(new ControlChangeEvent(this));
                }
            });
        }

        reAddAllControls();
        setDetached(false);
        setAllStateUnknown();
    }

    private void reAddAllControls() {
        paneAll.getChildren().clear();
        for (UIControl allControl : allControls) {
            if (allControl.isAdvancedSetting()) {
                if (showAdvanced) {
                    paneAll.getChildren().add(allControl.getUI());
                }
            } else {
                paneAll.getChildren().add(allControl.getUI());
            }
        }

    }

    @Override
    public void setShowAdvanced(boolean show) {
        showAdvanced = show;
        reAddAllControls();
    }

    public void setAllStateUnknown() {
        for (UIControl allControl : allControls) {
            allControl.setSyncState(Constants.SYNC_STATE_UNKNOWN);
        }
        uiCheckBoxUnknownSetting.setLabelText("Unknown");
        uiCheckBoxUnknownSetting.setControlTooltip(tooltipUnknown);
        uiSpinnerNoiseFilter.uiCtlSetDisable(false);
    }

    public void respondToResizeDetached() {
        Double w = windowDetached.getScene().getWidth();
        Double h = windowDetached.getScene().getHeight();
        Double controlW = w / Constants.FX_MISC_CONTROL_WIDTH_MUL;
        int visibleControls;
        if (showAdvanced) {
            visibleControls = verticalControlsCount;
        } else {
            visibleControls = verticalControlsCountWithoutAdvanced;
        }
        Double controlH = (h / ((visibleControls + 1)));
        respondToResize(w, h, controlW, controlH);
    }

    public void respondToResize(Double w, Double h, Double cW, Double cH) {
        super.respondToResize(w, h, cW, cH);
        titledPane.setWidth(controlW * Constants.FX_MISC_CONTROL_WIDTH_MUL);
        vBoxAll.setMaxWidth(controlW * 1.0 * Constants.FX_MISC_CONTROL_WIDTH_MUL);
        toolBar.setMaxWidth(controlW * 1.0 * Constants.FX_MISC_CONTROL_WIDTH_MUL);
        toolBar.setMinWidth(controlW * 1.0 * Constants.FX_MISC_CONTROL_WIDTH_MUL);
        toolBar.setMaxHeight(controlH);
        double buttonFontSize = controlH * 0.31;
        toolBar.setStyle("-fx-font-size: " + buttonFontSize + "pt");
        paneAll.setMinWidth(controlW * Constants.FX_MISC_CONTROL_WIDTH_MUL);
        paneAll.setMaxWidth(controlW * Constants.FX_MISC_CONTROL_WIDTH_MUL);
        int visibleControlsCount = -1;
        boolean showControl;
        for (UIControl allControl : allControls) {
            showControl = false;
            if (allControl.isAdvancedSetting()) {
                if (showAdvanced) {
                    visibleControlsCount++;
                    showControl = true;
                }
            } else {
                visibleControlsCount++;
                showControl = true;
            }
            if (showControl) {
                allControl.respondToResize(controlW * Constants.FX_MISC_CONTROL_WIDTH_MUL, controlH);
                allControl.getUI().setLayoutX(0);
                allControl.getUI().setLayoutY(visibleControlsCount * controlH);
            }
        }
    }

    public void setControlsFromConfig(ConfigMisc config, Boolean setFromSysex) {
        uiSpinnerNoteOffDelay.uiCtlSetValue(config.getNoteOff() * 10, setFromSysex);
        uiSpinnerPressrollTimeout.uiCtlSetValue(config.pressroll, setFromSysex);
        uiSpinnerLatency.uiCtlSetValue(config.latency, setFromSysex);
        uiSpinnerNotesOctaveShift.uiCtlSetValue(config.octave_shift, setFromSysex);
        uiCheckBoxBigVUmeter.uiCtlSetValue(config.big_vu_meter, setFromSysex);
        uiCheckBoxBigVUsplit.uiCtlSetValue(config.big_vu_split, setFromSysex);
        uiCheckBoxBigVUQuickAccess.uiCtlSetValue(config.quick_access, setFromSysex);
        uiCheckBoxAltFalseTrSupp.uiCtlSetValue(config.alt_false_tr_supp, setFromSysex);
        uiCheckBoxInputsPriority.uiCtlSetValue(config.inputs_priority, setFromSysex);
        uiCheckBoxUnknownSetting.uiCtlSetValue(config.all_gains_low, setFromSysex);
        uiCheckBoxMIDIThru.uiCtlSetValue(config.midi_thru, setFromSysex);
        uiCheckBoxSendTriggeredIn.uiCtlSetValue(config.send_triggered_in, setFromSysex);
        uiCheckBoxAltNoteChoking.uiCtlSetValue(config.alt_note_choking, setFromSysex);
        uiSpinnerNoiseFilter.uiCtlSetValue(config.noise_filter, setFromSysex);
    }

    public void setConfigFromControls(ConfigMisc config) {
        config.setNoteOff(uiSpinnerNoteOffDelay.uiCtlGetValue() / 10);
        config.pressroll = uiSpinnerPressrollTimeout.uiCtlGetValue();
        config.latency = uiSpinnerLatency.uiCtlGetValue();
        config.octave_shift = uiSpinnerNotesOctaveShift.uiCtlGetValue();
        config.big_vu_meter = uiCheckBoxBigVUmeter.uiCtlIsSelected();
        config.big_vu_split = uiCheckBoxBigVUsplit.uiCtlIsSelected();
        config.quick_access = uiCheckBoxBigVUQuickAccess.uiCtlIsSelected();
        config.alt_false_tr_supp = uiCheckBoxAltFalseTrSupp.uiCtlIsSelected();
        config.inputs_priority = uiCheckBoxInputsPriority.uiCtlIsSelected();
        config.all_gains_low = uiCheckBoxUnknownSetting.uiCtlIsSelected();
        config.midi_thru = uiCheckBoxMIDIThru.uiCtlIsSelected();
        config.send_triggered_in = uiCheckBoxSendTriggeredIn.uiCtlIsSelected();
        config.alt_note_choking = uiCheckBoxAltNoteChoking.uiCtlIsSelected();
        config.noise_filter = uiSpinnerNoiseFilter.uiCtlGetValue();
    }

    @Override
    public int getVerticalControlsCount() {
        if (showAdvanced) {
            return verticalControlsCount + 2;
        } else {
            return verticalControlsCountWithoutAdvanced + 2;
        }
    }

    public void setUnknownLabel(int mcuType) {
        if (mcuType < 3) {
            uiCheckBoxUnknownSetting.setLabelText("All Gains Low");
            uiCheckBoxUnknownSetting.setControlTooltip(tooltipAllGainsLow);
        } else {
            if (mcuType < 6) {
                uiCheckBoxUnknownSetting.setLabelText("Unused");
                uiCheckBoxUnknownSetting.setControlTooltip(tooltipUnused);
            } else {
                uiCheckBoxUnknownSetting.setLabelText("Alt Sampling Alg");
                uiCheckBoxUnknownSetting.setControlTooltip(tooltipAltSampling);
            }
        }
        uiSpinnerNoiseFilter.uiCtlSetDisable(mcuType < 6);
    }
}
