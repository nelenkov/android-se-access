package org.nick.nfc.seaccess;

import static org.nick.nfc.seaccess.Hex.toHex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import sasc.terminal.CardConnection;
import sasc.terminal.CardResponse;
import sasc.terminal.Terminal;
import sasc.terminal.TerminalException;
import android.util.Log;

public class SEConnection implements CardConnection {

    private static final String TAG = SEConnection.class.getSimpleName();

    private Object se;
    private Method openMethod;
    private Method closeMethod;
    private Method transceiveMethod;

    private SETerminal terminal;

    public SEConnection(SETerminal terminal) {
        this.terminal = terminal;
        try {
            Class<?> nfcExtrasClazz = Class
                    .forName("com.android.nfc_extras.NfcAdapterExtras");
            Method getMethod = nfcExtrasClazz.getMethod("get",
                    Class.forName("android.nfc.NfcAdapter"));
            Object nfcExtras = getMethod.invoke(nfcExtrasClazz,
                    terminal.getDefaultAdapter());
            // public NfcExecutionEnvironment getEmbeddedExecutionEnvironment()
            Method getEEMethod = nfcExtras.getClass().getMethod(
                    "getEmbeddedExecutionEnvironment", (Class<?>[]) null);
            se = getEEMethod.invoke(nfcExtras, (Object[]) null);
            // public byte[] transceive(byte[] in) throws IOException {
            Class<?> seClazz = se.getClass();
            openMethod = seClazz.getMethod("open", (Class<?>[]) null);
            transceiveMethod = se.getClass().getMethod("transceive",
                    new Class<?>[] { byte[].class });
            closeMethod = seClazz.getMethod("close", (Class<?>[]) null);
            openMethod.invoke(se, (Object[]) null);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Error: " + e.getCause().getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean disconnect(boolean arg0) throws TerminalException {
        try {
            closeMethod.invoke(se, (Object[]) null);

            return true;
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Error: " + e.getCause().getMessage());
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public byte[] getATR() {
        // dummy
        return new byte[10];
    }

    @Override
    public String getConnectionInfo() {
        return "Android emedded SE";
    }

    @Override
    public String getProtocol() {
        // dummy
        return "T1";
    }

    @Override
    public Terminal getTerminal() {
        return terminal;
    }

    @Override
    public void resetCard() throws TerminalException {
        // TODO Auto-generated method stub
    }

    @Override
    public CardResponse transmit(byte[] command) throws TerminalException {
        byte[] response = sendApdu(command);

        return new SECardResponse(response);
    }

    private byte[] sendApdu(byte[] commandApdu) {
        try {
            Log.d(TAG, String.format("--> %s", toHex(commandApdu)));
            Object response = transceiveMethod.invoke(se, commandApdu);
            byte[] responseApdu = (byte[]) response;
            short status = SECardResponse.getStatus(responseApdu);
            String statusStr = String.format("%02X", status);
            if (responseApdu.length > 2) {
                Log.d(TAG, String.format("<-- %s %s", toHex(Arrays.copyOf(
                        responseApdu, responseApdu.length - 2)), statusStr));
            } else {
                Log.d(TAG, String.format("<-- %s", statusStr));
            }

            return responseApdu;
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Error: " + e.getCause().getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
