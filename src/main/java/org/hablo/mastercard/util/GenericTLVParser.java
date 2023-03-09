package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Loggeable;

/**
 * Created by Arsalan Khan on 09/06/21.
 */
public class GenericTLVParser implements DEParserSupport {

    String sourceTLVData;
    private LinkedHashSet<GenericTag> elements;
    int MIN_TAG_ID = 0;
    int MAX_TAG_ID = 9999;
    private int tagSize;
    private int lengthSize;
    private int tlvFieldId;
    private String fieldType;

    public GenericTLVParser(int fieldId, int tagSize, int lengthSize, String fieldType) {
        elements = new LinkedHashSet<>();
        this.tlvFieldId = fieldId;
        this.tagSize = tagSize;
        this.lengthSize = lengthSize;
        this.fieldType = fieldType;
    }

    public GenericTLVParser(int fieldId, int tagSize, int lengthSize, String fieldType, int minSEId, int maxSEId) {
        this(fieldId, tagSize, lengthSize, fieldType);
        this.MIN_TAG_ID = minSEId;
        this.MAX_TAG_ID = maxSEId;
    }

    public String getFieldType() {
        return fieldType;
    }

    @Override
    public void parse(ISOMsg m) throws BLException, ISOException {
        if (!m.hasField(tlvFieldId)) {
            return;
        }
        this.sourceTLVData = m.getString(tlvFieldId);
        if (this.sourceTLVData == null || this.sourceTLVData.length() == 0) {
            return;
        }
        int i = 0;

        //tags
        while (i < sourceTLVData.length()) {
            String tagId = sourceTLVData.substring(i, i + tagSize);
            i = i + tagSize;
            int tagIdInt = Integer.parseInt(tagId);
            if (tagIdInt > MAX_TAG_ID || tagIdInt < MIN_TAG_ID) {
                throw new BLException("Encountered tag id out of range. ");
            }

            String tagLength = sourceTLVData.substring(i, i + lengthSize);
            i = i + lengthSize;

            int tagLenInt = Integer.parseInt(tagLength);

            String value = "";
            GenericTag se = new GenericTag(tagId);
            se.setLength(tagLenInt);
            value = sourceTLVData.substring(i, i + tagLenInt);
            se.setValue(value);
            elements.add(se);
            i = i + tagLenInt;
        }
    }

    public boolean hasElement(String id) {
        Objects.requireNonNull(id);
        for (GenericTag e : elements) {
            if (e.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public GenericTag getElementById(String id) {
        Objects.requireNonNull(id);
        for (GenericTag e : elements) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    public Set<GenericTag> getElements() {
        return elements;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        for (GenericTag e : elements) {
            e.dump(p, indent + fieldType + e.getId());
        }
    }

    public static class GenericTag implements Loggeable {

        private String id;
        private int length;
        private String value;
        LinkedHashSet<GenericTag> elements;
        private boolean isSF = false;

        public GenericTag(String id) {
            this.id = id;
            elements = new LinkedHashSet<>();
        }

        public GenericTag(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public GenericTag(String id, String value, boolean sf) {
            this(id, value);
            isSF = sf;
        }

        public GenericTag(String id, int length, String value) {
            this.id = id;
            this.length = length;
            this.value = value;
        }

        public GenericTag(String id, int length, String value, boolean sf) {
            this(id, length, value);
            isSF = sf;
        }

        public boolean hasField(String id) {
            Objects.requireNonNull(id);
            for (GenericTag e : elements) {
                if (e.getId().equals(id)) {
                    return true;
                }
            }
            return false;
        }

        public GenericTag getFieldById(String id) {
            Objects.requireNonNull(id);
            for (GenericTag e : elements) {
                if (e.getId().equals(id)) {
                    return e;
                }
            }
            return null;
        }

        public void add(GenericTag item) {
            elements.add(item);
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
            if (elements != null && !elements.isEmpty()) {
                for (GenericTag e : elements) {
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
