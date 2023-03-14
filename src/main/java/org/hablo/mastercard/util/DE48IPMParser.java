package org.hablo.mastercard.util;

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

    static {
        pdsElementParsers.put("0146", PDS0146Parser.class);
        pdsElementParsers.put("0159", PDS0159Parser.class);
        pdsElementParsers.put("0164", PDS0164Parser.class);
    }

    public DE48IPMParser() {
        super(48, 4, 3, "pds", 0, 9999);
    }

    @Override
    public void parse(ISOMsg m) throws BLException, ISOException {
        super.parse(m);
        for (GenericTag e : getTags()) {
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
}
