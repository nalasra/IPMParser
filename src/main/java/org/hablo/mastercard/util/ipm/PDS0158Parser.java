package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.GenericTLVParser.GenericTag;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0158Parser extends PDSParserSupport {

    public void parse(GenericTag tag) {
        tag.add(new GenericTag("1", tag.getValue().substring(0, 3), "SF", "Card Program Identifier"));
        tag.add(new GenericTag("2", tag.getValue().substring(3, 4), "SF", "Business Service Arrangement Type Code"));
        tag.add(new GenericTag("3", tag.getValue().substring(4, 10), "SF", "Business Service ID Code"));
        tag.add(new GenericTag("4", tag.getValue().substring(10, 12), "SF", "Interchange Rate Designator"));
        tag.add(new GenericTag("5", tag.getValue().substring(12, 18), "SF", "Central Site Business Date"));
        tag.add(new GenericTag("6", tag.getValue().substring(18, 20), "SF", "Business Cycle"));
        tag.add(new GenericTag("7", tag.getValue().substring(20, 21), "SF",
                "Card Acceptor Classification Override Indicator"));
        tag.add(new GenericTag("8", tag.getValue().substring(21, 24), "SF", "Product Class Override Indicator"));
        tag.add(new GenericTag("9", tag.getValue().substring(24, 25), "SF",
                "Corporate Incentive Rates Apply Indicator"));
        tag.add(new GenericTag("10", tag.getValue().substring(25, 26), "SF", "Special Conditions Indicator"));
        tag.add(new GenericTag("11", tag.getValue().substring(26, 27), "SF",
                "Mastercard Assigned ID Override Indicator"));
        tag.add(new GenericTag("12", tag.getValue().substring(27, 28), "SF",
                "Account Level Management Account Category Code"));
        tag.add(new GenericTag("13", tag.getValue().substring(28, 29), "SF", "Rate Indicator"));
        tag.add(new GenericTag("14", tag.getValue().substring(29, 30), "SF", "Masterpass Incentive Indicator"));
        if (tag.getValue().length() > 30) {
            tag.add(new GenericTag("15", tag.getValue().substring(30, 31), "SF",
                    "Digital Wallet Interchange Override Indicator"));
        }
    }
}
