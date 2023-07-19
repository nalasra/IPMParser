package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.PDSParserSupport;
import org.hablo.mastercard.util.TLVParser.TLV;

public class PDS0208Parser extends PDSParserSupport {

    public void parse(TLV tag) {
        tag.add(createTLV("1", 0, 11, tag));
        tag.add(createTLV("2", 11, 25, tag));
    }
}
