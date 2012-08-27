package org.nick.nfc.seaccess;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import sasc.emv.EMVUtil;
import sasc.iso7816.BERTLV;
import sasc.iso7816.Tag;
import sasc.iso7816.TagImpl;
import sasc.iso7816.TagValueType;

public class CPLC {

    public static final Tag CPLC_TAG = new TagImpl("9f7f", TagValueType.BINARY,
            "Card Production Life Cycle Data",
            "Card Production Life Cycle Data");

    private static Map<String, Integer> FIELD_NAMES_LENGTHS = new LinkedHashMap<String, Integer>();

    private Map<String, String> fields = new LinkedHashMap<String, String>();

    static {
        FIELD_NAMES_LENGTHS.put("IC Fabricator", 2);
        FIELD_NAMES_LENGTHS.put("IC Type", 2);
        FIELD_NAMES_LENGTHS.put("Operating System Provider Identifier", 2);
        FIELD_NAMES_LENGTHS.put("Operating System Release Date", 2);
        FIELD_NAMES_LENGTHS.put("Operating System Release Level", 2);
        FIELD_NAMES_LENGTHS.put("IC Fabrication Date", 2);
        FIELD_NAMES_LENGTHS.put("IC Serial Number", 4);
        FIELD_NAMES_LENGTHS.put("IC Batch Identifier", 2);
        FIELD_NAMES_LENGTHS.put("IC ModuleFabricator", 2);
        FIELD_NAMES_LENGTHS.put("IC ModulePackaging Date", 2);
        FIELD_NAMES_LENGTHS.put("ICC Manufacturer", 2);
        FIELD_NAMES_LENGTHS.put("IC Embedding Date", 2);
        FIELD_NAMES_LENGTHS.put("Prepersonalizer Identifier", 2);
        FIELD_NAMES_LENGTHS.put("Prepersonalization Date", 2);
        FIELD_NAMES_LENGTHS.put("Prepersonalization Equipment", 4);
        FIELD_NAMES_LENGTHS.put("Personalizer Identifier", 2);
        FIELD_NAMES_LENGTHS.put("Personalization Date", 2);
        FIELD_NAMES_LENGTHS.put("Personalization Equipment", 4);
    }


    private CPLC() {
    }

    public static CPLC parse(byte[] raw) {
        CPLC result = new CPLC();
        BERTLV tlv = EMVUtil.getNextTLV(new ByteArrayInputStream(raw));
        if (!tlv.getTag().equals(CPLC_TAG)) {
            throw new IllegalArgumentException("Not a valid CPLC. Found tag: "
                    + tlv.getTag());
        }

        int idx = 0;
        byte[] cplc = tlv.getValueBytes();
        for (String fieldName : FIELD_NAMES_LENGTHS.keySet()) {
            int length = FIELD_NAMES_LENGTHS.get(fieldName);
            byte[] value = Arrays.copyOfRange(cplc, idx, idx + length);
            idx += length;
            result.fields.put(fieldName, Hex.toHex(value));
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("CPLC");
        buff.append("\n");
        for (String key : fields.keySet()) {
            buff.append(String.format("  %s: %s", key, fields.get(key)));
            buff.append("\n");
        }

        return buff.toString();
    }
}
