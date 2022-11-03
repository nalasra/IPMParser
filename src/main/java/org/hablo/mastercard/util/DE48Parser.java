package org.hablo.mastercard.util;

import org.hablo.helper.ISOMsgHelper;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Arsalan Khan on 09/06/21.
 */
public class DE48Parser implements DEParserSupport {
    String de48;
    private String tcc;
    private boolean hasTCC = false;
    LinkedHashSet<DE48SE> elements;
    int MIN_SE_ID = 0;
    int MAX_SE_ID = 99;
    int MAX_SE_LEN_0100 = 99;
    int MAX_SE_LEN_0800 = 96;
    private static final Map<String, String> allowedSF = new HashMap<>();

    static {
        allowedSF.put("37", "01,02,03,04");
        allowedSF.put("42", "01");
        allowedSF.put("66", "01,02");
    }

    public DE48Parser(ISOMsg m) throws BLException, ISOException {
        elements = new LinkedHashSet<>();
        parse(m);
    }

    @Override
    public void parse(ISOMsg m) throws ISOException, BLException {
        if(!m.hasField(48)) return;
        this.de48 = m.getString(48);
        if (this.de48 == null || this.de48.length() == 0) return;
        int i = 0;
        if (!ISOMsgHelper.isNetworkMessage(m)) {
            //tcc
            String tcc = de48.substring(0, 1);
            if (!TCC.isValid(tcc))
                throw new BLException("format.error", "048");
            this.tcc = tcc;
            this.hasTCC = true;
            i = 1;
        }
        //subElements
        while (i < de48.length()) {
            String subElementId = de48.substring(i, i + 2);
            i = i + 2;
            int sE = Integer.parseInt(subElementId);
            if (sE > MAX_SE_ID || sE < MIN_SE_ID)
                throw new BLException("format.error", "0480" + subElementId);

            String subElementLength = de48.substring(i, i + 2);
            i = i + 2;

            int sEL = Integer.parseInt(subElementLength);
            boolean parseSF = allowedSF.containsKey(subElementId);

            String value = "";
            DE48SE se = new DE48SE(subElementId);
            if (parseSF) {
                int j = 0;
                String de48_se = de48.substring(i);
                while (j < sEL) {
                    String subFieldId = de48_se.substring(j, j + 2);
                    j = j + 2;
                    if (!allowedSF.get(subElementId).contains(subFieldId))
                        throw new BLException("format.error", "0480" + subElementId);

                    String subFieldLength = de48_se.substring(j, j + 2);
                    j = j + 2;
                    int sFL = Integer.parseInt(subFieldLength);
                    String subFieldData = de48_se.substring(j, j + sFL);
                    j = j + sFL;
                    se.add(new DE48SE(subFieldId, sFL, subFieldData, true));
                }
            } else {
                se.setLength(sEL);
                value = de48.substring(i, i + sEL);
            }
            se.setValue(value);
            elements.add(se);
            i = i + sEL;
        }
    }

    public boolean hasTCC() {
        return hasTCC;
    }

    public String getTcc() {
        return tcc;
    }

    public boolean hasElement(String id) {
        Objects.requireNonNull(id);
        for (DE48SE e : elements) {
            if (e.getId().equals(id))
                return true;
        }
        return false;
    }

    public DE48SE getElementById(String id) {
        Objects.requireNonNull(id);
        for (DE48SE e : elements) {
            if (e.getId().equals(id))
                return e;
        }
        return null;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + de48 + "'");
        if (tcc != null)
            p.println(indent + " TCC=" + tcc);
        for (DE48SE e : elements) {
            e.dump(p, indent + "SE" + e.getId());
        }
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
                if (tcc.toString().equals(value)) return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return this.val;
        }
    }

    public class DE48SE implements Loggeable {
        private String id;
        private int length;
        private String value;
        LinkedHashSet<DE48SE> elements;
        private boolean isSF = false;

        public DE48SE(String id) {
            this.id = id;
            elements = new LinkedHashSet<>();
        }

        public DE48SE(String id, int length, String value) {
            this.id = id;
            this.length = length;
            this.value = value;
        }

        public DE48SE(String id, int length, String value, boolean sf) {
            this(id, length, value);
            isSF = sf;
        }

        public boolean hasField(String id) {
            Objects.requireNonNull(id);
            for (DE48SE e : elements) {
                if (e.getId().equals(id))
                    return true;
            }
            return false;
        }

        public DE48SE getFieldById(String id) {
            Objects.requireNonNull(id);
            for (DE48SE e : elements) {
                if (e.getId().equals(id))
                    return e;
            }
            return null;
        }

        public void add(DE48SE de48SE) {
            elements.add(de48SE);
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public void dump(PrintStream p, String indent) {
            if (elements != null && elements.size() > 0) {
                for (DE48SE e : elements) {
                    e.dump(p, indent + " SF" + e.getId());
                }
            } else {
                p.print(" ");
                p.print(indent);
                p.print(" ");
                p.print(getLength());
                p.print(" ");
                p.println(getValue());
            }
        }
    }
}
