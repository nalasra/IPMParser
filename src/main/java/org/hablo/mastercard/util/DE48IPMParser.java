package org.hablo.mastercard.util;

import java.io.PrintStream;

/**
 * Created by Arsalan Khan on 09/06/21.
 */
public class DE48IPMParser extends GenericTLVParser {

    public DE48IPMParser() {
        super(48,4, 3, "PDS");
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + sourceTLVData + "'");
        super.dump(p, indent);
    }
}
