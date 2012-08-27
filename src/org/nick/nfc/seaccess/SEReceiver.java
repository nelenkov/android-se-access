package org.nick.nfc.seaccess;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SEReceiver extends BroadcastReceiver {

    private static final String TAG = SEReceiver.class.getSimpleName();

    public static final String ACTION_AID_SELECTED = "com.android.nfc_extras.action.AID_SELECTED";
    public static final String EXTRA_AID = "com.android.nfc_extras.extra.AID";

    public static final String ACTION_APDU_RECEIVED = "com.android.nfc_extras.action.APDU_RECEIVED";
    public static final String EXTRA_APDU_BYTES = "com.android.nfc_extras.extra.APDU_BYTES";

    public static final String ACTION_EMV_CARD_REMOVAL = "com.android.nfc_extras.action.EMV_CARD_REMOVAL";

    public static final String ACTION_MIFARE_ACCESS_DETECTED = "com.android.nfc_extras.action.MIFARE_ACCESS_DETECTED";
    public static final String EXTRA_MIFARE_BLOCK = "com.android.nfc_extras.extra.MIFARE_BLOCK";


    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received: " + action);
        if (ACTION_AID_SELECTED.equals(action)) {
            byte[] aid = intent.getByteArrayExtra(EXTRA_AID);
            Log.d(TAG, "AID: " + Hex.toHex(aid));
        } else if (ACTION_APDU_RECEIVED.equals(action)) {
            byte[] apdu = intent.getByteArrayExtra(EXTRA_APDU_BYTES);
            Log.d(TAG, "APDU: " + Hex.toHex(apdu));
        } else if (ACTION_MIFARE_ACCESS_DETECTED.equals(action)) {
            byte[] block = intent.getByteArrayExtra(EXTRA_MIFARE_BLOCK);
            Log.d(TAG, "Mifare block: " + Hex.toHex(block));
        }
    }
}
