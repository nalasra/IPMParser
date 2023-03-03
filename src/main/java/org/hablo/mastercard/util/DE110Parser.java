package org.hablo.mastercard.util;

import java.io.PrintStream;

public class DE110Parser extends GenericTLVParser {
    static {
    }

    public DE110Parser() {
        super(110, 2, 3, "SE", 0, 10);
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + sourceTLVData + "'");
        super.dump(p, indent);
    }
}
