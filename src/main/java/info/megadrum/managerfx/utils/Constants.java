package info.megadrum.managerfx.utils;

import javafx.scene.paint.Color;

public interface Constants {
    String WINDOWS_TITLE = "MegaDrumManager FX";
    String WINDOWS_TITLE_SHORT = "MDM FX: ";
    String MD_VERSION = "20240120";
    int MD_MINIMUM_VERSION = 20200329;
    String WARNING_VERSION = "<html><font size=4>For full compatibilty between MegaDrum Manager and MegaDrum</font></html>\n" +
            "<html><font size=4>you should upgrade MegaDrum to version " + MD_MINIMUM_VERSION + " or newer</font></html>";
    Double MD_CONFIG_VERSION = 0.5;
    int MAX_INPUTS = 55;
    int CONFIGS_COUNT = 32;
    int PANEL_HIDE = 0;
    int PANEL_SHOW = 1;
    int PANEL_DETACH = 2;
    int PANELS_COUNT = 5;
    int MCU_TYPE_STM32F205TEST1 = 9;
    String[] MCU_TYPES = {"Unknown", "Atmega644", "Atmega1284", "STM32F103VBT6",
            "STM32F103RBT6", "STM32F103RCT6", "STM32F205RBT6", "STM32F205RCT6",
            "STM32F205LITE", "STM32F205TEST1", "STM32F446RCT6", "STM32F446RET6",
            "STM32F405RGT6", "GD32F405RGT6", "STM32TEST14"
    };
    int Error_NoResponse = 0x00;
    int Error_OK = 0x11;
    int Error_CRC = 0x22;
    int Error_Read = 0x23;
    String MD_MANAGER_CONFIG = System.getProperty("user.home") + System.getProperty("file.separator") + "megadrummanagerfx.cfg";
    int PADS_COUNT = 55;
    int CURVES_COUNT = 16;
    int CUSTOM_NAMES_MAX = 32;
    int CONFIG_NAMES_MAX = 127;
    byte SYSEX_START = (byte) 0xf0;
    byte SYSEX_END = (byte) 0xf7;
    byte MD_SYSEX = (byte) 0x70;
    byte MD_SYSEX_MISC = (byte) 0x01;
    byte MD_SYSEX_MISC_SIZE = 19;
    byte MD_SYSEX_PEDAL = (byte) 0x02;
    byte MD_SYSEX_PEDAL_SIZE_OLD = 79;
    byte MD_SYSEX_PEDAL_SIZE = 95;
    byte MD_SYSEX_PAD = (byte) 0x03;
    byte MD_SYSEX_PAD_SIZE = 36;
    byte MD_SYSEX_3RD = (byte) 0x04;
    byte MD_SYSEX_3RD_SIZE = 16;
    byte MD_SYSEX_VERSION = (byte) 0x05;
    byte MD_SYSEX_VERSION_SIZE = 13;
    byte MD_SYSEX_CURVE = (byte) 0x06;
    byte MD_SYSEX_CURVE_SIZE = 24;
    byte MD_SYSEX_POS = (byte) 0x07;
    byte MD_SYSEX_POS_SIZE = 12;
    byte MD_SYSEX_CUSTOM_NAME = (byte) 0x08;
    byte MD_SYSEX_CUSTOM_NAME_SIZE = 22;
    byte MD_SYSEX_GLOBAL_MISC = (byte) 0x09;
    byte MD_SYSEX_GLOBAL_MISC_SIZE = 13;
    byte MD_SYSEX_BOOTLOADER = (byte) 0x0b;
    byte MD_SYSEX_MCU_TYPE = (byte) 0x0c;
    byte MD_SYSEX_MCU_TYPE_SIZE = 7;
    byte MD_SYSEX_CONFIG_NAME = (byte) 0x0d;
    byte MD_SYSEX_CONFIG_NAME_SIZE = 30;
    byte MD_SYSEX_CONFIG_COUNT = (byte) 0x0e;
    byte MD_SYSEX_CONFIG_COUNT_SIZE = 6;
    byte MD_SYSEX_CONFIG_SAVE = (byte) 0x0f;
    byte MD_SYSEX_CONFIG_CURRENT = (byte) 0x10;
    byte MD_SYSEX_CONFIG_CURRENT_SIZE = 6;
    byte MD_SYSEX_CONFIG_LOAD = (byte) 0x11;
    byte MD_SYSEX_PEDAL_LEVEL_RAW = (byte) 0x12;
    byte MD_SYSEX_PEDAL_LEVEL_RAW_SIZE = 9;
    String[] MD_SYSEX_NAMES = {
            "Undefined", "Misc", "Pedal", "Input", "3rd Zone", "Version", "Curve", "Positional",    //0-7
            "Custom Name", "Global Misc", "Undefined", "Bootloader", "MCU Type", "Config Name", "Config Count", "Config Save",    //8-f
            "Config Current", "Config Load", "Pedal Level Raw"                        //10-
    };

