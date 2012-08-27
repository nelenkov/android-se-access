package org.nick.nfc.seaccess;

import sasc.iso7816.AID;

public class WalletControllerCommands {

    public static final AID WALLET_CONTROLLER_AID = new AID(
            "A0 00 00 04 76 20 10");

    public static final byte[] SELECT_WALLET_CONTROLLER_COMMAND = {
            (byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, (byte) 0x07,
            (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x76,
            (byte) 0x20, (byte) 0x10, (byte) 0x00 };

    private WalletControllerCommands() {
    }
}
