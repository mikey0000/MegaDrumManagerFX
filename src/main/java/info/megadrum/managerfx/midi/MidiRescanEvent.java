package info.megadrum.managerfx.midi;

import java.io.Serial;
import java.util.EventObject;

public class MidiRescanEvent extends EventObject {

    @Serial
    private static final long serialVersionUID = -7011061399353307850L;

    public MidiRescanEvent(Object source) {
        super(source);
    }
}