    int MD_SYSEX_STATUS_OK = 0;
    int MD_SYSEX_STATUS_WORKING = 1;
    int MD_SYSEX_STATUS_TIMEOUT = 2;
    int MD_SYSEX_STATUS_MISMATCH = 3;
    int MD_SYSEX_STATUS_MIDI_IS_NOT_OPEN = 4;
    int MD_SYSEX_STATUS_MIDI_INIT_ERROR = 5;
    String[] MD_SYSEX_STATUS_NAMES = {
            "Sysex: OK", "Sysex: Working", "Timout", "Mismatch", "MIDI is NOT Open", "MIDI Init Error"
    };

    String[] CURVES_LIST = {"LinearCustom1", "Log1Custom2", "Log2Custom3", "Log3Custom4", "Log4Custom5",
            "Exp1Custom6", "Exp2Custom7", "S1Custom8", "S2Custom9", "Strong1Custom10", "Strong2Custom11", "MaxCustom12",
            "Custom13", "Custom14", "Custom15", "Custom16"};
    String[] PADS_NAMES_LIST = {"Kick ", "HiHatB",
            "HiHatE", "SnareH", "SnareR", "RideB", "RideE", "CrashB", "CrashE",
            "Tom1H", "Tom1R", "Tom2H", "Tom2R", "Tom3H", "Tom3R", "Tom4H",
            "Tom4R", "Aux1H", "Aux1R", "Aux2H", "Aux2R", "Aux3H", "Aux3R",
            "Aux4H", "Aux4R", "Aux5H", "Aux5R", "Aux6H", "Aux6R", "Aux7H",
            "Aux7R", "Aux8H", "Aux8R", "Aux9H", "Aux9R", "Aux10H", "Aux10R",
            "Aux11H", "Aux11R", "Aux12H", "Aux12R", "Aux13H", "Aux13R",
            "Aux14H", "Aux14R", "Aux15H", "Aux15R", "Aux16H", "Aux16R",
            "Aux17H", "Aux17R", "Aux18H", "Aux18R", "Aux19H", "Aux19R"};
    String[] CUSTOM_PADS_NAMES_LIST = {"Kick", "HiHat",
            "Snare1", "Snare2", "Ride1", "Ride2", "Crash1", "Crash2", "Tom1",
            "Tom2", "Tom3", "Tom4", "Tom5", "Tom6", "Chinese1", "Chinese2",
            "Tambrn1", "Tambrn2", "Cowbell1", "Cowbell2", "Bongo1", "Bongo2",
            "Conga1", "Conga2", "Conga3", "Timbale1", "Timbale2", "Agogo1",
            "Agogo2", "Claves1", "Claves2", "Wood1", "Wood2", "Cuica1",
            "Cuica2", "Triangl1", "Triangl2", "Bass1", "Bass2", "Sizzle1",
            "Sizzle2", "Splash1", "Splash2", "Swish1", "Swish2", "Clash1",
            "Clash2", "Chenda1", "Chenda2", "Tenor1", "Tenor2", "Timpani1",
            "Timpani2", "Timpani3", "Timpani4", "Crash3", "Crash4", "Crash5",
            "Splash3", "Splash4", "Splash5", "RotoTom1", "RotoTom2", "RotoTom3",
            "Sticks1", "Sticks2", "Sticks3", "HndClap1", "HndClap2", "HndClap3",
            "FngrSnap", "Mtronome"
    };
    String MIDI_NOTE_ON_COLOR = "#007f00";
    String MIDI_NOTE_OFF_COLOR = "#2fbf2f";
    String MIDI_AFTERTOUCH_COLOR = "#007f7f";
    String MIDI_CC_COLOR = "#7f7f00";
    String MIDI_PC_COLOR = "#7f007f";
    String MIDI_CH_PR_COLOR = "#00007f";
    String MIDI_PITCH_COLOR = "#7f0000";

