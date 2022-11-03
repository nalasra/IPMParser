package org.hablo.mastercard.util;

import org.jpos.ee.BLException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public class DE108Parser implements DEParserSupport {
    String deValue;
    LinkedHashSet<DE108SE> elements;
    int MIN_SE_ID = 0;
    int MAX_SE_ID = 6;
    private static final Map<String, String> allowedSF = new HashMap<>();

    static {
        allowedSF.put("01", "01,03,07,11,18");
        allowedSF.put("02", "01,03,04,07,11,18");
        allowedSF.put("03", "01");
    }

    public DE108Parser(ISOMsg m) throws BLException {
        elements = new LinkedHashSet<>();
        parse(m);
    }

    @Override
    public void parse(ISOMsg m) throws BLException {
        deValue = m.getString(108);
        if (this.deValue == null || this.deValue.length() == 0) return;
        //010640109FIRSTNAME0308LASTNAME0703USA1122123456789+123456789012180202020690109FIRSTNAME0308LASTNAME04121234 MAIN ST0703USA1111123456789101802050302301190877775555580121530
        //subElement 2 - 01 to 06
        //subElementLength 3
        //subField 2
        //subFieldLength 2
        //subFieldData up to N
        int i = 0;
        while (i < deValue.length()) {
            String subElementId = deValue.substring(i, i + 2);
            i = i + 2;
            int sE = Integer.parseInt(subElementId);
            if (sE > MAX_SE_ID || sE < MIN_SE_ID)
                throw new BLException("format.error", "0480" + subElementId);

            String subElementLength = deValue.substring(i, i + 3);
            i = i + 3;

            int sEL = Integer.parseInt(subElementLength);
            boolean parseSF = allowedSF.containsKey(subElementId);

            String value = "";
            DE108SE se = new DE108SE(subElementId);
            if (parseSF) {
                int j = 0;
                String f108_se = deValue.substring(i);
                while (j < sEL) {
                    String subFieldId = f108_se.substring(j, j + 2);
                    j = j + 2;
                    if (!allowedSF.get(subElementId).contains(subFieldId))
                        throw new BLException("format.error", "1080" + subElementId);

                    String subFieldLength = f108_se.substring(j, j + 2);
                    j = j + 2;
                    int sFL = Integer.parseInt(subFieldLength);
                    String subFieldData = f108_se.substring(j, j + sFL);
                    j = j + sFL;
                    se.add(new DE108SE(subFieldId, sFL, subFieldData, true));
                }
            } else {
                se.setLength(sEL);
                value = deValue.substring(i, i + sEL);
            }

            se.setValue(value);
            elements.add(se);
            i = i + sEL;
        }
    }

    public boolean hasElement(String id) {
        Objects.requireNonNull(id);
        for (DE108SE e : elements) {
            if (e.getId().equals(id))
                return true;
        }
        return false;
    }

    public DE108SE getElementById(String id) {
        Objects.requireNonNull(id);
        for (DE108SE e : elements) {
            if (e.getId().equals(id))
                return e;
        }
        return null;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + deValue + "'");
        for (DE108SE e : elements) {
            e.dump(p, indent + "SE" + e.getId());
        }
    }

    public class DE108SE implements Loggeable {
        private String id;
        private int length;
        private String value;
        LinkedHashSet<DE108SE> elements;
        private boolean isSF = false;

        public DE108SE(String id) {
            this.id = id;
            elements = new LinkedHashSet<>();
        }

        public DE108SE(String id, int length, String value) {
            this.id = id;
            this.length = length;
            this.value = value;
        }

        public DE108SE(String id, int length, String value, boolean sf) {
            this(id, length, value);
            isSF = sf;
        }

        public boolean hasField(String id) {
            Objects.requireNonNull(id);
            for (DE108SE e : elements) {
                if (e.getId().equals(id))
                    return true;
            }
            return false;
        }

        public DE108SE getFieldById(String id) {
            Objects.requireNonNull(id);
            for (DE108SE e : elements) {
                if (e.getId().equals(id))
                    return e;
            }
            return null;
        }

        public void add(DE108SE de108SE) {
            elements.add(de108SE);
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
                for (DE108SE e : elements) {
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
