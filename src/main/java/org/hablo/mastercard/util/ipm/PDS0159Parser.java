package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.TLVParser.TLV;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0159Parser extends PDSParserSupport {

    public void parse(TLV tag) {
        tag.add(new TLV("1", tag.getValue().substring(0, 11), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".1")));
        tag.add(new TLV("2", tag.getValue().substring(11, 39), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".2")));
        tag.add(new TLV("3", tag.getValue().substring(39, 40), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".3")));
        tag.add(new TLV("4", tag.getValue().substring(40, 50), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".4")));
        tag.add(new TLV("5", tag.getValue().substring(50, 51), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".5")));
        tag.add(new TLV("6", tag.getValue().substring(51, 57), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".6")));
        tag.add(new TLV("7", tag.getValue().substring(57, 59), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".7")));
        tag.add(new TLV("8", tag.getValue().substring(59, 65), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".8")));
        tag.add(new TLV("9", tag.getValue().substring(65, 67), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".9")));
    }
}
