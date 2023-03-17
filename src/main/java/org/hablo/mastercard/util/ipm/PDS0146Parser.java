package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.TLVParser.TLV;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0146Parser extends PDSParserSupport {

    public void parse(TLV tag) {
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

            TLV t1 = new TLV();


            tag.add(new TLV(
                    " <p0146 idx=\"" + counter + "\" fee_type_code=\"" + feeTypeCode + "\" fee_proc_code=\""
                            + feeProcCode + "\" fee_sett_indc=\"" + feeSettIndc + "\" curr_code_fee=\"" + currCodeFee
                            + "\" amt_fee=\"" + amtFee + "\" curr_code_fee_recon=\"" + currCodeFeeRecon
                            + "\" amt_fee_recon=\"" + amtFeeRecon + "\"/>"));
            counter++;
        }
    }
}
