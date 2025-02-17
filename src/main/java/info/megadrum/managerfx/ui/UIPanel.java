package info.megadrum.managerfx.ui;

import info.megadrum.managerfx.utils.Constants;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class UIPanel {

    protected int viewSate = Constants.PANEL_SHOW;
    protected MdTitledPane titledPane;
    protected VBox vBoxAll;
    protected Pane paneAll;
    protected Button buttonGet;
    protected Button buttonSend;
    protected Button buttonLoad;
    protected Button buttonSave;
    protected Button buttonSetLow;
    protected Button buttonSetHigh;
    protected boolean showExtraPedalButtonsIsSet;
    protected boolean detached = false;
    protected String panelTitle;
    protected RadioMenuItem rbHide;
    protected RadioMenuItem rbShow;
    protected RadioMenuItem rbDetach;
    protected Stage windowDetached;
    protected double lastTitleHeight = 10.0;
    private double lastX = -1.0;
    private double lastY = -1.0;
    private double lastW = -1.0;
    private double lastH = -1.0;
    protected double controlW, controlH, tabsFontSize, tabHeaderPadding, tabHeaderHeight, titledPaneFontHeight;
    protected double comboBoxFontHeight, buttonFontSize;
    protected double lastControlH = 0.0;
    protected boolean showAdvanced = false;
    protected int verticalControlsCount = 0;
    protected int verticalControlsCountWithoutAdvanced = 0;
    private final Image imageWindowIcon;

    public UIPanel(String title, boolean showExtraPedalButtons) {
        panelTitle = title;
        showExtraPedalButtonsIsSet = showExtraPedalButtons;
        ToggleGroup toggleGroup = new ToggleGroup();
        rbHide = new RadioMenuItem("Hide");
        rbHide.setToggleGroup(toggleGroup);
        rbShow = new RadioMenuItem("Show");
        rbShow.setToggleGroup(toggleGroup);
        rbDetach = new RadioMenuItem("Detach");
        rbDetach.setToggleGroup(toggleGroup);
        windowDetached = new Stage();
        windowDetached.setTitle(Constants.WINDOWS_TITLE_SHORT + title);
        imageWindowIcon = new Image("/icon_256x256.png");
        buttonGet = new Button("Get");
        buttonSend = new Button("Send");
        buttonLoad = new Button("Load");
        buttonSave = new Button("Save");
        if (showExtraPedalButtonsIsSet) {
            buttonSetLow = new Button("SetLow");
            buttonSetHigh = new Button("SetHigh");
        }
        vBoxAll = new VBox(1);
        vBoxAll.setStyle("-fx-padding: 0.0em 0.0em 0.0em 0.0em");
        vBoxAll.setAlignment(Pos.TOP_CENTER);
        vBoxAll.setLayoutX(0);
        paneAll = new Pane();
        paneAll.setStyle("-fx-padding: 0.0em 0.0em 0.0em 0.0em");
    }

    public void setLastX(double x) {
        lastX = x;
    }

    public void setLastY(double y) {
        lastY = y;
    }

    public void setLastW(double w) {
        lastW = w;
    }

    public void setLastH(double h) {
        lastH = h;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public double getLastW() {
        return lastW;
    }

    public double getLastH() {
        return lastH;
    }

    public void setViewState(int state) {
        viewSate = state;
    }

    public int getViewState() {
        return viewSate;
    }

    public final Node getUI() {
        if (detached) {
            return vBoxAll;
        } else {
            return titledPane;
        }
    }

    public void respondToResize(double w, double h, double cW, double cH) {
        if (detached) {
            windowDetached.getIcons().clear();
            windowDetached.getIcons().add(imageWindowIcon);
        }
        if (cH > cW * Constants.FX_CONTROL_H_TO_W) {
            controlH = cW * Constants.FX_CONTROL_H_TO_W;
        } else {
            controlH = cH;
        }
        controlW = cW;
        lastControlH = controlH;
        titledPaneFontHeight = controlH * Constants.FX_TITLEBARS_FONT_SCALE;
        if (!(titledPaneFontHeight > Constants.FX_TITLEBARS_FONT_MIN_SIZE)) {
            titledPaneFontHeight = Constants.FX_TITLEBARS_FONT_MIN_SIZE;
        }
        titledPane.setFont(new Font(titledPaneFontHeight));
        titledPane.setTitleHeight(controlH);
        lastTitleHeight = controlH;
        tabsFontSize = controlH * Constants.FX_TABS_FONT_SCALE;
        tabHeaderPadding = -controlH * 0.015;
        tabHeaderHeight = controlH * 0.5;
        comboBoxFontHeight = controlH * Constants.FX_COMBOBOX_FONT_SCALE;
        buttonFontSize = controlH * 0.31;
        vBoxAll.setLayoutY(lastTitleHeight);
    }

    public void respondToResizeDetached() {
    }

    public final Parent getTopLayout() {
        if (detached) {
            return vBoxAll;
        } else {
            return titledPane;
        }
    }

    public Stage getWindow() {
        return windowDetached;
    }

    public void selectRadioMenuItemHide() {
        rbHide.setSelected(true);
    }

    public void selectRadioMenuItemShow() {
        rbShow.setSelected(true);
    }

    public void selectRadioMenuItemDetach() {
        rbDetach.setSelected(true);
    }

    public void setDetached(boolean d) {
        detached = d;
        if (!detached) {
            titledPane = new MdTitledPane();
            titledPane.setText(panelTitle);
            vBoxAll.setLayoutY(lastTitleHeight);
            titledPane.getChildren().add(vBoxAll);
            titledPane.setId("panelTitle");
        }
    }

    public boolean isDetached() {
        return detached;
    }

    public final Button getButtonGet() {
        return buttonGet;
    }

    public final Button getButtonSend() {
        return buttonSend;
    }

    public final Button getButtonLoad() {
        return buttonLoad;
    }

    public final Button getButtonSetLow() {
        return buttonSetLow;
    }

    public final Button getButtonSetHigh() {
        return buttonSetHigh;
    }

    public final Button getButtonSave() {
        return buttonSave;
    }

    public RadioMenuItem getRadioMenuItemHide() {
        return rbHide;
    }

    public RadioMenuItem getRadioMenuItemShow() {
        return rbShow;
    }

    public RadioMenuItem getRadioMenuItemDetach() {
        return rbDetach;
    }

    public void setShowAdvanced(boolean show) {
        showAdvanced = show;
    }

    public double getLastControlH() {
        return lastControlH;
    }

    public int getVerticalControlsCount() {
        if (showAdvanced) {
            return verticalControlsCount + 1;
        } else {
            return verticalControlsCountWithoutAdvanced + 1;
        }
    }

}
