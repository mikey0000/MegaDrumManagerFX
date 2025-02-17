package info.megadrum.managerfx.ui;

import java.util.Timer;
import java.util.TimerTask;

import info.megadrum.managerfx.utils.Constants;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

public class UISpinnerNote extends UIControl {
    private SpinnerFast<Integer> spinnerFast;
    private SpinnerValueFactory<Integer> valueFactory;
    private int minValue;
    private Integer maxValue;
    private int step;
    private Label labelNote;
    private CheckBox checkBoxNoteLinked;
    private boolean linkedNote = false;
    private GridPane layoutC;
    private final int octaveShift = 0;
    private int octave;
    private int note_pointer;
    private int linkedChangedFromSet = 0;
    private boolean mainNote = false;
    private static final String[] note_names = {"C ", "C#", "D ", "D#", "E ", "F ", "F#", "G ", "G#", "A ", "A#", "B "};
    private boolean changedByEdit = false;
    private int changedByEditTimers = 0;

    public UISpinnerNote(String labelText, boolean showCopyButton) {
        super(labelText, showCopyButton);
        init();
    }

    public UISpinnerNote(String labelText, boolean showCopyButton, boolean showLinked) {
        super(labelText, showCopyButton);
        linkedNote = showLinked;
        init();
    }

    private void init() {
        init(0, 127, 0, 1);
    }

    private void init(int min, int max, int initial, int s) {
        minValue = min;
        maxValue = max;
        intValue = initial;
        valueType = Constants.VALUE_TYPE_INT;
        step = s;
        valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, intValue, step);

