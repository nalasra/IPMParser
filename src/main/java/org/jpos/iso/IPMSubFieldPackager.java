package org.jpos.iso;

import java.util.ArrayList;
import java.util.Hashtable;

import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

public class IPMSubFieldPackager extends ISOBasePackager {

    public IPMSubFieldPackager() {
        super();
    }

    @Override
    protected boolean emitBitMap() {
        return false;
    }

    @Override
    public int unpack(ISOComponent m, byte[] b) throws ISOException {
        LogEvent evt = new LogEvent(this, "unpack");

        int consumed = 0;
        ISOField c;
        ISOFieldPackager fieldPackager = new IFIPM_LLLCHAR();

        while (consumed < b.length) {
            c = new ISOField();
            consumed += fieldPackager.unpack(c, b, consumed);
            if (logger != null) {
                evt.addMessage("<unpack fld=\"" + c.getFieldNumber()
                        + "\" packager=\""
                        + fieldPackager.getClass().getName() + "\">");
                evt.addMessage("  <value>"
                        + c.getValue().toString()
                        + "</value>");
                evt.addMessage("</unpack>");
            }
            m.set(c);
        }
        Logger.log(evt);
        return consumed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] pack(ISOComponent c) throws ISOException {
        LogEvent evt = new LogEvent(this, "pack");

        try {
            ISOFieldPackager fieldPackager = new IFIPM_LLLCHAR();

            int len = 0;
            var tab = c.getChildren();
            ArrayList<byte[]> l = new ArrayList<>();

            // Pack all the subfields
            for (Object ko : tab.keySet()) {
                Object obj = tab.get(ko);
                if (obj instanceof ISOField) {
                    ISOField f = (ISOField) obj;
                    byte[] b = fieldPackager.pack(f);
                    len += b.length;
                    l.add(b);
                    if (logger != null) {
                        evt.addMessage("<pack fld=\"" + f.getFieldNumber()
                                + "\" packager=\""
                                + fieldPackager.getClass().getName() + "\">");
                        evt.addMessage("  <value>"
                                + f.getValue().toString()
                                + "</value>");
                        evt.addMessage("</unpack>");
                    }
                }

            }

            // Concatenate arraylist byte-arrays together
            int k = 0;
            byte[] d = new byte[len];
            for (byte[] b : l) {
                for (int j = 0; j < b.length; j++) {
                    d[k++] = b[j];
                }
            }
            Logger.log(evt);
            return d;
        } catch (Exception ex) {
            throw new ISOException(ex);
        }
    }

}