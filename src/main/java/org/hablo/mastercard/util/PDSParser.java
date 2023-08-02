package org.hablo.mastercard.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.hablo.helper.PropertiesLoader;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

/**
 * Created by Arsalan Khan on 09/06/21.
 * For MC DE48,DE62,DE123,DE124,DE125 (IPM Clearing Format)
 */
public class PDSParser extends TLVParser {

    private static final Map<String, Class<PDSSubFieldParserSupport>> pdsElementParsers = new HashMap<>();
    private static final PropertiesLoader converter = new PropertiesLoader("mc_ipm_pds_list.properties");


    public PDSParser(int fieldId, int tagSize, int lengthSize, String fieldType, int minTagId, int maxTagId) {
        super(fieldId, tagSize, lengthSize, fieldType, minTagId, maxTagId);
    }

    public static PropertiesLoader getConverter() {
        return converter;
    }

    @Override
    public void parse(ISOMsg m) throws BLException, ISOException {
        super.parse(m);

        for (TLV e : getTlvs()) {
            e.setDescription(converter.convert(e.getId()));
            Class clazz = null;
            if(pdsElementParsers.containsKey(e.getId())) {
                clazz = pdsElementParsers.get(e.getId());
            } else {
                try {
                    clazz = Class.forName("org.hablo.mastercard.util.ipm.PDS" + e.getId() + "Parser");
                    pdsElementParsers.put(e.getId(), clazz);
                } catch (ClassNotFoundException ex) {
                    //we don't have a parser for this PDS
                }
            }
            PDSSubFieldParserSupport parserSupport;
            try {
                if(clazz == null) continue;
                parserSupport = (PDSSubFieldParserSupport) clazz.getDeclaredConstructor().newInstance();
                parserSupport.parse(e);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException instantiationException) {
                instantiationException.printStackTrace();
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
