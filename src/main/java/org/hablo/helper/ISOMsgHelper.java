package org.hablo.helper;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class ISOMsgHelper {
    public static ISOMsg createISOMsg(byte[] data, GenericPackager genericPackager) throws ISOException {
        ISOMsg msg = new ISOMsg();
        genericPackager.setLogger(Logger.getLogger("Q2"), "packager");
        msg.setPackager(genericPackager);
        msg.unpack(data);
        return msg;
    }
    public static String toString(Loggeable m) throws UnsupportedEncodingException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, false, utf8)) {
            m.dump(ps, "");
        }
        return baos.toString(utf8);
    }

    //X1XX
    public static boolean isAuthorization(ISOMsg m) throws ISOException {
        return m.getMTI().charAt(1) == '1';
    }

    //X2XX
    public static boolean isFinancial(ISOMsg m) throws ISOException {
        return m.getMTI().charAt(1) == '2';
    }

    //X4XX
    public static boolean isReversal(ISOMsg m) throws ISOException {
        return m.getMTI().charAt(1) == '4';
    }

    //X8XX
    public static boolean isNetworkMessage(ISOMsg m) throws ISOException {
        return m.getMTI().charAt(1) == '8';
    }

    //XX2X
    public static boolean isAdvice(ISOMsg m) throws ISOException {
        return m.getMTI().charAt(2) == '2';
    }

    //X12X
    public static boolean isAuthorizationAdvice(ISOMsg m) throws ISOException {
        return isAuthorization(m) && isAdvice(m);
    }

    //X22X
    public static boolean isFinancialAdvice(ISOMsg m) throws ISOException {
        return isFinancial(m) && isAdvice(m);
    }


    //XX3X
    public static boolean isAdviceResponse(ISOMsg m) throws ISOException {
        return m.getMTI().charAt(2) == '3';
    }

    public static boolean hasTranCode(String tranCode, ISOMsg m) {
        if (m.hasField(3)) {
            String procCode = m.getString(3);
            return procCode.startsWith(tranCode);
        }
        return false;
    }

    public static boolean isPartialReversal(ISOMsg m) throws ISOException {
        return isReversal(m) && (m.hasField(95) && !ISOUtil.isZero(m.getString(95)));
    }

    public static boolean isFullReversal(ISOMsg m) throws ISOException {
        return isReversal(m) && (!m.hasField(95) || ISOUtil.isZero(m.getString(95)));
    }

}
