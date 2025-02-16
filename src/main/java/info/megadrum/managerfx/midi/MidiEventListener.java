package info.megadrum.managerfx.midi;

import java.util.EventListener;

public interface MidiEventListener extends EventListener {
    void midiEventOccurred(MidiEvent evt);

    void midiEventOccurredWithBuffer(MidiEvent evt, byte[] buffer);
}