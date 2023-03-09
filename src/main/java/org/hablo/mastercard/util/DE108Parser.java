package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class DE108Parser extends GenericTLVParser {
    private static final Map<String, Object> de108SEList = new HashMap<>();

    static {
        de108SEList.put("01", "01,03,07,11,18");
        de108SEList.put("02", "01,03,04,07,11,18");
        de108SEList.put("03", "01");
    }

    public DE108Parser() {
        super(108, 2, 3, "SE", 0, 6);
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + sourceTLVData + "'");
        for (GenericTag e : getElements()) {
            e.dump(p, indent + getFieldType() + e.getId());
            if (de108SEList.containsKey(e.getId())) {
                int j = 0;
                while (j < e.getLength()) {
                    String subTagId = e.getValue().substring(j, j + 2);
                    j = j + 2;
                    String subFieldLength = e.getValue().substring(j, j + 2);
                    j = j + 2;
                    int sFL = Integer.parseInt(subFieldLength);
                    String subFieldData = e.getValue().substring(j, j + sFL);
                    j = j + sFL;
                    p.println(indent + "  SF" + subTagId + " " + subFieldLength + " " + subFieldData);
                }
            }
        }
    }
}
