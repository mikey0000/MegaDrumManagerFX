package info.megadrum.managerfx.ui;

import java.util.EventListener;

public interface ControlChangeEventListener extends EventListener {
    void controlChangeEventOccurred(ControlChangeEvent evt, Integer parameter);
}
