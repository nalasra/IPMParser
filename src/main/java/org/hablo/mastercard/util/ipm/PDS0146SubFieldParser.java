package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.PDSSubFieldParserSupport;
import org.hablo.mastercard.util.TLVParser.TLV;

public class PDS0146SubFieldParser extends PDSSubFieldParserSupport {

    public void parse(TLV tag) {
        int i = 0;
        int counter = 1;
        String data = tag.getValue();
        int startPos = 0;

        while (i < data.length()) {
            startPos = i;

            String feeTypeCode = data.substring(i, i + 2);
            i += 2;
            String feeProcCode = data.substring(i, i + 2);
            i += 2;
            String feeSettIndc = data.substring(i, i + 2);
            i += 2;
            String currCodeFee = data.substring(i, i + 3);
            i += 3;
            String amtFee = data.substring(i, i + 12);
            i += 12;
            String currCodeFeeRecon = data.substring(i, i + 3);
            i += 3;
            String amtFeeRecon = data.substring(i, i + 12);
            i += 12;

            TLV t1 = new TLV();
            t1.setId("[" + counter + "]");
            t1.setType("");
            t1.setValue(data.substring(startPos, i));
            t1.setDescription(tag.getDescription());
            t1.add(createTLV("1", feeTypeCode, "Fee Type Code"));
            t1.add(createTLV("2", feeProcCode, "Fee Processing Code"));
            t1.add(createTLV("3", feeSettIndc, "Fee Settlement Indicator"));
            t1.add(createTLV("4", currCodeFee, "Currency Code, Fee"));
            t1.add(createTLV("5", amtFee, "Amount, Fee"));
            t1.add(createTLV("6", currCodeFeeRecon, "Currency Code, Fee, Reconciliation"));
            t1.add(createTLV("7", amtFeeRecon, "Amount, Fee, Reconciliation"));
            tag.add(t1);
            counter++;
        }
    }
}
