package org.nick.nfc.seaccess;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import sasc.emv.EMVTags;
import sasc.emv.EMVUtil;
import sasc.iso7816.AID;
import sasc.iso7816.BERTLV;
import sasc.iso7816.Tag;
import sasc.iso7816.TagImpl;
import sasc.iso7816.TagValueType;
import sasc.util.Util;

public class SecurityDomainFCI {

    public static final Tag SECURITY_DOMAIN_MANAGEMENT_DATA = new TagImpl("73",
            TagValueType.BINARY, "Security domain management data",
            "Security domain management data");
    public static final Tag APPLICATION_PRODUCTION_LIFECYCLE_DATA = new TagImpl(
            "9f6e", TagValueType.BINARY,
            "Application production life cycle data",
            "Application production life cycle data");
    public static final Tag DATA_FIELD_MAX_LENGTH = new TagImpl("9f65",
            TagValueType.BINARY,
            "Maximum length of data field in command message",
            "Maximum length of data field in command message");

    public static final Tag OID = new TagImpl("06", TagValueType.BINARY,
            "Universal OID tag", "Universal OID tag");
    public static final Tag CARD_MANAGEMENT_TYPE_AND_VERSION_OID = new TagImpl(
            "60", TagValueType.BINARY, "Card management type and version",
            "Card management type and version");
    public static final Tag CARD_IDENTIFICATION_SCHEME_OID = new TagImpl("63",
            TagValueType.BINARY, "Card identification scheme",
            "Card identification scheme");
    public static final Tag SECURE_CHANNEL_OID = new TagImpl("64",
            TagValueType.BINARY,
            "Secure channel version and implementation options",
            "Secure channel version and implementation options");

    public static final Tag CARD_CONFIGURATION_DETAILS = new TagImpl("65",
            TagValueType.BINARY, "Card configuration details",
            "Card configuration details");
    public static final Tag CARD_CHIP_DETAILS = new TagImpl("66",
            TagValueType.BINARY, "Card/chip details", "Card/chip details");

    private static String GLOBAL_PLATFORM_OID = "2A864886FC6B";


    private AID securityManagerAid;
    private int dataFieldMaxLength;
    private String applicationProductionLifecycleData;
    private String tagAllocationAuthorityOID;
    private String cardManagementTypeAndVersion;
    private String cardIdentificationScheme;
    private String gpVersion;
    private String secureChannelVersion;
    private String cardConfigurationDetails;
    private String cardChipDetails;

    private SecurityDomainFCI() {
    }

