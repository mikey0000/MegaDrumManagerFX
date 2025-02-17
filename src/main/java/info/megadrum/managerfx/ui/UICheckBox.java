package info.megadrum.managerfx.ui;

import info.megadrum.managerfx.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

public class UICheckBox extends UIControl{
	
	private CheckBox checkBox;
    private boolean ignoreSyncState = false;

	public UICheckBox(String labelText, boolean showCopyButton) {
		super(labelText, showCopyButton);
		init();
	}
	
	private void init () {
		checkBox = new CheckBox();
		checkBox.selectedProperty().addListener(new ChangeListener<>() {
		    @Override
		    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		    	if (ignoreSyncState) {
		    		setSyncState(Constants.SYNC_STATE_SYNCED);
		    	} else {
			    	if (changedFromSet > 0) {
			    		changedFromSet = 0;
			    	} else {
				    	booleanValue = newValue;
						fireControlChangeEvent(new ControlChangeEvent(this), 0);
						if (syncState != Constants.SYNC_STATE_UNKNOWN) {
							if (booleanValue == mdBooleanValue) {
								setSyncState(Constants.SYNC_STATE_SYNCED);						
							} else {
								setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
							}
						}
			    	}
		    	}
		    }
		});
        HBox layout = new HBox();
		layout.setAlignment(Pos.CENTER_LEFT);
		layout.getChildren().addAll(checkBox);
		initControl(layout);
  }
	
    @Override
    public void respondToResize(double w, double h) {
    	super.respondToResize(w, h);
    	double checkBoxFontSize = h*0.35;
    	checkBox.setStyle("-fx-font-size: " + checkBoxFontSize + "pt");
    }
    
    public void uiCtlSetValue(boolean selected, boolean setFromSysex) {
    	if (selected != checkBox.isSelected()) {
    		changedFromSet = 1;
        	checkBox.setSelected(selected);    		
    	}
		booleanValue = selected;
    	if (setFromSysex) {
    		setSyncState(Constants.SYNC_STATE_SYNCED);
    		mdBooleanValue = selected;
    	} else {
        	updateSyncStateConditional();
    	}
    }
        
    public boolean uiCtlIsSelected() {
    	return checkBox.isSelected();
    }
    
    public void addListener(ChangeListener<Boolean> listener) {
    	checkBox.selectedProperty().addListener(listener);
    }

    public void setIgnoreSyncState() {
    	ignoreSyncState = true;
    }
}