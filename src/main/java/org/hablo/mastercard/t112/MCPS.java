package org.hablo.mastercard.t112;


import static org.hablo.helper.CurrencyHelper.convertAmountToDecimal;
import static org.hablo.helper.CurrencyHelper.formatDecimal;
import static org.hablo.helper.FileHelper.getFiles;
import static org.hablo.mastercard.t112.T112Parser.getKey;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hablo.helper.CurrencyHelper;
import org.hablo.helper.FilenameComparator;
import org.hablo.mastercard.util.PDSParser;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class MCPS {

    static Integer MAX_CYCLES = 7;
    static String ID = "/" + System.currentTimeMillis() + "/";
    static String filePath = System.getProperty("user.dir") + "/src/dist/files/mastercard/t112/in/";
    static String filePathO = System.getProperty("user.dir") + "/src/dist/files/mastercard/t112/out/";

    static Map<String, ReconObject> currencyReconMap = Collections.synchronizedMap(new LinkedHashMap<>());
    static Map<String, ReconObject> transactionReconMap = Collections.synchronizedMap(new LinkedHashMap<>());

    static Map<String,String> businessTypeCodeMap = new HashMap<>();
    static Map<String,String> transactionCodeMap = new HashMap<>();

    static {

        transactionCodeMap.put("00", "PURCHASE");
        transactionCodeMap.put("01", "ATM CASH");
        transactionCodeMap.put("18", "UNIQUE TXN");
        transactionCodeMap.put("20", "CREDIT");

        businessTypeCodeMap.put("1", "INTERREGIONAL");
        businessTypeCodeMap.put("2", "INTRAREGIONAL");
        businessTypeCodeMap.put("3", "INTERCOUNTRY");
        businessTypeCodeMap.put("4", "INTRACOUNTRY");
        businessTypeCodeMap.put("8", "MBR. TO MBR.");
        businessTypeCodeMap.put(" ", "CUSTOM");
    }

    public static void main(String[] args) {
        try {
            File[] fs = getFiles("mox", filePath);
            if (fs != null) {
                Arrays.sort(fs, new FilenameComparator());
                System.out.printf("Total %d files/folders found\n", fs.length);

                File theDir = new File(filePathO + ID);
                if (!theDir.exists()) {
                    theDir.mkdirs();
                }

                int index = 1;
                for (File f : fs) {
                    System.out.printf("Processing file... %d/%d %s%n", index, fs.length, f.getAbsolutePath());
                    BufferedWriter fileWriter = initializeWriter(filePath, filePathO, f);
                    T112Parser parser = new T112Parser();
                    parser.setOutputParsedFile(true);
                    parser.setWriter(fileWriter);
                    parser.parse(f);
                    index++;
                    fileWriter.close();
                    Thread.sleep(1000);
                    //process messages parsed from the file
                    processMsgs(FilenameUtils.getExtension(f.getName()), parser);
                    Thread.sleep(1000);
                }

                Thread.sleep(1000);

                System.out.printf("Generate presentment report...\n");
                generatePresentmentReport();

                System.out.printf("Generate summary report...");
                generateSummaryReport();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static void printHeader(BufferedWriter writer, String... items) throws IOException {
        StringBuilder underline = new StringBuilder();
        for (String s : items) {
            writer.write(s);
            underline.append(StringUtils.rightPad("", s.length(), '-'));
        }
        writer.newLine();
        writer.write(underline.toString());
        writer.newLine();
    }

    private static Map<String, List<ReconObject>> getDataByBusinessServiceLevels() {
        Map<String, List<ReconObject>> map = new TreeMap<>();

        for (String txnType : transactionReconMap.keySet()) {
            ReconObject reconObject = transactionReconMap.get(txnType);

            List<ReconObject> tt;
            String key = reconObject.getBstype() + "." + reconObject.getBsid();
            if (map.containsKey(key)) {
                tt = map.get(key);
            } else {
                tt = new ArrayList<>();
                map.put(key, tt);
            }
            tt.add(reconObject);
        }

        return map;
    }

    private static void generatePresentmentReport() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePathO + ID + "presentment.txt"))) {
            printHeader(writer,
                    "      ",
                    StringUtils.leftPad("TRANS. FUNC", 12, ' '),
                    StringUtils.leftPad("PROC CODE", 13, ' '),
                    StringUtils.leftPad("IRD", 13, ' '),
                    StringUtils.leftPad("COUNTS", 9, ' '),
                    StringUtils.leftPad("RECON AMOUNT", 30, ' '),
                    StringUtils.leftPad("RECON CURR CODE", 18, ' '),
                    StringUtils.leftPad("TRANS FEE", 22, ' '),
                    StringUtils.leftPad("FEE CURR CODE", 16, ' ')
            );

            for (int i = 1; i <= MAX_CYCLES; i++) {
                writer.newLine();
                writer.write("CYCLE : " + i);

                BigInteger totalTxnCount = BigInteger.ZERO;
                BigDecimal totalTxnAmount = BigDecimal.ZERO;
                BigDecimal totalFeeAmount = BigDecimal.ZERO;

                BigInteger totalTxnCountAsOfCycle = BigInteger.ZERO;
                BigDecimal totalTxnAmountAsOfCycle = BigDecimal.ZERO;
                BigDecimal totalFeeAmountAsOfCycle = BigDecimal.ZERO;

                var data = getDataByBusinessServiceLevels();
                for (String t : data.keySet()) {
                    List<ReconObject> list = getDataByBusinessServiceLevels().get(t);
                    writer.newLine();
                    writer.newLine();
                    String[] bs = t.split("\\.");
                    writer.write("BUSINESS SERVICE LEVEL: " + businessTypeCodeMap.get(bs[0]));
                    writer.newLine();
                    writer.write("BUSINESS SERVICE ID: " + bs[1]);

                    for (ReconObject reconObject : list) {
                        ReconFigures currentReconFigures = reconObject.getReconFigures(i);

                        if(!currentReconFigures.getTransactionAmounts().isEmpty() && !currentReconFigures.getFeeAmounts().isEmpty()) {
                            writer.newLine();
                            writer.write("         ");
                            writer.write(reconObject.getMti());
                            writer.write(" ");
                            writer.write(reconObject.getFunc());
                            writer.write("     ");
                            writer.write(StringUtils.rightPad(transactionCodeMap.get(reconObject.getTranCode()), 10, ' '));
                            writer.write(" ");
                            writer.write(reconObject.getIndicator());
                            writer.write("    ");
                            writer.write(reconObject.getIrd());
                            writer.write(" ");

                            writer.write(
                                    StringUtils.leftPad(currentReconFigures.getReconAmounts().size() + "", 9, ' '));
                            writer.write(
                                    StringUtils.leftPad(formatDecimal(currentReconFigures.sumReconAmounts()), 30, ' '));

                            //recon curr
                            Currency c1 = null;
                            if (reconObject.getReconCurrency() != null) {
                                c1 = CurrencyHelper.getInstance(reconObject.getReconCurrency());
                            }
                            if (c1 != null) {
                                writer.write(
                                        StringUtils.leftPad(reconObject.getReconCurrency() + "-" + c1.getCurrencyCode(),
                                                10,
                                                ' '));
                            } else {
                                writer.write(
                                        StringUtils.leftPad(reconObject.getReconCurrency() + "-???", 10, ' '));
                            }

                            //trans fee
                            writer.write(
                                    StringUtils.leftPad(formatDecimal(currentReconFigures.sumFeeAmounts()), 30, ' '));

                            //fee curr code
                            Currency c2 = null;
                            if (reconObject.getFeeCurrency() != null) {
                                c2 = CurrencyHelper.getInstance(reconObject.getFeeCurrency());
                                if (c2 != null) {
                                    writer.write(
                                            StringUtils.leftPad(
                                                    reconObject.getFeeCurrency() + "-" + c2.getCurrencyCode(),
                                                    10,
                                                    ' '));
                                } else {
                                    writer.write(
                                            StringUtils.leftPad(reconObject.getFeeCurrency() + "-???", 10, ' '));
                                }
                            }
                        }

                        totalTxnCount = totalTxnCount.add(
                                new BigInteger(String.valueOf(currentReconFigures.getReconAmounts().size())));
                        totalTxnAmount = totalTxnAmount.add(currentReconFigures.sumReconAmounts());
                        totalFeeAmount = totalFeeAmount.add(currentReconFigures.sumFeeAmounts());

                        //Clearing Day Totals
                        Set<ReconFigures> reconFiguresList = reconObject.getReconFiguresTillCycle(i);
                        BigDecimal tmpTxnAmount = BigDecimal.ZERO;
                        BigDecimal tmpFeeAmount = BigDecimal.ZERO;
                        int tmpTxnCount = 0;

                        for (ReconFigures reconFigures : reconFiguresList) {
                            if (reconFigures == null) {
                                continue;
                            }
                            tmpTxnAmount = tmpTxnAmount.add(reconFigures.sumReconAmounts());
                            tmpFeeAmount = tmpFeeAmount.add(reconFigures.sumFeeAmounts());
                            tmpTxnCount += reconFigures.getReconAmounts().size();
                        }

                        totalTxnCountAsOfCycle = totalTxnCountAsOfCycle.add(
                                new BigInteger(String.valueOf(tmpTxnCount)));
                        totalTxnAmountAsOfCycle = totalTxnAmountAsOfCycle.add(tmpTxnAmount);
                        totalFeeAmountAsOfCycle = totalFeeAmountAsOfCycle.add(tmpFeeAmount);
                    }
                }

                writer.newLine();
                writer.write(
                        "---------------------------------------------------------------------------------------------------------------------------------\n");
                writer.write(StringUtils.leftPad("SUBTOTAL:", 27, ' '));
                writer.write(StringUtils.leftPad(totalTxnCount.toString(), 26, ' '));
                writer.write(StringUtils.leftPad(formatDecimal(totalTxnAmount), 30, ' '));
                writer.write(StringUtils.leftPad(formatDecimal(totalFeeAmount), 40, ' '));
                writer.newLine();

                //CLEARING DAY TOTAL AS OF CYCLE: X
                writer.write(StringUtils.rightPad("TOTAL AS OF CYCLE - " + i, 27, ' '));
                writer.write(StringUtils.leftPad(totalTxnCountAsOfCycle.toString(), 26, ' '));
                writer.write(StringUtils.leftPad(formatDecimal(totalTxnAmountAsOfCycle), 30, ' '));
                writer.write(StringUtils.leftPad(formatDecimal(totalFeeAmountAsOfCycle), 40, ' '));

                writer.newLine();
            }
        }
    }

    private static void generateSummaryReport() throws IOException {
        try (BufferedWriter summaryWriter = new BufferedWriter(new FileWriter(filePathO + ID + "summary.txt"))) {

            //Clearing Cycle Summary Reconciliation Report

            int cycleSpaces = 5;
            int currencySpaces = 10;
            int netAmountSpaces = 28;

            summaryWriter.write("=========================\n");
            summaryWriter.write("  CLEARING CYCLE DETAIL  \n");
            summaryWriter.write("=========================\n");
            summaryWriter.newLine();

            summaryWriter.write("\t");
            summaryWriter.write("CYCLE ACTIVITY");
            summaryWriter.write(StringUtils.rightPad(" ", cycleSpaces, ' '));
            summaryWriter.write("FILE ID TO MC");
            summaryWriter.write(StringUtils.leftPad(" ", currencySpaces, ' '));
            summaryWriter.write("FILE ID FROM MC");
            summaryWriter.write(StringUtils.leftPad(" ", 19, ' '));
            summaryWriter.write("NET RECON CURRENCY AMOUNT");
            summaryWriter.newLine();
            summaryWriter.write("\t");
            summaryWriter.write("______________");
            summaryWriter.write(StringUtils.rightPad(" ", cycleSpaces, ' '));
            summaryWriter.write("______________");
            summaryWriter.write(StringUtils.leftPad(" ", 9, ' '));
            summaryWriter.write("_________________________");
            summaryWriter.write(StringUtils.leftPad(" ", 9, ' '));
            summaryWriter.write("_________________________");
            summaryWriter.newLine();

            for (String r : currencyReconMap.keySet()) {
                summaryWriter.newLine();
                ReconObject reconObject = currencyReconMap.get(r);
                Currency curr = CurrencyHelper.getInstance(r);
                if (curr != null) {
                    summaryWriter.write("CURRENCY : " + r + "-" + curr.getCurrencyCode());
                } else {
                    summaryWriter.write("CURRENCY : " + r + "-");
                }

                summaryWriter.newLine();
                summaryWriter.newLine();

                for (int i = 1; i <= MAX_CYCLES; i++) {
                    ReconFigures reconFigures = reconObject.getReconFigures(i);
                    summaryWriter.write("\t");
                    summaryWriter.write(StringUtils.rightPad("CYCLE " + i + "", cycleSpaces, ' '));
                    if (reconFigures.getFileId() != null) {
                        summaryWriter.write(StringUtils.leftPad(reconFigures.getFileId(), 60, ' '));
                    } else {
                        summaryWriter.write(StringUtils.leftPad(" ", 60, ' '));
                    }
                    summaryWriter.write(
                            StringUtils.leftPad(formatDecimal(reconFigures.sumNetTotalAmounts()), 30, ' '));
                    summaryWriter.newLine();
                }
                summaryWriter.newLine();

                summaryWriter.write("\t");
                summaryWriter.write("TOTAL");
                summaryWriter.write(
                        StringUtils.leftPad(formatDecimal(reconObject.getTotalNetAmount()), 92, ' '));
                summaryWriter.newLine();
                summaryWriter.newLine();
            }
            summaryWriter.newLine();
            summaryWriter.newLine();
            summaryWriter.newLine();

            summaryWriter.write("=====================================\n");
            summaryWriter.write("   CLEARING TOTALS BY END OF CYCLE   \n");
            summaryWriter.write("=====================================\n");
            summaryWriter.newLine();

            summaryWriter.write("CYCLE");
            summaryWriter.write(StringUtils.rightPad(" ", 12, ' '));
            summaryWriter.write("NET TRANSACTION");
            summaryWriter.write(StringUtils.leftPad(" ", 16, ' '));
            summaryWriter.write("NET FEE");
            summaryWriter.write(StringUtils.leftPad(" ", 18, ' '));
            summaryWriter.write("NET TOTAL");
            summaryWriter.newLine();
            summaryWriter.newLine();

            for (String r : currencyReconMap.keySet()) {
                summaryWriter.write("CURRENCY : " + r);
                summaryWriter.newLine();
                summaryWriter.newLine();
                for (int i = 1; i <= MAX_CYCLES; i++) {
                    ReconObject reconObject = currencyReconMap.get(r);
                    Set<ReconFigures> reconFiguresList = reconObject.getReconFiguresTillCycle(i);
                    BigDecimal tmpTxnAmount = BigDecimal.ZERO;
                    BigDecimal tmpFeeAmount = BigDecimal.ZERO;
                    BigDecimal tmpTotalAmount = BigDecimal.ZERO;

                    for (ReconFigures reconFigures : reconFiguresList) {
                        tmpTxnAmount = tmpTxnAmount.add(reconFigures.sumNetTransactionAmounts());
                        tmpFeeAmount = tmpFeeAmount.add(reconFigures.sumNetFeeAmounts());
                        tmpTotalAmount = tmpTotalAmount.add(reconFigures.sumNetTotalAmounts());
                    }
                    summaryWriter.write(StringUtils.rightPad(i + "", cycleSpaces, ' '));
                    summaryWriter.write(
                            StringUtils.leftPad(formatDecimal(tmpTxnAmount), netAmountSpaces, ' '));
                    summaryWriter.write(
                            StringUtils.leftPad(formatDecimal(tmpFeeAmount), netAmountSpaces, ' '));
                    summaryWriter.write(
                            StringUtils.leftPad(formatDecimal(tmpTotalAmount), netAmountSpaces,
                                    ' '));
                    summaryWriter.newLine();
                }
            }

            //2. calculate values for whole day
            summaryWriter.newLine();
            summaryWriter.newLine();
            summaryWriter.newLine();
            summaryWriter.newLine();
            summaryWriter.write("===========================\n");
            summaryWriter.write("    CURRENCY WISE RECON    \n");
            summaryWriter.write("===========================\n");
            summaryWriter.newLine();

            summaryWriter.write(StringUtils.leftPad(" ", 5, ' '));
            summaryWriter.write("CURRENCY");
            summaryWriter.write(StringUtils.leftPad(" ", currencySpaces, ' '));
            summaryWriter.write("NET TRANSACTION");
            summaryWriter.write(StringUtils.leftPad(" ", 21, ' '));
            summaryWriter.write("NET FEE");
            summaryWriter.write(StringUtils.leftPad(" ", 19, ' '));
            summaryWriter.write("NET TOTAL");
            summaryWriter.write(StringUtils.leftPad(" ", 15, ' '));
            summaryWriter.newLine();
            summaryWriter.newLine();

            for (String r : currencyReconMap.keySet()) {
                ReconObject reconObject = currencyReconMap.get(r);
                summaryWriter.write(StringUtils.leftPad(r + "", currencySpaces, ' '));
                summaryWriter.write(
                        StringUtils.leftPad(formatDecimal(reconObject.getTotalReconAmount()), netAmountSpaces, ' '));
                summaryWriter.write(
                        StringUtils.leftPad(formatDecimal(reconObject.getTotalFeeAmount()), netAmountSpaces, ' '));
                summaryWriter.write(
                        StringUtils.leftPad(formatDecimal(reconObject.getTotalNetAmount()), netAmountSpaces, ' '));
                summaryWriter.newLine();
            }
        }
    }


    public static BufferedWriter initializeWriter(String fileIn, String fileOut, File file) throws IOException {
        String dir = StringUtils.mid(file.getAbsolutePath(), fileIn.length(),
                file.getAbsolutePath().length() - file.getName().length() - fileIn.length());
        File theDir = new File(fileOut + ID + dir);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        return new BufferedWriter(new FileWriter(fileOut + ID + dir + file.getName() + ".txt"));
    }

    private static void processMsgs(String key, T112Parser parser) {
        System.out.println(new Date() + " - Starting processing messages..." + key);
        if (key != null) {
            var list = parser.getISOMessages();
            //1. calculate values for each cycle 1-6
            for (ISOMsg m : list) {
                try {
                    //header
                    if (m.getMTI().equals("1644") && m.getString(24).equals("697")) {
                        process1644Header(m, parser);
                    }
                    //presentment
                    else if (m.getMTI().equals("1240")) {
                        process1240(m, parser);
                    }
                    //recon
                    else if (m.getMTI().equals("1644") && m.getString(24).equals("685")) {
                        process1644Reconciliation(m, parser);
                    }
                    //fees
                    else if (m.getMTI().equals("1740")) {
                        process1740(m, parser);
                    } else {
                        //System.out.println("Unknown message: " + m);
                    }
                } catch (ISOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println(new Date() + " - Stopping processing messages..." + key);
    }

    private static void earnedByIssuer() {

    }

    private static void process1740(ISOMsg m, T112Parser parser) {
        int cycle = parser.getCycleNumber();
        PDSParser p = parser.getParser(getKey(m) + "_DE48");
        String mti = m.getString(0);
        String de3 = m.getString(3);
        String de24 = m.getString(24);
        String transactionKey;
        String de5 = m.getString(5);
        String de50 = m.getString(50);
        String ind = "ORIG";
        String bstype = "";
        String bsid = "";
        ReconObject reconObject;
        ReconFigures reconFigures;

        if (p.hasElement("0158")) {
            String pds0158 = p.getElementById("0158").getValue();
            bstype = pds0158.substring(3, 4);
            bsid = pds0158.substring(4, 10);
        }

        transactionKey = bstype + "." + bsid + "." + mti + "." + de24 + "." + de3.substring(0, 2) + "." + ind;

        if (transactionReconMap.containsKey(transactionKey)) {
            reconObject = transactionReconMap.get(transactionKey);
        } else {
            reconObject = new ReconObject(mti, de24, de3.substring(0, 2), ind, "  ", bstype, bsid);
            transactionReconMap.put(transactionKey, reconObject);
        }

        reconObject.setReconCurrency(de50);
        reconFigures = reconObject.getReconFigures(cycle);
        reconFigures.setFileId(parser.getFileId());

        BigDecimal amount = convertAmountToDecimal(de5, de50);
        amount = amount.negate(new MathContext(0));
        reconFigures.getReconAmounts().add(amount);
    }

    private static void process1644Header(ISOMsg m, T112Parser parser) {
        PDSParser p = parser.getParser(getKey(m) + "_DE48");

        if (p.hasElement("0105")) {
            String pds105 = p.getElementById("0105").getValue();
            parser.setFileId(pds105);
            int cycle = Integer.parseInt(pds105.substring(22, 23));
            parser.setCycleNumber(cycle);
        }
    }

    private static void process1240(ISOMsg m, T112Parser parser) {
        int cycle = parser.getCycleNumber();
        PDSParser p = parser.getParser(getKey(m) + "_DE48");

        String mti = m.getString(0);
        String de3 = m.getString(3);
        String tranCode = de3.substring(0, 2);
        String de24 = m.getString(24);
        String transactionKey;
        String de6 = m.getString(6);
        String de51 = m.getString(51);
        String ind = "ORIG";
        String ird = "";
        String bstype = "";
        String bsid = "";

        boolean isReversal = false;
        boolean isRefund = false;
        boolean isATMCash = false;

        //check for reversal or original
        if (p.hasElement("0025")) {
            String pds0025 = p.getElementById("0025").getValue();
            if (pds0025.charAt(0) == 'R') {
                isReversal = true;
            }
        }
        if (p.hasElement("0158")) {
            String pds0158 = p.getElementById("0158").getValue();
            bstype = pds0158.substring(3, 4);
            bsid = pds0158.substring(4, 10);
            ird = pds0158.substring(10, 12);
        }

        if (de3.startsWith("20")) {
            isRefund = true;
        }
        if (de3.startsWith("01")) {
            isATMCash = true;
        }

        if (isReversal) {
            ind = "RVSL";
        }
        transactionKey = bstype + "." + bsid + "." + mti + "." + de24 + "." + tranCode + "." + ind + "." + ird;

        ReconObject reconObject;
        ReconFigures reconFigures;
        if (transactionReconMap.containsKey(transactionKey)) {
            reconObject = transactionReconMap.get(transactionKey);
        } else {
            reconObject = new ReconObject(mti, de24, tranCode, ind, ird, bstype, bsid);
            transactionReconMap.put(transactionKey, reconObject);
        }
        reconObject.setReconCurrency(de51);
        reconFigures = reconObject.getReconFigures(cycle);
        reconFigures.setFileId(parser.getFileId());

        BigDecimal billingAmount = convertAmountToDecimal(de6, de51);

        MathContext mc = new MathContext(0);

        //fee amount
        BigDecimal feeAmount = BigDecimal.ZERO;
        if (p.hasElement("0146")) {
            String pds0146 = p.getElementById("0146").getValue();

            String feeCurr = pds0146.substring(21, 24);
            reconObject.setFeeCurrency(feeCurr);
            String feeAmount1 = pds0146.substring(24, 36);

            feeAmount = convertAmountToDecimal(feeAmount1, feeCurr);
        }

        //recon amount
        BigDecimal reconAmount;
        if (!isATMCash) {
            reconAmount = billingAmount.subtract(feeAmount);
        } else {
            reconAmount = billingAmount.add(feeAmount);
        }

        if (!isReversal && !isRefund) {
            reconAmount = reconAmount.negate(mc);
        }

        //interchange
        if (isReversal || isRefund || isATMCash) {
            feeAmount = feeAmount.negate(mc);
        }

        reconFigures.getTransactionAmounts().add(billingAmount);
        reconFigures.getFeeAmounts().add(feeAmount);
        reconFigures.getReconAmounts().add(reconAmount);
    }

    private static void process1644Reconciliation(ISOMsg m, T112Parser parser) {
        //get interchange amount from PDS395
        PDSParser p = parser.getParser(getKey(m) + "_DE48");
        if (!p.hasElement("0394") || !p.hasElement("0395") || !p.hasElement("0396")) {
            System.out.println("Important PDS not present");
            return;
        }

        String currency = m.getString(50);
        ReconObject reconObject;
        ReconFigures reconFigures;

        int cycle = parser.getCycleNumber();
        if (currencyReconMap.containsKey(currency)) {
            reconObject = currencyReconMap.get(currency);
        } else {
            reconObject = new ReconObject();
            currencyReconMap.put(currency, reconObject);
        }
        reconFigures = reconObject.getReconFigures(cycle);
        reconFigures.setFileId(parser.getFileId());

        String pds394 = p.getElementById("0394").getValue();
        String pds395 = p.getElementById("0395").getValue();
        String pds396 = p.getElementById("0396").getValue();

        MathContext mc = new MathContext(0);
        BigDecimal transactionAmount = convertAmountToDecimal(pds394.substring(1), currency);
        if (!pds394.substring(0, 1).startsWith("C")) {
            transactionAmount = transactionAmount.negate(mc);
        }
        reconFigures.getNetTransactionAmounts().add(transactionAmount);

        BigDecimal feeAmount = convertAmountToDecimal(pds395.substring(1), currency);
        if (!pds395.substring(0, 1).startsWith("C")) {
            feeAmount = feeAmount.negate(mc);
        }
        reconFigures.getNetFeeAmounts().add(feeAmount);

        BigDecimal totalAmount = convertAmountToDecimal(pds396.substring(1), currency);
        if (!pds396.substring(0, 1).startsWith("C")) {
            totalAmount = totalAmount.negate(mc);
        }
        reconFigures.getNetTotalAmounts().add(totalAmount);
    }

    static class ReconObject {

        String id;
        String mti;
        String func;
        String tranCode;
        String indicator;
        String ird;
        String bstype;
        String bsid;
        String reconCurrency;
        String feeCurrency;

        Map<Integer, ReconFigures> reconFigures = new HashMap<>();

        public ReconObject() {
            this.id = UUID.randomUUID().toString();
        }

        public ReconObject(String mti, String func, String tc, String ind, String ird, String bstype, String bsid) {
            this();
            this.mti = mti;
            this.func = func;
            this.tranCode = tc;
            this.indicator = ind;
            this.ird = ird;
            this.bstype = bstype;
            this.bsid = bsid;
        }

        public String getMti() {
            return mti;
        }

        public String getFunc() {
            return func;
        }

        public String getTranCode() {
            return tranCode;
        }

        public String getIndicator() {
            return indicator;
        }

        public String getIrd() {
            return ird;
        }

        public String getBstype() {
            return bstype;
        }

        public String getBsid() {
            return bsid;
        }

        public void setReconCurrency(String de51) {
            this.reconCurrency = de51;
        }

        public void setFeeCurrency(String feeCurr) {
            this.feeCurrency = feeCurr;
        }

        public String getReconCurrency() {
            return reconCurrency;
        }

        public String getFeeCurrency() {
            return feeCurrency;
        }

        public ReconFigures getReconFigures(int cycle) {
            reconFigures.computeIfAbsent(cycle, x -> new ReconFigures(cycle));
            return reconFigures.get(cycle);
        }

        public Set<ReconFigures> getReconFiguresTillCycle(int cycle) {
            Set<ReconFigures> figures = new LinkedHashSet<>();
            for (int i = 1; i <= cycle; i++) {
                figures.add(reconFigures.get(i));
            }
            return figures;
        }

        public BigDecimal getTotalReconAmount() {
            BigDecimal d = BigDecimal.ZERO;
            for (int i = 1; i <= MAX_CYCLES; i++) {
                if (reconFigures.containsKey(i)) {
                    d = d.add(reconFigures.get(i).sumReconAmounts());
                }
            }
            return d;
        }

        public BigDecimal getTotalFeeAmount() {
            BigDecimal d = BigDecimal.ZERO;
            for (int i = 1; i <= MAX_CYCLES; i++) {
                if (reconFigures.containsKey(i)) {
                    d = d.add(reconFigures.get(i).sumFeeAmounts());
                }
            }
            return d;
        }

        public BigDecimal getTotalNetAmount() {
            BigDecimal d = BigDecimal.ZERO;
            for (int i = 1; i <= MAX_CYCLES; i++) {
                if (reconFigures.containsKey(i)) {
                    d = d.add(reconFigures.get(i).sumNetTotalAmounts());
                }
            }
            return d;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ReconObject that = (ReconObject) o;

            return new EqualsBuilder().append(id, that.id).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(id).toHashCode();
        }

    }

    static class ReconFigures {

        String id;
        int cycleNumber;
        String fileId;

        //used by 1240 messages
        List<BigDecimal> txnAmountList;
        List<BigDecimal> reconAmountList;
        List<BigDecimal> feeAmountList;

        //used by 1644 recon messages
        List<BigDecimal> netTransactionAmountList;
        List<BigDecimal> netFeeAmountList;
        List<BigDecimal> netTotalAmountList;

        public ReconFigures(int cycleNumber) {
            this.id = UUID.randomUUID().toString();
            this.cycleNumber = cycleNumber;
            this.txnAmountList = new ArrayList<>();
            this.reconAmountList = new ArrayList<>();
            this.feeAmountList = new ArrayList<>();

            this.netTransactionAmountList = new ArrayList<>();
            this.netFeeAmountList = new ArrayList<>();
            this.netTotalAmountList = new ArrayList<>();
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public int getCycleNumber() {
            return cycleNumber;
        }

        public List<BigDecimal> getTransactionAmounts() {
            return txnAmountList;
        }

        public List<BigDecimal> getReconAmounts() {
            return reconAmountList;
        }

        public List<BigDecimal> getFeeAmounts() {
            return feeAmountList;
        }

        public List<BigDecimal> getNetTransactionAmounts() {
            return netTransactionAmountList;
        }

        public List<BigDecimal> getNetFeeAmounts() {
            return netFeeAmountList;
        }

        public List<BigDecimal> getNetTotalAmounts() {
            return netTotalAmountList;
        }

        public BigDecimal sumReconAmounts() {
            BigDecimal d = BigDecimal.ZERO;
            if (reconAmountList == null) {
                return d;
            }
            for (BigDecimal bigDecimal : reconAmountList) {
                d = d.add(bigDecimal);
            }
            return d;
        }

        public BigDecimal sumFeeAmounts() {
            BigDecimal d = BigDecimal.ZERO;
            if (feeAmountList == null) {
                return d;
            }
            for (BigDecimal e : feeAmountList) {
                d = d.add(e);
            }
            return d;
        }

        public BigDecimal sumNetTransactionAmounts() {
            BigDecimal d = BigDecimal.ZERO;
            if (netTransactionAmountList == null) {
                return d;
            }
            for (BigDecimal e : netTransactionAmountList) {
                d = d.add(e);
            }
            return d;
        }

        public BigDecimal sumNetFeeAmounts() {
            BigDecimal d = BigDecimal.ZERO;
            if (netFeeAmountList == null) {
                return d;
            }
            for (BigDecimal e : netFeeAmountList) {
                d = d.add(e);
            }
            return d;
        }

        public BigDecimal sumNetTotalAmounts() {
            BigDecimal d = BigDecimal.ZERO;
            if (netTotalAmountList == null) {
                return d;
            }
            for (BigDecimal e : netTotalAmountList) {
                d = d.add(e);
            }
            return d;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ReconFigures that = (ReconFigures) o;

            return new EqualsBuilder().append(id, that.id).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(id).toHashCode();
        }
    }
}
