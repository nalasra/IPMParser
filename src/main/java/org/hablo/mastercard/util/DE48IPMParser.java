package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.hablo.mastercard.util.ipm.PDS0164Parser;

/**
 * Created by Arsalan Khan on 09/06/21.
 * For MC DE48 (IPM Clearing Format)
 */
public class DE48IPMParser extends GenericTLVParser {

    private static final Map<String, Class> pdsList = new HashMap<>();

    static {
        pdsList.put("0164", PDS0164Parser.class);
    }

    public DE48IPMParser() {
        super(48, 4, 3, "PDS", 0, 9999);
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + sourceTLVData + "'");
        for (GenericTag e : getElements()) {
            e.dump(p, indent + getFieldType() + e.getId());
            if (pdsList.containsKey(e.getId())) {
                Class<PDSParserSupport> clazz = pdsList.get(e.getId());
                PDSParserSupport parserSupport;
                try {
                    parserSupport = clazz.newInstance();
                    parserSupport.parse(e.getValue());
                    parserSupport.dump(p, "  ");
                } catch (InstantiationException | IllegalAccessException instantiationException) {
                    instantiationException.printStackTrace();
                }
            }
        }
    }
}