        spinnerFast = new SpinnerFast<>();
        spinnerFast.setValueFactory(valueFactory);
        spinnerFast.setEditable(true);
        spinnerFast.getEditor().textProperty().addListener(new ChangeListener<>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (changedFromSet > 0) {
                    changedFromSet--;
                } else {
                    if (!newValue.matches("\\d*")) {
                        spinnerFast.getEditor().setText(String.valueOf(intValue));
                    } else {
                        if (newValue.matches("")) {
                            spinnerFast.getEditor().setText(String.valueOf(intValue));
                        } else {
                            if (intValue != Integer.parseInt(newValue)) {
                                intValue = Integer.parseInt(newValue);
                                if ((intValue >= minValue) & (intValue <= maxValue)) {
                                    changeNoteName();
                                    if (mainNote) {
                                        fireControlChangeEvent(new ControlChangeEvent(this), Constants.CONTROL_CHANGE_EVENT_NOTE_MAIN);
                                    } else {
                                        fireControlChangeEvent(new ControlChangeEvent(this), 0);
                                    }
                                    changedByEdit = true;
                                    changedByEditTimers++;
                                    new Timer().schedule(new TimerTask() {

                                        @Override
                                        public void run() {
                                            Platform.runLater(() -> {
                                                if (changedByEditTimers == 1) {
                                                    changedByEdit = false;
                                                    if (syncState != Constants.SYNC_STATE_UNKNOWN) {
                                                        if (intValue == mdIntValue) {
                                                            setSyncState(Constants.SYNC_STATE_SYNCED);
                                                        } else {
                                                            setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
                                                        }
                                                    }
                                                    if (!Integer.valueOf(spinnerFast.getEditor().getText()).equals(intValue)) {
                                                        int cursor = spinnerFast.getEditor().getCaretPosition();
                                                        spinnerFast.getEditor().setText(String.valueOf(intValue));
                                                        spinnerFast.getEditor().positionCaret(cursor);
                                                    }
                                                }
                                            });
                                        }
                                    }, 1500);
                                }
                            }
                        }
                    }
                }
                if (syncState != Constants.SYNC_STATE_UNKNOWN) {
                    if (intValue == mdIntValue) {
                        setSyncState(Constants.SYNC_STATE_SYNCED);
                    } else {
                        setSyncState(Constants.SYNC_STATE_NOT_SYNCED);
                    }
                }
            }
        });


        layoutC = new GridPane();
        labelNote = new Label("");
        checkBoxNoteLinked = new CheckBox();
        checkBoxNoteLinked.setTooltip(new Tooltip("Linked to Note"));
        layoutC.setAlignment(Pos.CENTER_LEFT);

        GridPane.setConstraints(spinnerFast, 0, 0);
        GridPane.setHalignment(spinnerFast, HPos.LEFT);
        GridPane.setValignment(spinnerFast, VPos.CENTER);

        GridPane.setConstraints(labelNote, 1, 0);
        GridPane.setHalignment(labelNote, HPos.CENTER);
        GridPane.setValignment(labelNote, VPos.CENTER);

        GridPane.setConstraints(checkBoxNoteLinked, 2, 0);
        GridPane.setHalignment(checkBoxNoteLinked, HPos.RIGHT);
        GridPane.setValignment(checkBoxNoteLinked, VPos.CENTER);

        if (linkedNote) {
            layoutC.getChildren().addAll(spinnerFast, labelNote, checkBoxNoteLinked);
            checkBoxNoteLinked.selectedProperty().addListener(new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (linkedChangedFromSet > 0) {
                        linkedChangedFromSet = 0;
                    } else {
                        fireControlChangeEvent(new ControlChangeEvent(this), Constants.CONTROL_CHANGE_EVENT_NOTE_LINKED);
                    }
                }
            });
        } else {
            layoutC.getChildren().addAll(spinnerFast, labelNote);
        }

        changeNoteName();
        initControl(layoutC);
    }

    private void resizeFont(double h) {
        int l = maxValue.toString().length();
        double ll = (16 / (16 + (double) l)) * 0.31;
        spinnerFast.getEditor().setFont(new Font((h * 2.2) * ll));
    }

    @Override
    public void respondToResize(double w, double h) {
        super.respondToResize(w, h);
        double spinnerButtonsFontSize = h * 0.3;
        double checkBoxFontSize = h * 0.26;
        spinnerFast.setMinHeight(h);
        spinnerFast.setMaxHeight(h);
        spinnerFast.setMaxWidth(h * 2.7);
        spinnerFast.setMinWidth(h * 2.7);

        layoutC.getColumnConstraints().clear();
        layoutC.getColumnConstraints().add(new ColumnConstraints(w * 0.265));
        if (linkedNote) {
            layoutC.getColumnConstraints().add(new ColumnConstraints(w * 0.22));
            layoutC.getColumnConstraints().add(new ColumnConstraints(w * 0.02));
        } else {
            layoutC.getColumnConstraints().add(new ColumnConstraints(w * 0.23));
            layoutC.getColumnConstraints().add(new ColumnConstraints(w * 0.01));
        }
        layoutC.getRowConstraints().clear();
        layoutC.getRowConstraints().add(new RowConstraints(h - padding * 2 - 1));

        spinnerFast.setStyle("-fx-font-size: " + spinnerButtonsFontSize + "pt");
        resizeFont(h);
        if (linkedNote) {
            labelNote.setFont(new Font(h * 0.50));
            checkBoxNoteLinked.setStyle("-fx-font-size: " + checkBoxFontSize + "pt");
        } else {
            labelNote.setFont(new Font(h * 0.55));
        }
    }

    public void changeNoteName() {
        int note_number;
        int base;
        String note_text;
        note_number = intValue;
        boolean disabledNoteAllowed = false;
        if ((note_number > 0) || (!disabledNoteAllowed)) {
            octave = note_number / 12;
            base = octave * 12;
            note_pointer = note_number - base;
            note_text = note_names[note_pointer] + " " + (octave - 3 + octaveShift);
            labelNote.setText(note_text);
            labelNote.setTooltip(new Tooltip("Note = " + note_text));
        } else {
            labelNote.setText("Disabled");
            labelNote.setTooltip(new Tooltip("Input is Disabled"));
        }
    }

    public void uiCtlSetValue(int n, boolean setFromSysex) {
        if (intValue != n) {
            changedFromSet++;
            intValue = n;
        }
        if (setFromSysex) {
            setSyncState(Constants.SYNC_STATE_SYNCED);
            mdIntValue = n;
        } else {
            updateSyncStateConditional();
        }
        if (changedByEdit) {
            changedByEdit = false;
        } else {
            valueFactory.setValue(n);
            spinnerFast.getEditor().setText(String.valueOf(intValue));
        }
        resizeFont(spinnerFast.getHeight());
        changeNoteName();
    }

    public Integer uiCtlGetValue() {
        return intValue;
    }

    public void setLinked(boolean linked) {
        if (linked != checkBoxNoteLinked.isSelected()) {
            linkedChangedFromSet = 1;
            checkBoxNoteLinked.setSelected(linked);
        }
        spinnerFast.setDisable(linked);
    }

    public boolean getLinked() {
        return checkBoxNoteLinked.isSelected();
    }

    public void setNoteIsMain(boolean main) {
        mainNote = main;
    }
}
