package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.GenericTLVParser.GenericTag;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0146Parser extends PDSParserSupport {

    public void parse(GenericTag tag) {
        int i = 0;
        int counter = 1;
        String data = tag.getValue();

        while (i < data.length()) {
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

            GenericTag t1 = new GenericTag(counter + "");
            t1.add(new GenericTag("1", feeTypeCode, "fee_type_code"));
            t1.add(new GenericTag("2", feeProcCode, "fee_proc_code"));
            t1.add(new GenericTag("3", feeSettIndc, "fee_sett_indc"));
            t1.add(new GenericTag("4", currCodeFee, "curr_code_fee"));
            t1.add(new GenericTag("5", amtFee, "amt_fee"));
            t1.add(new GenericTag("6", currCodeFeeRecon, "curr_code_fee_recon"));
            t1.add(new GenericTag("7", currCodeFee, "curr_code_fee"));
            t1.add(new GenericTag("8", amtFeeRecon, "amt_fee_recon"));

            tag.add(t1);
            counter++;
        }
    }
}
