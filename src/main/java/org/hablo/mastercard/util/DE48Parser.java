package org.hablo.mastercard.util;

import org.hablo.helper.PropertiesConverter;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arsalan Khan on 09/06/21.
 */
public class DE48Parser extends GenericTLVParser {

    private String tcc;
    private static final Map<String, String> de48SEList = new HashMap<>();
    private static final PropertiesConverter seConverter = new PropertiesConverter("mc_de48_se_list.properties");

    static {
        de48SEList.put("37", "01,02,03,04");
        de48SEList.put("42", "01");
        de48SEList.put("66", "01,02");
    }

    public DE48Parser() {
        super(48, 2, 2, "SE", 0, 99);
    }

    @Override
    public void parse(ISOMsg m) throws ISOException, BLException {
        if (!m.hasField(48)) {
            return;
        }
        String de48 = m.getString(48);
        if (de48 == null || de48.length() == 0) {
            return;
        }
        if (!ISOMsgHelper.isNetworkMessage(m)) {
            //tcc
            String tcc1 = de48.substring(0, 1);
            if (!TCC.isValid(tcc1)) {
                throw new BLException("format.error", "048");
            }
            this.tcc = tcc1;
            m.set(48, de48.substring(1));
        }

        super.parse(m);

        for (GenericTag e : getTags()) {
            e.setDescription(seConverter.convert(e.getId()));
            if (de48SEList.containsKey(e.getId())) {
                int j = 0;
                while (j < e.getLength()) {
                    String subTagId = e.getValue().substring(j, j + 2);
                    j = j + 2;
                    String subFieldLength = e.getValue().substring(j, j + 2);
                    j = j + 2;
                    int sFL = Integer.parseInt(subFieldLength);
                    String subFieldData = e.getValue().substring(j, j + sFL);
                    j = j + sFL;
                    GenericTag ee = new GenericTag(subTagId, sFL, subFieldData, "SF");
                    ee.setDescription(seConverter.convert(e.getId() + "." + ee.getId()));
                    e.add(ee);
                }
            }
        }
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + tcc + sourceTLVData + "'");
        if (tcc != null) {
            p.println(indent + " TCC=" + tcc);
        }
        p.println(" DATAELEMENT   LENGTH       DESCRIPTION");
        for (GenericTag e : getTags()) {
            e.dump(p, indent + " ");
        }
    }

    public String getTcc() {
        return tcc;
    }

    public enum TCC {
        AUTO_VEHICLE_RENTAL("A"),
        CASH_DISBURSEMENT("C"),
        RESTAURANT("F"),
        HOTEL_MOTEL("H"),
        HOSPITALIZATION_COLLEGE("O"),
        PAYMENT_TRANSACTION("P"),
        RETAIL_SALE("R"),
        PHONE_MOTO_ECOMMERCE("T"),
        UNIQUE("U"),
        AIRLINE_OTHER_TRANSPORTATION("X"),
        ATM_CASH_DISBURSEMENT("Z");

        private final String val;

        TCC(String id) {
            this.val = id;
        }

        public static boolean isValid(String value) {
            for (TCC tcc : values()) {
                if (tcc.toString().equals(value)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return this.val;
        }
    }
}
