package info.megadrum.managerfx.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MdTitledPane extends Pane {
	private final Label titleLabel;
	private final VBox vBox;
	public MdTitledPane () {
		
		titleLabel = new Label();
		titleLabel.setLayoutY(0);
		titleLabel.setAlignment(Pos.CENTER);
		setStyle("-fx-border-width: 1px; -fx-border-color: darkgrey");
		vBox = new VBox();
		vBox.setAlignment(Pos.CENTER);
		vBox.getChildren().add(titleLabel);
		vBox.setStyle("-fx-background-color: lightgrey");
		getChildren().add(vBox);
	}
	
	public void setText(String title) {
		titleLabel.setText(title);
	}
	
	public void setFont(Font font) {
		titleLabel.setFont(font);
	}
	
	public void setTitleHeight(Double h) {
		vBox.setMinHeight(h);
		vBox.setMaxHeight(h);
	}
	
	public void setWidth(Double w) {
		setMinWidth(w);
		setMaxWidth(w);
		vBox.setMinWidth(w);
		vBox.setMaxWidth(w);
	}
}