    int SYNC_STATE_UNKNOWN = 0;
    Color SYNC_STATE_UNKNOWN_COLOR = Color.BLUE;
    int SYNC_STATE_SYNCED = 1;
    Color SYNC_STATE_SYNCED_COLOR = Color.BLACK;
    int SYNC_STATE_NOT_SYNCED = 2;
    Color SYNC_STATE_NOT_SYNCED_COLOR = Color.RED;
    int SYNC_STATE_RECEIVED = 3;

    int VALUE_TYPE_BOOLEAN = 0;
    int VALUE_TYPE_INT = 1;

    String[] NOTES_NAMES = {
            "C ",
            "C#",
            "D ",
            "D#",
            "E ",
            "F ",
            "F#",
            "G ",
            "G#",
            "A ",
            "A#",
            "B "
    };

    String HELP_ABOUT = "<html><font size=5>MegaDrum Manager FX</font></html>\n" +
            "<html><font size=5>Version: " + MD_VERSION + "</font></html>\n" +
            "<html><font size=5>www.megadrum.info<br></font></html>\n" +
            "<html><font size=5>\u00a9 2007-2017 Dmitri Skachkov<br></font></html>";
    String HELP_ABOUT_MMJ = "\n<html><font size=4>mmj Library is included with MegaDrum Manager for Mac OS X<br></font></html>\n" +
            "<html><font size=4>mmj is copyright of Humatic<br></font></html>\n" +
            "<html><font size=4>See www.humatic.de/htools/mmj.htm for details</font></html>";
    String MIDI_PORTS_WARNING =
            "<html><font size=4>Before using MegaDrum Manager</font></html>\n" +
                    "<html><font size=4>you first must set MIDI In/Out ports in Main->Options!!!</font></html>";
    String UPGRADE_INSTRUCTION_ATMEGA =
            "Upgrade instruction for:\n" +
                    "    Atmega based MegaDrum\n" +
                    "    ARM based MegaDrum in 'recovery' mode\n" +
                    "\n" +
                    "1. Make sure you have a backup copy of your MegaDrum config.\n" +
                    "2. Select the MegaDrum firmware file.\n" +
                    "3. Disconnect (power off) MegaDrum.\n" +
                    "4. While holding MegaDrum's button LEFT connect (power on) MegaDrum.\n" +
                    "5. Skip this step on an ARM based MegaDrum.\n" +
                    "    By pressing MegaDrum's button UP select a correct MegaDrum crystal frequency.\n" +
                    "6. Skip this step on an ARM based MegaDrum.\n" +
                    "    Press MegaDrum's button DOWN. MegaDrum LCD will show 'StartUpdateOnPC'.\n" +
                    "7. Click button Start.\n" +
                    "8. Wait for the upgrade to finish.\n" +
                    "9. Click button Close.";
    String UPGRADE_INSTRUCTION_ARM =
            "Upgrade instruction for:\n" +
                    "    ARM based MegaDrum\n" +
                    "\n" +
                    "1. Make sure you have a backup copy of your MegaDrum config.\n" +
                    "2. Select the MegaDrum firmware file.\n" +
                    "3. Click button Start.\n" +
                    "4. Wait for the upgrade to finish.\n" +
                    "5. Click button Close.";

