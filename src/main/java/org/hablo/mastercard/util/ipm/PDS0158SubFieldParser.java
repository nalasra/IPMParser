package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.PDSSubFieldParserSupport;
import org.hablo.mastercard.util.TLVParser.TLV;

public class PDS0158SubFieldParser extends PDSSubFieldParserSupport {

    public void parse(TLV tag) {
        tag.add(createTLV("1", 0, 3, tag));
        tag.add(createTLV("2", 3, 4, tag));
        tag.add(createTLV("3", 4, 10, tag));
        tag.add(createTLV("4", 10, 12, tag));

        if (tag.getLength() > 12) {
            tag.add(createTLV("5", 12, 18, tag));
            if (tag.getLength() > 18) {
                tag.add(createTLV("6", 18, 20, tag));
                tag.add(createTLV("7", 20, 21, tag));
                tag.add(createTLV("8", 21, 24, tag));
                tag.add(createTLV("9", 24, 25, tag));
                tag.add(createTLV("10", 25, 26, tag));
                tag.add(createTLV("11", 26, 27, tag));
                tag.add(createTLV("12", 27, 28, tag));
                tag.add(createTLV("13", 28, 29, tag));
                tag.add(createTLV("14", 29, 30, tag));
                if (tag.getLength() > 30) {
                    tag.add(createTLV("15", 30, 31, tag));
                }
            }
        }
    }
}
