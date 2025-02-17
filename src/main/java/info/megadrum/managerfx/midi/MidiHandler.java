package info.megadrum.managerfx.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.sound.midi.Receiver;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.SysexMessage;
import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.utils.Constants;
import info.megadrum.managerfx.utils.Utils;

public class MidiHandler {

    private MidiDevice midiin;
    private String midiInName = "";
    private MidiDevice midiout;
    private String midiOutName = "";
    private MidiDevice midithru;
    private String midiThruName = "";
    private MidiDevice.Info[] aInfos;
    private Receiver receiver;
    private Receiver thruReceiver;
    private DumpReceiver dump_receiver;
    private Transmitter transmitter;

    private int chainId = 0;
    private byte[] bufferIn;
    private boolean sysexReceived;
    private final boolean useMidiThru = false;

    protected EventListenerList listenerList = new EventListenerList();

    public void addMidiEventListener(MidiEventListener listener) {
        listenerList.add(MidiEventListener.class, listener);
    }

    protected void fireMidiEvent(MidiEvent evt) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == MidiEventListener.class) {
                ((MidiEventListener) listeners[i + 1]).midiEventOccurred(evt);
            }
        }
    }

    public MidiHandler() {
        init();
    }

    public void init() {
        midiin = null;
        midiout = null;
        midithru = null;
        receiver = null;
        thruReceiver = null;
        transmitter = null;
        chainId = 0;
        dump_receiver = null;
        dump_receiver = new DumpReceiver();
        dump_receiver.addMidiEventListener(new MidiEventListener() {
            @Override
            public void midiEventOccurred(MidiEvent evt) {
                fireMidiEvent(new MidiEvent(this));
            }

        });
        bufferIn = null;
        sysexReceived = false;
    }

    public void setChainId(int id) {
        chainId = id;
    }

    public byte[] getBufferIn() {
        return bufferIn;
    }

    public Boolean isSysexReceived() {
        return sysexReceived;
    }

    public void resetSysexReceived() {
        sysexReceived = false;
    }

    public void resetBufferIn() {
        bufferIn = null;
    }

    public void closeAllPorts() {
        if (midiin != null) {
            if (midiin.isOpen()) {
                midiin.close();
            }
        }
        if (midiout != null) {
            if (midiout.isOpen()) {
                midiout.close();
            }
        }
        if (midithru != null) {
            if (midithru.isOpen()) {
                midithru.close();
            }
        }
    }

    public void clearMidiOut() {
        if (midiout != null) {
            if (midiout.isOpen()) {
                midiout.close();
            }
        }
        try {
            assert midiout != null;
            midiout.open();
        } catch (MidiUnavailableException e) {
            Utils.show_error("Error re-opening MIDI Out port:\n" +
                    midiout.getDeviceInfo().getName() + "\n" +
                    "(" + e.getMessage() + ")");
        }
    }

    public void sendMidiShortThru(byte[] buf) {
        if ((midithru != null) && (midithru.isOpen())) {
            ShortMessage shortMessage = new ShortMessage();
            try {
                switch (buf.length) {
                    case 1:
                        shortMessage.setMessage(buf[0]);
                        break;
                    case 2:
                        shortMessage.setMessage(buf[0], buf[1], 0);
                        break;
                    default:
                        shortMessage.setMessage(buf[0], buf[1], buf[2]);
                        break;
                }
                thruReceiver.send(shortMessage, -1);
            } catch (InvalidMidiDataException e) {
                Utils.show_error("Error sending Short MIDI message to port:\n" +
                        midithru.getDeviceInfo().getName() + "\n" +
                        "(" + e.getMessage() + ")");
            }
        }
    }

    public void sendSysex(byte[] buf) {
        if (midiout != null) {
            if (midiout.isOpen()) {
                SysexMessage sysexMessage = new SysexMessage();
                try {
                    receiver = midiout.getReceiver();
                } catch (MidiUnavailableException e1) {
                    e1.printStackTrace();
                }
                try {
                    sysexMessage.setMessage(buf, buf.length);
                    receiver.send(sysexMessage, -1);
                } catch (InvalidMidiDataException e) {
                    Utils.show_error("Error sending Sysex MIDI message to port:\n" +
                            midiout.getDeviceInfo().getName() + "\n" +
                            "(" + e.getMessage() + ")");
                }
            }
        }
    }

    public void sendSysexUpgrade(byte[] buf) {
        if (midiout != null) {
            if (!midiout.isOpen()) {
                try {
                    midiout.open();
                } catch (MidiUnavailableException e) {
                    e.printStackTrace();
                }
            }
            SysexMessage sysexMessage = new SysexMessage();
            try {
                receiver = midiout.getReceiver();
            } catch (MidiUnavailableException e2) {
                e2.printStackTrace();
            }
            try {
                sysexMessage.setMessage(buf, buf.length);
            } catch (InvalidMidiDataException e) {
                Utils.show_error("Error sending Sysex MIDI message to port:\n" +
                        midiout.getDeviceInfo().getName() + "\n" +
                        "(" + e.getMessage() + ")");
            }
            receiver.send(sysexMessage, -1);
        }
    }

    private void sendSysexRequest(byte[] sx) {
        sx[0] = Constants.SYSEX_START;
        sx[1] = Constants.MD_SYSEX;
        sx[2] = (byte) chainId;
        sx[sx.length - 1] = Constants.SYSEX_END;
        sendSysex(sx);
    }

    public void requestConfigGlobalMisc() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_GLOBAL_MISC;
        sendSysexRequest(sx);
    }

    public void requestConfigMisc() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_MISC;
        sendSysexRequest(sx);
    }

    public void requestVersion() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_VERSION;
        sendSysexRequest(sx);
    }

    public void requestMCU() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_MCU_TYPE;
        sendSysexRequest(sx);
    }

    public void requestConfigCount() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_CONFIG_COUNT;
        sendSysexRequest(sx);
    }

    public void requestConfigCurrent() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_CONFIG_CURRENT;
        sendSysexRequest(sx);
    }

    public void requestConfigPedal() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_PEDAL;
        sendSysexRequest(sx);
    }

    public void requestConfigPad(int pad_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_PAD;
        sx[4] = (byte) pad_id;
        sendSysexRequest(sx);
    }

    public void requestConfigPos(int pad_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_POS;
        sx[4] = (byte) pad_id;
        sendSysexRequest(sx);
    }

    public void requestConfig3rd(int third_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_3RD;
        sx[4] = (byte) third_id;
        sendSysexRequest(sx);
    }

    public void requestConfigCurve(int curve_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_CURVE;
        sx[4] = (byte) curve_id;
        sendSysexRequest(sx);
    }

    public void requestConfigCustomName(int name_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_CUSTOM_NAME;
        sx[4] = (byte) name_id;
        sendSysexRequest(sx);
    }

    public void requestConfigConfigName(int name_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_CONFIG_NAME;
        sx[4] = (byte) name_id;
        sendSysexRequest(sx);
    }

    public void requestSaveToSlot(int config_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_CONFIG_SAVE;
        sx[4] = (byte) config_id;
        sendSysexRequest(sx);
    }

    public void requestLoadFromSlot(int config_id) {
        byte[] sx = new byte[6];
        sx[3] = Constants.MD_SYSEX_CONFIG_LOAD;
        sx[4] = (byte) config_id;
        sendSysexRequest(sx);
    }

    public void requestPedalLevelRaw() {
        byte[] sx = new byte[5];
        sx[3] = Constants.MD_SYSEX_PEDAL_LEVEL_RAW;
        sendSysexRequest(sx);
    }

    public void requestArmBootloader() {
        byte[] sx = new byte[21];
        sx[3] = Constants.MD_SYSEX_BOOTLOADER;
        sx[4] = 0x09;
        sx[5] = 0x03;
        sx[6] = 0x0a;
        sx[7] = 0x07;
        sx[8] = 0x0c;
        sx[9] = 0x01;
        sx[10] = 0x05;
        sx[11] = 0x0d;
        sx[12] = 0x03;
        sx[13] = 0x0f;
        sx[14] = 0x0e;
        sx[15] = 0x06;
        sx[16] = 0x04;
        sx[17] = 0x0b;
        sx[18] = 0x00;
        sx[19] = 0x05;
        sendSysexRequest(sx);
    }

    public void clear_midi_input() {
        int size = 1;
        if (midiin != null && midiin.isOpen()) {
            while (size > 0) {
                byte[] result = dump_receiver.getByteMessage();
                if (result == null) {
                    size = 0;
                } else {
                    size = result.length;
                }
            }
        }
    }

    public void getMidi() {
        if (bufferIn == null) {
            bufferIn = dump_receiver.getByteMessage();
            if (bufferIn != null) {
                int size = bufferIn.length;
                if ((bufferIn[0] == Constants.SYSEX_START) && (bufferIn[size - 1] == Constants.SYSEX_END)) {
                    sysexReceived = true;
                } else {
                    sendMidiShortThru(bufferIn);
                }
            }
        }
    }

    public String[] getMidiInList() {
        aInfos = MidiSystem.getMidiDeviceInfo();
        int nPorts = aInfos.length;
        int port = 0;
        int[] table = new int[nPorts];
        for (int i = 0; i < nPorts; i++) {
            try {
                midiin = MidiSystem.getMidiDevice(aInfos[i]);
                if (midiin.getMaxTransmitters() != 0) {
                    table[port] = i;
                    port++;
                }
            } catch (MidiUnavailableException e) {
                Utils.show_error("Error trying to list MIDI In ports.\n"
                        + "(" + e.getMessage() + ")");
            }

        }
        String[] list = new String[port];
        for (int i = 0; i < port; i++) {
            list[i] = aInfos[table[i]].getName();
        }
        return list;
    }

    public String[] getMidiOutList() {
        aInfos = MidiSystem.getMidiDeviceInfo();
        int nPorts = aInfos.length;
        int port = 0;
        int[] table = new int[nPorts];

        for (int i = 0; i < nPorts; i++) {
            try {
                midiout = MidiSystem.getMidiDevice(aInfos[i]);
                if (midiout.getMaxReceivers() != 0) {
                    table[port] = i;
                    port++;
                }
            } catch (MidiUnavailableException e) {
                Utils.show_error("Error trying to list MIDI Out ports.\n"
                        + "(" + e.getMessage() + ")");
            }

        }
        String[] list = new String[port];
        for (int i = 0; i < port; i++) {
            list[i] = aInfos[table[i]].getName();
        }
        return list;
    }

    public void setMidiInName(String name) {
        midiInName = name;
    }

    public void setMidiOutName(String name) {
        midiOutName = name;
    }

    public void setMidiThruName(String name) {
        midiThruName = name;
    }

    public void initPorts() {
        aInfos = MidiSystem.getMidiDeviceInfo();
        int nPorts = aInfos.length;
        closeAllPorts();
        if (!midiInName.isEmpty()) {
            try {
                for (int i = 0; i < nPorts; i++) {
                    midiin = MidiSystem.getMidiDevice(aInfos[i]);
                    if (midiin.getMaxTransmitters() != 0 && aInfos[i].getName().equals(midiInName)) {
                        midiin = MidiSystem.getMidiDevice(aInfos[i]);
                        midiin.open();
                        transmitter = midiin.getTransmitter();
                        transmitter.setReceiver(dump_receiver);
                        break;
                    }
                }
            } catch (MidiUnavailableException e) {
                Utils.show_error("Error trying to open MIDI In port:\n" +
                        midiin.getDeviceInfo().getName());
            }
        }

        if (!midiOutName.isEmpty()) {
            try {
                for (int i = 0; i < nPorts; i++) {
                    midiout = MidiSystem.getMidiDevice(aInfos[i]);
                    if (midiout.getMaxReceivers() != 0 && aInfos[i].getName().equals(midiOutName)) {
                        midiout = MidiSystem.getMidiDevice(aInfos[i]);
                        midiout.open();
                        receiver = midiout.getReceiver();
                        break;
                    }
                }
            } catch (MidiUnavailableException e) {
                Utils.show_error("Error trying to open MIDI Out port:\n" +
                        midiout.getDeviceInfo().getName());
            }
        }

        if ((!midiThruName.isEmpty()) && (useMidiThru)) {
            try {
                for (int i = 0; i < nPorts; i++) {
                    midithru = MidiSystem.getMidiDevice(aInfos[i]);
                    if (midithru.getMaxReceivers() != 0 && aInfos[i].getName().equals(midiThruName)) {
                        midithru = MidiSystem.getMidiDevice(aInfos[i]);
                        midithru.open();
                        thruReceiver = midithru.getReceiver();
                        break;
                    }
                }
            } catch (MidiUnavailableException e) {
                Utils.show_error("Error trying to open MIDI Thru port:\n" +
                        midithru.getDeviceInfo().getName());
            }
        }

    }

    public boolean isMidiOpen() {
        return (midiin != null) && (midiout != null) && midiin.isOpen() && midiout.isOpen();
    }

    public DumpReceiver getDumpReceiver() {
        return dump_receiver;
    }

}
