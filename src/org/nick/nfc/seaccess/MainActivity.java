package org.nick.nfc.seaccess;

import static org.nick.nfc.seaccess.Hex.fromHex;

import java.util.Collection;
import java.util.List;

import sasc.emv.EMVApplication;
import sasc.emv.EMVCard;
import sasc.emv.EMVUtil;
import sasc.emv.SW;
import sasc.iso7816.AID;
import sasc.iso7816.BERTLV;
import sasc.terminal.CardConnection;
import sasc.terminal.CardResponse;
import sasc.terminal.Terminal;
import sasc.terminal.TerminalException;
import sasc.util.Util;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button gpInfoButton;
    private Button emvInfoButton;
    private Button walletInfoButton;

    private TextView infoText;

    private Terminal terminal;
    private CardConnection seConn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setProgressBarIndeterminateVisibility(false);

        gpInfoButton = (Button) findViewById(R.id.gp_info_button);
        gpInfoButton.setOnClickListener(this);

        emvInfoButton = (Button) findViewById(R.id.emv_info_button);
        emvInfoButton.setOnClickListener(this);

        walletInfoButton = (Button) findViewById(R.id.wallet_info_button);
        walletInfoButton.setOnClickListener(this);

        infoText = (TextView) findViewById(R.id.info_text);

        terminal = new SETerminal(getApplication());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        closeSeSilently();
    }

    private void closeSeSilently() {
        if (seConn != null) {
            try {
                seConn.disconnect(false);
            } catch (TerminalException e) {
                Log.w(TAG, "Eror closing SE: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            seConn = terminal.connect();
        } catch (TerminalException e) {
            String message = "Failed to open SE: " + e.getMessage();
            Log.w(TAG, message, e);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            seConn = null;
            finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        closeSeSilently();
        seConn = null;
    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.gp_info_button) {
                new AsyncTask<Void, Void, Object[]>() {

                    Exception error;

                    @Override
                    protected void onPreExecute() {
                        setProgressBarIndeterminateVisibility(true);
                        toggleButtons(false);
                    }

                    @Override
                    protected Object[] doInBackground(Void... arg0) {
                        try {
                            CardResponse response = transmit(
                                    GPCommands.EMPTY_SELECT, "EMPTY_SELECT");
                            if (response.getSW() != SW.SUCCESS.getSW()) {
                                // do something
                                return null;
                            }

                            SecurityDomainFCI sdFci = SecurityDomainFCI
                                    .parse(response.getData());
                            Log.d(TAG, "SD FCI: " + sdFci.toString());

                            response = transmit(
                                    GPCommands.GET_ISSUER_ID_COMMAND,
                                    "GET_ISSUER_ID_COMMAND");

                            response = transmit(fromHex("00CA004500"),
                                    "GET_CARD_IMAGE_NUMBER");

                            response = transmit(GPCommands.GET_CARD_DATA,
                                    "GET_CARD_DATA");

                            List<KeyInfo> keys = null;
                            response = transmit(
                                    GPCommands.GET_KEY_INFORMATION_TEMPLATE_COMMAND,
                                    "GET_KEY_INFORMATION_TEMPLATE_COMMAND");
                            if (response.getSW() == SW.SUCCESS.getSW()) {
                                keys = KeyInfo.parse(response.getData());
                                for (KeyInfo key : keys) {
                                    Log.d(TAG, "Key: " + key);
                                }
                            }

                            response = transmit(
                                    GPCommands.GET_KEY_VERSION_SEQUENCE_COUNTER_COMMAND,
                                    "GET_KEY_VERSION_SEQUENCE_COUNTER_COMMAND");

                            response = transmit(GPCommands.GET_CPLC_COMMAND,
                                    "GET_CPLC_COMMAND");
                            CPLC cplc = null;
                            if (response.getSW() == SW.SUCCESS.getSW()) {
                                cplc = CPLC.parse(response.getData());
                            }
                            Log.d(TAG, "CPLC: " + cplc);

                            return new Object[] { sdFci, keys, cplc };
                        } catch (Exception e) {
                            Log.e(TAG, "Error:" + e.getMessage(), e);
                            return null;
                        }
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    protected void onPostExecute(Object[] data) {
                        setProgressBarIndeterminateVisibility(false);
                        toggleButtons(true);

                        if (data == null && error != null) {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();

                            return;
                        }

                        if (data == null) {
                            infoText.setText("Error selecting CardManager. Does the SE work?");
                            return;
                        }

                        infoText.setText(getGpInfoDisplayString(
                                (SecurityDomainFCI) data[0],
                                (List<KeyInfo>) data[1], (CPLC) data[2]));
                    }
                }.execute();
            } else if (v.getId() == R.id.emv_info_button) {
                new AsyncTask<Void, Void, EMVCard>() {

                    Exception error;

                    @Override
                    protected void onPreExecute() {
                        setProgressBarIndeterminateVisibility(true);
                        toggleButtons(false);
                    }

                    @Override
                    protected EMVCard doInBackground(Void... arg0) {
                        try {
                            SEEMVSession emvSession = SEEMVSession
                                    .startSession(getApplication(), seConn);
                            EMVCard emvCard = emvSession.initCard();
                            Log.d(TAG, "card: " + emvCard);
                            if (emvCard != null) {
                                Collection<EMVApplication> apps = emvCard
                                        .getApplications();
                                for (EMVApplication app : apps) {
                                    Log.d(TAG, "EMV app: " + app);
                                    // always fails with 0x6999
                                    // emvSession.selectApplication(app);
                                }
                            }

                            return emvCard;
                        } catch (Exception e) {
                            Log.e(TAG, "Error: " + e.getMessage(), e);
                            error = e;
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(EMVCard emvCard) {
                        setProgressBarIndeterminateVisibility(false);
                        toggleButtons(true);

                        if (emvCard == null && error != null) {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();

                            return;
                        }

                        infoText.setText(getEmvCardDisplayString(emvCard));
                    }
                }.execute();

            } else if (v.getId() == R.id.wallet_info_button) {
                new AsyncTask<Void, Void, Object[]>() {

                    Exception error;

                    @Override
                    protected void onPreExecute() {
                        setProgressBarIndeterminateVisibility(true);
                        toggleButtons(false);
                    }

                    @Override
                    protected Object[] doInBackground(Void... arg0) {
                        try {
                            CardResponse response = transmit(
                                    WalletControllerCommands.SELECT_WALLET_CONTROLLER_COMMAND,
                                    "SELECT_WALLET_CONTROLLER_COMMAND");

                            WalletControllerFCI wcFci = null;
                            if (response.getSW() == SW.SUCCESS.getSW()) {
                                wcFci = WalletControllerFCI.parse(response
                                        .getData());
                                Log.d(TAG,
                                        "Wallet controller: "
                                                + wcFci.toString());
                            } else {
                                Log.d(TAG, "Wallet controller applet not found");
                            }

                            AID mmAid = null;
                            response = transmit(
                                    MifareManagerCommands.SELECT_MIFARE_MANAGER_COMMAND,
                                    "SELECT_MIFARE_MANAGER_COMMAND");
                            if (response.getSW() == SW.SUCCESS.getSW()) {
                                mmAid = MifareManagerCommands.MIFARE_MANAGER_AID;
                            } else {
                                Log.d(TAG,
                                        "Mifare manager applet not found. SW: "
                                                + Integer.toHexString(response
                                                        .getSW()));
                            }

                            return new Object[] { wcFci, mmAid };
                        } catch (Exception e) {
                            Log.e(TAG, "Error: " + e.getMessage(), e);
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Object[] data) {
                        setProgressBarIndeterminateVisibility(false);
                        toggleButtons(true);

                        if (data == null && error != null) {
                            Toast.makeText(MainActivity.this,
                                    "Error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();

                            return;
                        }

                        infoText.setText(getWalletDisplayString(
                                (WalletControllerFCI) data[0], (AID) data[1]));
                    }
                }.execute();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private CardResponse transmit(byte[] command, String description)
            throws TerminalException {
        Log.d(TAG, description);
        CardResponse response = seConn.transmit(command);
        EMVUtil.printResponse(response, true);

        return response;
    }

    private CharSequence getGpInfoDisplayString(SecurityDomainFCI sdFci,
            List<KeyInfo> keys, CPLC cplc) {
        StringBuilder buff = new StringBuilder();
        buff.append(sdFci.toString());
        buff.append("\n\n");
        if (keys != null) {
            buff.append("Security domain keys:\n");
            for (KeyInfo key : keys) {
                buff.append(Util.getSpaces(2) + key.toString() + "\n");
            }
        }
        buff.append("\n");

        buff.append("Card Production Life Cyle Data");
        buff.append("\n");
        buff.append(cplc.toString());

        return buff.toString();
    }

    private void toggleButtons(boolean enable) {
        gpInfoButton.setEnabled(enable);
        emvInfoButton.setEnabled(enable);
        walletInfoButton.setEnabled(enable);
    }

    private String getWalletDisplayString(WalletControllerFCI wcFci, AID mmAid) {
        StringBuilder buff = new StringBuilder();
        buff.append("Wallet applets: ");
        buff.append("\n\n");
        buff.append(wcFci == null ? "Wallet controller: not installed" : wcFci
                .toString());
        buff.append("\n\n");
        buff.append("MIFARE manager applet");
        buff.append("\n");
        buff.append(mmAid == null ? "Mifare manager: not installed" : mmAid
                .toString());

        return buff.toString();
    }

    private String getEmvCardDisplayString(EMVCard card) {
        StringBuilder buff = new StringBuilder();
        int indent = 2;

        if (card.getApplications().isEmpty()) {
            buff.append("Google Wallet not installed or locked. Install and unlock Wallet and try again.");
            buff.append("\n");
            // PPSE in fact
            if (card.getPSE() != null) {
                buff.append("\n");
                buff.append("PPSE: ");
                buff.append("\n");
                buff.append(Util.getSpaces(indent) + card.getPSE().toString());

                return buff.toString();
            }
        }

        buff.append((Util.getSpaces(indent) + "EMV applications on SE"));
        buff.append("\n\n");
        buff.append(Util.getSpaces(indent + 2 * indent) + "Applications ("
                + card.getApplications().size() + " found):");
        buff.append("\n");
        for (EMVApplication app : card.getApplications()) {
            buff.append(Util.getSpaces(indent + 3 * indent) + app.toString());
        }

        if (card.getMasterFile() != null) {
            buff.append(Util.getSpaces(indent + indent) + "MF: "
                    + card.getMasterFile());
        }

        buff.append("------------------------------------------------------");
        buff.append("Extra info (if any)");
        buff.append(Util.getSpaces(indent) + "ATR: " + card.getATR());
        buff.append(Util.getSpaces(indent + indent) + "Interface Type: "
                + card.getType());
        buff.append("\n");


        if (!card.getUnhandledRecords().isEmpty()) {
            buff.append(Util.getSpaces(indent + indent)
                    + "UNHANDLED GLOBAL RECORDS ("
                    + card.getUnhandledRecords().size() + " found):");

            for (BERTLV tlv : card.getUnhandledRecords()) {
                buff.append(Util.getSpaces(indent + 2 * indent) + tlv.getTag()
                        + " " + tlv);
            }
        }
        buff.append("\n");

        return buff.toString();
    }

}
