package org.hablo.mastercard.iso;

import org.hablo.ISOMsgParserSupport;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

public class MCDMSParser implements ISOMsgParserSupport {
    static GenericPackager packager;

    public ISOMsg parse(byte[] d){
        ISOMsg m = null;
        try {
            packager = new GenericPackager("jar:packager/mc_dms.xml");
            m = packager.createISOMsg();
            m.setPackager(packager);
            m.unpack(d);
        } catch (ISOException e) {
            System.out.println("Exception occurred...");
            e.printStackTrace();
        }
        return m;
    }
}
