package org.hablo.mastercard.util;

import java.io.PrintStream;

import org.hablo.helper.PropertiesLoader;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class DE110Parser extends TLVParser {
    private static final PropertiesLoader converter = new PropertiesLoader("mc_de110_se_list.properties");

    public DE110Parser() {
        super(110, 2, 3, "SE", 9, 10);
    }

    @Override
    public void parse(ISOMsg m) throws BLException, ISOException {
        super.parse(m);
        for (TLV e : getTlvs()) {
            e.setDescription(converter.convert(e.getId()));
        }
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + sourceTLVData + "'");
        p.println(indent + " DATAELEMENT   LENGTH       DESCRIPTION");
        for (TLV e : getTlvs()) {
            e.dump(p, indent + " ");
        }
    }
}
