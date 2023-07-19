package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.TLVParser.TLV;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0159Parser extends PDSParserSupport {

    public void parse(TLV tag) {
        tag.add(createTLV("1", 0, 11, tag));
        tag.add(createTLV("2", 11, 39, tag));
        tag.add(createTLV("3", 39, 40, tag));
        tag.add(createTLV("4", 40, 50, tag));
        tag.add(createTLV("5", 50, 51, tag));
        tag.add(createTLV("6", 51, 57, tag));
        tag.add(createTLV("7", 57, 59, tag));
        tag.add(createTLV("8", 59, 65, tag));
        tag.add(createTLV("9", 65, 67, tag));
    }
}
