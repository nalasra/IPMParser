package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.PDSSubFieldParserSupport;
import org.hablo.mastercard.util.TLVParser.TLV;

public class PDS0218SubFieldParser extends PDSSubFieldParserSupport {

    public void parse(TLV tag) {
        tag.add(createTLV("1", 0, 2, tag));
        tag.add(createTLV("2", 2, 4, tag));
    }
}
