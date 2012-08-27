package org.nick.nfc.seaccess;

import sasc.iso7816.AID;

public class MifareManagerCommands {

    public static final AID MIFARE_MANAGER_AID = new AID("A0000004763030");

    public static final byte[] SELECT_MIFARE_MANAGER_COMMAND = { (byte) 0x00,
            (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07, (byte) 0xA0,
            0x00, 0x00, 0x04, 0x76, 0x30, 0x30, 0x00 };

}
