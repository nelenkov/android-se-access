package org.nick.nfc.seaccess;

import static org.nick.nfc.seaccess.Hex.fromHex;
import sasc.emv.ApplicationDefinitionFile;
import sasc.emv.DDF;
import sasc.emv.EMVAPDUCommands;
import sasc.emv.EMVApplication;
import sasc.emv.EMVCard;
import sasc.emv.EMVUtil;
import sasc.emv.SW;
import sasc.iso7816.AID;
import sasc.iso7816.SmartCardException;
import sasc.terminal.CardConnection;
import sasc.terminal.CardResponse;
import sasc.terminal.TerminalException;
import sasc.util.Util;
import android.app.Application;
import android.util.Log;

// mostly copied from javaemvreader/EMVSession.java with some modifications
public class SEEMVSession {

    private static final String TAG = SEEMVSession.class.getSimpleName();

    private EMVCard card = null;
    private CardConnection seConn;

    private boolean cardInitalized = false;

    public static SEEMVSession startSession(Application appCtx,
            CardConnection seConn) {
        if (seConn == null) {
            throw new IllegalArgumentException(
                    "Needs initialized SE connection.");
        }

        return new SEEMVSession(seConn);
    }

    private SEEMVSession(CardConnection seConn) {
        this.seConn = seConn;
    }

    public EMVCard getCurrentCard() {
        return card;
    }

    public EMVCard initCard() throws TerminalException {
        card = new EMVCard(new sasc.iso7816.ATR(seConn.getATR()));

        // ATR file
        String command = "00 A4 00 00 02 2F01";
        CardResponse response = seConn.transmit(fromHex(command));
        if (response.getSW() == SW.SUCCESS.getSW()) {
            card = new EMVCard(new sasc.iso7816.ATR(response.getData()));
        } else {
            Log.d(TAG, "ATR file not found. Will use dummy. Response: "
                    + response);
            card = new EMVCard(
                    new sasc.iso7816.ATR(
                            Hex.fromHex("3B 8A 80 01 00 31 C1 73 C8 40 00 00 90 00 90")));
        }

        // try to select the PPSE (Proximity Payment System Environment)
        // 2PAY.SYS.DDF01
        Log.d(TAG, "SELECT FILE 2PAY.SYS.DDF01 to get the PPSE directory");
        command = EMVAPDUCommands.selectPPSE();
        CardResponse selectPPSEdirResponse = EMVUtil.sendCmd(seConn, command);
        short sw = selectPPSEdirResponse.getSW();
        if (sw == SW.SUCCESS.getSW()) {
            Log.d(TAG, "***************************************************");
            // PPSE is available
            DDF ppse = PPSE.parse(selectPPSEdirResponse.getData(), card);
            Log.d(TAG, "Name: " + new String(ppse.getName()));
            Log.d(TAG, "PPSE DDF: " + ppse.toString());
            card.setType(EMVCard.Type.CONTACTLESS);
            card.setPSE(ppse);

            // loopback command test
            response = EMVUtil.sendCmd(seConn, "80ee00000301020300");
            Log.d(TAG, "loopback response: " + response.toString());
        }

        // Still no applications?
        if (card.getApplications().isEmpty()) {
            Log.d(TAG,
                    "No PSE '2PAY.SYS.DDF01' or application(s) found. Might not be an EMV card. Is Wallet locked?");
        }

        cardInitalized = true;
        return card;
    }

    public void selectApplication(EMVApplication app) throws TerminalException {
        if (app == null) {
            throw new IllegalArgumentException("Parameter 'app' cannot be null");
        }

        if (!cardInitalized) {
            throw new SmartCardException(
                    "Card not initialized. Call initCard() first");
        }

        EMVApplication currentSelectedApp = card.getSelectedApplication();
        if (currentSelectedApp != null
                && app.getAID().equals(currentSelectedApp.getAID())) {
            throw new SmartCardException("Application already selected. AID: "
                    + app.getAID());
        }

        AID aid = app.getAID();
        Log.d(TAG, "Select application by AID: " + aid);
        String command = EMVAPDUCommands.selectByDFName(aid.getAIDBytes());
        CardResponse selectAppResponse = EMVUtil.sendCmd(seConn, command);

        if (selectAppResponse.getSW() == SW.SELECTED_FILE_INVALIDATED.getSW()) {
            // App blocked
            Log.i(TAG, "Application BLOCKED");
            // TODO abort execution if app blocked?
            throw new SmartCardException("EMVApplication "
                    + Util.byteArrayToHexString(aid.getAIDBytes()) + " blocked");
        }

        if (selectAppResponse.getSW() != SW.SUCCESS.getSW()) {
            Log.e(TAG, "Can't select app. Card response: " + selectAppResponse);
            return;
        }

        ApplicationDefinitionFile adf = EMVUtil.parseFCIADF(
                selectAppResponse.getData(), app);
        Log.d(TAG, "ADF: " + adf);

        getCurrentCard().setSelectedApplication(app);
    }

}
