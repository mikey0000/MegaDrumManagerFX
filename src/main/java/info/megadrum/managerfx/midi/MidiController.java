package info.megadrum.managerfx.midi;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.EventListenerList;

import info.megadrum.managerfx.utils.Constants;
import info.megadrum.managerfx.utils.Utils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;

public class MidiController {
    private final DumpReceiver dump_receiver;
    private Boolean isInFirmwareUpgrade = false;

    private final MidiHandler midiHandler;
    private Boolean sysexTimedOut = false;
    private Boolean sysexMismatch = false;
    private Boolean sysexReceived;
    private byte currentSysexType;
    private byte currentSysexId;
    private boolean currentSysexWithId;
    private Integer sendSysexConfigRetries = 1;
    private Boolean compareSysex = false;
    private byte[] sysexToCompare;
    private final int[] sysexStatus;
    private int chainId;
    private boolean upgradeCancelled = false;
    private String upgradeResultString;
    private int upgradeError;

    private final List<byte[]> receivedMidiDataList;

    class SendSysexTask<V> extends Task<V> {

        private List<byte[]> sysexesList;
        private Integer maxRetries;
        private Integer retryDelay;

        @Override
        protected V call() {
            try {
                sysexStatus[0] = Constants.MD_SYSEX_STATUS_OK;
                int i = 0;
                final int max = sysexesList.size();
                updateProgress(0, max);
                while (!sysexesList.isEmpty()) {
                    byte[] buf = sysexesList.getFirst();
                    if (buf.length > 3) {
                        sysexStatus[1] = buf[3];
                    } else {
                        sysexStatus[1] = buf[0];
                    }
                    sendSysexFromThread(buf, maxRetries, retryDelay);
                    if (sysexTimedOut) {
                        if ((sysexStatus[0]) == Constants.MD_SYSEX_STATUS_OK) {
                            sysexStatus[0] = Constants.MD_SYSEX_STATUS_TIMEOUT;
                        }
                        break;
                    }
                    updateProgress(i, max);
                    i++;
                    sysexesList.removeFirst();
                }
            } catch (Exception e) {
                Utils.show_error(String.format("Sysex Send thread exception text = %s\n", e.getMessage()));
            }
            return null;
        }

        public void setParameters(List<byte[]> list, Integer retries, Integer msDelay) {
            sysexesList = list;
            maxRetries = retries;
            retryDelay = msDelay;
        }

    }

    private SendSysexTask<Void> sendSysexTask;

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

    public MidiController() {
        midiHandler = new MidiHandler();
        dump_receiver = midiHandler.getDumpReceiver();
        receivedMidiDataList = new ArrayList<>();
        sysexStatus = new int[2];

        midiHandler.addMidiEventListener(new MidiEventListener() {
            @Override
            public void midiEventOccurred(MidiEvent evt) {
                if (!isInFirmwareUpgrade) {
                    midiHandler.getMidi();
                    if (midiHandler.isSysexReceived()) {
                        midiHandler.resetSysexReceived();
                        processSysex(midiHandler.getBufferIn());
                        midiHandler.resetBufferIn();
                    } else if (midiHandler.getBufferIn() != null) {
                        processShortMidi(midiHandler.getBufferIn());
                        midiHandler.resetBufferIn();
                    }
                }
            }

        });
    }

    private void processSysex(byte[] buffer) {
        if (compareSysex) {
            if (buffer[3] == Constants.MD_SYSEX_GLOBAL_MISC) {
                buffer[8] = (byte) (buffer[8] & 0xfe);
                buffer[9] = (byte) (buffer[9] & 0xf6);
                sysexToCompare[8] = (byte) (sysexToCompare[8] & 0xfe);
                sysexToCompare[9] = (byte) (sysexToCompare[9] & 0xf6);
            }
            if (Arrays.equals(buffer, sysexToCompare)) {
                sysexReceived = true;
            } else {
                sysexStatus[0] = Constants.MD_SYSEX_STATUS_MISMATCH;
                for (int i = 0; i < buffer.length; i++) {
                    if (buffer[i] != sysexToCompare[i]) {
                        break;
                    }
                }
                sysexMismatch = true;
            }
        } else {
            if (buffer[3] == currentSysexType) {
                if (currentSysexWithId) {
                    if (buffer[4] == currentSysexId) {
                        sysexReceived = true;
                    }
                } else {
                    sysexReceived = true;
                }
            }
        }
        if (sysexReceived) {
            sysexStatus[0] = 0;
            receivedMidiDataList.add(buffer);
            fireMidiEvent(new MidiEvent(this));
        }
    }

    public int[] getStatus() {
        return sysexStatus;
    }

    private void processShortMidi(byte[] buffer) {
        receivedMidiDataList.add(buffer);
        fireMidiEvent(new MidiEvent(this));
    }

    public List<byte[]> getMidiDataList() {
        return receivedMidiDataList;
    }

    public String[] getMidiInList() {
        return midiHandler.getMidiInList();
    }

    public String[] getMidiOutList() {
        return midiHandler.getMidiOutList();
    }

