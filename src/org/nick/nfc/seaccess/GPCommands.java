package org.nick.nfc.seaccess;

public class GPCommands {

    public static final byte[] EMPTY_SELECT = { (byte) 0x00, (byte) 0xa4,
            (byte) 0x04, (byte) 0x00, (byte) 0x00 };

    public static final byte[] GET_ISSUER_ID_COMMAND = { (byte) 0x80,
            (byte) 0xCA, (byte) 0x00, 0x42, 0x00 };

    public static final byte[] GET_CARD_DATA = { (byte) 0x80, (byte) 0xCA,
            (byte) 0x00, 0x66, 0x00 };

    public static final byte[] GET_KEY_INFORMATION_TEMPLATE_COMMAND = {
            (byte) 0x80, (byte) 0xCA, (byte) 0x00, (byte) 0xe0, 0x00 };

    public static final byte[] GET_KEY_VERSION_SEQUENCE_COUNTER_COMMAND = {
            (byte) 0x80, (byte) 0xCA, (byte) 0x00, (byte) 0xc1, 0x00 };

    public static final byte[] SELECT_CARD_MANAGER_COMMAND = { (byte) 0x00,
            (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x08, (byte) 0xA0,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00 };

    public static final byte[] GET_CPLC_COMMAND = { (byte) 0x80, (byte) 0xCA,
            (byte) 0x9F, (byte) 0x7F, 0x00 };

    private GPCommands() {
    }
}
