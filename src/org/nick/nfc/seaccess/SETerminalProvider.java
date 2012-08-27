package org.nick.nfc.seaccess;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sasc.terminal.CardConnection;
import sasc.terminal.Terminal;
import sasc.terminal.TerminalException;
import sasc.terminal.TerminalProvider;
import android.app.Application;

public class SETerminalProvider implements TerminalProvider {

    private SETerminal terminal;

    public SETerminalProvider(Application appCtx) {
        terminal = new SETerminal(appCtx);
    }

    @Override
    public CardConnection connectAnyTerminal() throws TerminalException {
        return terminal.connect();
    }

    @Override
    public CardConnection connectTerminal(String terminalName)
            throws TerminalException {
        return connectAnyTerminal();
    }

    @Override
    public CardConnection connectTerminal(int terminalNum)
            throws TerminalException {
        return connectAnyTerminal();
    }

    @Override
    public String getProviderInfo() {
        return "Android embedded secure element";
    }

    @Override
    public List<Terminal> listTerminals() throws TerminalException {
        return Collections.unmodifiableList(Arrays
                .asList(new Terminal[] { terminal }));
    }

}
