package org.hablo.helper;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
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
}
