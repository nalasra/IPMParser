package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.PDSParserSupport;
import org.hablo.mastercard.util.TLVParser.TLV;

public class PDS0170Parser extends PDSParserSupport {

    public void parse(TLV tag) {
        tag.add(createTLV("1", 0, 16, tag));
        if (tag.getLength() > 16) {
            tag.add(createTLV("2", 16, 32, tag));
            if (tag.getLength() > 32) {
                tag.add(createTLV("3", 32, tag));
            }
        }
    }
}