    public void openMidi(String midiIn, String midiOut, String midiThru) {
        midiHandler.setMidiInName(midiIn);
        midiHandler.setMidiOutName(midiOut);
        midiHandler.setMidiThruName(midiThru);
        midiHandler.initPorts();
    }

    private void midi_reset_ports() {
        midiHandler.clearMidiOut();
    }

    public void sendSysexTaskRecreate() {
        if (sendSysexTask != null) {
            sendSysexTask = null;
        }
        sendSysexTask = new SendSysexTask<>();
    }

    public void addSendSysexTaskSucceedEventHandler(EventHandler<WorkerStateEvent> eh) {
        sendSysexTask.setOnSucceeded(eh);
    }

    public void setChainId(int id) {
        midiHandler.setChainId(id);
        chainId = id;
    }

    public void sendSysexFromThread(byte[] sysex, Integer maxRetries, Integer retryDelay) {
        sendSysexConfigRetries = 10;
        byte type;
        byte id;
        if (sysex.length > 2) {
            type = sysex[3];
            id = sysex[4];
        } else {
            type = sysex[0];
            id = sysex[1];
        }
        int delayCounter;
        currentSysexType = type;
        currentSysexWithId = false;
        currentSysexId = id;
        sysexReceived = false;
        sysexMismatch = true;
        compareSysex = false;
        while (sendSysexConfigRetries > 0) {
            if (sysex.length > 2) {
                if (sysexMismatch) {
                    sysexMismatch = false;
                    sysex[2] = (byte) chainId;
                    sysexToCompare = Arrays.copyOf(sysex, sysex.length);
                    compareSysex = true;
                    midiHandler.sendSysex(sysex);
                    if (sendSysexConfigRetries < maxRetries) {
                        Utils.delayMs(sysex.length / 4);
                    }
                    Utils.delayMs(sysex.length / 5);
                }
                compareSysex = true;
            }
            sendSysexConfigRetries--;
            delayCounter = retryDelay;
            switch (type) {
                case Constants.MD_SYSEX_3RD:
                    currentSysexWithId = true;
                    midiHandler.requestConfig3rd(id);
                    break;
                case Constants.MD_SYSEX_CONFIG_COUNT:
                    midiHandler.requestConfigCount();
                    break;
                case Constants.MD_SYSEX_CONFIG_CURRENT:
                    midiHandler.requestConfigCurrent();
                    break;
                case Constants.MD_SYSEX_CONFIG_NAME:
                    currentSysexWithId = true;
                    midiHandler.requestConfigConfigName(id);
                    break;
                case Constants.MD_SYSEX_CURVE:
                    currentSysexWithId = true;
                    midiHandler.requestConfigCurve(id);
                    break;
                case Constants.MD_SYSEX_CUSTOM_NAME:
                    currentSysexWithId = true;
                    midiHandler.requestConfigCustomName(id);
                    break;
                case Constants.MD_SYSEX_GLOBAL_MISC:
                    midiHandler.requestConfigGlobalMisc();
                    break;
                case Constants.MD_SYSEX_MCU_TYPE:
                    midiHandler.requestMCU();
                    break;
                case Constants.MD_SYSEX_MISC:
                    midiHandler.requestConfigMisc();
                    break;
                case Constants.MD_SYSEX_PAD:
                    currentSysexWithId = true;
                    if (sysex.length > 2) {
                        currentSysexId = id;
                    } else {
                        currentSysexId = (byte) (id + 1);
                    }
                    midiHandler.requestConfigPad(currentSysexId);
                    break;
                case Constants.MD_SYSEX_PEDAL:
                    midiHandler.requestConfigPedal();
                    break;
                case Constants.MD_SYSEX_POS:
                    currentSysexWithId = true;
                    midiHandler.requestConfigPos(id);
                    break;
                case Constants.MD_SYSEX_VERSION:
                    midiHandler.requestVersion();
                    break;
                case Constants.MD_SYSEX_CONFIG_LOAD:
                    sysexReceived = true;
                    midiHandler.requestLoadFromSlot(id);
                    break;
                case Constants.MD_SYSEX_CONFIG_SAVE:
                    sysexReceived = true;
                    midiHandler.requestSaveToSlot(id);
                    break;
                case Constants.MD_SYSEX_PEDAL_LEVEL_RAW:
                    midiHandler.requestPedalLevelRaw();
                    break;
                default:
                    break;
            }
            while ((delayCounter > 0) && (!sysexReceived)) {
                Utils.delayMs(1);
                delayCounter--;
            }
            if (sysexReceived) {
                break;
            } else {
                midi_reset_ports();
            }
        }
        if (!sysexReceived) {
            sysexTimedOut = true;
        }
    }

    public int sendSysex(List<byte[]> sysexSendList, ProgressBar progressBar, Integer maxRetries, Integer retryDelay) {
        int result = 0;
        sysexReceived = false;
        sendSysexConfigRetries = maxRetries;
        sysexTimedOut = false;

        if (midiHandler.isMidiOpen()) {
            List<byte[]> sysexSendListLocal = new ArrayList<>(sysexSendList);
            sysexSendList.clear();
            sendSysexTask.setParameters(sysexSendListLocal, maxRetries, retryDelay);
            progressBar.progressProperty().bind(sendSysexTask.progressProperty());
            Platform.runLater(() -> new Thread(sendSysexTask).start());
        } else {
            progressBar.setVisible(false);
            result = 1;
        }
        return result;
    }

