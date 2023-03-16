package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
            e.dump(p, indent + " ");
        }
    }

    public static class GenericTag implements Loggeable {

        private String id;
        private int length;
        private String value;
        private String type;
        private String description;
        LinkedHashSet<GenericTag> elements;

        public GenericTag() {
            elements = new LinkedHashSet<>();
        }

        public GenericTag(String id) {
            this();
            this.id = id;
        }

        public GenericTag(String id, String value) {
            this(id);
            this.value = value;
        }

        public GenericTag(String id, String value, String type) {
            this(id, value);
            this.type = type;
        }

        public GenericTag(String id, String value, String type, String description) {
            this(id, value, type);
            this.description = description;
        }

        public GenericTag(String id, int length, String value) {
            this(id, value);
            this.length = length;
        }

        public GenericTag(String id, int length, String value, String type) {
            this(id, length, value);
            this.type = type;
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
            return length > 0 ? length : value != null ? value.length() : 0;
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public void dump(PrintStream p, String indent) {
            p.println(indent + StringUtils.rightPad("_", 100 - indent.length(), '_'));

            p.print(indent);
            String pTypeId = StringUtils.rightPad(getType() + getId(), 15 - indent.length(), ' ');
            p.print(pTypeId);

            p.print(StringUtils.leftPad(getLength() + "", 3, '0'));

            String spaceAfterLength = StringUtils.rightPad(" ", 10 , ' ');
            p.print(spaceAfterLength);

            p.println(getDescription());
            p.print(StringUtils.rightPad(" ", (pTypeId + spaceAfterLength).length() + 3 + indent.length(), ' '));
            p.println(getValue());

            if (elements != null && !elements.isEmpty()) {
                for (GenericTag e : elements) {
                    e.dump(p, indent + " ");
                }
            }
        }
    }
}
