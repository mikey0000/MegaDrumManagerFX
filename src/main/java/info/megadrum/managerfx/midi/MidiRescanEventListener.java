package info.megadrum.managerfx.midi;

import java.util.EventListener;

public interface MidiRescanEventListener extends EventListener {
    void midiRescanEventOccurred(MidiRescanEvent evt);
}
