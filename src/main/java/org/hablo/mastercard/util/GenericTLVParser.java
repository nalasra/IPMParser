package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.LinkedHashSet;
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
    private LinkedHashSet<GenericTag> tags;
    int MIN_TAG_ID = 0;
    int MAX_TAG_ID = 9999;
    private final int tagSize;
    private final int lengthSize;
    private final int tlvFieldId;
    private final String fieldType;

    public GenericTLVParser(int fieldId, int tagSize, int lengthSize, String fieldType) {
        tags = new LinkedHashSet<>();
        this.tlvFieldId = fieldId;
        this.tagSize = tagSize;
        this.lengthSize = lengthSize;
        this.fieldType = fieldType;
    }

    public GenericTLVParser(int fieldId, int tagSize, int lengthSize, String fieldType, int minTagId, int maxTagId) {
        this(fieldId, tagSize, lengthSize, fieldType);
        this.MIN_TAG_ID = minTagId;
        this.MAX_TAG_ID = maxTagId;
    }

    public int getTlvFieldId() {
        return tlvFieldId;
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
                throw new BLException("Encountered tag id out of range.");
            }

            String tagLength = sourceTLVData.substring(i, i + lengthSize);
            i = i + lengthSize;

            int tagLenInt = Integer.parseInt(tagLength);

            String value = "";
            value = sourceTLVData.substring(i, i + tagLenInt);
            tags.add(new GenericTag(tagId, tagLenInt, value, fieldType));
            i = i + tagLenInt;
        }
    }

    public boolean hasElement(String id) {
        Objects.requireNonNull(id);
        for (GenericTag e : tags) {
            if (e.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public GenericTag getElementById(String id) {
        Objects.requireNonNull(id);
        for (GenericTag e : tags) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    protected Set<GenericTag> getTags() {
        return tags;
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + sourceTLVData + "'");
        for (GenericTag e : getTags()) {
            e.dump(p, indent+ " ");
        }
    }

    public static class GenericTag implements Loggeable {

        private String id;
        private int length;
        private String value;
        private String type;
        private String description;
        Set<GenericTag> elements;
        private boolean dumpable = false;
        private String dumpData;

        public GenericTag() {
            elements = new LinkedHashSet<>();
        }

        public GenericTag(String id) {
            this();
            this.id = id;
        }

        public GenericTag(String dumpData, boolean dumpable) {
            this();
            this.dumpData = dumpData;
            this.dumpable = dumpable;
        }

        public GenericTag(String id, String value) {
            this();
            this.id = id;
            this.value = value;
        }

        public GenericTag(String id, String value, String description) {
            this(id, value);
            this.description = description;
        }

        public GenericTag(String id, int length, String value) {
            this();
            this.id = id;
            this.length = length;
            this.value = value;
        }

        public GenericTag(String id, int length, String value, String type) {
            this(id, length, value);
            this.type = type;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public boolean hasElements(){
            return !elements.isEmpty();
        }

        public Set<GenericTag> getElements() {
            return elements;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public void dump(PrintStream p, String indent) {
            if (dumpable) {
                p.println(indent + dumpData);
            } else {
                p.print(indent);
                p.print("<");
                p.print(getType());
                p.print(" id=\"");
                p.print(getId());
                p.print("\"");
                p.print(" length=\"");
                p.print(getLength());
                p.print("\"");
                p.print(" value=\"");
                p.print(getValue());
                p.print("\">");
            }
            if (elements != null && !elements.isEmpty()) {
                p.println();
                for (GenericTag e : elements) {
                    e.dump(p, indent);
                }
            }
            if(!dumpable) {
                p.print(indent);
                p.print("</");
                p.print(getType());
                p.println(">");
            }
        }
    }
}
