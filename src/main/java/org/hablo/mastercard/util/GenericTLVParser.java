package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;

import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Loggeable;

/**
 * Created by Arsalan Khan on 09/06/21.
 */
public class GenericTLVParser implements DEParserSupport {

    String sourceTLVData;
    LinkedHashSet<GenericTag> elements;
    int MIN_TAG_ID = 0;
    int MAX_TAG_ID = 9999;
    int MAX_SE_LEN_0100 = 99;
    int MAX_SE_LEN_0800 = 96;
    private int tagSize;
    private int lengthSize;
    private int tlvFieldId;
    private String fieldType;
    private static Map<String, String> allowedSubTags = new HashMap<>();

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
    public GenericTLVParser(int fieldId, int tagSize, int lengthSize, String fieldType, int minSEId, int maxSEId, Map<String, String> allowedSubTags) {
        this(fieldId, tagSize, lengthSize, fieldType, minSEId, maxSEId);
        this.allowedSubTags = allowedSubTags;
    }

    public void setTagSize(int tagSize) {
        this.tagSize = tagSize;
    }

    public void setLengthSize(int lengthSize) {
        this.lengthSize = lengthSize;
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
                throw new BLException("format.error", "0480" + tagId);
            }

            String tagLength = sourceTLVData.substring(i, i + lengthSize);
            i = i + lengthSize;

            int tagLenInt = Integer.parseInt(tagLength);
            boolean parseSubTag = allowedSubTags.containsKey(tagId);

            String value = "";
            GenericTag se = new GenericTag(tagId);
            if (parseSubTag) {
                int j = 0;
                String de48_se = sourceTLVData.substring(i);
                while (j < tagLenInt) {
                    String subTagId = de48_se.substring(j, j + 2);
                    j = j + 2;
                    if (!allowedSubTags.get(tagId).contains(subTagId)) {
                        throw new BLException("format.error", "0480" + tagId);
                    }

                    String subFieldLength = de48_se.substring(j, j + 2);
                    j = j + 2;
                    int sFL = Integer.parseInt(subFieldLength);
                    String subFieldData = de48_se.substring(j, j + sFL);
                    j = j + sFL;
                    se.add(new GenericTag(subTagId, sFL, subFieldData, true));
                }
            } else {
                se.setLength(tagLenInt);
                value = sourceTLVData.substring(i, i + tagLenInt);
            }
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

    @Override
    public void dump(PrintStream p, String indent) {
        for (GenericTag e : elements) {
            e.dump(p, indent + fieldType + e.getId());
        }
    }

    public class GenericTag implements Loggeable {

        private String id;
        private int length;
        private String value;
        LinkedHashSet<GenericTag> elements;
        private boolean isSF = false;

        public GenericTag(String id) {
            this.id = id;
            elements = new LinkedHashSet<>();
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

        public void add(GenericTag de48SE) {
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
