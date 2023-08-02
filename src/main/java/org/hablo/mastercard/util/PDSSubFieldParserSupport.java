package org.hablo.mastercard.util;

import org.hablo.mastercard.util.TLVParser.TLV;

public abstract class PDSSubFieldParserSupport {
    protected abstract void parse(TLV data);
    protected TLV createTLV(String id, String value, String desc){
        return new TLV(id, value, "SF", desc);
    }
    protected TLV createTLV(String id, int start, int end, TLV tag){
        return new TLV(id, tag.getValue().substring(start, end), "SF", PDSParser.getConverter().convert(tag.getId() + "." + id));
    }
    protected TLV createTLV(String id, int start, TLV tag){
        return new TLV(id, tag.getValue().substring(start), "SF", PDSParser.getConverter().convert(tag.getId() + "." + id));
    }
}

