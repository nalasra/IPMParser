package org.hablo.visa.basei;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;

public class BaseIParser {
    static GenericPackager packager;

    public static ISOMsg parseRaw(String hexString){
        ISOMsg m = null;
        try {
            packager = new GenericPackager("jar:packager/base1.xml");
            m = packager.createISOMsg();
            m.setPackager(packager);
            m.unpack(ISOUtil.hex2byte(hexString.replace(" ", "")));
        } catch (ISOException e) {
            System.out.println("Exception occurred...");
            e.printStackTrace();
        }
        return m;
    }
}
