package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class DE108Parser extends GenericTLVParser {
    private static final Map<String, String> allowedSF = new HashMap<>();

    static {
        allowedSF.put("01", "01,03,07,11,18");
        allowedSF.put("02", "01,03,04,07,11,18");
        allowedSF.put("03", "01");
    }

    public DE108Parser() {
        super(108, 2, 3, "SE", 0, 6, allowedSF);
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + sourceTLVData + "'");
        super.dump(p, indent);
    }
}
