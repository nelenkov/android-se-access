package org.nick.nfc.seaccess;

import java.io.ByteArrayInputStream;

import sasc.emv.ApplicationPriorityIndicator;
import sasc.emv.DDF;
import sasc.emv.EMVApplication;
import sasc.emv.EMVCard;
import sasc.emv.EMVTags;
import sasc.emv.EMVUtil;
import sasc.iso7816.AID;
import sasc.iso7816.BERTLV;
import sasc.util.Util;

public class PPSE {

    // mostly extracted from javaemvreader/EMVUtil.parseFCIDDF()
    public static DDF parse(byte[] data, EMVCard card) {
        DDF ddf = new DDF();
        BERTLV tlv = EMVUtil.getNextTLV(new ByteArrayInputStream(data));

        if (tlv.getTag().equals(EMVTags.FCI_TEMPLATE)) {
            ByteArrayInputStream templateStream = tlv.getValueStream();

            while (templateStream.available() >= 2) {
                tlv = EMVUtil.getNextTLV(templateStream);
                if (tlv.getTag().equals(EMVTags.DEDICATED_FILE_NAME)) {
                    ddf.setName(tlv.getValueBytes());
                } else if (tlv.getTag()
                        .equals(EMVTags.FCI_PROPRIETARY_TEMPLATE)) {
                    ByteArrayInputStream bis2 = new ByteArrayInputStream(
                            tlv.getValueBytes());
                    int totalLen = bis2.available();
                    int templateLen = tlv.getLength();
                    while (bis2.available() > (totalLen - templateLen)) {
                        tlv = EMVUtil.getNextTLV(bis2);

                        if (tlv.getTag().equals(
                                EMVTags.FCI_ISSUER_DISCRETIONARY_DATA)) {
                            ByteArrayInputStream discrStream = new ByteArrayInputStream(
                                    tlv.getValueBytes());
                            int total3Len = discrStream.available();
                            int template3Len = tlv.getLength();
                            while (discrStream.available() > (total3Len - template3Len)) {
                                tlv = EMVUtil.getNextTLV(discrStream);

                                if (tlv.getTag().equals(
                                        EMVTags.APPLICATION_TEMPLATE)) {
                                    ByteArrayInputStream appTemplateStream = new ByteArrayInputStream(
                                            tlv.getValueBytes());
                                    int appTemplateTotalLen = appTemplateStream
                                            .available();
                                    int template4Len = tlv.getLength();
                                    EMVApplication app = new EMVApplication();
                                    while (appTemplateStream.available() > (appTemplateTotalLen - template4Len)) {
                                        tlv = EMVUtil
                                                .getNextTLV(appTemplateStream);

                                        if (tlv.getTag().equals(
                                                EMVTags.AID_CARD)) {
                                            app.setAID(new AID(tlv
                                                    .getValueBytes()));
                                        } else if (tlv.getTag().equals(
                                                EMVTags.APPLICATION_LABEL)) {
                                            // Use only safe print chars, just
                                            // in case
                                            String label = Util
                                                    .getSafePrintChars(tlv
                                                            .getValueBytes());
                                            app.setLabel(label);
                                        } else if (tlv
                                                .getTag()
                                                .equals(EMVTags.APPLICATION_PRIORITY_INDICATOR)) {
                                            ApplicationPriorityIndicator api = new ApplicationPriorityIndicator(
                                                    tlv.getValueBytes()[0]);
                                            app.setApplicationPriorityIndicator(api);
                                        } else {
                                            card.addUnhandledRecord(tlv);
                                        }
                                        card.addApplication(app);
                                    }
                                } else {
                                    card.addUnhandledRecord(tlv);
                                }
                            }
                        } else {
                            card.addUnhandledRecord(tlv);
                        }
                    }
                } else {
                    card.addUnhandledRecord(tlv);
                }
            }
        } else {
            card.addUnhandledRecord(tlv);
        }

        return ddf;
    }
}
