package org.hablo.mastercard.util;

import org.jpos.ee.BLException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Loggeable;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

public class DE110Parser implements DEParserSupport {
    String de110;
    LinkedHashSet<DE110SE> elements;
    int MIN_SE_ID = 0;
    int MAX_SE_ID = 10;
    private static final Map<String, String> allowedSF = new HashMap<>();

    static {
//        allowedSF.put("09", "01,03,07,11,18");
    }

    public DE110Parser(ISOMsg m) throws BLException {
        elements = new LinkedHashSet<>();
        parse(m);
    }

    @Override
    public void parse(ISOMsg m) throws BLException {
        de110 = m.getString(110);
        if (this.de110 == null || this.de110.length() == 0) return;
        //09080B0080P0TB00S000082E9F773BCFF20772A6D292A45F5F4C97EAD3C519679D1E95AC6A9E509F7BEE21000658D4C6
        //subElement 2 - 01 to 06
        //subElementLength 3
        //subField 2
        //subFieldLength 2
        //subFieldData up to N
        int i = 0;
        while (i < de110.length()) {
            String subElementId = de110.substring(i, i + 2);
            i = i + 2;
            int sE = Integer.parseInt(subElementId);
            if (sE > MAX_SE_ID || sE < MIN_SE_ID)
                throw new BLException("format.error", "0480" + subElementId);

            String subElementLength = de110.substring(i, i + 3);
            i = i + 3;

            int sEL = Integer.parseInt(subElementLength);
            boolean parseSF = allowedSF.containsKey(subElementId);

            String value = "";
            DE110SE se = new DE110SE(subElementId);
            if (parseSF) {
                int j = 0;
                String f108_se = de110.substring(i);
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
                    se.add(new DE110SE(subFieldId, sFL, subFieldData, true));
                }
            } else {
                se.setLength(sEL);
                value = de110.substring(i, i + sEL);
            }

            se.setValue(value);
            elements.add(se);
            i = i + sEL;
        }
    }

    public boolean hasElement(String id) {
        Objects.requireNonNull(id);
        for (DE110SE e : elements) {
            if (e.getId().equals(id))
                return true;
        }
        return false;
    }

    public DE110SE getElementById(String id) {
        Objects.requireNonNull(id);
        for (DE110SE e : elements) {
            if (e.getId().equals(id))
                return e;
        }
        return null;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + de110 + "'");
        for (DE110SE e : elements) {
            e.dump(p, indent + "SE" + e.getId());
        }
    }

    public class DE110SE implements Loggeable {
        private String id;
        private int length;
        private String value;
        LinkedHashSet<DE110SE> elements;
        private boolean isSF = false;

        public DE110SE(String id) {
            this.id = id;
            elements = new LinkedHashSet<>();
        }

        public DE110SE(String id, int length, String value) {
            this.id = id;
            this.length = length;
            this.value = value;
        }

        public DE110SE(String id, int length, String value, boolean sf) {
            this(id, length, value);
            isSF = sf;
        }

        public boolean hasField(String id) {
            Objects.requireNonNull(id);
            for (DE110SE e : elements) {
                if (e.getId().equals(id))
                    return true;
            }
            return false;
        }

        public DE110SE getFieldById(String id) {
            Objects.requireNonNull(id);
            for (DE110SE e : elements) {
                if (e.getId().equals(id))
                    return e;
            }
            return null;
        }

        public void add(DE110SE de108SE) {
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
                for (DE110SE e : elements) {
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
