package info.megadrum.managerfx.ui;

import java.util.List;

import info.megadrum.managerfx.utils.Constants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

public class UIComboBox extends UIControl {
	private ComboBox<String> comboBox;
	private HBox layout;
	private Boolean comboBoxWide = false;
	private List<String> listValues;
	private int maxStringLength;

	public UIComboBox(Boolean showCopyButton) {
		super(showCopyButton);
		init();
	}
	
	public UIComboBox(String labelText, Boolean showCopyButton) {
		super(labelText, showCopyButton);
		init();
	}
	
	private void init () {
		valueType = Constants.VALUE_TYPE_INT;
		comboBox = new ComboBox<String>();
		comboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		    	if (changedFromSet > 0) {
		    		changedFromSet--;
		    	} else {
		    		Integer newIntValue = comboBox.getSelectionModel().getSelectedIndex();
		    		if (newIntValue > -1) {
						if (intValue.intValue() != newIntValue.intValue()) {
							intValue = newIntValue;
							fireControlChangeEvent(new ControlChangeEvent(this), 0);
							if (syncState != Constants.SYNC_STATE_UNKNOWN) {
								if (intValue.intValue() == mdIntValue.intValue()) {
									setSyncState(Constants.SYNC_STATE_SYNCED);						
								} else {
									setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
								}
								
							}
						}
		    		}
		    	}				
			}
        });

		comboBox.setEditable(false);
		layout = new HBox();
		layout.setAlignment(Pos.CENTER_LEFT);
		layout.getChildren().addAll(comboBox);
		initControl(layout);		
	}
    @Override
    public void respondToResize(Double w, Double h) {
    	Double width;
    	super.respondToResize(w, h);
    	comboBox.setMinHeight(h);
    	comboBox.setMaxHeight(h);
    	width = w*0.1;
    	if (comboBoxWide) {
    		width = w*0.67;
    	} else {    		
        	if (copyButtonShown) {
        		width = w*0.50;
        	} else {
        		width = w*0.50;
        	}    		
    	}
    	comboBox.setMinWidth(width);
    	comboBox.setMaxWidth(width);
		Double fontSize;
    	if (maxStringLength > 16) {
    		fontSize = h*0.26;    		
    	} else if (maxStringLength > 8) {
    		fontSize = h*0.31;    		
    	} else {
    		fontSize = h*0.39;    		    		
    	}
		comboBox.setStyle("-fx-font-size: " + fontSize.toString() + "pt");			
    }
    
    public void uiCtlSetValuesArray(List<String> list) {
    	maxStringLength = 0;
    	for (int i = 0; i < list.size(); i++) {
    		if (list.get(i).length() > maxStringLength) {
    			maxStringLength = list.get(i).length();
    		}
    	}
    	int s = comboBox.getSelectionModel().getSelectedIndex();
    	comboBox.getItems().clear();
    	comboBox.getItems().addAll(list);
    	listValues = list;
    	comboBox.getSelectionModel().select(s);
    }
    
    public void uiCtlSetValue(Integer n, Boolean setFromSysex) {
    	if (intValue.intValue() != n.intValue()) {
        	changedFromSet = 1;
    	   	intValue = n;
        }
    	if (setFromSysex) {
    		setSyncState(Constants.SYNC_STATE_SYNCED);
    		mdIntValue = n;
    	} else {
        	updateSyncStateConditional();
    	}
    	comboBox.getSelectionModel().select(n);
   }
   
   public Integer uiCtlGetValue() {
    	return intValue;
   }
 
   public void uiCtlSetValue(String value) {
    	comboBox.setValue(value);
   }
    
	public String uiCtlGetSelected() {
		return comboBox.getValue();
	}
    
    public void addListener(ChangeListener<String> listener) {
    	comboBox.getSelectionModel().selectedItemProperty().addListener(listener);
    }
    
    public void uiCtlSetDisable(Boolean state) {
    	comboBox.setDisable(state);
    }
    
    public void setComboBoxWide(Boolean w) {
    	comboBoxWide = true;
    }
}
