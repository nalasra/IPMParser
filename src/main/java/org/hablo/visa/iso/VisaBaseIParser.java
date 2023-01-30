package org.hablo.visa.iso;

import org.hablo.ISOMsgParserSupport;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;

public class VisaBaseIParser implements ISOMsgParserSupport {
    static GenericPackager packager;

    public ISOMsg parse(byte[] d){
        ISOMsg m = null;
        try {
            packager = new GenericPackager("jar:packager/base1.xml");
            packager.setLogger(Logger.getLogger("Q2"), getClass().getName());
            m = packager.createISOMsg();
            m.setPackager(packager);
            m.unpack(d);
            m.dump(System.out, "");
        } catch (ISOException e) {
            System.out.println("Exception occurred...");
            e.printStackTrace();
        }
        return m;
    }
}