    Double FX_BUTTONS_FONT_MAX_SIZE = 10.0;
    Double FX_BUTTONS_FONT_SCALE = 0.28;
    Double FX_COMBOBOX_FONT_MAX_SIZE = 10.0;
    Double FX_COMBOBOX_FONT_SCALE = 0.28;
    Double FX_TITLEBARS_FONT_MIN_SIZE = 4.4;
    Double FX_TITLEBARS_FONT_SCALE = 0.6;
    Double FX_SUB_TITLEBARS_FONT_SCALE = 0.5;
    Double FX_TABS_FONT_SCALE = 0.35;
    Double FX_MISC_CONTROL_WIDTH_MUL = 1.2;
    Double FX_PEDAL_CONTROL_WIDTH_MUL = 1.4;
    Double FX_INPUT_CONTROL_WIDTH_MUL = 1.3;
    Double FX_CONTROL_H_TO_W = 0.12;
    Double FX_MISC_LABEL_WIDTH_MUL = 0.60;
    Double FX_PEDAL_LABEL_WIDTH_MUL = 0.45;
    Double FX_INPUT_LABEL_WIDTH_MUL = 0.36;
    int FX_SPINNER_TYPE_STANDARD = 0;
    int FX_SPINNER_TYPE_SYSEX = 1;

    String[] PAD_FUNCTION_LIST = {"Normal", "ProgramChange", "CutoOff"};
    String[] PAD_COMPRESSION_LIST = {"0", "1", "2", "3", "4", "5", "6", "7"};
    String[] PAD_LEVEL_SHIFT_LIST = {"0", "8", "16", "24", "32", "40", "48", "56"};
    String[] PAD_XTALK_LEVEL_LIST = {"0", "1", "2", "3", "4", "5", "6", "7"};
    String[] PAD_XTALK_GROUP_LIST = {"0", "1", "2", "3", "4", "5", "6", "7"};
    String[] PAD_GAIN_LIST = {"0", "1", "2", "3", "4", "5", "6", "7", "8"};
    String[] PAD_DYN_LEVEL_LIST = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"};
    String[] PAD_DYN_TIME_LIST = {"0", "4", "8", "12", "16", "20", "24", "28", "32", "36", "40", "44", "48", "52", "56", "60"};
    String[] PAD_POS_LEVEL_LIST = {"Disabled", "1", "2", "3"};
    String[] ROLL_SMOOTH_LEVEL_LIST = {"Disabled", "1", "2", "3"};
    String[] PAD_TYPE_HEAD_LIST = {"Single Piezo", "Dual or 3way Yamaha", "3way Roland"};
    String[] PAD_TYPE_EDGE_LIST = {"Piezo", "Switch"};
    int PAD_TYPE_HEAD = 0;
    int PAD_TYPE_EDGE = 1;
    int CONTROL_CHANGE_EVENT_LEFT_INPUT = 0;
    int CONTROL_CHANGE_EVENT_RIGHT_INPUT = 1;
    int CONTROL_CHANGE_EVENT_3RD_INPUT = 2;
    int CONTROL_CHANGE_EVENT_NAME = 1;
    int CONTROL_CHANGE_EVENT_NOTE_LINKED = 2;
    int CONTROL_CHANGE_EVENT_NOTE_MAIN = 3;

    int CONTROL_CHANGE_EVENT_CURVE = 0;
    int CUSTOM_NAME_CHANGE_TEXT_START = 64;
    int CUSTOM_NAME_CHANGE_GET_START = CUSTOM_NAME_CHANGE_TEXT_START + CUSTOM_NAMES_MAX;
    int CUSTOM_NAME_CHANGE_SEND_START = CUSTOM_NAME_CHANGE_GET_START + CUSTOM_NAMES_MAX;

    String[] PEDAL_TYPES_LIST = {"Pot", "FootContr"};

