package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.hablo.mastercard.util.ipm.PDS0146Parser;
import org.hablo.mastercard.util.ipm.PDS0159Parser;
import org.hablo.mastercard.util.ipm.PDS0164Parser;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

/**
 * Created by Arsalan Khan on 09/06/21.
 * For MC DE48 (IPM Clearing Format)
 */
public class DE48IPMParser extends GenericTLVParser {

    private static final Map<String, Class> pdsElementParsers = new HashMap<>();
    private static final Map<String, String> pdsDescriptions = new HashMap<>();

    static {
        //pdsElementParsers.put("0146", PDS0146Parser.class);
        pdsElementParsers.put("0159", PDS0159Parser.class);
        //pdsElementParsers.put("0164", PDS0164Parser.class);

        pdsDescriptions.put("0001", "Mastercard® Mapping Service Account Number");
        pdsDescriptions.put("0002", "GCMS Product Identifier");
        pdsDescriptions.put("0003", "Licensed Product Identifier");
        pdsDescriptions.put("0015", "Clearing Currency Conversion Identifier");
        pdsDescriptions.put("0023", "Terminal Type");
        pdsDescriptions.put("0105", "File ID");
        pdsDescriptions.put("0122", "Processing Mode");
        pdsDescriptions.put("0145", "Amount, Alternate Transaction Fee");
        pdsDescriptions.put("0146", "Amounts, Transaction Fee");
        pdsDescriptions.put("0148", "Currency Exponents");
        pdsDescriptions.put("0158", "Business Activity");
        pdsDescriptions.put("0159", "Settlement Data");
        pdsDescriptions.put("0165", "Settlement Indicator");
        pdsDescriptions.put("0176", "Mastercard Assigned ID");
        pdsDescriptions.put("0177", "Cross-border");
        pdsDescriptions.put("0191", "Originating Message Format");
        pdsDescriptions.put("0198", "Device Type");
    }

    public DE48IPMParser() {
        super(48, 4, 3, "PDS", 0, 9999);
    }

    @Override
    public void parse(ISOMsg m) throws BLException, ISOException {
        super.parse(m);

        for (GenericTag e : getTags()) {
            e.setDescription(pdsDescriptions.getOrDefault(e.getId(), ""));
            if (pdsElementParsers.containsKey(e.getId())) {
                Class<PDSParserSupport> clazz = pdsElementParsers.get(e.getId());
                PDSParserSupport parserSupport;
                try {
                    parserSupport = clazz.newInstance();
                    parserSupport.parse(e);
                } catch (InstantiationException | IllegalAccessException instantiationException) {
                    instantiationException.printStackTrace();
                }
            }
        }
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + sourceTLVData + "'");
        p.println(indent + " DE             LENGTH     DESCRIPTION");
        for (GenericTag e : getTags()) {
            e.dump(p, indent + " ");
        }
    }
}
