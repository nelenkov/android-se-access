package org.nick.nfc.seaccess;

import java.io.ByteArrayInputStream;

import sasc.emv.EMVTags;
import sasc.emv.EMVUtil;
import sasc.iso7816.AID;
import sasc.iso7816.BERTLV;
import sasc.util.Util;

public class WalletControllerFCI {

    private AID aid;
    private short version;

    private WalletControllerFCI() {
    }

    public static WalletControllerFCI parse(byte[] raw) {
        WalletControllerFCI result = new WalletControllerFCI();

        BERTLV tlv = EMVUtil.getNextTLV(new ByteArrayInputStream(raw));
        if (tlv.getTag().equals(EMVTags.FCI_TEMPLATE)) {
            ByteArrayInputStream templateStream = tlv.getValueStream();
            while (templateStream.available() >= 2) {
                tlv = EMVUtil.getNextTLV(templateStream);
                if (tlv.getTag().equals(EMVTags.DEDICATED_FILE_NAME)) {
                    result.aid = new AID(tlv.getValueBytes());
                } else if (tlv.getTag()
                        .equals(EMVTags.FCI_PROPRIETARY_TEMPLATE)) {
                    ByteArrayInputStream fciBis = new ByteArrayInputStream(
                            tlv.getValueBytes());
                    int totalLen = fciBis.available();
                    int templateLen = tlv.getLength();
                    while (fciBis.available() > (totalLen - templateLen)) {
                        tlv = EMVUtil.getNextTLV(fciBis);
                        if (tlv.getTag().equals(
                                EMVTags.RESPONSE_MESSAGE_TEMPLATE_1)) {
                            int len = tlv.getValueBytes().length;
                            result.version = Util.byte2Short(
                                    tlv.getValueBytes()[len - 2],
                                    tlv.getValueBytes()[len - 1]);
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid Wallet FCI response");
        }

        return result;
    }

    public AID getAid() {
        return aid;
    }

    public short getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("Wallet controller applet");
        buff.append("\n");
        buff.append("AID: " + aid.toString());
        buff.append("version: v" + version);

        return buff.toString();
    }


}