    int INPUT_VALUE_ID_MIN = 100;
    int INPUT_VALUE_ID_NAME = INPUT_VALUE_ID_MIN;
    int INPUT_VALUE_ID_DISABLED = INPUT_VALUE_ID_NAME + 1;
    int INPUT_VALUE_ID_NOTE = INPUT_VALUE_ID_DISABLED + 1;
    int INPUT_VALUE_ID_ALT_NOTE = INPUT_VALUE_ID_NOTE + 1;
    int INPUT_VALUE_ID_PRESSROLL_NOTE = INPUT_VALUE_ID_ALT_NOTE + 1;
    int INPUT_VALUE_ID_CHANNEL = INPUT_VALUE_ID_PRESSROLL_NOTE + 1;
    int INPUT_VALUE_ID_FUNCTION = INPUT_VALUE_ID_CHANNEL + 1;
    int INPUT_VALUE_ID_CURVE = INPUT_VALUE_ID_FUNCTION + 1;
    int INPUT_VALUE_ID_COMPRESSION = INPUT_VALUE_ID_CURVE + 1;
    int INPUT_VALUE_ID_SHIFT = INPUT_VALUE_ID_COMPRESSION + 1;
    int INPUT_VALUE_ID_XTALK_LEVEL = INPUT_VALUE_ID_SHIFT + 1;
    int INPUT_VALUE_ID_XTALK_GROUP = INPUT_VALUE_ID_XTALK_LEVEL + 1;
    int INPUT_VALUE_ID_THRESHOLD = INPUT_VALUE_ID_XTALK_GROUP + 1;
    int INPUT_VALUE_ID_GAIN = INPUT_VALUE_ID_THRESHOLD + 1;
    int INPUT_VALUE_ID_HIGHLEVEL_AUTO = INPUT_VALUE_ID_GAIN + 1;
    int INPUT_VALUE_ID_HIGHLEVEL = INPUT_VALUE_ID_HIGHLEVEL_AUTO + 1;
    int INPUT_VALUE_ID_RETRIGGER = INPUT_VALUE_ID_HIGHLEVEL + 1;
    int INPUT_VALUE_ID_DYN_LEVEL = INPUT_VALUE_ID_RETRIGGER + 1;
    int INPUT_VALUE_ID_DYN_TIME = INPUT_VALUE_ID_DYN_LEVEL + 1;
    int INPUT_VALUE_ID_MINSCAN = INPUT_VALUE_ID_DYN_TIME + 1;
    int INPUT_VALUE_ID_POS_LEVEL = INPUT_VALUE_ID_MINSCAN + 1;
    int INPUT_VALUE_ID_POS_LOW = INPUT_VALUE_ID_POS_LEVEL + 1;
    int INPUT_VALUE_ID_POS_HIGH = INPUT_VALUE_ID_POS_LOW + 1;
    int INPUT_VALUE_ID_TYPE = INPUT_VALUE_ID_POS_HIGH + 1;
    int INPUT_VALUE_ID_ROLL_SMOOTH = INPUT_VALUE_ID_TYPE + 1;
    int INPUT_VALUE_ID_EXTRA_FALSE = INPUT_VALUE_ID_ROLL_SMOOTH + 1;
    int INPUT_VALUE_ID_MAX = INPUT_VALUE_ID_EXTRA_FALSE;

    int THIRD_ZONE_VALUE_ID_MIN = INPUT_VALUE_ID_MAX + 1;
    int THIRD_ZONE_VALUE_ID_DISABLED = THIRD_ZONE_VALUE_ID_MIN;
    int THIRD_ZONE_VALUE_ID_NOTE = THIRD_ZONE_VALUE_ID_DISABLED + 1;
    int THIRD_ZONE_VALUE_ID_ALT_NOTE = THIRD_ZONE_VALUE_ID_NOTE + 1;
    int THIRD_ZONE_VALUE_ID_PRESSROLL_NOTE = THIRD_ZONE_VALUE_ID_ALT_NOTE + 1;
    int THIRD_ZONE_VALUE_ID_DAMPENED_NOTE = THIRD_ZONE_VALUE_ID_PRESSROLL_NOTE + 1;
    int THIRD_ZONE_VALUE_ID_THRESHOLD = THIRD_ZONE_VALUE_ID_DAMPENED_NOTE + 1;
}
