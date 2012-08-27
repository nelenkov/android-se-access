package org.nick.nfc.seaccess;

import sasc.terminal.CardConnection;
import sasc.terminal.Terminal;
import sasc.terminal.TerminalException;
import android.app.Application;
import android.nfc.NfcAdapter;

public class SETerminal implements Terminal {

    private Application appCtx;
    private NfcAdapter defaultAdapter;

    public SETerminal(Application appCtx) {
        this.appCtx = appCtx;
    }

    @Override
    public CardConnection connect() throws TerminalException {
        defaultAdapter = NfcAdapter.getDefaultAdapter(appCtx);

        return new SEConnection(this);
    }

    @Override
    public String getTerminalInfo() {
        return "Android Embeded Secure Element";
    }

    public NfcAdapter getDefaultAdapter() {
        return defaultAdapter;
    }

}
