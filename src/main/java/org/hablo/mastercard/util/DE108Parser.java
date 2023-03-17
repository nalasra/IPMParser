package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.hablo.helper.PropertiesLoader;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class DE108Parser extends TLVParser {

    private static final Map<String, Object> de108SEList = new HashMap<>();
    private static final PropertiesLoader converter = new PropertiesLoader("mc_de108_se_list.properties");

    static {
        de108SEList.put("01", "01,03,07,11,18");
        de108SEList.put("02", "01,03,04,07,11,18");
        de108SEList.put("03", "01");
    }

    public DE108Parser() {
        super(108, 2, 3, "SE", 0, 6);
    }

    @Override
    public void parse(ISOMsg m) throws BLException, ISOException {
        super.parse(m);
        for (TLV e : getTlvs()) {
            e.setDescription(converter.convert(e.getId()));
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

                    TLV sf = new TLV(subTagId, sFL , subFieldData, "SF");
                    sf.setDescription(converter.convert(e.getId() + "." + subTagId));
                    e.add(sf);
                }
            }
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
