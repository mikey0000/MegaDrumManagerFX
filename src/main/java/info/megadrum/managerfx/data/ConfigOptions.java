package info.megadrum.managerfx.data;

import info.megadrum.managerfx.utils.Constants;
import info.megadrum.managerfx.utils.Utils;
import javafx.geometry.Point2D;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.DisabledListDelimiterHandler;

import java.io.Serial;

public class ConfigOptions implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = -4465922793257742902L;
    public boolean useSamePort = false;
    public boolean useThruPort = false;
    public boolean autoOpenPorts = false;
    public boolean saveOnExit = false;
    public boolean liveUpdates = false;
    public int lastConfig = 0;
    public String[] configFileNames;
    public String[] configFullPaths;
    public boolean[] configLoaded;
    public String lastFullPathFirmware = "";
    public String lastFullPathSysex = "";
    public String MidiInName = "";
    public String MidiOutName = "";
    public String MidiThruName = "";
    public int chainId = 0;
    public int sysexDelay = 30;
    public String LookAndFeelName = "";
    public Point2D mainWindowPosition = new Point2D(10, 10);
    public Point2D mainWindowSize = new Point2D(1400, 900);

    public Point2D[] framesPositions = {new Point2D(10, 10), new Point2D(210, 10), new Point2D(410, 10), new Point2D(610, 10), new Point2D(810, 10)};
    public Point2D[] framesSizes = {new Point2D(200, 300), new Point2D(200, 300), new Point2D(400, 500), new Point2D(400, 500), new Point2D(500, 300)};
    public int[] showPanels = {Constants.PANEL_SHOW, Constants.PANEL_SHOW, Constants.PANEL_SHOW, Constants.PANEL_SHOW, Constants.PANEL_HIDE};
    public int globalMiscViewState = Constants.PANEL_SHOW;
    public int mcuType = 0;
    public int version = 0;
    public boolean autoResize = true;
    public boolean changeNotified = false;
    public boolean showAdvancedSettings = true;
    public int viewZoom = 0;

    public ConfigOptions() {
        configFileNames = new String[Constants.CONFIGS_COUNT];
        configFullPaths = new String[Constants.CONFIGS_COUNT];
        configLoaded = new boolean[Constants.CONFIGS_COUNT];
        int n;
        for (int i = 0; i < Constants.CONFIGS_COUNT; i++) {
            n = i + 1;
            configFileNames[i] = "config" + n;
            configFullPaths[i] = "";
            configLoaded[i] = false;
        }

    }

    public void copyToPropertiesConfiguration(PropertiesConfiguration prop) {
        prop.setHeader("MegaDrum options");
        prop.setProperty("MDconfigVersion", Constants.MD_CONFIG_VERSION.toString());
        prop.setProperty("useSamePort", useSamePort);
        prop.setProperty("useThruPort", useThruPort);
        prop.setProperty("autoOpenPorts", autoOpenPorts);
        prop.setProperty("saveOnExit", saveOnExit);
        prop.setProperty("showAdvancedSettings", showAdvancedSettings);
        prop.setProperty("interactive", liveUpdates);
        prop.setProperty("lastConfig", lastConfig);
        for (int i = 0; i < Constants.CONFIGS_COUNT; i++) {
            prop.setProperty("configFileName" + i, configFileNames[i]);
            prop.setProperty("configFullPath" + i, configFullPaths[i]);
        }
        prop.setProperty("lastFullPathFirmware", lastFullPathFirmware);
        prop.setProperty("lastFullPathSysex", lastFullPathSysex);
        prop.setProperty("MidiInName", MidiInName);
        prop.setProperty("MidiOutName", MidiOutName);
        prop.setProperty("MidiThruName", MidiThruName);
        prop.setProperty("chainId", chainId);
        prop.setProperty("sysexDelay", sysexDelay);
        prop.setProperty("LookAndFeelName", LookAndFeelName);
        prop.setProperty("mainWindowPositionX", mainWindowPosition.getX());
        prop.setProperty("mainWindowPositionY", mainWindowPosition.getY());
        prop.setProperty("mainWindowSizeX", mainWindowSize.getX());
        prop.setProperty("mainWindowSizeY", mainWindowSize.getY());
        for (int i = 0; i < Constants.PANELS_COUNT; i++) {
            prop.setProperty("framesPositions" + (i) + "X", framesPositions[i].getX());
            prop.setProperty("framesPositions" + (i) + "Y", framesPositions[i].getY());
            prop.setProperty("framesSizes" + (i) + "W", framesSizes[i].getX());
            prop.setProperty("framesSizes" + (i) + "H", framesSizes[i].getY());
            prop.setProperty("showPanels" + (i), showPanels[i]);
        }
        prop.setProperty("globalMiscViewState", globalMiscViewState);
        prop.setProperty("autoResize", autoResize);
        prop.setProperty("changeNotified", changeNotified);
        prop.setProperty("viewZoom", viewZoom);
    }


    public void copyFromPropertiesConfiguration(PropertiesConfiguration prop) {
        prop.setListDelimiterHandler(DisabledListDelimiterHandler.INSTANCE);
        useSamePort = prop.getBoolean("useSamePort", useSamePort);
        useThruPort = prop.getBoolean("useThruPort", useThruPort);
        autoOpenPorts = prop.getBoolean("autoOpenPorts", autoOpenPorts);
        saveOnExit = prop.getBoolean("saveOnExit", saveOnExit);
        showAdvancedSettings = prop.getBoolean("showAdvancedSettings", showAdvancedSettings);
        liveUpdates = prop.getBoolean("interactive", liveUpdates);
        lastConfig = Utils.validateInt(prop.getInt("lastConfig", lastConfig), 0, Constants.CONFIGS_COUNT - 1, lastConfig);

        for (int i = 0; i < Constants.CONFIGS_COUNT; i++) {
            configFileNames[i] = prop.getString("configFileName" + i, configFileNames[i]);
            configFullPaths[i] = prop.getString("configFullPath" + i, configFullPaths[i]);
        }
        lastFullPathFirmware = prop.getString("lastFullPathFirmware", lastFullPathFirmware);
        lastFullPathSysex = prop.getString("lastFullPathSysex", lastFullPathSysex);
        MidiInName = prop.getString("MidiInName", MidiInName);
        MidiOutName = prop.getString("MidiOutName", MidiOutName);
        MidiThruName = prop.getString("MidiThruName", MidiThruName);
        chainId = Utils.validateInt(prop.getInt("chainId", chainId), 0, 3, chainId);
        sysexDelay = Utils.validateInt(prop.getInt("sysexDelay", sysexDelay), 10, 100, sysexDelay);
        LookAndFeelName = prop.getString("LookAndFeelName", LookAndFeelName);
    }
}