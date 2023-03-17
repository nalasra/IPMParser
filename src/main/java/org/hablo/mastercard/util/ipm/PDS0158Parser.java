package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.TLVParser.TLV;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0158Parser extends PDSParserSupport {

    public void parse(TLV tag) {
        tag.add(new TLV("1", tag.getValue().substring(0, 3), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".1")));
        tag.add(new TLV("2", tag.getValue().substring(3, 4), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".2")));
        tag.add(new TLV("3", tag.getValue().substring(4, 10), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".3")));
        tag.add(new TLV("4", tag.getValue().substring(10, 12), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".4")));
        tag.add(new TLV("5", tag.getValue().substring(12, 18), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".5")));
        tag.add(new TLV("6", tag.getValue().substring(18, 20), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".6")));
        tag.add(new TLV("7", tag.getValue().substring(20, 21), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".7")));
        tag.add(new TLV("8", tag.getValue().substring(21, 24), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".8")));
        tag.add(new TLV("9", tag.getValue().substring(24, 25), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".9")));
        tag.add(new TLV("10", tag.getValue().substring(25, 26), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".10")));
        tag.add(new TLV("11", tag.getValue().substring(26, 27), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".11")));
        tag.add(new TLV("12", tag.getValue().substring(27, 28), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".12")));
        tag.add(new TLV("13", tag.getValue().substring(28, 29), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".13")));
        tag.add(new TLV("14", tag.getValue().substring(29, 30), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".14")));
        if (tag.getValue().length() > 30) {
            tag.add(new TLV("15", tag.getValue().substring(30, 31), "SF", DE48IPMParser.getConverter().convert(tag.getId() + ".15")));
        }
    }
}
