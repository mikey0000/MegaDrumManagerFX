package info.megadrum.managerfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import info.megadrum.managerfx.data.Config3rd;
import info.megadrum.managerfx.data.ConfigFull;
import info.megadrum.managerfx.data.ConfigOptions;
import info.megadrum.managerfx.data.ConfigPad;
import info.megadrum.managerfx.data.ConfigPedal;
import info.megadrum.managerfx.data.FileManager;
import info.megadrum.managerfx.midi.MidiController;
import info.megadrum.managerfx.midi.MidiRescanEvent;
import info.megadrum.managerfx.midi.MidiRescanEventListener;
import info.megadrum.managerfx.ui.MidiLevelBar;
import info.megadrum.managerfx.ui.UIPadsExtra;
import info.megadrum.managerfx.ui.UIPanel;
import info.megadrum.managerfx.ui.UIGlobal;
import info.megadrum.managerfx.ui.UIGlobalMisc;
import info.megadrum.managerfx.ui.UIMidiLog;
import info.megadrum.managerfx.ui.UIMisc;
import info.megadrum.managerfx.ui.UIOptions;
import info.megadrum.managerfx.ui.UIPad;
import info.megadrum.managerfx.ui.UIPedal;
import info.megadrum.managerfx.ui.UIUpgrade;
import info.megadrum.managerfx.utils.Constants;
import info.megadrum.managerfx.utils.Utils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;


public class Controller implements MidiRescanEventListener {
    private final Stage window;
    private final Image imageWindowIcon;
    private final Scene scene1;
    private MenuBar mainMenuBar;
    private Menu menuLoadFromMdSlot, menuSaveToMdSlot;
    private ContextMenu contextMenuLoadFromMdSlot, contextMenuSaveToMdSlot;
    private ArrayList<MenuItem> allMenuItemsLoadFromSlot, allMenuItemsSaveSlot;
    private ContextMenu contextMenuCopyPad;
    private Menu menuCopyPad, menuCopyHead, menuCopyRim, menuCopy3rd;
    private ArrayList<MenuItem> menuItemsCopyPad, menuItemsCopyHead, menuItemsCopyRim, menuItemsCopy3rd;

    private final UIOptions optionsWindow;
    private final UIUpgrade upgradeWindow;
    private final VBox topVBox;
    private final HBox hBoxUIviews;
    private final ScrollPane scrollPaneAllPanels;
    private final double hBoxUIviewsGap = 2.0;
    private final UIGlobal uiGlobal;
    private final UIGlobalMisc uiGlobalMisc;
    private final UIMisc uiMisc;
    private final UIPedal uiPedal;
    private final UIPad uiPad;
    private final UIPadsExtra uiPadsExtra;
    private final UIMidiLog uiMidiLog;
    private final ArrayList<UIPanel> allPanels;
    private int setPedalLevel = 0;

    private MidiController midiController;
    private ConfigOptions configOptions;
    private ConfigFull configFull;
    private ConfigFull moduleConfigFull;
    private ConfigFull[] fullConfigs;
    private final FileManager fileManager;
    private int padPair = 0;
    private int comboBoxInputChangedFromSet = 0;
    private int comboBoxFileChangedFromSet = 0;
    private int oldInputsCounts = 0;
    private boolean sendNextAllSysexRequestsFlag = false;
    private boolean sendSysexReadOnlyRequestFlag = false;
    private boolean loadConfigAfterLoadSlot = false;
    private boolean saveToSlotAfterSendAll = false;
    private int saveToSlot = 0;

    private int curvePointer = 0;

    private List<byte[]> sysexSendList;
    private List<byte[]> sysexLastChanged;
    private int sysexThreadsStarted = 0;

    private boolean versionWarningAlreadyShown = false;

    private boolean controlsSizeIsDouble = false;
    private boolean controlsSizeIsSingle = false;

    private int viewZoom = 0;
    private ToggleGroup toggleGroupZoom;

