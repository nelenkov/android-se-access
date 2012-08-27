package org.nick.nfc.seaccess;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import sasc.emv.EMVUtil;
import sasc.iso7816.BERTLV;
import sasc.iso7816.Tag;
import sasc.iso7816.TagImpl;
import sasc.iso7816.TagValueType;

public class KeyInfo {

    public static final Tag KEY_INFORMATION_TEMPLATE = new TagImpl("E0",
            TagValueType.BINARY, "Key information template",
            "Key information template");

    public static final Tag KEY_INFORMATION_DATA = new TagImpl("C0",
            TagValueType.BINARY, "Key information data", "Key information data");

    private static final int ID_IDX = 0;
    private static final int VERSION_IDX = 1;
    private static final int KEY_TYPE_IDX = 2;
    private static final int LENGTH_IDX = 3;


    private byte id;
    private byte version;
    private byte keyType;
    // bytes
    private byte length;

    public KeyInfo(byte id, byte version, byte keyType, byte length) {
        this.id = id;
        this.version = version;
        this.keyType = keyType;
        this.length = length;
    }

    public static KeyInfo createFromRawData(byte[] raw) {
        return new KeyInfo(raw[ID_IDX], raw[VERSION_IDX], raw[KEY_TYPE_IDX],
                raw[LENGTH_IDX]);
    }

    public byte getId() {
        return id;
    }

    public byte getVersion() {
        return version;
    }

    public byte getKeyType() {
        return keyType;
    }

    public String getKeyTypeAsString() {
        return keyTypeAsString(keyType);
    }

    public byte getLength() {
        return length;
    }

    public static String keyTypeAsString(byte keyType) {
        switch (keyType) {
        case (byte) 0x80:
            return "DES (EBC/CBC)";
        case (byte) 0xA0:
            return "RSA Pub - e (clear)";
        case (byte) 0xA1:
            return "RSA Pub - N (clear)";
        case (byte) 0xA2:
            return "RSA Priv - N";
        case (byte) 0xA3:
            return "RSA Priv - d";
        case (byte) 0xA4:
            return "RSA Priv - CRT P";
        case (byte) 0xA5:
            return "RSA Priv - CRT Q";
        case (byte) 0xA6:
            return "RSA Priv - CRT PQ";
        case (byte) 0xA7:
            return "RSA Priv - CRT DP1";
        case (byte) 0xA8:
            return "RSA Priv - CRT DQ1";
        case (byte) 0xFF:
            return "N/A";
        default:
            //  'A9'-'FE' RFU (asymmetric algorithms)
            //  '81'-'9F' RFU (symmetric algorithms) or anything else
            return "RFU or unknown";
        }
    }

    @Override
    public String toString() {
        return String.format("ID: %d, version: %s, type: %s, length: %d bits",
                id, version, getKeyTypeAsString(), length * 8);
    }

    public static List<KeyInfo> parse(byte[] raw) {
        List<KeyInfo> result = new ArrayList<KeyInfo>();
        BERTLV tlv = EMVUtil.getNextTLV(new ByteArrayInputStream(raw));

        if (tlv.getTag().equals(KEY_INFORMATION_TEMPLATE)) {
            ByteArrayInputStream templateStream = tlv.getValueStream();

            while (templateStream.available() >= 2) {
                tlv = EMVUtil.getNextTLV(templateStream);
                if (tlv.getTag().equals(KEY_INFORMATION_DATA)) {
                    byte[] keyInfoData = tlv.getValueBytes();
                    KeyInfo ki = KeyInfo.createFromRawData(keyInfoData);
                    result.add(ki);
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid key information data");
        }

        return result;
    }


}
