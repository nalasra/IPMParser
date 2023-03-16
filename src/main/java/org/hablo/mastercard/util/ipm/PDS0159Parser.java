package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.GenericTLVParser.GenericTag;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0159Parser extends PDSParserSupport {

    public void parse(GenericTag tag) {
        tag.add(new GenericTag("1", tag.getValue().substring(0, 11), "SF", "Settlement Service Transfer Agent ID Code"));
        tag.add(new GenericTag("2", tag.getValue().substring(11, 39), "SF", "Settlement Service Transfer Agent Account"));
        tag.add(new GenericTag("3", tag.getValue().substring(39, 40), "SF", "Settlement Service Level Code"));
        tag.add(new GenericTag("4", tag.getValue().substring(40, 50), "SF", "Settlement Service ID Code"));
        tag.add(new GenericTag("5", tag.getValue().substring(50, 51), "SF", "Settlement Foreign Exchange Rate Class Code"));
        tag.add(new GenericTag("6", tag.getValue().substring(51, 57), "SF", "Reconciliation Date"));
        tag.add(new GenericTag("7", tag.getValue().substring(57, 59), "SF", "Reconciliation Cycle"));
        tag.add(new GenericTag("8", tag.getValue().substring(59, 65), "SF", "Settlement Date"));
        tag.add(new GenericTag("9", tag.getValue().substring(65, 67), "SF", "Settlement Cycle"));
    }
}