    public static SecurityDomainFCI parse(byte[] raw) {
        SecurityDomainFCI result = new SecurityDomainFCI();

        BERTLV tlv = EMVUtil.getNextTLV(new ByteArrayInputStream(raw));
        if (tlv.getTag().equals(EMVTags.FCI_TEMPLATE)) {
            ByteArrayInputStream templateStream = tlv.getValueStream();
            while (templateStream.available() >= 2) {
                tlv = EMVUtil.getNextTLV(templateStream);
                if (tlv.getTag().equals(EMVTags.DEDICATED_FILE_NAME)) {
                    result.securityManagerAid = new AID(tlv.getValueBytes());
                } else if (tlv.getTag()
                        .equals(EMVTags.FCI_PROPRIETARY_TEMPLATE)) {
                    ByteArrayInputStream fciBis = new ByteArrayInputStream(
                            tlv.getValueBytes());
                    int totalLen = fciBis.available();
                    int templateLen = tlv.getLength();
                    while (fciBis.available() > (totalLen - templateLen)) {
                        tlv = EMVUtil.getNextTLV(fciBis);
                        if (tlv.getTag()
                                .equals(SECURITY_DOMAIN_MANAGEMENT_DATA)) {
                            ByteArrayInputStream sdmBis = new ByteArrayInputStream(
                                    tlv.getValueBytes());
                            int diff = sdmBis.available() - tlv.getLength();
                            while (sdmBis.available() > diff) {
                                tlv = EMVUtil.getNextTLV(sdmBis);
                                if (tlv.getTag().equals(OID)) {
                                    result.tagAllocationAuthorityOID = Hex
                                            .toHex(tlv.getValueBytes());
                                    result.tagAllocationAuthorityOID = result.tagAllocationAuthorityOID
                                            .replace(GLOBAL_PLATFORM_OID,
                                                    "globalPlatform ");
                                } else if (tlv.getTag().equals(
                                        CARD_MANAGEMENT_TYPE_AND_VERSION_OID)) {
                                    ByteArrayInputStream cmBis = new ByteArrayInputStream(
                                            tlv.getValueBytes());
                                    tlv = EMVUtil.getNextTLV(cmBis);
                                    if (tlv.getTag().equals(OID)) {
                                        result.cardManagementTypeAndVersion = Hex
                                                .toHex(tlv.getValueBytes());
                                        result.cardManagementTypeAndVersion = result.cardManagementTypeAndVersion
                                                .replace(GLOBAL_PLATFORM_OID,
                                                        "globalPlatform ");
                                        //2A864886FC6B 02 v == {globalPlatform 2 v}
                                        int prefixLength = 7;
                                        int valueLength = tlv.getValueBytes().length;
                                        byte[] version = Arrays.copyOfRange(
                                                tlv.getValueBytes(),
                                                prefixLength, valueLength);
                                        StringBuilder buff = new StringBuilder();
                                        for (int i = 0; i < version.length; i++) {
                                            buff.append(0xff & version[i]);
                                            if (i != version.length - 1) {
                                                buff.append(".");
                                            }
                                        }
                                        result.gpVersion = buff.toString();
                                    }
                                } else if (tlv.getTag().equals(
                                        CARD_IDENTIFICATION_SCHEME_OID)) {
                                    ByteArrayInputStream cisBis = new ByteArrayInputStream(
                                            tlv.getValueBytes());
                                    tlv = EMVUtil.getNextTLV(cisBis);
                                    if (tlv.getTag().equals(OID)) {
                                        result.cardIdentificationScheme = Hex
                                                .toHex(tlv.getValueBytes());
                                        result.cardIdentificationScheme = result.cardIdentificationScheme
                                                .replace(GLOBAL_PLATFORM_OID,
                                                        "globalPlatform ");
                                    }
                                } else if (tlv.getTag().equals(
                                        SECURE_CHANNEL_OID)) {
                                    int len = tlv.getValueBytes().length;
                                    result.secureChannelVersion = String
                                            .format("SC%02d (options: %02X)",
                                                    tlv.getValueBytes()[len - 2],
                                                    tlv.getValueBytes()[len - 1]);
                                } else if (tlv.getTag().equals(
                                        CARD_CONFIGURATION_DETAILS)) {
                                    result.cardConfigurationDetails = Hex
                                            .toHex(tlv.getValueBytes());
                                } else if (tlv.getTag().equals(
                                        CARD_CHIP_DETAILS)) {
                                    result.cardChipDetails = Hex.toHex(tlv
                                            .getValueBytes());
                                }
                            }
                        } else if (tlv.getTag().equals(
                                APPLICATION_PRODUCTION_LIFECYCLE_DATA)) {
                            result.applicationProductionLifecycleData = Hex.toHex(tlv.getValueBytes());
                        } else if (tlv.getTag().equals(DATA_FIELD_MAX_LENGTH)) {
                            // check length?
                            if (tlv.getValueBytes().length == 1) {
                                result.dataFieldMaxLength = 0xff & tlv
                                        .getValueBytes()[0];
                            } else if (tlv.getValueBytes().length == 2) {
                                result.dataFieldMaxLength = Util.byte2Short(
                                        tlv.getValueBytes()[0],
                                        tlv.getValueBytes()[1]);
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(
                    "Invalid Security Domain FCI format");
        }

        return result;
    }

    public AID getSecurityManagerAid() {
        return securityManagerAid;
    }

    public int getDataFieldMaxLength() {
        return dataFieldMaxLength;
    }

    public String getApplicationProductionLifecycleData() {
        return applicationProductionLifecycleData;
    }

    public String getTagAllocationAuthorityOID() {
        return tagAllocationAuthorityOID;
    }

    public String getCardManagementTypeAndVersion() {
        return cardManagementTypeAndVersion;
    }

    public String getCardIdentificationScheme() {
        return cardIdentificationScheme;
    }

    public String getGpVersion() {
        return gpVersion;
    }

    public String getSecureChannelVersion() {
        return secureChannelVersion;
    }

    public String getCardConfigurationDetails() {
        return cardConfigurationDetails;
    }

    public String getCardChipDetails() {
        return cardChipDetails;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("Security Domain FCI");
        buff.append("\n");
        buff.append("  AID: " + securityManagerAid.toString());
        buff.append("\n");
        buff.append("  Data field max length: " + dataFieldMaxLength);
        buff.append("\n");
        buff.append("  Application prod. life cycle data: "
                + applicationProductionLifecycleData);
        buff.append("\n");
        buff.append("  Tag allocation authority (OID): "
                + tagAllocationAuthorityOID);
        buff.append("\n");
        buff.append("  Card management type and version (OID): "
                + cardManagementTypeAndVersion);
        buff.append("\n");
        buff.append("  Card identification scheme (OID): "
                + cardIdentificationScheme);
        buff.append("\n");
        buff.append("  Global Platform version: " + gpVersion);
        buff.append("\n");
        buff.append("  Secure channel version: " + secureChannelVersion);
        buff.append("\n");
        buff.append("  Card config details: " + cardConfigurationDetails);
        buff.append("\n");
        buff.append("  Card/chip details: " + cardChipDetails);

        return buff.toString();
    }


}
