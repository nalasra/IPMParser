package org.hablo.onelink;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.util.FSDMsg;

public class OnelinkPOSSettParser extends FileParserSupport {

    private int counter;

    @Override
    public void parse(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;

            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                if (outputParsedFile) {
                    FSDMsg msgCurrent;
                    if (row.startsWith("HE")) {
                        msgCurrent = parseHeader(row);
                    } else if (row.startsWith("FO")) {
                        msgCurrent = parseFooter(row);
                    } else {
                        msgCurrent = parseDataRecord(row);
                    }
                    writer.write(ISOMsgHelper.toString(msgCurrent));
                }
                counter++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error at line# " + counter);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private FSDMsg parseDataRecord(String t) {
        FSDMsg m = new FSDMsg("");
        int start = 0, len = 14;
        m.set("exportDate", t.substring(start, len));
        start = len; len += 4;
        m.set("messageType", t.substring(start, len));
        start = len; len += 20;
        m.set("pan", t.substring(start, len));
        start = len; len += 6;
        m.set("procCode", t.substring(start, len));
        start = len; len += 12;
        m.set("transactionAmount", t.substring(start, len));
        start = len; len += 12;
        m.set("amountSettlement", t.substring(start, len));
        start = len; len += 12;
        m.set("amountCardholderBilling", t.substring(start, len));
        start = len; len += 10;
        m.set("transmissionDateTime", t.substring(start, len));
        start = len; len += 8;
        m.set("amountCardholderBillingFee", t.substring(start, len));
        start = len; len += 8;
        m.set("conversionRateSettlement", t.substring(start, len));
        start = len; len += 8;
        m.set("conversionRateBilling", t.substring(start, len));
        start = len; len += 6;
        m.set("stan", t.substring(start, len));
        start = len; len += 6;
        m.set("timeLocal", t.substring(start, len));
        start = len; len += 4;
        m.set("dateLocal", t.substring(start, len));
        start = len; len += 4;
        m.set("dateExpiry", t.substring(start, len));
        start = len; len += 4;
        m.set("settlementDate", t.substring(start, len));
        start = len; len += 4;
        m.set("dateConversion", t.substring(start, len));
        start = len; len += 4;
        m.set("reserved1", t.substring(start, len));
        start = len; len += 4;
        m.set("merchantType", t.substring(start, len));
        start = len; len += 3;
        m.set("acquiringInstCtryCode", t.substring(start, len));
        start = len; len += 3;
        m.set("posEntMode", t.substring(start, len));
        start = len; len += 2;
        m.set("posCondCode", t.substring(start, len));
        start = len; len += 9;
        m.set("amountTxnFee", t.substring(start, len));
        start = len; len += 9;
        m.set("amountSettFee", t.substring(start, len));
        start = len; len += 9;
        m.set("reserved2", t.substring(start, len));
        start = len; len += 9;
        m.set("reserved3", t.substring(start, len));
        start = len; len += 11;
        m.set("acquiringInstIdCode", t.substring(start, len));
        start = len; len += 11;
        m.set("forwardingInstIdCode", t.substring(start, len));
        start = len; len += 37;
        m.set("reserved4", t.substring(start, len));
        start = len; len += 12;
        m.set("rrn", t.substring(start, len));
        start = len; len += 6;
        m.set("authIdResp", t.substring(start, len));
        start = len; len += 2;
        m.set("responseCode", t.substring(start, len));
        start = len; len += 16;
        m.set("cardAcceptorTerminalId", t.substring(start, len));
        start = len; len += 15;
        m.set("cardAcceptorIdCode", t.substring(start, len));
        start = len; len += 40;
        m.set("cardAcceptorNameLoc", t.substring(start, len));
        start = len; len += 25;
        m.set("additionalRespData", t.substring(start, len));
        start = len; len += 3;
        m.set("currCodeTxn", t.substring(start, len));
        start = len; len += 3;
        m.set("currCodeSett", t.substring(start, len));
        start = len; len += 3;
        m.set("currCodeBilling", t.substring(start, len));
        start = len; len += 42;
        m.set("originalDataElements", t.substring(start, len));
        start = len; len += 42;
        m.set("replacementAmounts", t.substring(start, len));
        start = len; len += 11;
        m.set("receivingInstIdCode", t.substring(start, len));
        start = len; len += 28;
        m.set("accountId1", t.substring(start, len));
        start = len; len += 28;
        m.set("accountId2", t.substring(start, len));
        start = len; len += 23;
        m.set("reserved5", t.substring(start, len));
        start = len; len += 4;
        m.set("merchantCategoryCode", t.substring(start, len));
        start = len; len += 8;
        m.set("1SSProcessingDate", t.substring(start, len));
        start = len; len += 2;
        m.set("1SSSettlementStatus", t.substring(start, len));
        start = len; len += 12;
        m.set("1SSSourceAmount", t.substring(start, len));
        start = len; len += 12;
        m.set("1SSDestinationAmount", t.substring(start, len));
        start = len; len += 12;
        m.set("1SSPKRAmount", t.substring(start, len));
        start = len; len += 8;
        m.set("1SSConversionRate", t.substring(start, len));
        start = len; len += 8;
        m.set("1SSAcquirerBusinessId", t.substring(start, len));
        start = len; len += 8;
        m.set("1SSPurchaseDate", t.substring(start, len));
        start = len; len += 1;
        m.set("1SSSettlementFlag", t.substring(start, len));
        start = len; len += 8;
        m.set("1SSCentralProcessingDate", t.substring(start, len));
        start = len; len += 12;
        m.set("1SSCommissionAmount", t.substring(start, len));
        return m;
    }

    private FSDMsg parseHeader(String t) {
        FSDMsg m = new FSDMsg("");
        m.set("recordHeaderElement", t.substring(0, 2));
        m.set("timestamp", t.substring(2, 14));
        return m;
    }

    private FSDMsg parseFooter(String t) {
        FSDMsg m = new FSDMsg("");
        m.set("recordHeaderElement", t.substring(0, 2));
        m.set("timestamp", t.substring(2, 14));
        m.set("recordCount", t.substring(14, 26));
        m.set("totalAmountCredit", t.substring(26, 38));
        m.set("totalAmountDebit", t.substring(38, 50));
        return m;
    }
}
