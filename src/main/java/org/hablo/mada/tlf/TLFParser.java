package org.hablo.mada.tlf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.regex.Pattern;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.util.FSDMsg;

public class TLFParser extends FileParserSupport {

    private int counter;

    @Override
    public void parse(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            String type = "";
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                if (outputParsedFile) {
                    FSDMsg msgCurrent = null;
                    if (row.startsWith("HDTLF")) {
                        type = row.substring(5, 7);
                        msgCurrent = parseHeader(row);
                    } else if (row.startsWith("TRTLF")) {
                        msgCurrent = parseTrailer(row);
                    } else {
                        if ("02,04,06".contains(type)) {
                            msgCurrent = parseRecord_02_04_06(row);
                        } else if ("08".equals(type)) {
                            msgCurrent = parseRecord_02_04_06(row);
                        } else if ("12,14".equals(type)) {

                        }
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

    private FSDMsg parseHeader(String t) {
        FSDMsg m = new FSDMsg("");
        m.set("tlfHeaderId", t.substring(0, 5));
        m.set("extractFileId", t.substring(5, 7));
        m.set("institutionName", t.substring(7, 11));
        m.set("settlementDate", t.substring(11, 19));
        m.set("extractDate", t.substring(19, 27));
        m.set("numberOfRecords", t.substring(27));
        return m;
    }

    private FSDMsg parseTrailer(String t) {
        FSDMsg m = new FSDMsg("");
        m.set("tlfTrailerId", t.substring(0, 5));
        m.set("extractFileId", t.substring(5, 7));
        m.set("institutionName", t.substring(7, 11));
        m.set("settlementDate", t.substring(11, 19));
        m.set("extractDate", t.substring(19, 27));
        m.set("numberOfRecords", t.substring(27));
        return m;
    }

    private FSDMsg parseRecord_12_14(String t) {
        FSDMsg m = new FSDMsg("");
        String[] fields = t.split(Pattern.quote("%@$"));
        m.set("msgType", fields[0]);
        return m;
    }

    private FSDMsg parseRecord_02_04_06(String t) {
        FSDMsg m = new FSDMsg("");
        String[] fields = t.split(Pattern.quote("%@$"));
        m.set("msgType", fields[0]);
        m.set("pan", fields[1]);
        m.set("processingCode", fields[2]);
        m.set("amountTransaction", fields[3]);
        m.set("reconciliationAmount", fields[4]);
        m.set("transactionDateAndTime", fields[5]);
        m.set("reconConversionRate", fields[6]);
        m.set("stan", fields[7]); //de11
        m.set("localDateTime", fields[8]);
        m.set("expirationDate", fields[9]);
        m.set("conversionDate", fields[10]);
        m.set("captureDate", fields[11]);
        m.set("posDataCode", fields[12]);
        m.set("functionCode", fields[13]);
        m.set("messageReasonCode", fields[14]);
        m.set("cardAcceptorBusinessCode", fields[15]);
        m.set("reconDate", fields[16]);
        m.set("originalAmount", fields[17]); //de30
        m.set("settlementAmount", fields[18]); //de05
        m.set("acqInstIdCode", fields[19]); //de32
        m.set("rrn", fields[20]); //de37
        m.set("approvalCode", fields[21]); //de38
        m.set("responseCode", fields[22]); //de39
        m.set("terminalId", fields[23]); //de41
        m.set("acceptorId", fields[24]); //de42
        m.set("acceptorName", fields[25]); //de43
        m.set("transactionCurrencyCode", fields[26]); //de49
        m.set("reconCurrencyCode", fields[27]); //de50
        m.set("additionalAmounts", fields[28]); //de54
        m.set("originalDataElements", fields[29]); //de56
        m.set("transportData", fields[30]); //de59
        m.set("posTerminalData", fields[31]); //de60
        m.set("dataRecord", fields[32]); //mada internal
        m.set("transactionDestination", fields[33]); //de100
        m.set("transactionOriginator", fields[34]); //
        m.set("(additional separator)", fields[35]); //
        m.set("receivingInstId", fields[36]); //
        m.set("cardSchemeSponsorId", fields[37]); //de122
        m.set("cardSchemeInformation", fields[38]); //de123
        m.set("issuerData", fields[39]); //NA
        m.set("bank_card_scheme_totals", fields[40]); //de123.8
        m.set("madaPOSmadaReconcileTotal", fields[41]); //
        m.set("chipIndex", fields[42]); //
        m.set("cardproductId", fields[43]); //
        if (fields.length > 44) {
            m.set("merchantRefId", fields[44]); //
        }
        if (fields.length > 45) {
            m.set("gatewayId", fields[45]); //
        }
        if (fields.length > 46) {
            m.set("cof data", fields[46]); //
        }

        return m;
    }
}