    private static int readHex(DataInputStream d) throws IOException {
        StringBuilder curr;
        int result;

        curr = new StringBuilder();

        curr.append(String.format("%c", d.readByte()).toUpperCase());
        curr.append(String.format("%c", d.readByte()).toUpperCase());

        result = Integer.parseInt(curr.toString(), 16);
        return result;
    }

    public Task<Integer> doFirmwareUpgrade(File file, int mcuType, ProgressBar progressBar) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis;
        DataInputStream dis;
        upgradeResultString = "Upgrade completed successufully";
        upgradeCancelled = false;
        int[] buffer = new int[0x40000];

        int bufferSize = 0;
        upgradeError = 0;

        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Utils.show_error("Error loading from file:\n" +
                    file.getAbsolutePath() + "\n" +
                    "(" + e.getMessage() + ")");
        }
        assert fis != null;
        bis = new BufferedInputStream(fis);
        dis = new DataInputStream(bis);
        while (dis.available() > 1) {
            buffer[bufferSize] = readHex(dis);
            bufferSize++;
        }
        dis.close();
        bis.close();
        fis.close();

        final int bufferSizeFinal = bufferSize;
        Task<Integer> task = new Task<>() {
            int frameSize;
            int bytesSent = 0;
            int prevBytesSent = 0;
            int retries = 0;
            int inDelay;


            @Override
            protected Integer call() {
                updateProgress(0, bufferSizeFinal);

                if (mcuType > 2) {
                    midiHandler.requestArmBootloader();
                    Utils.delayMs(4000);
                }
                closeAllPorts();
                midiHandler.initPorts();
                midiHandler.clear_midi_input();

                boolean firstDelay = true;
                for (int index = 0; index < bufferSizeFinal; index += frameSize) {
                    frameSize = ((buffer[index] << 8) | buffer[index + 1]) + 2;
                    if (((bytesSent - prevBytesSent) * 100 / (bufferSizeFinal / 10)) > 1) {
                        updateProgress(bytesSent, bufferSizeFinal);
                        prevBytesSent = bytesSent;
                    }
//                    midiHandler.writeMid(receiver, buffer, index, frameSize);

                    int nBytes = 0;
                    if (firstDelay) {
                        inDelay = 5000;
                        firstDelay = false;
                    } else {
                        inDelay = 1000;
                    }
                    byte[] receivedBuffer = null;
                    int t = 0;
                    while ((nBytes == 0) && (inDelay > 0)) {
                        receivedBuffer = dump_receiver.getByteMessage();
                        t++;
                        if (t > 100) {
                            t = 0;
                        }
                        if (receivedBuffer != null) {
                            nBytes = receivedBuffer.length;
                        }
                        inDelay--;
                        Utils.delayMs(2);
                        if (upgradeCancelled) break;
                    }
                    int receivedByte = Constants.Error_NoResponse;
                    if (nBytes > 2) {
                        receivedByte = receivedBuffer[1] << 4;
                        receivedByte = receivedBuffer[2] | receivedByte;
                    } else if (nBytes > 0) {
                        receivedByte = Constants.Error_Read;
                    }

                    if (receivedByte == Constants.Error_OK) {
                        bytesSent += frameSize;
                        retries = 0;
                    } else {
                        if (++retries < 4) {
                            index -= frameSize;
                            Utils.delayMs(10);
                        } else {
                            switch (receivedByte) {
                                case Constants.Error_CRC:
                                    upgradeError = 2;
                                    upgradeResultString = "CRC error. File damaged?";
                                    break;
                                case Constants.Error_NoResponse:
                                    upgradeError = 3;
                                    upgradeResultString = "MegaDrum is not responding";
                                    break;
                                case Constants.Error_Read:
                                    upgradeError = 4;
                                    upgradeResultString = "Read error. Bad communication?";
                                    break;
                                default:
                                    upgradeError = 99;
                                    upgradeResultString = "Unknown error";
                                    break;
                            }
                        }
                    }
                    if (upgradeCancelled) {
                        upgradeError = 1;
                        upgradeResultString = "Upgrade cancelled";
                    }
                    if (upgradeError > 0) {
                        break;
                    }
                }
                return 0;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }

    public void setInFirmwareUpgrade(Boolean inUpgrade) {
        isInFirmwareUpgrade = inUpgrade;
    }

    public void cancelUpgrade() {
        upgradeCancelled = true;
    }

    public int getUpgradeError() {
        return upgradeError;
    }

    public String getUpgradeString() {
        return upgradeResultString;
    }

    public Boolean isMidiOpen() {
        return midiHandler.isMidiOpen();
    }

    public void closeAllPorts() {
        midiHandler.closeAllPorts();
    }

}