    public Controller(Stage primaryStage) {
        window = primaryStage;
        window.setTitle(Constants.WINDOWS_TITLE);
        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });
        imageWindowIcon = new Image("/icon_256x256.png");

        fileManager = new FileManager(window);
        uiGlobal = new UIGlobal();
        uiGlobalMisc = new UIGlobalMisc();
        uiMisc = new UIMisc("Misc");
        uiPedal = new UIPedal("HiHat Pedal");
        uiPad = new UIPad("Pads");
        uiPadsExtra = new UIPadsExtra("Pads Extra Settings");
        uiMidiLog = new UIMidiLog("MIDI Log");
        initMidi();
        initConfigs();

        allPanels = new ArrayList<>();
        allPanels.add(uiMisc);
        allPanels.add(uiPedal);
        allPanels.add(uiPad);
        allPanels.add(uiPadsExtra);
        allPanels.add(uiMidiLog);

        createMainMenuBar();
        createContextMenuCopyPad();
        uiGlobal.getButtonGetAll().setOnAction(_ -> sendAllSysexRequests());
        uiGlobal.getButtonSendAll().setOnAction(_ -> sendAllSysex());
        uiGlobal.getButtonLoadAll().setOnAction(_ -> load_all());
        uiGlobal.getButtonSaveAll().setOnAction(_ -> save_all());
        uiGlobal.getButtonLoadFromSlot().setOnAction(_ -> contextMenuLoadFromMdSlot.show(uiGlobal.getButtonLoadFromSlot(), Side.RIGHT, 0, 0));
        uiGlobal.getButtonSaveToSlot().setOnAction(_ -> contextMenuSaveToMdSlot.show(uiGlobal.getButtonSaveToSlot(), Side.RIGHT, 0, 0));
        uiGlobal.getComboBoxFile().getSelectionModel().selectedItemProperty().addListener((_, _, _) -> {
            if (comboBoxFileChangedFromSet > 0) {
                comboBoxFileChangedFromSet--;
            } else {
                if (!uiGlobal.getComboBoxFile().getItems().isEmpty()) {
                    configOptions.lastConfig = uiGlobal.getComboBoxFile().getSelectionModel().getSelectedIndex();
                    fileManager.loadAllSilent(fullConfigs[configOptions.lastConfig], configOptions);
                    loadAllFromConfigFull();
                }
            }
        });
        uiGlobal.getButtonPrevFile().setOnAction(_ -> {
            if (configOptions.lastConfig > 0) {
                uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig - 1);
            }
        });
        uiGlobal.getButtonNextFile().setOnAction(_ -> {
            if (configOptions.lastConfig < (Constants.CONFIGS_COUNT - 1)) {
                uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig + 1);
            }
        });


        uiGlobalMisc.getButtonGet().setOnAction(_ -> sendSysexGlobalMiscRequest());
        uiGlobalMisc.getButtonSend().setOnAction(_ -> sendSysexGlobalMisc());
        uiGlobalMisc.getCheckBoxLiveUpdates().selectedProperty().addListener((_, _, newValue) -> configOptions.liveUpdates = newValue);
        uiGlobalMisc.addControlChangeEventListener((_, _) -> controlsChangedGlobalMisc());
        uiGlobalMisc.getToggleButtonMidi().setOnAction(_ -> openMidiPorts(uiGlobalMisc.getToggleButtonMidi().isSelected()));
        uiMisc.getButtonGet().setOnAction(_ -> sendSysexMiscRequest());
        uiMisc.getButtonSend().setOnAction(_ -> sendSysexMisc());
        uiMisc.getButtonLoad().setOnAction(_ -> loadSysexMisc());
        uiMisc.getButtonSave().setOnAction(_ -> saveSysexMisc());

        uiMisc.addControlChangeEventListener((_, _) -> controlsChangedMisc());

        uiPedal.getButtonSend().setOnAction(_ -> sendSysexPedal());
        uiPedal.getButtonGet().setOnAction(_ -> sendSysexPedalRequest());
        uiPedal.getButtonLoad().setOnAction(_ -> loadSysexPedal());
        uiPedal.getButtonSave().setOnAction(_ -> saveSysexPedal());
        uiPedal.getButtonSetLow().setOnAction(_ -> setPedalLow());
        uiPedal.getButtonSetHigh().setOnAction(_ -> setPedalHigh());
        uiPedal.addControlChangeEventListener((_, _) -> controlsChangedPedal());
        uiPad.addControlChangeEventListener((_, parameter) -> {
            int inputNumber = 0;
            if (padPair > 0) {
                inputNumber = ((padPair - 1) * 2) + 1;
            }
            switch (parameter) {
                case Constants.CONTROL_CHANGE_EVENT_LEFT_INPUT:
                    if (uiPad.isCopyPressed()) {
                        uiPad.resetCopyPressed();
                        copyLeftInputValueToAllOthers();
                        switchToSelectedPair(padPair);
                    } else {
                        controlsChangedInput(inputNumber, true);
                    }
                    break;
                case Constants.CONTROL_CHANGE_EVENT_RIGHT_INPUT:
                    if (uiPad.isCopyPressed()) {
                        uiPad.resetCopyPressed();
                        copyPadPairValueToAllOthers();
                    } else {
                        controlsChangedInput(inputNumber + 1, false);
                    }
                    break;
                case Constants.CONTROL_CHANGE_EVENT_3RD_INPUT:
                    if (padPair > 0) {
                        if (uiPad.isCopyPressed()) {
                            uiPad.resetCopyPressed();
                            copy3rdZoneValueToAllOthers();
                        } else {
                            controlsChangedInput(inputNumber + 1, false);
                            controlsChanged3rd(padPair);
                        }
                    }
                    break;
                default:
                    break;
            }
            if (uiPad.isNameChanged()) {
                uiPad.resetNameChanged();
                updateComboBoxInput(true);
            }
        });

        padPair = 0;
        uiPad.getButtonGet().setOnAction(_ -> {
            if (padPair == 0) {
                sendSysexInputRequest(0);
            } else {
                sendSysexPairRequest(padPair);
            }
        });
        uiPad.getButtonSend().setOnAction(_ -> {
            if (padPair == 0) {
                sendSysexInput(0, true);
            } else {
                sendSysexPair(padPair);
            }
        });
        uiPad.getButtonGetAll().setOnAction(_ -> sendAllInputsSysexRequests());
        uiPad.getButtonSendAll().setOnAction(_ -> sendAllInputsSysex());
        uiPad.getButtonLoad().setOnAction(_ -> loadSysexPad());
        uiPad.getButtonSave().setOnAction(_ -> saveSysexPad());
        uiPad.getButtonPrev().setOnAction(_ -> {
            if (padPair > 0) {
                switchToSelectedPair(padPair - 1);
            }
        });
        uiPad.getButtonNext().setOnAction(_ -> {
            if (padPair < ((configFull.configGlobalMisc.inputs_count / 2) - 1)) {
                switchToSelectedPair(padPair + 1);
            }
        });
        uiPad.getButtonFirst().setOnAction(_ -> switchToSelectedPair(0));
        uiPad.getButtonLast().setOnAction(_ -> switchToSelectedPair((configFull.configGlobalMisc.inputs_count / 2) - 1));
        uiPad.getComboBoxInput().getSelectionModel().selectedItemProperty().addListener((_, _, _) -> {
            if (comboBoxInputChangedFromSet > 0) {
                comboBoxInputChangedFromSet--;
            } else {
                int newInValue = uiPad.getComboBoxInput().getSelectionModel().getSelectedIndex();
                if (newInValue > -1) {
                    switchToSelectedPair(newInValue);
                }
            }
        });
        updateComboBoxInput(true);
        uiPad.setInputPair(0, configFull.configPads[0], configFull.configPos[0], null, null, null);

        uiPadsExtra.addControlChangeEventListener((_, parameter) -> {
            if (parameter == Constants.CONTROL_CHANGE_EVENT_CURVE) {
                controlsChangedCurve();
            } else {
                if (parameter >= Constants.CUSTOM_NAME_CHANGE_TEXT_START) {
                    if (parameter < Constants.CUSTOM_NAME_CHANGE_GET_START) {
                        controlsChangedCustomName(parameter - Constants.CUSTOM_NAME_CHANGE_TEXT_START);
                    } else if (parameter < Constants.CUSTOM_NAME_CHANGE_SEND_START) {
                        sendSysexCustomNameRequest(parameter - Constants.CUSTOM_NAME_CHANGE_GET_START);
                    } else {
                        sendSysexCustomName(parameter - Constants.CUSTOM_NAME_CHANGE_SEND_START);
                    }
                }
            }
        });
        uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().selectedItemProperty().addListener((_, _, _) -> {
            switch (uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().getSelectedIndex()) {
                case 0:
                    configFull.customNamesCount = 2;
                    break;
                case 1:
                    configFull.customNamesCount = 16;
                    break;
                default:
                    configFull.customNamesCount = 32;
                    break;
            }
        });
        uiPadsExtra.getCustomNamesButtonGetAll().setOnAction(_ -> sendAllCustomNamesSysexRequests());
        uiPadsExtra.getCustomNamesButtonSendAll().setOnAction(_ -> sendAllCustomNamesSysex());
        uiPadsExtra.getCustomNamesButtonLoadAll().setOnAction(_ -> loadSysexAllCustomNames());
        uiPadsExtra.getCustomNamesButtonSaveAll().setOnAction(_ -> saveSysexAllCustomNames());
        setCustomNamesCountControl();
        uiPadsExtra.getCurvesButtonGet().setOnAction(_ -> sendSysexCurveRequest());
        uiPadsExtra.getCurvesButtonSend().setOnAction(_ -> sendSysexCurve());
        uiPadsExtra.getCurvesButtonGetAll().setOnAction(_ -> sendAllCurvesSysexRequests());
        uiPadsExtra.getCurvesButtonSendAll().setOnAction(_ -> sendAllCurvesSysex());
        uiPadsExtra.getCurvesButtonLoad().setOnAction(_ -> loadSysexCurve());
        uiPadsExtra.getCurvesButtonSave().setOnAction(_ -> saveSysexCurve());
        uiPadsExtra.getCurvesComboBox().getSelectionModel().selectedItemProperty().addListener((_, _, _) -> {
            int newCurve = uiPadsExtra.getCurvesComboBox().getSelectionModel().getSelectedIndex();
            if (newCurve != curvePointer) {
                switchToSelectedCurve(newCurve);
            }
        });
        uiPadsExtra.getCurvesButtonFirst().setOnAction(_ -> switchToSelectedCurve(0));
        uiPadsExtra.getCurvesButtonPrev().setOnAction(_ -> switchToSelectedCurve(curvePointer - 1));
        uiPadsExtra.getCurvesButtonNext().setOnAction(_ -> switchToSelectedCurve(curvePointer + 1));
        uiPadsExtra.getCurvesButtonLast().setOnAction(_ -> switchToSelectedCurve(Constants.CURVES_COUNT - 1));

        topVBox = new VBox();

        mainMenuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        topVBox.getChildren().add(mainMenuBar);
        topVBox.getChildren().add(uiGlobal.getUI());
        topVBox.getChildren().add(uiGlobalMisc.getUI());

        hBoxUIviews = new HBox(hBoxUIviewsGap);
        hBoxUIviews.setPadding(new Insets(1.0, 2.0, 0.0, 2.0));

        scrollPaneAllPanels = new ScrollPane();
        scrollPaneAllPanels.setContent(hBoxUIviews);
        scrollPaneAllPanels.setHbarPolicy(ScrollBarPolicy.NEVER);

        topVBox.getChildren().add(scrollPaneAllPanels);
        scene1 = new Scene(topVBox);
        scene1.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        optionsWindow = new UIOptions(configOptions);
        optionsWindow.addMidiRescanEventListener(this);

        upgradeWindow = new UIUpgrade(midiController, fileManager, configOptions);

        window.setScene(scene1);
        window.setMinWidth(1000.0);
        window.setMinHeight(520);
        scene1.widthProperty().addListener((_, _, _) -> respondToResize(scene1));

        scene1.heightProperty().addListener((_, _, _) -> respondToResize(scene1));
        loadConfig();
        showGlobalMisc();
        showPanels();
        window.show();
    }

    public void respondToResize(Scene sc) {
        window.getIcons().clear();
        window.getIcons().add(imageWindowIcon);

        uiGlobalMisc.respondToResize(sc.getHeight() * 0.05);
        double mainMenuBarHeight = mainMenuBar.getHeight();
        double globalBarHeight = uiGlobal.getUI().layoutBoundsProperty().getValue().getHeight();
        double globalMiscBarHeight;
        if (uiGlobalMisc.getViewState() == Constants.PANEL_SHOW) {
            globalMiscBarHeight = sc.getHeight() * 0.05;
        } else {
            globalMiscBarHeight = 0.0;
        }
        double height = sc.getHeight() - mainMenuBarHeight - globalBarHeight;
        if (uiGlobalMisc.getViewState() == Constants.PANEL_SHOW) {
            height -= globalMiscBarHeight;
        }
        double width = sc.getWidth();
        double controlH, controlW;
        double midiLogHeight;
        double midiLogWidth = width;
        if (uiPad.getViewState() == Constants.PANEL_SHOW) {
            controlH = (height / uiPad.getVerticalControlsCount()) - 0.4;
            midiLogHeight = height * 0.7;
        } else if (uiPedal.getViewState() == Constants.PANEL_SHOW) {
            controlH = height / uiPedal.getVerticalControlsCount() * 1.045 - 0.95;
            midiLogHeight = height * 0.7;
        } else if (uiPadsExtra.getViewState() == Constants.PANEL_SHOW) {
            controlH = height / uiPadsExtra.getVerticalControlsCount();
            midiLogHeight = height * 0.7;
        } else if (uiMisc.getViewState() == Constants.PANEL_SHOW) {
            controlH = height / uiMisc.getVerticalControlsCount() * 1.045 - 0.95;
            midiLogHeight = height * 0.7;
        } else {
            controlH = height / uiMidiLog.getVerticalControlsCount();
            midiLogHeight = height * 0.95;
            midiLogWidth = midiLogWidth * 0.997;
        }

        controlW = width;
        if (viewZoom > 0) {
            controlH = controlH * (0.9 + (viewZoom * 0.1));
            controlW = width * (0.9 + (viewZoom * 0.1));
            if (!controlsSizeIsDouble) {
                controlsSizeIsDouble = true;
                controlsSizeIsSingle = false;
                topVBox.getChildren().removeLast();
                scrollPaneAllPanels.setContent(hBoxUIviews);
                topVBox.getChildren().add(scrollPaneAllPanels);
            }
            if (viewZoom > 1) {
                scrollPaneAllPanels.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
                scrollPaneAllPanels.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
            } else {
                scrollPaneAllPanels.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
                scrollPaneAllPanels.setHbarPolicy(ScrollBarPolicy.NEVER);
            }
        } else {
            if (!controlsSizeIsSingle) {
                controlsSizeIsSingle = true;
                controlsSizeIsDouble = false;
                scrollPaneAllPanels.setHbarPolicy(ScrollBarPolicy.NEVER);
                scrollPaneAllPanels.setVbarPolicy(ScrollBarPolicy.NEVER);
                scrollPaneAllPanels.setContent(null);
                topVBox.getChildren().removeLast();
                topVBox.getChildren().add(hBoxUIviews);
            }
        }
        double controlWdivider = 0.0;
        if (uiMisc.getViewState() == Constants.PANEL_SHOW) {
            controlWdivider += Constants.FX_MISC_CONTROL_WIDTH_MUL * 1.000;
            controlW -= hBoxUIviewsGap;
        }
        if (uiPedal.getViewState() == Constants.PANEL_SHOW) {
            controlWdivider += Constants.FX_PEDAL_CONTROL_WIDTH_MUL * 1.000;
            controlW -= hBoxUIviewsGap;
        }
        if (uiPad.getViewState() == Constants.PANEL_SHOW) {
            controlWdivider += Constants.FX_INPUT_CONTROL_WIDTH_MUL * 2.000;
            controlW -= hBoxUIviewsGap * 8;
        }
        if (uiPadsExtra.getViewState() == Constants.PANEL_SHOW) {
            controlW -= 340.0 + hBoxUIviewsGap;
        }
        if (uiMidiLog.getViewState() == Constants.PANEL_SHOW) {
            controlW -= width * 0.23 + hBoxUIviewsGap;
        }

        controlWdivider = (controlWdivider == 0) ? 1 : controlWdivider;
        controlW = controlW / controlWdivider;
        controlW = (controlW > 360.0) ? 360.0 : controlW;
        if (viewZoom > 0) {
            controlW = controlH / Constants.FX_CONTROL_H_TO_W;
        }
        int correction = 0;
        if (uiMisc.getViewState() == Constants.PANEL_SHOW) {
            uiMisc.respondToResize(width, height, controlW, controlH);
            midiLogWidth -= (controlW * Constants.FX_MISC_CONTROL_WIDTH_MUL + hBoxUIviewsGap);
            correction = 1;
        }
        if (uiPedal.getViewState() == Constants.PANEL_SHOW) {
            uiPedal.respondToResize(width, height, controlW, controlH);
            midiLogWidth -= (controlW * Constants.FX_PEDAL_CONTROL_WIDTH_MUL + hBoxUIviewsGap);
            correction = 1;
        }
        if (uiPad.getViewState() == Constants.PANEL_SHOW) {
            uiPad.respondToResize(width, height, controlW, controlH);
            midiLogWidth -= ((controlW * Constants.FX_INPUT_CONTROL_WIDTH_MUL) * 2 + hBoxUIviewsGap * 7.0);
            correction = 1;
        }
        if (uiPadsExtra.getViewState() == Constants.PANEL_SHOW) {
            uiPadsExtra.respondToResize(width, height, controlW, controlH);
            midiLogWidth -= (340.0 + hBoxUIviewsGap);
            correction = 1;
        }
        if (correction > 0) {
            midiLogWidth -= hBoxUIviewsGap * correction * 2;
        }
        if (uiMidiLog.getViewState() == Constants.PANEL_SHOW) {
            if (viewZoom > 0 && midiLogWidth < controlW * 3) {
                midiLogWidth = controlW * 3;
            }
            uiMidiLog.respondToResize(midiLogWidth, midiLogHeight, controlW, controlH);
        }
    }

    public void respondToResizeDetached(UIPanel uiPanel) {
        uiPanel.respondToResizeDetached();
    }

    public void setCustomNamesCountControl() {
        switch (configFull.customNamesCount) {
            case 32:
                uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().select(2);
                break;
            case 16:
                uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().select(1);
                break;
            case 2:
            default:
                uiPadsExtra.getComboBoxCustomNamesCount().getSelectionModel().select(0);
                break;
        }
    }

    private void reCreateSlotsMenuItems() {
        allMenuItemsLoadFromSlot.clear();
        for (int i = 0; i < configFull.configNamesCount; i++) {
            final int iFinal = i;
            if (configFull.configGlobalMisc.config_names_en) {
                allMenuItemsLoadFromSlot.add(new MenuItem(i + 1 + " " + configFull.configConfigNames[i].name));
            } else {
                allMenuItemsLoadFromSlot.add(new MenuItem(Integer.toString(i + 1)));
            }
            allMenuItemsLoadFromSlot.get(i).setOnAction(_ -> sendSysexLoadFromSlotRequest(iFinal));
        }
        menuLoadFromMdSlot.getItems().clear();
        menuLoadFromMdSlot.getItems().addAll(allMenuItemsLoadFromSlot);
        contextMenuLoadFromMdSlot.getItems().clear();
        contextMenuLoadFromMdSlot.getItems().addAll(allMenuItemsLoadFromSlot);
        allMenuItemsSaveSlot.clear();
        for (int i = 0; i < configFull.configNamesCount; i++) {
            final int iFinal = i;
            if (configFull.configGlobalMisc.config_names_en) {
                allMenuItemsSaveSlot.add(new MenuItem(i + 1 + " " + configFull.configConfigNames[i].name));
            } else {
                allMenuItemsSaveSlot.add(new MenuItem(Integer.toString(i + 1)));
            }
            allMenuItemsSaveSlot.get(i).setOnAction(_ -> sendSysexSaveToSlotRequest(iFinal));
        }
        menuSaveToMdSlot.getItems().clear();
        menuSaveToMdSlot.getItems().addAll(allMenuItemsSaveSlot);
        contextMenuSaveToMdSlot.getItems().clear();
        contextMenuSaveToMdSlot.getItems().addAll(allMenuItemsSaveSlot);
    }

    private void createContextMenuCopyPad() {
        contextMenuCopyPad = new ContextMenu();
        menuCopyPad = new Menu("CopyPad");
        menuCopyHead = new Menu("CopyHead");
        menuCopyRim = new Menu("CopyRim");
        menuCopy3rd = new Menu("Copy3rd");
        contextMenuCopyPad.getItems().addAll(menuCopyPad, menuCopyHead, menuCopyRim, menuCopy3rd);
        uiPad.getButtonCopy().setOnAction(_ -> contextMenuCopyPad.show(uiPad.getButtonCopy(), Side.RIGHT, 0, 0));
        menuItemsCopyPad = new ArrayList<>();
        menuItemsCopyHead = new ArrayList<>();
        menuItemsCopyRim = new ArrayList<>();
        menuItemsCopy3rd = new ArrayList<>();
    }

    private void allWindowsToFront() {
        for (UIPanel allPanel : allPanels) {
            if (allPanel.isDetached()) {
                allPanel.getWindow().toFront();
            }
        }
    }

    private void createMainMenuBar() {
        mainMenuBar = new MenuBar();
        mainMenuBar.useSystemMenuBarProperty().set(true);
        mainMenuBar.setStyle("-fx-font-size: 10 pt");
        Menu menuMain = new Menu("Main");
        Menu menuView = new Menu("View");
        Menu menuHelp = new Menu("Help");
        Menu menuWindows = new Menu("Windows");
        MenuItem menuItemAbout = new MenuItem("About");
        menuItemAbout.setOnAction(_ -> showAbout());
        menuHelp.getItems().add(menuItemAbout);

        mainMenuBar.getMenus().addAll(menuMain, menuView, menuWindows, menuHelp);
        MenuItem menuItemWindowsToFront = new MenuItem("All to Front");
        menuItemWindowsToFront.setOnAction(_ -> allWindowsToFront());
        menuWindows.getItems().add(menuItemWindowsToFront);

        Menu menuAllSettings = new Menu("All Settings");
        MenuItem menuItemAllSettingsGet = new MenuItem("Get from MD");
        menuItemAllSettingsGet.setOnAction(_ -> sendAllSysexRequests());
        MenuItem menuItemAllSettingsSend = new MenuItem("Send to MD");
        menuItemAllSettingsSend.setOnAction(_ -> sendAllSysex());
        MenuItem menuItemAllSettingsLoad = new MenuItem("Load from file");
        menuItemAllSettingsLoad.setOnAction(_ -> load_all());
        MenuItem menuItemAllSettingsSave = new MenuItem("Save to file");
        menuItemAllSettingsSave.setOnAction(_ -> save_all());
        menuLoadFromMdSlot = new Menu("Load from MD Slot:");
        contextMenuLoadFromMdSlot = new ContextMenu();
        allMenuItemsLoadFromSlot = new ArrayList<>();
        menuSaveToMdSlot = new Menu("Save to MD Slot:");
        contextMenuSaveToMdSlot = new ContextMenu();
        allMenuItemsSaveSlot = new ArrayList<>();
        reCreateSlotsMenuItems();
        menuAllSettings.getItems().addAll(menuItemAllSettingsGet, menuItemAllSettingsSend, menuItemAllSettingsLoad, menuItemAllSettingsSave, menuLoadFromMdSlot, menuSaveToMdSlot);

        Menu menuGlobalMisc = new Menu("Global Misc Settings");
        MenuItem menuItemGlobalMiscGet = new MenuItem("Get from MD");
        menuItemGlobalMiscGet.setOnAction(_ -> sendSysexGlobalMiscRequest());
        MenuItem menuItemGlobalMiscSend = new MenuItem("Send to MD");
        menuItemGlobalMiscSend.setOnAction(_ -> sendSysexGlobalMisc());
        menuGlobalMisc.getItems().addAll(menuItemGlobalMiscGet, menuItemGlobalMiscSend);

        Menu menuMisc = new Menu("Misc Settings");
        MenuItem menuItemMiscGet = new MenuItem("Get from MD");
        menuItemMiscGet.setOnAction(_ -> sendSysexMiscRequest());
        MenuItem menuItemMiscSend = new MenuItem("Send to MD");
        menuItemMiscSend.setOnAction(_ -> sendSysexMisc());
        menuMisc.getItems().addAll(menuItemMiscGet, menuItemMiscSend);

        Menu menuHiHat = new Menu("HiHat Pedal Settings");
        MenuItem menuItemHiHatGet = new MenuItem("Get from MD");
        menuItemHiHatGet.setOnAction(_ -> sendSysexPedalRequest());
        MenuItem menuItemHiHatSend = new MenuItem("Send to MD");
        menuItemHiHatSend.setOnAction(_ -> sendSysexPedal());
        menuHiHat.getItems().addAll(menuItemHiHatGet, menuItemHiHatSend);

        Menu menuAllPads = new Menu("All Pads Settings");
        MenuItem menuItemAllPadsGet = new MenuItem("Get from MD");
        menuItemAllPadsGet.setOnAction(_ -> sendAllInputsSysexRequests());
        MenuItem menuItemAllPadsSend = new MenuItem("Send to MD");
        menuItemAllPadsSend.setOnAction(_ -> sendAllInputsSysex());
        menuAllPads.getItems().addAll(menuItemAllPadsGet, menuItemAllPadsSend);

        Menu menuSelectedPad = new Menu("Selected Pad Settings");
        MenuItem menuItemSelectedPadGet = new MenuItem("Get from MD");
        menuItemSelectedPadGet.setOnAction(_ -> {
            if (padPair == 0) {
                sendSysexInputRequest(0);
            } else {
                sendSysexPairRequest(padPair);
            }
        });
        MenuItem menuItemSelectedPadSend = new MenuItem("Send to MD");
        menuItemSelectedPadSend.setOnAction(_ -> {
            if (padPair == 0) {
                sendSysexInput(0, true);
            } else {
                sendSysexPair(padPair);
            }
        });
        menuSelectedPad.getItems().addAll(menuItemSelectedPadGet, menuItemSelectedPadSend);

        Menu menuCustomCurves = new Menu("All Custom Curves");
        MenuItem menuItemCustomCurvesGet = new MenuItem("Get from MD");
        menuItemCustomCurvesGet.setOnAction(_ -> sendAllCurvesSysexRequests());
        MenuItem menuItemCustomCurvesSend = new MenuItem("Send to MD");
        menuItemCustomCurvesSend.setOnAction(_ -> sendAllCurvesSysex());
        menuCustomCurves.getItems().addAll(menuItemCustomCurvesGet, menuItemCustomCurvesSend);

        Menu menuCustomNames = new Menu("All Custom Names");
        MenuItem menuItemCustomNamesGet = new MenuItem("Get from MD");
        menuItemCustomNamesGet.setOnAction(_ -> sendAllCustomNamesSysexRequests());
        MenuItem menuItemCustomNamesSend = new MenuItem("Send to MD");
        menuItemCustomNamesSend.setOnAction(_ -> sendAllCustomNamesSysex());
        menuCustomNames.getItems().addAll(menuItemCustomNamesGet, menuItemCustomNamesSend);

        MenuItem firmwareUpgradeMenuItem = new MenuItem("Firmware Upgrade");
        firmwareUpgradeMenuItem.setOnAction(_ -> showUpgradeWindow());
        MenuItem optionsMenuItem = new MenuItem("Options");
        optionsMenuItem.setOnAction(_ -> showOptionsWindow());
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(_ -> closeProgram());

        menuMain.getItems().addAll(menuAllSettings, menuGlobalMisc, menuMisc, menuHiHat, menuAllPads, menuSelectedPad, menuCustomCurves, menuCustomNames, new SeparatorMenuItem(), firmwareUpgradeMenuItem, new SeparatorMenuItem(), optionsMenuItem, new SeparatorMenuItem(), exitMenuItem);

        Menu menuViewZoom = new Menu("Zoom");
        menuView.getItems().add(menuViewZoom);
        toggleGroupZoom = new ToggleGroup();

        for (int i = 0; i < 12; i++) {
            int iFinal = i;
            double zoom = (10.0 + i) * 0.1 - 0.1;
            RadioMenuItem radioMenuItem;
            if (i == 0) {
                radioMenuItem = new RadioMenuItem("Fit window");
            } else {
                radioMenuItem = new RadioMenuItem(String.format("Zoom %1.1fx", zoom));
            }
            radioMenuItem.setToggleGroup(toggleGroupZoom);
            radioMenuItem.setOnAction(_ -> {
                viewZoom = iFinal;
                configOptions.viewZoom = viewZoom;
                respondToResize(scene1);
            });
            menuViewZoom.getItems().add(radioMenuItem);
        }
        toggleGroupZoom.getToggles().getFirst().setSelected(true);

        menuView.getItems().add(new SeparatorMenuItem());
        uiGlobalMisc.getRadioMenuItemHide().setOnAction(_ -> {
            uiGlobalMisc.setViewState(Constants.PANEL_HIDE);
            showGlobalMisc();
        });
        uiGlobalMisc.getRadioMenuItemShow().setOnAction(_ -> {
            uiGlobalMisc.setViewState(Constants.PANEL_SHOW);
            showGlobalMisc();
        });

        Menu menuViewGlobalMisc = createMenu("Global Misc", uiGlobalMisc.getRadioMenuItemHide(), uiGlobalMisc.getRadioMenuItemShow());
        menuView.getItems().add(menuViewGlobalMisc);

        Menu menuViewMisc = createMenu("Misc", uiMisc.getRadioMenuItemHide(), uiMisc.getRadioMenuItemShow(), uiMisc.getRadioMenuItemDetach());
        menuView.getItems().add(menuViewMisc);

        Menu menuViewPedal = createMenu("Pedal", uiPedal.getRadioMenuItemHide(), uiPedal.getRadioMenuItemShow(), uiPedal.getRadioMenuItemDetach());
        menuView.getItems().add(menuViewPedal);

        Menu menuViewPads = createMenu("Pads", uiPad.getRadioMenuItemHide(), uiPad.getRadioMenuItemShow(), uiPad.getRadioMenuItemDetach());
        menuView.getItems().add(menuViewPads);

        Menu menuViewPadsExtra = createMenu("PadsExtra", uiPadsExtra.getRadioMenuItemHide(), uiPadsExtra.getRadioMenuItemShow(), uiPadsExtra.getRadioMenuItemDetach());
        menuView.getItems().add(menuViewPadsExtra);

        Menu menuViewMidiLog = createMenu("MidiLog", uiMidiLog.getRadioMenuItemHide(), uiMidiLog.getRadioMenuItemShow(), uiMidiLog.getRadioMenuItemDetach());
        menuView.getItems().add(menuViewMidiLog);

        for (int i = 0; i < allPanels.size(); i++) {
            final int iFinal = i;
            allPanels.get(i).getRadioMenuItemShow().setSelected(true);
            allPanels.get(i).getRadioMenuItemHide().setOnAction(_ -> {
                allPanels.get(iFinal).setViewState(Constants.PANEL_HIDE);
                showPanels();
            });
            allPanels.get(i).getRadioMenuItemShow().setOnAction(_ -> {
                allPanels.get(iFinal).setViewState(Constants.PANEL_SHOW);
                showPanels();
            });
            allPanels.get(i).getRadioMenuItemDetach().setOnAction(_ -> {
                allPanels.get(iFinal).setViewState(Constants.PANEL_DETACH);
                showPanels();
            });
        }
    }

    private Menu createMenu(String name, MenuItem... items) {
        Menu menu = new Menu(name);
        menu.getItems().addAll(items);
        return menu;
    }


    private void showGlobalMisc() {
        if (uiGlobalMisc.getViewState() == Constants.PANEL_SHOW) {
            if (!topVBox.getChildren().contains(uiGlobalMisc.getUI())) {
                topVBox.getChildren().add(2, uiGlobalMisc.getUI());
            }
        } else {
            topVBox.getChildren().remove(uiGlobalMisc.getUI());
        }
        respondToResize(scene1);
    }

    private void showPanels() {
        hBoxUIviews.getChildren().clear();
        for (int i = 0; i < allPanels.size(); i++) {
            final int iFinal = i;
            switch (allPanels.get(i).getViewState()) {
                case Constants.PANEL_DETACH:
                    if (!allPanels.get(i).isDetached()) {
                        allPanels.get(i).setDetached(true);
                        allPanels.get(iFinal).selectRadioMenuItemDetach();
                        VBox scenePane = new VBox();
                        scenePane.setAlignment(Pos.TOP_CENTER);
                        scenePane.getChildren().add(allPanels.get(i).getTopLayout());
                        Scene scene = new Scene(scenePane);
                        allPanels.get(i).getWindow().setScene(scene);
                        allPanels.get(i).getWindow().sizeToScene();
                        allPanels.get(i).getWindow().setOnCloseRequest(_ -> {
                            allPanels.get(iFinal).setLastX(allPanels.get(iFinal).getWindow().getX());
                            allPanels.get(iFinal).setLastY(allPanels.get(iFinal).getWindow().getY());
                            allPanels.get(iFinal).setLastW(allPanels.get(iFinal).getWindow().getWidth());
                            allPanels.get(iFinal).setLastH(allPanels.get(iFinal).getWindow().getHeight());
                            scenePane.getChildren().clear();
                            allPanels.get(iFinal).setViewState(Constants.PANEL_HIDE);
                            allPanels.get(iFinal).selectRadioMenuItemHide();
                            allPanels.get(iFinal).setDetached(false);
                        });
                        scene.heightProperty().addListener(_ -> respondToResizeDetached(allPanels.get(iFinal)));
                        scene.widthProperty().addListener(_ -> respondToResizeDetached(allPanels.get(iFinal)));
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    allPanels.get(iFinal).getWindow().setX(allPanels.get(iFinal).getLastX());
                                    allPanels.get(iFinal).getWindow().setY(allPanels.get(iFinal).getLastY());
                                    allPanels.get(iFinal).getWindow().setWidth(allPanels.get(iFinal).getLastW());
                                    allPanels.get(iFinal).getWindow().setHeight(allPanels.get(iFinal).getLastH());
                                    allPanels.get(iFinal).getWindow().show();
                                });
                            }
                        }, 1);
                    } else {
                        allPanels.get(i).getWindow().toFront();
                    }
                    break;
                case Constants.PANEL_HIDE:
                    if (allPanels.get(i).getWindow().isShowing()) {
                        allPanels.get(i).getWindow().close();
                        allPanels.get(i).getWindow().setScene(null);
                    }
                    allPanels.get(iFinal).selectRadioMenuItemHide();
                    allPanels.get(i).setDetached(false);
                    break;
                case Constants.PANEL_SHOW:
                default:
                    if (allPanels.get(i).getWindow().isShowing()) {
                        allPanels.get(i).getWindow().close();
                        allPanels.get(i).getWindow().setScene(null);
                    }
                    allPanels.get(iFinal).selectRadioMenuItemShow();
                    allPanels.get(i).setDetached(false);
                    hBoxUIviews.getChildren().add(allPanels.get(i).getUI());
                    break;
            }
        }
        respondToResize(scene1);
    }

    private void showMidiWarningIfNeeded() {
        if (configOptions.MidiInName.isEmpty() || configOptions.MidiOutName.isEmpty()) {
            WebView webView = new WebView();
            webView.getEngine().loadContent(Constants.MIDI_PORTS_WARNING);
            webView.setPrefSize(300, 120);

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("MIDI ports warning!");
            alert.getDialogPane().setContent(webView);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(alert::showAndWait);
                }
            }, 500);
        }
    }

    private void loadConfig() {
        configOptions = fileManager.loadLastOptions(configOptions);
        viewZoom = configOptions.viewZoom;
        toggleGroupZoom.getToggles().get(viewZoom).setSelected(true);
        uiGlobal.getComboBoxFile().getItems().clear();
        uiGlobal.getComboBoxFile().getItems().addAll(configOptions.configFileNames);
        uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig);
        showMidiWarningIfNeeded();
        if (configOptions.autoOpenPorts) {
            openMidiPorts(true);
        }
        midiController.setChainId(configOptions.chainId);
        window.setX(configOptions.mainWindowPosition.getX());
        window.setY(configOptions.mainWindowPosition.getY());
        window.setWidth(configOptions.mainWindowSize.getX());
        window.setHeight(configOptions.mainWindowSize.getY());

        for (int i = 0; i < Constants.PANELS_COUNT; i++) {
            allPanels.get(i).setViewState(configOptions.showPanels[i]);
            allPanels.get(i).setLastX(configOptions.framesPositions[i].getX());
            allPanels.get(i).setLastY(configOptions.framesPositions[i].getY());
            allPanels.get(i).setLastW(configOptions.framesSizes[i].getX());
            allPanels.get(i).setLastH(configOptions.framesSizes[i].getY());
        }
        uiGlobalMisc.setViewState(configOptions.globalMiscViewState);
        uiGlobalMisc.getCheckBoxLiveUpdates().setSelected(configOptions.liveUpdates);
        setShowAdvanced();
    }

    private void closeProgram() {
        midiController.closeAllPorts();
        Point2D point2d = new Point2D(window.getX(), window.getY());
        configOptions.mainWindowPosition = point2d;
        point2d = new Point2D(window.getWidth(), window.getHeight());
        configOptions.mainWindowSize = point2d;
        for (int i = 0; i < Constants.PANELS_COUNT; i++) {
            if (allPanels.get(i).getViewState() == Constants.PANEL_DETACH) {
                point2d = new Point2D(allPanels.get(i).getWindow().getX(), allPanels.get(i).getWindow().getY());
                configOptions.framesPositions[i] = point2d;
                point2d = new Point2D(allPanels.get(i).getWindow().getWidth(), allPanels.get(i).getWindow().getHeight());
                configOptions.framesSizes[i] = point2d;
            }
            configOptions.showPanels[i] = allPanels.get(i).getViewState();
        }
        configOptions.globalMiscViewState = uiGlobalMisc.getViewState();
        if (configOptions.saveOnExit) {
            fileManager.saveLastOptions(configOptions);
        }
        window.close();
        System.exit(0);
    }

    private void copyLeftInputValueToAllOthers() {
        int maxInputs = configFull.configGlobalMisc.inputs_count - 1;
        int valueId = uiPad.getCopyPressedValueId();
        int currentInput = 0;
        if (padPair > 0) {
            currentInput = (padPair * 2) - 1;
        }
        int value = configFull.configPads[currentInput].getValueById(valueId);
        for (int i = 0; i < maxInputs; i++) {
            if (i != currentInput && ((i & 1) > 0) || (valueId != Constants.INPUT_VALUE_ID_TYPE)) {
                if ((valueId >= Constants.INPUT_VALUE_ID_POS_LEVEL) && (valueId <= Constants.INPUT_VALUE_ID_POS_HIGH)) {
                    value = configFull.configPos[currentInput].getValueById(valueId);
                    configFull.configPos[i].setValueById(valueId, value);
                } else {
                    configFull.configPads[i].setValueById(valueId, value);
                }
            }
        }
    }

    private void copyPadPairValueToAllOthers() {
        int maxPair = (configFull.configGlobalMisc.inputs_count / 2) - 1;
        int valueId = uiPad.getCopyPressedValueId();
        int currentPair = padPair - 1;
        int valueLeft = configFull.configPads[currentPair * 2 + 1].getValueById(valueId);
        int valueRight = configFull.configPads[currentPair * 2 + 2].getValueById(valueId);
        for (int i = 0; i < maxPair; i++) {
            if (i != currentPair) {
                if ((valueId >= Constants.INPUT_VALUE_ID_POS_LEVEL) && (valueId <= Constants.INPUT_VALUE_ID_POS_HIGH)) {
                    valueLeft = configFull.configPos[currentPair * 2 + 1].getValueById(valueId);
                    valueRight = configFull.configPos[currentPair * 2 + 2].getValueById(valueId);
                    configFull.configPos[i * 2 + 1].setValueById(valueId, valueLeft);
                    configFull.configPos[i * 2 + 2].setValueById(valueId, valueRight);
                } else {
                    configFull.configPads[i * 2 + 1].setValueById(valueId, valueLeft);
                    configFull.configPads[i * 2 + 2].setValueById(valueId, valueRight);
                }
            }
        }
    }

    private void copy3rdZoneValueToAllOthers() {
        int max3rd = (configFull.configGlobalMisc.inputs_count / 2) - 1;
        int valueId = uiPad.getCopyPressedValueId();
        int current3rd = padPair - 1;
        int value = configFull.config3rds[current3rd].getValueById(valueId);
        for (int i = 0; i < max3rd; i++) {
            if (i != current3rd) {
                configFull.config3rds[i].setValueById(valueId, value);
            }
        }
    }

    private void sendSysexLastChanged() {
        sysexSendList.addAll(sysexLastChanged);
        sysexLastChanged.clear();
        sendSysex();
    }

    private void sendSysex() {
        if (sysexThreadsStarted > 0) {
            sysexLastChanged.clear();
            sysexLastChanged.addAll(sysexSendList);
            sysexSendList.clear();
        } else {
            midiController.sendSysexTaskRecreate();
            uiGlobal.getProgressBarSysex().setVisible(true);
            midiController.addSendSysexTaskSucceedEventHandler(_ -> {
                sysexThreadsStarted--;
                uiGlobal.getProgressBarSysex().progressProperty().unbind();
                uiGlobal.getProgressBarSysex().setProgress(1.0);
                uiGlobal.getProgressBarSysex().setVisible(false);
                int[] status = midiController.getStatus();
                if (status[0] > 0) {
                    uiGlobal.setSysexStatusLabel(status[0], status[1]);
                } else {
                    if (!sysexLastChanged.isEmpty()) {
                        sendSysexLastChanged();
                    } else {
                        if (sendSysexReadOnlyRequestFlag) {
                            sendSysexReadOnlyRequestFlag = false;
                            sendSysexReadOnlyRequest();
                        }
                        if (saveToSlotAfterSendAll) {
                            saveToSlotAfterSendAll = false;
                            sendSysexSaveToSlotOnlyRequest(saveToSlot);
                        }
                        if (sendNextAllSysexRequestsFlag) {
                            sendNextAllSysexRequestsFlag = false;
                            sendNextAllSysexRequests();
                        }
                        if (loadConfigAfterLoadSlot) {
                            loadConfigAfterLoadSlot = false;
                            sendAllSysexRequests();
                        }
                    }
                    if (sysexThreadsStarted == 0) {
                        uiGlobal.setSysexStatusLabel(Constants.MD_SYSEX_STATUS_OK, 0);
                    }
                }
            });
            uiGlobal.setSysexStatusLabel(Constants.MD_SYSEX_STATUS_WORKING, 0);
            sysexThreadsStarted++;
            midiController.setChainId(configOptions.chainId);
            if (midiController.sendSysex(sysexSendList, uiGlobal.getProgressBarSysex(), 10, 50) > 0) {
                sysexThreadsStarted--;
                uiGlobal.getProgressBarSysex().progressProperty().unbind();
                uiGlobal.getProgressBarSysex().setProgress(1.0);
                uiGlobal.getProgressBarSysex().setVisible(false);
                sysexSendList.clear();
                sysexLastChanged.clear();
                loadConfigAfterLoadSlot = false;
                sendNextAllSysexRequestsFlag = false;
                saveToSlotAfterSendAll = false;
                sendSysexReadOnlyRequestFlag = false;
                uiGlobal.setSysexStatusLabel(Constants.MD_SYSEX_STATUS_MIDI_IS_NOT_OPEN, 0);
            }
        }
    }

    private void sendSysexReadOnlyRequest() {
        byte[] typeAndId;
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_VERSION;
        sysexSendList.add(typeAndId);
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_MCU_TYPE;
        sysexSendList.add(typeAndId);
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_CONFIG_COUNT;
        sysexSendList.add(typeAndId);
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void sendSysexGlobalMisc() {
        sysexSendList.add(configFull.configGlobalMisc.getSysexFromConfig());
        sendSysexReadOnlyRequestFlag = true;
        sendSysex();
    }

    private void controlsChangedGlobalMisc() {
        uiGlobalMisc.setConfigFromControls(configFull.configGlobalMisc);
        updateComboBoxInput(false);
        if (configOptions.liveUpdates) {
            sendSysexGlobalMisc();
        }
    }

    private void sendSysexGlobalMiscRequest() {
        byte[] typeAndId;
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_GLOBAL_MISC;
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void sendSysexCustomName(int id) {
        sysexSendList.add(configFull.configCustomNames[id].getSysexFromConfig());
        sendSysex();
    }

    private void controlsChangedCustomName(int id) {
        uiPadsExtra.getCustomName(configFull.configCustomNames[id], id);
        if (configOptions.liveUpdates) {
            sendSysexCustomName(id);
        }
        addCustomNamesToPads();
    }

    private void sendSysexCustomNameRequest(int id) {
        byte[] typeAndId;
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_CUSTOM_NAME;
        typeAndId[1] = (byte) id;
        if (configOptions.mcuType < 2 && id > 15) {
            typeAndId[1] = 15;
        }
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void sendAllCustomNamesSysex() {
        for (byte i = 0; i < configFull.customNamesCount; i++) {
            sysexSendList.add(configFull.configCustomNames[i].getSysexFromConfig());
        }
        sendSysex();
    }

    private void sendAllCustomNamesSysexRequests() {
        for (byte i = 0; i < configFull.customNamesCount; i++) {
            if (configOptions.mcuType < 2) {
                if (i > 15) {
                    break;
                }
            }
            byte[] typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_CUSTOM_NAME;
            typeAndId[1] = i;
            sysexSendList.add(typeAndId);
        }
        sendSysex();
    }


    private void sendSysexCurve() {
        sysexSendList.add(configFull.configCurves[curvePointer].getSysexFromConfig());
        sendSysex();
    }

    private void sendSysexLoadFromSlotRequest(int slot) {
        setAllStatesUnknown();
        byte[] typeAndId;
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_CONFIG_LOAD;
        typeAndId[1] = (byte) slot;
        sysexSendList.add(typeAndId);
        sendSysex();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    byte[] typeAndId1;
                    typeAndId1 = new byte[2];
                    typeAndId1[0] = Constants.MD_SYSEX_GLOBAL_MISC;
                    sysexSendList.add(typeAndId1);
                    typeAndId1 = new byte[2];
                    typeAndId1[0] = Constants.MD_SYSEX_VERSION;
                    sysexSendList.add(typeAndId1);
                    typeAndId1 = new byte[2];
                    typeAndId1[0] = Constants.MD_SYSEX_MCU_TYPE;
                    sysexSendList.add(typeAndId1);
                    typeAndId1 = new byte[2];
                    typeAndId1[0] = Constants.MD_SYSEX_CONFIG_COUNT;
                    sysexSendList.add(typeAndId1);
                    typeAndId1 = new byte[2];
                    typeAndId1[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
                    sysexSendList.add(typeAndId1);
                    loadConfigAfterLoadSlot = true;
                    sendSysex();
                });
            }
        }, 200);
    }

    private void sendSysexSaveToSlotOnlyRequest(int slot) {
        byte[] typeAndId;
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_CONFIG_SAVE;
        typeAndId[1] = (byte) slot;
        sysexSendList.add(typeAndId);
        sendSysex();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    byte[] typeAndIdFinal = new byte[2];
                    typeAndIdFinal[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
                    sysexSendList.add(typeAndIdFinal);
                    sendSysex();
                });
            }
        }, 5000);
    }

    private void sendSysexConfigName(int id) {
        sysexSendList.add(configFull.configConfigNames[id].getSysexFromConfig());
        sendSysex();
    }

    private void sendSysexSaveToSlotRequest(int slot) {
        configFull.configConfigNames[slot].name = (uiGlobalMisc.getTextFieldSlotName().getText() + "            ").substring(0, 12);
        if (configFull.configGlobalMisc.config_names_en) {
            sendSysexConfigName(slot);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    saveToSlot = slot;
                    saveToSlotAfterSendAll = true;
                    sendAllSysex();
                });
            }
        }, 200);
    }

    private void controlsChangedCurve() {
        uiPadsExtra.getYvalues(configFull.configCurves[curvePointer].yValues);
        if (configOptions.liveUpdates) {
            sendSysexCurve();
        }
    }

    private void sendSysexCurveRequest() {
        byte[] typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_CURVE;
        typeAndId[1] = (byte) curvePointer;
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void sendAllCurvesSysex() {
        for (byte i = 0; i < Constants.CURVES_COUNT; i++) {
            sysexSendList.add(configFull.configCurves[i].getSysexFromConfig());
        }
        sendSysex();
    }

    private void sendAllCurvesSysexRequests() {
        for (byte i = 0; i < Constants.CURVES_COUNT; i++) {
            byte[] typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_CURVE;
            typeAndId[1] = i;
            sysexSendList.add(typeAndId);
        }
        sendSysex();
    }

    private void sendSysexMisc() {
        sysexSendList.add(configFull.configMisc.getSysexFromConfig());
        sendSysex();
    }

    private void controlsChangedMisc() {
        uiMisc.setConfigFromControls(configFull.configMisc);
        if (configOptions.liveUpdates) {
            sendSysexMisc();
        }
    }

    private void sendSysexMiscRequest() {
        byte[] typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_MISC;
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void sendSysexPedal() {
        sysexSendList.add(configFull.configPedal.getSysexFromConfig(configOptions.mcuType));
        sendSysex();
    }

    private void controlsChangedPedal() {
        uiPedal.setConfigFromControls(configFull.configPedal);
        if (configOptions.liveUpdates) {
            sendSysexPedal();
        }
    }

    private void sendSysexPedalRequest() {
        byte[] typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_PEDAL;
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void sendSysexInput(int input, boolean leftInput) {
        sysexSendList.add(configFull.configPads[input].getSysexFromConfig());
        if (configOptions.mcuType > 2) {
            sysexSendList.add(configFull.configPos[input].getSysexFromConfig());
        } else {
            if (input > 2 && (input & 1) > 0) {
                byte[] sysex = configFull.configPos[input].getSysexFromConfig();
                sysex[4] = (byte) ((input - 3) / 2);
                if (configOptions.mcuType == 1 && ((input - 3) / 2) < 4) {
                    sysexSendList.add(sysex);
                }
                if (configOptions.mcuType == 2 && ((input - 3) / 2) < 8) {
                    sysexSendList.add(sysex);
                }
            }
        }
        sendSysex();
    }

    private void sendSysexPair(int pair) {
        sysexSendList.add(configFull.configPads[(pair * 2) - 1].getSysexFromConfig());
        sysexSendList.add(configFull.configPads[(pair * 2)].getSysexFromConfig());
        if (configOptions.mcuType > 2) {
            sysexSendList.add(configFull.configPos[(pair * 2) - 1].getSysexFromConfig());
            sysexSendList.add(configFull.configPos[(pair * 2)].getSysexFromConfig());
        } else {
            int input = padPair * 2 - 1;
            if (input > 2 && (input & 1) > 0) {
                byte[] sysex = configFull.configPos[(pair * 2) - 1].getSysexFromConfig();
                sysex[4] = (byte) ((input - 3) / 2);
                if (configOptions.mcuType == 1 && ((input - 3) / 2) < 4) {
                    sysexSendList.add(sysex);
                }
                if (configOptions.mcuType == 2 && ((input - 3) / 2) < 8) {
                    sysexSendList.add(sysex);
                }
            }
        }
        sysexSendList.add(configFull.config3rds[pair - 1].getSysexFromConfig());
        sendSysex();
    }

    private void controlsChangedInput(int input, boolean leftInput) {
        uiPad.setConfigFromControlsPad(configFull.configPads[input], leftInput);
        uiPad.setConfigPosFromControlsPad(configFull.configPos[input], leftInput);
        if (configOptions.liveUpdates) {
            sendSysexInput(input, leftInput);
        }
    }

    private void sendSysex3rd(int pair) {
        sysexSendList.add(configFull.config3rds[pair - 1].getSysexFromConfig());
        sendSysex();
    }

    private void controlsChanged3rd(int pair) {
        uiPad.setConfig3rdFromControlsPad(configFull.config3rds[pair - 1]);
        if (configOptions.liveUpdates) {
            sendSysex3rd(pair);
        }
    }

    private void sendSysexInputRequest(Integer input) {
        byte[] typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_PAD;
        typeAndId[1] = input.byteValue();
        sysexSendList.add(typeAndId);

        if (configOptions.mcuType > 2) {
            typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_POS;
            typeAndId[1] = input.byteValue();
            sysexSendList.add(typeAndId);
        } else if (input > 2 && (input & 1) > 0) {
            int in = (input - 3) / 2;
            if (((in < 4) && (configOptions.mcuType > 0)) || ((in < 8) && (configOptions.mcuType > 1))) {
                typeAndId = new byte[2];
                typeAndId[0] = Constants.MD_SYSEX_POS;
                typeAndId[1] = (byte) in;
                sysexSendList.add(typeAndId);
            }
        }
        sendSysex();
    }

    private void sendSysexPairRequest(int pair) {
        byte[] typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_PAD;
        typeAndId[1] = (byte) ((pair * 2) - 1);
        sysexSendList.add(typeAndId);

        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_PAD;
        typeAndId[1] = (byte) (pair * 2);
        sysexSendList.add(typeAndId);

        if (configOptions.mcuType > 2) {
            typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_POS;
            typeAndId[1] = (byte) ((pair * 2) - 1);
            sysexSendList.add(typeAndId);

            typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_POS;
            typeAndId[1] = (byte) (pair * 2);
            sysexSendList.add(typeAndId);
        } else {
            int input = (pair * 2) - 1;
            if (input > 2 && (input & 1) > 0) {
                int in = (input - 3) / 2;
                if (((in < 4) && (configOptions.mcuType > 0)) || ((in < 8) && (configOptions.mcuType > 1))) {
                    typeAndId = new byte[2];
                    typeAndId[0] = Constants.MD_SYSEX_POS;
                    typeAndId[1] = (byte) in;
                    sysexSendList.add(typeAndId);
                }
            }
        }

        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_3RD;
        typeAndId[1] = (byte) (pair - 1);
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void sendAllInputsSysex() {
        boolean doPos;
        for (byte i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
            sysexSendList.add(configFull.configPads[i].getSysexFromConfig());
            doPos = false;
            if (configOptions.mcuType > 2) {
                doPos = true;
            } else {
                if (configOptions.mcuType > 1) {
                    if (i < 8) {
                        doPos = true;
                    }
                } else if (i < 4) {
                    doPos = true;
                }
            }
            if (doPos) {
                sysexSendList.add(configFull.configPos[i].getSysexFromConfig());
            }
            if ((i & 1) > 0) {
                sysexSendList.add(configFull.config3rds[(i - 1) / 2].getSysexFromConfig());
            }
        }
        sendSysex();
    }

    private void sendAllInputsSysexRequests() {
        for (byte i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
            byte[] typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_PAD;
            typeAndId[1] = i;
            sysexSendList.add(typeAndId);

            if (configOptions.mcuType > 2) {
                typeAndId = new byte[2];
                typeAndId[0] = Constants.MD_SYSEX_POS;
                typeAndId[1] = i;
                sysexSendList.add(typeAndId);

            } else {
                if (i > 2 && (i & 1) > 0) {
                    int in = (i - 3) / 2;
                    if (((in < 4) && (configOptions.mcuType > 0)) || ((in < 8) && (configOptions.mcuType > 1))) {
                        typeAndId = new byte[2];
                        typeAndId[0] = Constants.MD_SYSEX_POS;
                        typeAndId[1] = (byte) in;
                        sysexSendList.add(typeAndId);
                    }
                }
            }

            if ((i & 1) > 0) {
                typeAndId = new byte[2];
                typeAndId[0] = Constants.MD_SYSEX_3RD;
                typeAndId[1] = (byte) ((i - 1) / 2);
                sysexSendList.add(typeAndId);
            }
        }
        sendSysex();
    }

    private void sendAllSysex() {
        boolean doPos;
        sysexSendList.add(configFull.configGlobalMisc.getSysexFromConfig());
        sysexSendList.add(configFull.configMisc.getSysexFromConfig());
        sysexSendList.add(configFull.configPedal.getSysexFromConfig(configOptions.mcuType));
        for (byte i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
            sysexSendList.add(configFull.configPads[i].getSysexFromConfig());
            doPos = false;
            if (configOptions.mcuType > 2) {
                doPos = true;
            } else {
                if (configOptions.mcuType > 1) {
                    if (i < 8) {
                        doPos = true;
                    }
                } else {
                    if (i < 4) {
                        doPos = true;
                    }
                }
            }
            if (doPos) {
                sysexSendList.add(configFull.configPos[i].getSysexFromConfig());
            }

            if ((i & 1) > 0) {
                sysexSendList.add(configFull.config3rds[(i - 1) / 2].getSysexFromConfig());
            }
        }
        for (byte i = 0; i < Constants.CURVES_COUNT; i++) {
            sysexSendList.add(configFull.configCurves[i].getSysexFromConfig());

        }
        for (byte i = 0; i < configFull.customNamesCount; i++) {
            sysexSendList.add(configFull.configCustomNames[i].getSysexFromConfig());

        }
        if (configFull.configGlobalMisc.config_names_en) {
            for (byte i = 0; i < configFull.configNamesCount; i++) {
                sysexSendList.add(configFull.configConfigNames[i].getSysexFromConfig());
            }
        }
        sendSysexReadOnlyRequestFlag = true;
        sendSysex();
    }

    private void sendAllSysexRequests() {
        byte[] typeAndId;
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_GLOBAL_MISC;
        sysexSendList.add(typeAndId);
        sendNextAllSysexRequestsFlag = true;
        sendSysex();
    }

    private void sendNextAllSysexRequests() {
        byte[] typeAndId;
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_MISC;
        sysexSendList.add(typeAndId);
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_PEDAL;
        sysexSendList.add(typeAndId);
        for (byte i = 0; i < (configFull.configGlobalMisc.inputs_count - 1); i++) {
            typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_PAD;
            typeAndId[1] = i;
            sysexSendList.add(typeAndId);

            if (configOptions.mcuType > 2) {
                typeAndId = new byte[2];
                typeAndId[0] = Constants.MD_SYSEX_POS;
                typeAndId[1] = i;
                sysexSendList.add(typeAndId);
            } else {
                if ((int) i > 2) {
                    if (((int) i & 1) > 0) {
                        int in = ((int) i - 3) / 2;
                        if (((in < 4) && (configOptions.mcuType > 0)) || ((in < 8) && (configOptions.mcuType > 1))) {
                            typeAndId = new byte[2];
                            typeAndId[0] = Constants.MD_SYSEX_POS;
                            typeAndId[1] = (byte) in;
                            sysexSendList.add(typeAndId);
                        }
                    }
                }
            }

            if ((i & 1) > 0) {
                typeAndId = new byte[2];
                typeAndId[0] = Constants.MD_SYSEX_3RD;
                typeAndId[1] = (byte) ((i - 1) / 2);
                sysexSendList.add(typeAndId);
            }
        }
        for (byte i = 0; i < Constants.CURVES_COUNT; i++) {
            typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_CURVE;
            typeAndId[1] = i;
            sysexSendList.add(typeAndId);
        }
        for (byte i = 0; i < configFull.customNamesCount; i++) {
            if (configOptions.mcuType < 2 && i > 15) {
                break;
            }
            typeAndId = new byte[2];
            typeAndId[0] = Constants.MD_SYSEX_CUSTOM_NAME;
            typeAndId[1] = i;
            sysexSendList.add(typeAndId);
        }
        if (configFull.configGlobalMisc.config_names_en) {
            for (byte i = 0; i < configFull.configNamesCount; i++) {
                typeAndId = new byte[2];
                typeAndId[0] = Constants.MD_SYSEX_CONFIG_NAME;
                typeAndId[1] = i;
                sysexSendList.add(typeAndId);
            }
        }
        typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_CONFIG_CURRENT;
        sysexSendList.add(typeAndId);
        sendSysex();
    }

    private void showUpgradeWindow() {
        upgradeWindow.show();
        switch (upgradeWindow.getUpgradeResult()) {
            case UIUpgrade.UPGRADE_SUCCESS:
                openMidiPorts(true);
                break;
            case UIUpgrade.UPGRADE_FAILED:
                openMidiPorts(false);
                break;
            case UIUpgrade.UPGRADE_NOT_PERFORMED:
            default:
                break;
        }
    }

    private void showOptionsWindow() {
        optionsUpdatePorts();
        optionsWindow.show();
        if (optionsWindow.getClosedWithOk()) {
            openMidiPorts(true);
            setShowAdvanced();
        }
    }

    private void setShowAdvanced() {
        for (UIPanel allPanel : allPanels) {
            allPanel.setShowAdvanced(configOptions.showAdvancedSettings);
            if (allPanel.isDetached()) {
                allPanel.getWindow().setHeight(allPanel.getLastControlH() * allPanel.getVerticalControlsCount());
            } else {
                respondToResize(scene1);
            }
        }
    }

    private void setAllStatesUnknown() {
        uiGlobalMisc.setAllStatesUnknown();
        uiMisc.setAllStateUnknown();
        uiPedal.setAllStateUnknown();
        for (int i = 0; i < Constants.MAX_INPUTS; i++) {
            moduleConfigFull.configPads[i].sysexReceived = false;
            moduleConfigFull.configPos[i].sysexReceived = false;
            if ((i & 1) > 0) {
                moduleConfigFull.config3rds[(i - 1) / 2].sysexReceived = false;
            }
        }
        uiPad.setAllStatesUnknown(false, false, false);
        uiPadsExtra.setCurveSysexReceived(false);
        uiPadsExtra.testCurveSyncState();
        for (int i = 0; i < Constants.CURVES_COUNT; i++) {
            moduleConfigFull.configCurves[i].sysexReceived = false;
        }
        uiPadsExtra.setAllCustomNamesStatesUnknown();
    }

    private void openMidiPorts(boolean toOpen) {
        boolean clearMidiLabelStatus = true;
        if (toOpen) {
            if (midiController.isMidiOpen()) {
                midiController.closeAllPorts();
            }
            if (configOptions.useThruPort) {
                midiController.openMidi(configOptions.MidiInName, configOptions.MidiOutName, configOptions.MidiThruName);
            } else {
                midiController.openMidi(configOptions.MidiInName, configOptions.MidiOutName, "");
            }
            if (!midiController.isMidiOpen()) {
                uiGlobal.setSysexStatusLabel(Constants.MD_SYSEX_STATUS_MIDI_INIT_ERROR, 0);
                clearMidiLabelStatus = false;
            }
        } else {
            midiController.closeAllPorts();
            configOptions.mcuType = 0;
            configOptions.version = 0;
        }
        if (midiController.isMidiOpen()) {
            uiGlobalMisc.getToggleButtonMidi().setSelected(true);
            uiGlobalMisc.getToggleButtonMidi().setText("Close MIDI");
            sysexThreadsStarted = 0;
            sendSysexReadOnlyRequest();
        } else {
            uiGlobalMisc.getToggleButtonMidi().setSelected(false);
            uiGlobalMisc.getToggleButtonMidi().setText("Open MIDI");
            uiGlobalMisc.setAllStatesUnknown();
            if (clearMidiLabelStatus) {
                uiGlobal.clearSysexStatusLabel();
            }
            configOptions.mcuType = 0;
            setAllStatesUnknown();
        }
    }

    private void initMidi() {
        midiController = new MidiController();
        sysexSendList = new ArrayList<>();
        sysexLastChanged = new ArrayList<>();
        midiController.addMidiEventListener(_ -> {
            byte[] buffer = new byte[midiController.getMidiDataList().getFirst().length];
            System.arraycopy(midiController.getMidiDataList().getFirst(), 0, buffer, 0, buffer.length);
            midiController.getMidiDataList().removeFirst();
            Platform.runLater(() -> {
                if (buffer.length > 3) {
                    processSysex(buffer);
                } else {
                    processShortMidi(buffer);
                }
            });
        });
    }

    private int getNoteOnBarType(int note) {
        for (int i = 0; i < configFull.configPads.length; i++) {
            ConfigPad check = configFull.configPads[i];
            if (check.disabled) {
                continue;
            }
            if ((note == check.note) || (note == check.altNote) || (note == check.pressrollNote)) {
                return ((i == 0) || ((i & 1) == 1)) ? MidiLevelBar.barTypeHead : MidiLevelBar.barTypeRim;
            }
        }
        for (int i = 0; i < configFull.config3rds.length; i++) {
            Config3rd check = configFull.config3rds[i];
            ConfigPad checkPad = configFull.configPads[2 * i + 1];
            if ((!checkPad.dual && !checkPad.threeWay) || check.disabled) {
                continue;
            }
            if ((note == check.note) || (note == check.altNote) || (note == check.pressrollNote) || (note == check.dampenedNote)) {
                return MidiLevelBar.barType3rd;
            }
        }
        ConfigPedal check = configFull.configPedal;
        if ((note == check.bowClosedNote) || (note == check.bowSemiClosedNote) || (note == check.bowHalfOpenNote) || (note == check.bowHalfOpen2Note) || (note == check.bowSemiOpenNote) || (note == check.bowSemiOpen2Note)) {
            return MidiLevelBar.barTypeHead;
        }
        if ((note == check.edgeClosedNote) || (note == check.edgeSemiClosedNote) || (note == check.edgeHalfOpenNote) || (note == check.edgeHalfOpen2Note) || (note == check.edgeSemiOpenNote) || (note == check.edgeSemiOpen2Note)) {
            return MidiLevelBar.barTypeRim;
        }
        if ((note == check.bellClosedNote) || (note == check.bellSemiClosedNote) || (note == check.bellHalfOpenNote) || (note == check.bellHalfOpen2Note) || (note == check.bellSemiOpenNote) || (note == check.bellSemiOpen2Note)) {
            return MidiLevelBar.barType3rd;
        }

        return MidiLevelBar.barTypeUnknown;
    }

    private void processShortMidi(byte[] buffer) {
        switch (buffer.length) {
            case 1, 2:
                break;
            default:
                uiMidiLog.addRawMidi(buffer);
                int type = (buffer[0] & 0xf0);
                if ((type == 0x90) && (buffer[2] > 0)) {
                    int note = buffer[1];
                    int velocity = buffer[2];
                    int barType = getNoteOnBarType(note);
                    uiMidiLog.addNewMidiData(barType, note, velocity);
                }
                if (type == 0xa0) {
                    uiMidiLog.addNewMidiData((buffer[2] > 0) ? MidiLevelBar.barTypeChokeOn : MidiLevelBar.barTypeChokeOff, buffer[1], buffer[2]);
                }
                if ((type == 0xb0) && (buffer[1] == 0x04)) {
                    uiMidiLog.setHiHatLevel(127 - buffer[2]);
                }
                if ((type == 0xb0) && (buffer[1] == 0x10)) {
                    uiMidiLog.addNewPositional((int) buffer[2]);
                }
                if ((type == 0xb0) && (buffer[1] == 0x13)) {
                    int id = buffer[2];
                    if (id > 0x3f) {
                        id = (id - 0x40) * 2 + 1;
                    } else {
                        id--;
                    }
                    if (id > 0) {
                        id = (id - 1) / 2 + 1;
                    } else {
                        id = 0;
                    }
                    if (configFull.configMisc.send_triggered_in) {
                        switchToSelectedPair(id);
                    }
                }
                break;
        }
    }

    private void processSysex(byte[] sysex) {
        if (sysex.length >= 5) {
            byte pointer = sysex[4];
            switch (sysex[3]) {
                case Constants.MD_SYSEX_3RD:
                    if (pointer < configFull.config3rds.length) {
                        configFull.config3rds[pointer].setConfigFromSysex(sysex);
                        moduleConfigFull.config3rds[pointer].setConfigFromSysex(sysex);
                        moduleConfigFull.config3rds[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
                        moduleConfigFull.config3rds[pointer].sysexReceived = true;
                        if ((pointer + 1) == padPair) {
                            uiPad.setControlsFromConfig3rd(configFull.config3rds[pointer], true);
                        }
                    }
                    break;
                case Constants.MD_SYSEX_CONFIG_COUNT:
                    if (sysex.length >= Constants.MD_SYSEX_CONFIG_COUNT_SIZE) {
                        int b = sysex[4];
                        uiGlobalMisc.setConfigsCount(b);
                        configFull.configNamesCount = b;
                        configFull.configCountSysexReceived = true;
                        reCreateSlotsMenuItems();
                    }
                    break;
                case Constants.MD_SYSEX_CONFIG_CURRENT:
                    if (sysex.length >= Constants.MD_SYSEX_CONFIG_CURRENT_SIZE) {
                        int b = sysex[4];
                        uiGlobalMisc.setConfigCurrent(b);
                        configFull.configCurrentSysexReceived = true;
                        uiGlobalMisc.getTextFieldSlotName().setText(configFull.configConfigNames[pointer].name.trim());
                    }
                    break;
                case Constants.MD_SYSEX_CONFIG_NAME:
                    configFull.configConfigNames[pointer].setConfigFromSysex(sysex);
                    moduleConfigFull.configConfigNames[pointer].setConfigFromSysex(sysex);
                    configFull.configConfigNames[pointer].sysexReceived = true;
                    reCreateSlotsMenuItems();
                    break;
                case Constants.MD_SYSEX_CURVE:
                    configFull.configCurves[pointer].setConfigFromSysex(sysex);
                    moduleConfigFull.configCurves[pointer].setConfigFromSysex(sysex);
                    moduleConfigFull.configCurves[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
                    moduleConfigFull.configCurves[pointer].sysexReceived = true;
                    if (pointer == curvePointer) {
                        uiPadsExtra.setYvalues(configFull.configCurves[pointer].yValues, true);
                    }
                    break;
                case Constants.MD_SYSEX_CUSTOM_NAME:
                    configFull.configCustomNames[pointer].setConfigFromSysex(sysex);
                    moduleConfigFull.configCustomNames[pointer].setConfigFromSysex(sysex);
                    moduleConfigFull.configCustomNames[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
                    moduleConfigFull.configCustomNames[pointer].sysexReceived = true;
                    uiPadsExtra.setCustomName(configFull.configCustomNames[pointer], pointer, true);
                    addCustomNamesToPads();
                    break;
                case Constants.MD_SYSEX_GLOBAL_MISC:
                    configFull.configGlobalMisc.setConfigFromSysex(sysex);
                    moduleConfigFull.configGlobalMisc.setConfigFromSysex(sysex);
                    moduleConfigFull.configGlobalMisc.syncState = Constants.SYNC_STATE_RECEIVED;
                    moduleConfigFull.configGlobalMisc.sysexReceived = true;
                    uiGlobalMisc.setControlsFromConfig(configFull.configGlobalMisc, true);
                    updateComboBoxInput(false);
                    break;
                case Constants.MD_SYSEX_MCU_TYPE:
                    if (sysex.length >= Constants.MD_SYSEX_MCU_TYPE_SIZE) {
                        configOptions.mcuType = (sysex[4] << 4);
                        configOptions.mcuType |= sysex[5];
                        if (configOptions.mcuType < Constants.MCU_TYPES.length) {
                            uiGlobalMisc.setMcu(configOptions.mcuType);
                        }
                        uiMisc.setUnknownLabel(configOptions.mcuType);
                    }
                    break;
                case Constants.MD_SYSEX_MISC:
                    configFull.configMisc.setConfigFromSysex(sysex);
                    moduleConfigFull.configMisc.setConfigFromSysex(sysex);
                    moduleConfigFull.configMisc.syncState = Constants.SYNC_STATE_RECEIVED;
                    moduleConfigFull.configMisc.sysexReceived = true;
                    uiMisc.setControlsFromConfig(configFull.configMisc, true);
                    break;
                case Constants.MD_SYSEX_PAD:
                    configFull.configPads[pointer - 1].setConfigFromSysex(sysex);
                    moduleConfigFull.configPads[pointer - 1].setConfigFromSysex(sysex);
                    moduleConfigFull.configPads[pointer - 1].syncState = Constants.SYNC_STATE_RECEIVED;
                    moduleConfigFull.configPads[pointer - 1].sysexReceived = true;
                    if ((pointer - 1) == 0) {
                        if (padPair == 0) {
                            uiPad.setControlsFromConfigPad(configFull.configPads[pointer - 1], true, true);
                        }
                    } else {
                        if ((((pointer - 2) / 2) + 1) == padPair) {
                            uiPad.setControlsFromConfigPad(configFull.configPads[pointer - 1], (pointer & 1) == 0, true);
                        }
                    }
                    break;
                case Constants.MD_SYSEX_PEDAL:
                    configFull.configPedal.setConfigFromSysex(sysex, configOptions.mcuType);
                    moduleConfigFull.configPedal.setConfigFromSysex(sysex, configOptions.mcuType);
                    moduleConfigFull.configPedal.syncState = Constants.SYNC_STATE_RECEIVED;
                    moduleConfigFull.configPedal.sysexReceived = true;
                    uiPedal.setControlsFromConfig(configFull.configPedal, true);
                    break;
                case Constants.MD_SYSEX_POS:
                    if (configOptions.mcuType < 3) {
                        pointer = (byte) (pointer * 2 + 3);
                    }
                    configFull.configPos[pointer].setConfigFromSysex(sysex);
                    moduleConfigFull.configPos[pointer].setConfigFromSysex(sysex);
                    moduleConfigFull.configPos[pointer].syncState = Constants.SYNC_STATE_RECEIVED;
                    moduleConfigFull.configPos[pointer].sysexReceived = true;
                    if (pointer == 0) {
                        if (padPair == 0) {
                            uiPad.setControlsFromConfigPos(configFull.configPos[pointer], true, true);
                        }
                    } else {
                        if ((((pointer - 1) / 2) + 1) == padPair) {
                            uiPad.setControlsFromConfigPos(configFull.configPos[pointer], ((pointer + 1) & 1) == 0, true);
                        }
                    }
                    break;
                case Constants.MD_SYSEX_VERSION:
                    int ver = 0;
                    if (sysex.length >= Constants.MD_SYSEX_VERSION_SIZE) {
                        int b;
                        for (int i = 0; i < 4; i++) {
                            b = (sysex[i * 2 + 4] << 4);
                            b |= sysex[i * 2 + 5];
                            ver += b << (8 * i);
                        }
                        configOptions.version = ver;
                        if (ver < Constants.MD_MINIMUM_VERSION) {
                            uiGlobalMisc.setVersion(ver, "salmon");
                            if (!versionWarningAlreadyShown) {
                                versionWarningAlreadyShown = true;
                                Alert alert = new Alert(AlertType.WARNING);
                                alert.setHeaderText("Firmware version is too old!");
                                WebView webView = new WebView();
                                webView.getEngine().loadContent(Constants.WARNING_VERSION);
                                webView.setPrefSize(300, 120);
                                alert.getDialogPane().setContent(webView);

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        Platform.runLater(alert::showAndWait);
                                    }
                                }, 200);
                            }
                        } else {
                            uiGlobalMisc.setVersion(ver, "lightgreen");
                        }
                    }
                    break;
                case Constants.MD_SYSEX_PEDAL_LEVEL_RAW:
                    byte[] sysex_short = new byte[4];
                    if (sysex.length >= Constants.MD_SYSEX_PEDAL_LEVEL_RAW_SIZE) {
                        sysex_short[0] = sysex[4];
                        sysex_short[1] = sysex[5];
                        sysex_short[2] = sysex[6];
                        sysex_short[3] = sysex[7];
                        int pedal_level_raw = Utils.sysex2short(sysex_short);

                        if (setPedalLevel == 1) {
                            configFull.configPedal.lowLevel = pedal_level_raw;
                        } else if (setPedalLevel == 2) {
                            configFull.configPedal.highLevel = pedal_level_raw;
                        }
                        uiPedal.setControlsFromConfig(configFull.configPedal, false);
                    }
                    setPedalLevel = 0;
                    break;
                default:
                    break;
            }
        }
    }

    private void initConfigs() {
        fullConfigs = new ConfigFull[Constants.CONFIGS_COUNT];
        String[] configFileNames = new String[Constants.CONFIGS_COUNT];
        for (int i = 0; i < Constants.CONFIGS_COUNT; i++) {
            fullConfigs[i] = new ConfigFull();
            configFileNames[i] = "";
        }
        configOptions = new ConfigOptions();
        configFull = new ConfigFull();
        moduleConfigFull = new ConfigFull();
    }

    private void optionsUpdatePorts() {
        optionsWindow.setMidiInList(Arrays.asList(midiController.getMidiInList()));
        optionsWindow.setMidiOutList(Arrays.asList(midiController.getMidiOutList()));
        optionsWindow.setMidiThruList(Arrays.asList(midiController.getMidiOutList()));
        optionsWindow.updateControls();
    }

    @Override
    public void midiRescanEventOccurred(MidiRescanEvent evt) {
        optionsUpdatePorts();
    }

    private String getInputName(int input) {
        String result = Integer.valueOf(input + 1).toString() + " ";
        int totalCustomNames = Constants.CUSTOM_PADS_NAMES_LIST.length;
        int namePointer = configFull.configPads[input].name;
        if (namePointer == 0) {
            result = result + Constants.PADS_NAMES_LIST[input];
        } else {
            if (namePointer < (totalCustomNames + 1)) {
                result = result + (Constants.CUSTOM_PADS_NAMES_LIST[namePointer - 1]);
            } else {
                result = result + configFull.configCustomNames[namePointer - totalCustomNames - 1].name;
            }
            if ((input & 1) > 0) {
                result += "h";
            } else {
                result += "r";
            }
        }
        return result;
    }

    private void copyPad(int index) {
        int src, dst;
        if (padPair == 0) {
            src = 0;
        } else {
            src = (padPair * 2) - 1;
        }
        if (index == 0) {
            for (int i = 1; i < (configFull.configGlobalMisc.inputs_count / 2); i++) {
                dst = (i * 2) - 1;
                configFull.configPads[dst].setConfigFromSysex(configFull.configPads[src].getSysexFromConfig());
                configFull.configPos[dst].setConfigFromSysex(configFull.configPos[src].getSysexFromConfig());
                if (src != 0) {
                    configFull.configPads[dst + 1].setConfigFromSysex(configFull.configPads[src + 1].getSysexFromConfig());
                    configFull.configPos[dst + 1].setConfigFromSysex(configFull.configPos[src + 1].getSysexFromConfig());
                    configFull.config3rds[i - 1].setConfigFromSysex(configFull.config3rds[padPair - 1].getSysexFromConfig());
                }
            }
        } else {
            dst = index * 2 - 1;
            configFull.configPads[dst].setConfigFromSysex(configFull.configPads[src].getSysexFromConfig());
            configFull.configPos[dst].setConfigFromSysex(configFull.configPos[src].getSysexFromConfig());
            if (src != 0) {
                configFull.configPads[dst + 1].setConfigFromSysex(configFull.configPads[src + 1].getSysexFromConfig());
                configFull.configPos[dst + 1].setConfigFromSysex(configFull.configPos[src + 1].getSysexFromConfig());
                configFull.config3rds[index - 1].setConfigFromSysex(configFull.config3rds[padPair - 1].getSysexFromConfig());
            }
        }
    }

    private void copy3rd(int index) {
        int src, dst;
        if (padPair > 0) {
            src = padPair - 1;
            if (index == 0) {
                for (int i = 1; i < (configFull.configGlobalMisc.inputs_count / 2); i++) {
                    dst = i - 1;
                    configFull.config3rds[dst].setConfigFromSysex(configFull.config3rds[src].getSysexFromConfig());
                }
            } else {
                dst = index - 1;
                configFull.config3rds[dst].setConfigFromSysex(configFull.config3rds[src].getSysexFromConfig());
            }
        }
    }

    private void copyHead(int index) {
        int src, dst;
        if (padPair == 0) {
            src = 0;
        } else {
            src = (padPair * 2) - 1;
        }
        if (index == 0) {
            for (int i = 1; i < (configFull.configGlobalMisc.inputs_count / 2); i++) {
                dst = (i * 2) - 1;
                configFull.configPads[dst].setConfigFromSysex(configFull.configPads[src].getSysexFromConfig());
                configFull.configPos[dst].setConfigFromSysex(configFull.configPos[src].getSysexFromConfig());
            }
        } else {
            dst = index * 2 - 1;
            configFull.configPads[dst].setConfigFromSysex(configFull.configPads[src].getSysexFromConfig());
            configFull.configPos[dst].setConfigFromSysex(configFull.configPos[src].getSysexFromConfig());
        }
    }

    private void copyRim(int index) {
        int src, dst;
        if (padPair == 0) {
            src = 0;
        } else {
            src = (padPair * 2) - 1;
        }
        if (index == 0) {
            for (int i = 1; i < (configFull.configGlobalMisc.inputs_count / 2); i++) {
                dst = (i * 2) - 1;
                if (src != 0) {
                    configFull.configPads[dst + 1].setConfigFromSysex(configFull.configPads[src + 1].getSysexFromConfig());
                    configFull.configPos[dst + 1].setConfigFromSysex(configFull.configPos[src + 1].getSysexFromConfig());
                }
            }
        } else {
            dst = index * 2 - 1;
            if (src != 0) {
                configFull.configPads[dst + 1].setConfigFromSysex(configFull.configPads[src + 1].getSysexFromConfig());
                configFull.configPos[dst + 1].setConfigFromSysex(configFull.configPos[src + 1].getSysexFromConfig());
            }
        }
    }

    private void updateComboBoxInput(boolean nameChanged) {
        MenuItem menuItem;
        if ((oldInputsCounts != configFull.configGlobalMisc.inputs_count) || nameChanged) {
            menuItemsCopyPad.clear();
            menuItemsCopyPad.add(new MenuItem("To All Pads"));
            menuItemsCopyPad.getFirst().setOnAction(_ -> copyPad(0));
            menuItemsCopyHead.clear();
            menuItemsCopyHead.add(new MenuItem("To All Heads"));
            menuItemsCopyHead.getFirst().setOnAction(_ -> copyHead(0));
            menuItemsCopyRim.clear();
            menuItemsCopyRim.add(new MenuItem("To All Rims"));
            menuItemsCopyRim.getFirst().setOnAction(_ -> copyRim(0));
            menuItemsCopy3rd.clear();
            menuItemsCopy3rd.add(new MenuItem("To All 3rds"));
            menuItemsCopy3rd.getFirst().setOnAction(_ -> copy3rd(0));
            oldInputsCounts = configFull.configGlobalMisc.inputs_count;
            List<String> list;
            String name;
            list = new ArrayList<>();
            int inputPointer;
            for (int i = 0; i < ((configFull.configGlobalMisc.inputs_count / 2)); i++) {
                final int iFinal = i;
                if (i == 0) {
                    list.add(getInputName(i));
                    menuItem = new MenuItem("To " + getInputName(i));
                    menuItemsCopyHead.add(menuItem);
                } else {
                    inputPointer = (i * 2) - 1;
                    name = getInputName(inputPointer);
                    menuItem = new MenuItem("To " + getInputName(inputPointer));
                    menuItem.setOnAction(_ -> copyHead(iFinal));
                    menuItemsCopyHead.add(menuItem);
                    name = name + "/";
                    inputPointer++;
                    name = name + getInputName(inputPointer);
                    menuItem = new MenuItem("To " + getInputName(inputPointer));
                    menuItem.setOnAction(_ -> copyRim(iFinal));
                    menuItemsCopyRim.add(menuItem);
                    list.add(name);
                    menuItem = new MenuItem("To " + name);
                    menuItem.setOnAction(_ -> copyPad(iFinal));
                    menuItemsCopyPad.add(menuItem);
                    menuItem = new MenuItem("To " + name);
                    menuItem.setOnAction(_ -> copy3rd(iFinal));
                    menuItemsCopy3rd.add(menuItem);
                }
            }
            uiPad.getComboBoxInput().getItems().clear();
            uiPad.getComboBoxInput().getItems().addAll(list);

            menuCopyPad.getItems().clear();
            menuCopyPad.getItems().addAll(menuItemsCopyPad);
            menuCopyHead.getItems().clear();
            menuCopyHead.getItems().addAll(menuItemsCopyHead);
            menuCopyRim.getItems().clear();
            menuCopyRim.getItems().addAll(menuItemsCopyRim);
            menuCopy3rd.getItems().clear();
            menuCopy3rd.getItems().addAll(menuItemsCopy3rd);

            if ((configFull.configGlobalMisc.inputs_count) / 2 > padPair) {
                uiPad.getComboBoxInput().getSelectionModel().select(padPair);
            } else {
                switchToSelectedPair(0);
            }
        }
    }

    private void switchToSelectedPair(int newPadPair) {
        menuCopyPad.setDisable(newPadPair == 0);
        menuCopyRim.setDisable(newPadPair == 0);
        menuCopy3rd.setDisable(newPadPair == 0);
        if (padPair != newPadPair) {
            if (padPair == 0) {
                uiPad.setConfigFromControlsPad(configFull.configPads[0], true);
                uiPad.setConfigPosFromControlsPad(configFull.configPos[0], true);
            } else {
                uiPad.setConfigFromControlsPad(configFull.configPads[((padPair - 1) * 2) + 1], true);
                uiPad.setConfigPosFromControlsPad(configFull.configPos[((padPair - 1) * 2) + 1], true);
                uiPad.setConfigFromControlsPad(configFull.configPads[((padPair - 1) * 2) + 2], false);
                uiPad.setConfigPosFromControlsPad(configFull.configPos[((padPair - 1) * 2) + 2], false);
                uiPad.setConfig3rdFromControlsPad(configFull.config3rds[padPair - 1]);
            }
            padPair = newPadPair;
        }
        if (padPair == 0) {
            uiPad.setAllStatesUnknown(moduleConfigFull.configPads[0].sysexReceived, false, false);
            if (moduleConfigFull.configPads[0].sysexReceived) {
                uiPad.setMdValuesPad(moduleConfigFull.configPads[0], configFull.configPos[0], true);
            }
            uiPad.setInputPair(padPair, configFull.configPads[0], configFull.configPos[0], null, null, null);
        } else {
            uiPad.setAllStatesUnknown(moduleConfigFull.configPads[((padPair - 1) * 2) + 1].sysexReceived, moduleConfigFull.configPads[((padPair - 1) * 2) + 2].sysexReceived, moduleConfigFull.config3rds[padPair - 1].sysexReceived);
            if (moduleConfigFull.configPads[((padPair - 1) * 2) + 1].sysexReceived) {
                uiPad.setMdValuesPad(moduleConfigFull.configPads[((padPair - 1) * 2) + 1], configFull.configPos[((padPair - 1) * 2) + 1], true);
            }
            if (moduleConfigFull.configPads[((padPair - 1) * 2) + 2].sysexReceived) {
                uiPad.setMdValuesPad(moduleConfigFull.configPads[((padPair - 1) * 2) + 2], configFull.configPos[((padPair - 1) * 2) + 2], false);
            }
            if (moduleConfigFull.config3rds[padPair - 1].sysexReceived) {
                uiPad.setMdValues3rd(moduleConfigFull.config3rds[padPair - 1]);
            }
            uiPad.setInputPair(padPair, configFull.configPads[((padPair - 1) * 2) + 1], configFull.configPos[((padPair - 1) * 2) + 1], configFull.configPads[((padPair - 1) * 2) + 2], configFull.configPos[((padPair - 1) * 2) + 2], configFull.config3rds[padPair - 1]);
        }
        uiPad.getComboBoxInput().getSelectionModel().select(padPair);
    }

    private void switchToSelectedCurve(int curve) {
        if ((curve > -1) && (curve < Constants.CURVES_COUNT)) {
            curvePointer = curve;
            if (moduleConfigFull.configCurves[curvePointer].sysexReceived) {
                uiPadsExtra.setMdYvalues(moduleConfigFull.configCurves[curvePointer].yValues);
                uiPadsExtra.setCurveSysexReceived(true);
            } else {
                uiPadsExtra.setCurveSysexReceived(false);
            }
            uiPadsExtra.getCurvesComboBox().getSelectionModel().select(curvePointer);
            uiPadsExtra.setYvalues(configFull.configCurves[curvePointer].yValues, false);
            uiPadsExtra.testCurveSyncState();
        }
    }

    private void loadAllFromConfigFull() {

        uiGlobal.setFileLoadStatus(configOptions.configLoaded[configOptions.lastConfig]);

        configFull.configGlobalMisc.setConfigFromSysex(fullConfigs[configOptions.lastConfig].configGlobalMisc.getSysexFromConfig());
        uiGlobalMisc.setControlsFromConfig(configFull.configGlobalMisc, false);

        configFull.configMisc.setConfigFromSysex(fullConfigs[configOptions.lastConfig].configMisc.getSysexFromConfig());
        uiMisc.setControlsFromConfig(configFull.configMisc, false);

        configFull.configPedal.setConfigFromSysex(fullConfigs[configOptions.lastConfig].configPedal.getSysexFromConfig(configOptions.mcuType), configOptions.mcuType);
        uiPedal.setControlsFromConfig(configFull.configPedal, false);

        for (int i = 0; i < (Constants.MAX_INPUTS - 1); i++) {
            configFull.configPads[i].setConfigFromSysex(fullConfigs[configOptions.lastConfig].configPads[i].getSysexFromConfig());
            configFull.configPads[i].altNote_linked = fullConfigs[configOptions.lastConfig].configPads[i].altNote_linked;
            configFull.configPads[i].pressrollNote_linked = fullConfigs[configOptions.lastConfig].configPads[i].pressrollNote_linked;
            configFull.configPos[i].setConfigFromSysex(fullConfigs[configOptions.lastConfig].configPos[i].getSysexFromConfig());
        }
        for (int i = 0; i < ((Constants.MAX_INPUTS / 2) - 1); i++) {
            configFull.config3rds[i].setConfigFromSysex(fullConfigs[configOptions.lastConfig].config3rds[i].getSysexFromConfig());
            configFull.config3rds[i].altNote_linked = fullConfigs[configOptions.lastConfig].config3rds[i].altNote_linked;
            configFull.config3rds[i].pressrollNote_linked = fullConfigs[configOptions.lastConfig].config3rds[i].pressrollNote_linked;
        }

        for (int i = 0; i < (Constants.CURVES_COUNT); i++) {
            configFull.configCurves[i].setConfigFromSysex(fullConfigs[configOptions.lastConfig].configCurves[i].getSysexFromConfig());
            if (i == curvePointer) {
                uiPadsExtra.setYvalues(configFull.configCurves[curvePointer].yValues, false);
            }
        }
        for (int i = 0; i < (Constants.CUSTOM_NAMES_MAX); i++) {
            configFull.configCustomNames[i].setConfigFromSysex(fullConfigs[configOptions.lastConfig].configCustomNames[i].getSysexFromConfig());
            uiPadsExtra.setCustomName(configFull.configCustomNames[i], i, false);
        }
        configFull.customNamesCount = fullConfigs[configOptions.lastConfig].customNamesCount;
        setCustomNamesCountControl();
        addCustomNamesToPads();
        switchToSelectedPair(padPair);
    }

    private void load_all() {
        configOptions.lastConfig = uiGlobal.getComboBoxFile().getSelectionModel().getSelectedIndex();
        if (fileManager.load_all(fullConfigs[configOptions.lastConfig], configOptions)) {
            uiGlobal.getComboBoxFile().getItems().clear();
            uiGlobal.getComboBoxFile().getItems().addAll(configOptions.configFileNames);
            uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig);
            loadAllFromConfigFull();
        }
    }

    private void loadSysexMisc() {
        byte[] sysex = configFull.configMisc.getSysexFromConfig();
        fileManager.loadSysex(sysex, configOptions);
        configFull.configMisc.setConfigFromSysex(sysex);
        uiMisc.setControlsFromConfig(configFull.configMisc, false);
    }

    private void saveSysexMisc() {
        fileManager.saveSysex(configFull.configMisc.getSysexFromConfig(), configOptions);
    }

    private void loadSysexPedal() {
        byte[] sysex = configFull.configPedal.getSysexFromConfig(configOptions.mcuType);
        fileManager.loadSysex(sysex, configOptions);
        configFull.configPedal.setConfigFromSysex(sysex, configOptions.mcuType);
        uiPedal.setControlsFromConfig(configFull.configPedal, false);
    }

    private void saveSysexPedal() {
        fileManager.saveSysex(configFull.configPedal.getSysexFromConfig(configOptions.mcuType), configOptions);
    }

    private void setPedalLow() {
        byte[] typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_PEDAL_LEVEL_RAW;
        sysexSendList.add(typeAndId);
        setPedalLevel = 1;
        sendSysex();
    }

    private void setPedalHigh() {
        byte[] typeAndId = new byte[2];
        typeAndId[0] = Constants.MD_SYSEX_PEDAL_LEVEL_RAW;
        sysexSendList.add(typeAndId);
        setPedalLevel = 2;
        sendSysex();
    }

    private void loadSysexPad() {
        byte[] sysex;
        byte[] sysex3rd;
        byte[] sysexPos;
        if (padPair > 0) {
            int leftInput = (padPair - 1) * 2 + 1;
            int rightInput = leftInput + 1;
            byte[] sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE * 2];
            sysex = configFull.configPads[leftInput].getSysexFromConfig();
            System.arraycopy(sysex, 0, sysexPad, 0, sysex.length);
            sysex = configFull.configPads[rightInput].getSysexFromConfig();
            System.arraycopy(sysex, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE, sysex.length);
            sysex3rd = configFull.config3rds[padPair - 1].getSysexFromConfig();
            System.arraycopy(sysex3rd, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2, sysex3rd.length);
            sysexPos = configFull.configPos[leftInput].getSysexFromConfig();
            System.arraycopy(sysexPos, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE, sysexPos.length);
            sysexPos = configFull.configPos[rightInput].getSysexFromConfig();
            System.arraycopy(sysexPos, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE, sysexPos.length);
            fileManager.loadSysex(sysexPad, configOptions);
            System.arraycopy(sysexPad, 0, sysex, 0, sysex.length);
            configFull.configPads[leftInput].setConfigFromSysex(sysex);
            System.arraycopy(sysexPad, Constants.MD_SYSEX_PAD_SIZE, sysex, 0, sysex.length);
            configFull.configPads[rightInput].setConfigFromSysex(sysex);
            System.arraycopy(sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2, sysex3rd, 0, sysex3rd.length);
            configFull.config3rds[padPair - 1].setConfigFromSysex(sysex3rd);
            System.arraycopy(sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE, sysexPos, 0, sysexPos.length);
            configFull.configPos[leftInput].setConfigFromSysex(sysexPos);
            System.arraycopy(sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE, sysexPos, 0, sysexPos.length);
            configFull.configPos[rightInput].setConfigFromSysex(sysexPos);
            uiPad.setControlsFromConfigPad(configFull.configPads[leftInput], true, false);
            uiPad.setControlsFromConfigPad(configFull.configPads[rightInput], false, false);
            uiPad.setControlsFromConfigPos(configFull.configPos[leftInput], true, false);
            uiPad.setControlsFromConfigPos(configFull.configPos[rightInput], false, false);
            uiPad.setControlsFromConfig3rd(configFull.config3rds[padPair - 1], false);
        } else {
            byte[] sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE + Constants.MD_SYSEX_POS_SIZE];
            sysex = configFull.configPads[0].getSysexFromConfig();
            System.arraycopy(sysex, 0, sysexPad, 0, sysex.length);
            sysexPos = configFull.configPos[0].getSysexFromConfig();
            System.arraycopy(sysexPos, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE, sysexPos.length);
            fileManager.loadSysex(sysexPad, configOptions);
            System.arraycopy(sysexPad, 0, sysex, 0, sysex.length);
            configFull.configPads[0].setConfigFromSysex(sysex);
            System.arraycopy(sysexPad, Constants.MD_SYSEX_PAD_SIZE, sysexPos, 0, sysexPos.length);
            configFull.configPos[0].setConfigFromSysex(sysexPos);
            uiPad.setControlsFromConfigPad(configFull.configPads[0], true, false);
            uiPad.setControlsFromConfigPos(configFull.configPos[0], true, false);
        }
    }

    private void saveSysexPad() {
        byte[] sysex;
        byte[] sysex3rd;
        byte[] sysexPos;
        byte[] sysexPad;
        if (padPair > 0) {
            int leftInput = (padPair - 1) * 2 + 1;
            int rightInput = leftInput + 1;
            sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE * 2];
            sysex = configFull.configPads[leftInput].getSysexFromConfig();
            System.arraycopy(sysex, 0, sysexPad, 0, sysex.length);
            sysex = configFull.configPads[rightInput].getSysexFromConfig();
            System.arraycopy(sysex, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE, sysex.length);
            sysex3rd = configFull.config3rds[padPair - 1].getSysexFromConfig();
            System.arraycopy(sysex3rd, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2, sysex3rd.length);
            sysexPos = configFull.configPos[leftInput].getSysexFromConfig();
            System.arraycopy(sysexPos, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE, sysexPos.length);
            sysexPos = configFull.configPos[rightInput].getSysexFromConfig();
            System.arraycopy(sysexPos, 0, sysexPad, Constants.MD_SYSEX_PAD_SIZE * 2 + Constants.MD_SYSEX_3RD_SIZE + Constants.MD_SYSEX_POS_SIZE, sysexPos.length);
        } else {
            sysexPad = new byte[Constants.MD_SYSEX_PAD_SIZE + Constants.MD_SYSEX_POS_SIZE];
            sysex = configFull.configPads[0].getSysexFromConfig();
            System.arraycopy(sysex, 0, sysexPad, 0, sysex.length);
            System.arraycopy(sysex, 0, sysexPad, 0, sysex.length);
        }
        fileManager.saveSysex(sysexPad, configOptions);
    }

    private void loadSysexCurve() {
        byte[] sysex = configFull.configCurves[curvePointer].getSysexFromConfig();
        fileManager.loadSysex(sysex, configOptions);
        configFull.configCurves[curvePointer].setConfigFromSysex(sysex);
        uiPadsExtra.setYvalues(configFull.configCurves[curvePointer].yValues, false);
    }

    private void saveSysexCurve() {
        fileManager.saveSysex(configFull.configCurves[curvePointer].getSysexFromConfig(), configOptions);
    }

    private void loadSysexAllCustomNames() {
        byte[] sysex;
        byte[] sysexAll = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE * configFull.customNamesCount];
        fileManager.loadSysex(sysexAll, configOptions);
        for (int i = 0; i < configFull.customNamesCount; i++) {
            sysex = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE];
            System.arraycopy(sysexAll, i * Constants.MD_SYSEX_CUSTOM_NAME_SIZE, sysex, 0, Constants.MD_SYSEX_CUSTOM_NAME_SIZE);
            configFull.configCustomNames[i].setConfigFromSysex(sysex);
            uiPadsExtra.setCustomName(configFull.configCustomNames[i], i, false);
        }
        addCustomNamesToPads();
    }

    private void saveSysexAllCustomNames() {
        byte[] sysexAll = new byte[Constants.MD_SYSEX_CUSTOM_NAME_SIZE * configFull.customNamesCount];
        for (int i = 0; i < configFull.customNamesCount; i++) {
            System.arraycopy(configFull.configCustomNames[i].getSysexFromConfig(), 0, sysexAll, i * Constants.MD_SYSEX_CUSTOM_NAME_SIZE, Constants.MD_SYSEX_CUSTOM_NAME_SIZE);
        }
        fileManager.saveSysex(sysexAll, configOptions);
    }

    private void save_all() {
        configOptions.lastConfig = uiGlobal.getComboBoxFile().getSelectionModel().getSelectedIndex();
        if (fileManager.save_all(configFull, configOptions)) {
            uiGlobal.getComboBoxFile().getItems().clear();
            uiGlobal.getComboBoxFile().getItems().addAll(configOptions.configFileNames);
            uiGlobal.getComboBoxFile().getSelectionModel().select(configOptions.lastConfig);
        }
    }

    private void addCustomNamesToPads() {
        String[] names = new String[configFull.configCustomNames.length];
        for (int i = 0; i < configFull.configCustomNames.length; i++) {
            names[i] = configFull.configCustomNames[i].name;
        }
        uiPad.addAllCustomNames(names);
        updateComboBoxInput(true);
    }

    private void showAbout() {
        String aboutString;
        if (System.getProperty("os.name").startsWith("Mac")) {
            aboutString = Constants.HELP_ABOUT + Constants.HELP_ABOUT_MMJ;
        } else {
            aboutString = Constants.HELP_ABOUT;
        }
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText("About MegaDrum Manager FX");
        WebView webView = new WebView();
        webView.getEngine().loadContent(aboutString);
        webView.setPrefSize(400, 220);
        alert.getDialogPane().setContent(webView);
        alert.showAndWait();
    }
}