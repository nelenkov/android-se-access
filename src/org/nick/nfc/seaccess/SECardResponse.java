package org.nick.nfc.seaccess;

import static org.nick.nfc.seaccess.Hex.toHex;

import java.util.Arrays;

import sasc.terminal.CardResponse;

public class SECardResponse implements CardResponse {

    private byte[] data;
    private byte sw1;
    private byte sw2;
    private short sw;

    public SECardResponse(byte[] raw) {
        this.data = getData(raw);
        this.sw = getStatus(raw);
        this.sw1 = raw[raw.length - 2];
        this.sw2 = raw[raw.length - 1];
    }

    public SECardResponse(byte[] data, byte sw1, byte sw2, short sw) {
        this.data = data.clone();
        this.sw1 = sw1;
        this.sw2 = sw2;
        this.sw = sw;
    }

    @Override
    public byte[] getData() {
        return data == null ? new byte[0] : data.clone();
    }

    @Override
    public byte getSW1() {
        return sw1;
    }

    @Override
    public byte getSW2() {
        return sw2;
    }

    @Override
    public short getSW() {
        return sw;
    }

    @Override
    public String toString() {
        String swStr = String.format("%02X", sw);
        if (data != null) {
            return String.format("<-- %s %s", toHex(data), swStr);
        }

        return String.format("<-- %s", swStr);
    }

    public static byte[] getData(byte[] responseApdu) {
        if (responseApdu.length <= 2) {
            return null;
        }

        return Arrays.copyOf(responseApdu, responseApdu.length - 2);
    }

    public static short getStatus(byte[] responseApdu) {
        int len = responseApdu.length;
        return (short) ((responseApdu[len - 2] << 8) | (0xff & responseApdu[len - 1]));
    }

}
