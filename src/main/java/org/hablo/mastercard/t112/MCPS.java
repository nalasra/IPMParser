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
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hablo.helper.CurrencyHelper;
import org.hablo.helper.FilenameComparator;
import org.hablo.mastercard.util.PDSParser;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class MCPS {

    static Integer MAX_CYCLES = 7;
    static String ID = "/" + System.currentTimeMillis() + "/";
    static String CASH_WITHDRAWAL = "01";
    static String REFUND = "20";
    static String filePath = System.getProperty("user.dir") + "/src/dist/files/mastercard/t112/in/";
    static String filePathO = System.getProperty("user.dir") + "/src/dist/files/mastercard/t112/out/";


    static List<ReconObject> messageReconMap = Collections.synchronizedList(new ArrayList<>());
    static List<ReconObject> financialAddendumMap = Collections.synchronizedList(new ArrayList<>());

    //used for accessing objects by DE71 as a key
    static Map<String, ReconObject> presentmentsMap = Collections.synchronizedMap(new TreeMap<>());

    //used for storing summary results
    static List<ReconSummary> reconSummaryPerBs = Collections.synchronizedList(new ArrayList<>());

    static Map<String, String> mtiFuncMap = new HashMap<>();
    static Map<String, String> businessServiceLevelMap = new HashMap<>();
    static Map<String, String> transactionCodeMap = new HashMap<>();

    static {
        mtiFuncMap.put("1240.200", "FIRST PRES.");
        mtiFuncMap.put("1240.205", "SEC. PRES.-F");
        mtiFuncMap.put("1240.282", "SEC. PRES.-P");

        transactionCodeMap.put("00", "PURCHASE");
        transactionCodeMap.put("01", "ATM CASH");
        transactionCodeMap.put("18", "UNIQUE TXN");
        transactionCodeMap.put("19", "FEE COL CR");
        transactionCodeMap.put("20", "CREDIT");

        businessServiceLevelMap.put("1", "INTERREGIONAL");
        businessServiceLevelMap.put("2", "INTRAREGIONAL");
        businessServiceLevelMap.put("3", "INTERCOUNTRY");
        businessServiceLevelMap.put("4", "INTRACOUNTRY");
        businessServiceLevelMap.put("8", "MBR-TO-MBR AGREEMENT");
        businessServiceLevelMap.put(" ", "CUSTOM");
    }

    public static void main(String[] args) {
        try {
            //File[] fs = getFiles("", filePath);
            //File[] fs = getFiles("mox", filePath);
            File[] fs = getFiles("analysis/MCI.AR.T112.M.E0070571.D240727.T191530.A001", filePath);
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
                    processMessages(FilenameUtils.getExtension(f.getName()), parser);
                    Thread.sleep(1000);
                }

                Thread.sleep(1000);

                System.out.printf("Generate presentment report...\n");
                generateT140Report();

                System.out.printf("Generate summary report...");
                //generateSummaryReport();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static Map<String, Map<String, Map<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>>>> getPresentmentsGroupedByBusinessServiceLevels(
            int cycle) {
        Map<String, Map<String, Map<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>>>> data;
        data = messageReconMap.stream()
                .filter(x -> x.getCycle() == cycle)
                .collect(
                        Collectors.groupingBy(ReconObject::getFileId,
                                Collectors.groupingBy(ReconObject::getBrand,
                                        Collectors.groupingBy(ReconObject::getBsLevel,
                                                Collectors.groupingBy(ReconObject::getBsId, TreeMap::new,
                                                        Collectors.groupingBy(ReconObject::getReconCurrency,
                                                                Collectors.groupingBy(x -> x.mti + "." + x.func,
                                                                        Collectors.groupingBy(ReconObject::getTranCode,
                                                                                Collectors.groupingBy(
                                                                                        ReconObject::getIndicator,
                                                                                        Collectors.groupingBy(
                                                                                                ReconObject::getIrd
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )));
        return data;
    }

    private static Map<String, Map<String, Map<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>>>> getSummaryByBusinessServiceId(
            int cycleNumber) {

        Map<String, Map<String, Map<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>>>> data;
        data = reconSummaryPerBs.stream().filter(x -> x.getCycle() == cycleNumber).collect(
                Collectors.groupingBy(ReconSummary::getFileId,
                        Collectors.groupingBy(ReconSummary::getBrand,
                                Collectors.groupingBy(ReconSummary::getBsLevel,
                                        Collectors.groupingBy(ReconSummary::getCurrency,
                                                Collectors.groupingBy(ReconSummary::getBsId, TreeMap::new,
                                                        Collectors.groupingBy(ReconSummary::getIndicator
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
        return data;
    }

    private static Map<String, Map<String, Map<String, Map<String, Long>>>> getAddendumCountGroupedByBusinessServiceId(
            int cycleNumber) {

        Map<String, Map<String, Map<String, Map<String, Long>>>> data;
        data = financialAddendumMap.stream().filter(x -> x.getCycle() == cycleNumber).collect(
                Collectors.groupingBy(ReconObject::getFileId,
                        Collectors.groupingBy(ReconObject::getBrand,
                                Collectors.groupingBy(ReconObject::getBsLevel,
                                        Collectors.groupingBy(ReconObject::getBsId, TreeMap::new,
                                                Collectors.counting()
                                        )
                                )
                        )
                )
        );
        return data;
    }

    private static String getMemberId(String pds0105) {
        return pds0105.substring(9, 20);
    }

    private static String formatFileId(String pds0105) {
        //001/231013/00000005891/01101
        return pds0105.substring(0, 3) + "/" + pds0105.substring(3, 9) + "/" + pds0105.substring(9, 20) + "/"
                + pds0105.substring(20);
    }

    private static void generateT140Report() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePathO + ID + "t140_report.txt"))) {
            for (int cycleNumber = 1; cycleNumber <= MAX_CYCLES; cycleNumber++) {
                writer.newLine();
                writer.newLine();
                writer.newLine();
                writer.write("CYCLE: " + cycleNumber);

                Map<String, Map<String, Map<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>>>> presentments = getPresentmentsGroupedByBusinessServiceLevels(
                        cycleNumber);

                //fileId, brand, bsLevel, bsId, currency, mti+func, tranCode, indicator
                // Iterate through the first level
                for (Entry<String, Map<String, Map<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>>>> level1Entry : presentments.entrySet()) {
                    String fileIdKey = level1Entry.getKey();
                    Map<String, Map<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>>> level1Value = level1Entry.getValue();

                    // Iterate through the second level
                    for (Entry<String, Map<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>>> level2Entry : level1Value.entrySet()) {
                        String brandKey = level2Entry.getKey();
                        Map<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>> level2Value = level2Entry.getValue();

                        // Iterate through the third level
                        for (Entry<String, Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>>> level3Entry : level2Value.entrySet()) {
                            String bsLevelKey = level3Entry.getKey();
                            Map<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>> level3Value = level3Entry.getValue();

                            // Iterate through the fourth level
                            for (Entry<String, Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>>> level4Entry : level3Value.entrySet()) {
                                String bsIdKey = level4Entry.getKey();

                                Map<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>> level4Value = level4Entry.getValue();

                                printHeader(writer,
                                        new String[]{brandKey, bsLevelKey, bsIdKey, fileIdKey, fileIdKey});

                                // Iterate through the fifth level
                                for (Entry<String, Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>>> level5Entry : level4Value.entrySet()) {
                                    String currencyKey = level5Entry.getKey();

                                    int perBSIDCount = 0;
                                    BigDecimal perBSIdReconAmountSum = BigDecimal.ZERO;
                                    BigDecimal perBSIDFeeAmountSum = BigDecimal.ZERO;

                                    Map<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>> level5Value = level5Entry.getValue();

                                    writer.newLine();
                                    writer.newLine();
                                    writer.write(
                                            "MASTERCARD SETTLED                                                      RECON                         FEE");
                                    writer.newLine();
                                    writer.write(
                                            "                                                                         CURR                         CURR");
                                    writer.newLine();
                                    writer.write(
                                            " TRANS. FUNC. PROC.CODE       IRD   COUNTS               RECON AMOUNT    CODE          TRANS FEE      CODE");
                                    writer.newLine();
                                    writer.write(
                                            " ------------ --------------- --- -------- -------------------------- ------- ----------------------- -------");
                                    writer.newLine();

                                    boolean moreMtiFuncToProcess = false;

                                    // Iterate through the sixth level
                                    for (Entry<Object, Map<String, Map<String, Map<String, List<ReconObject>>>>> level6Entry : level5Value.entrySet()) {
                                        Object mtiFuncKey = level6Entry.getKey();
                                        Map<String, Map<String, Map<String, List<ReconObject>>>> level6Value = level6Entry.getValue();

                                        int perMtiFuncCount = 0;
                                        BigDecimal perMtiFuncReconAmountSum = BigDecimal.ZERO;
                                        BigDecimal perMtiFuncFeeAmountSum = BigDecimal.ZERO;

                                        if (moreMtiFuncToProcess) {
                                            writer.newLine();
                                            writer.newLine();
                                        }

                                        writer.write(" ");
                                        writer.write(
                                                StringUtils.rightPad(getMsgDescription((String) mtiFuncKey, ""), 12,
                                                        ' '));

                                        boolean moreTranCodeToProcess = false;
                                        // Iterate through the seventh level
                                        for (Entry<String, Map<String, Map<String, List<ReconObject>>>> level7Entry : level6Value.entrySet()) {
                                            String tranCodeKey = level7Entry.getKey();
                                            Map<String, Map<String, List<ReconObject>>> level7Value = level7Entry.getValue();

                                            // Iterate through the eighth level
                                            for (Map.Entry<String, Map<String, List<ReconObject>>> level8Entry : level7Value.entrySet()) {
                                                String indicatorKey = level8Entry.getKey();
                                                Map<String, List<ReconObject>> level8Value = level8Entry.getValue();

                                                BigDecimal perIndicatorReconAmountSum = BigDecimal.ZERO;
                                                BigDecimal perIndicatorFeeAmountSum = BigDecimal.ZERO;

                                                // Iterate through the ninth level
                                                for (Map.Entry<String, List<ReconObject>> level9Entry : level8Value.entrySet()) {
                                                    String irdKey = level9Entry.getKey();
                                                    List<ReconObject> reconObjects = level9Entry.getValue();

                                                    int tmpCount = 0;
                                                    BigDecimal tmpSumReconAmount = BigDecimal.ZERO;
                                                    BigDecimal tmpSumFeeAmount = BigDecimal.ZERO;

                                                    // Process the List of ReconObject
                                                    for (ReconObject reconObject : reconObjects) {
                                                        perMtiFuncCount++;

                                                        // Perform operations on reconObject
                                                        ReconFigures currentReconFigures = reconObject.getReconFigures(
                                                                cycleNumber);
                                                        if (currentReconFigures.getCycleNumber() != cycleNumber) {
                                                            System.out.println("Cycle doesn't match!");
                                                        } else if (
                                                                currentReconFigures.getTransactionAmounts().isEmpty() &&
                                                                        currentReconFigures.getFeeAmounts().isEmpty() &&
                                                                        currentReconFigures.getReconAmounts().isEmpty()
                                                        ) {
                                                            //skip printing
                                                            System.out.printf(
                                                                    "Empty figures for BRAND[%s] BSLVL[%s] BSID[%s] FILEID[%s] CYCLE[%s]%n",
                                                                    reconObject.getBrand(), reconObject.getBsLevel(),
                                                                    reconObject.getBsId(),
                                                                    reconObject.getFileId(), cycleNumber);
                                                        } else {
                                                            tmpCount += currentReconFigures.getReconAmounts()
                                                                    .size();
                                                            perBSIDCount += currentReconFigures.getReconAmounts()
                                                                    .size();

                                                            perBSIdReconAmountSum = perBSIdReconAmountSum.add(
                                                                    currentReconFigures.sumReconAmounts());
                                                            tmpSumReconAmount = tmpSumReconAmount.add(
                                                                    currentReconFigures.sumReconAmounts());
                                                            perMtiFuncReconAmountSum = perMtiFuncReconAmountSum.add(
                                                                    currentReconFigures.sumReconAmounts());
                                                            perIndicatorReconAmountSum = perIndicatorReconAmountSum.add(
                                                                    currentReconFigures.sumReconAmounts());

                                                            perBSIDFeeAmountSum = perBSIDFeeAmountSum.add(
                                                                    currentReconFigures.sumFeeAmounts());
                                                            tmpSumFeeAmount = tmpSumFeeAmount.add(
                                                                    currentReconFigures.sumFeeAmounts());
                                                            perMtiFuncFeeAmountSum = perMtiFuncFeeAmountSum.add(
                                                                    currentReconFigures.sumFeeAmounts());
                                                            perIndicatorFeeAmountSum = perIndicatorFeeAmountSum.add(
                                                                    currentReconFigures.sumFeeAmounts());
                                                        }
                                                    }

                                                    if (moreTranCodeToProcess) {
                                                        writer.newLine();
                                                        writer.write(StringUtils.leftPad("", 13, ' '));
                                                    }

                                                    //print recon info
                                                    printReconInfo(writer, (String) mtiFuncKey, tranCodeKey,
                                                            indicatorKey,
                                                            irdKey,
                                                            currencyKey, currencyKey,
                                                            tmpCount, tmpSumReconAmount, tmpSumFeeAmount);

                                                    moreTranCodeToProcess = true;
                                                }
                                                //end of indicator
                                                reconSummaryPerBs.add(
                                                        new ReconSummary(cycleNumber, bsLevelKey, bsIdKey, fileIdKey,
                                                                currencyKey,
                                                                brandKey, indicatorKey, perIndicatorReconAmountSum,
                                                                perIndicatorFeeAmountSum));
                                            }
                                            //end of tranCode
                                        }
                                        //end of mtiFunc
                                        writer.newLine();
                                        writer.write(
                                                " ------------ --------------- --- -------- -------------------------- ------- ----------------------- -------");
                                        writer.newLine();
                                        writer.write(" ");
                                        writer.write(
                                                StringUtils.rightPad(getMsgDescription((String) mtiFuncKey, ""), 12,
                                                        ' '));
                                        writer.write(" TOTAL");
                                        writer.write(StringUtils.leftPad(perMtiFuncCount + "", 23, ' '));
                                        writer.write(
                                                StringUtils.leftPad(formatDecimal(perMtiFuncReconAmountSum),
                                                        27,
                                                        ' '));
                                        writer.write(" ");
                                        writer.write(getFormattedCurrency(currencyKey));
                                        writer.write(
                                                StringUtils.leftPad(formatDecimal(perMtiFuncFeeAmountSum), 24,
                                                        ' '));
                                        writer.write(" ");
                                        writer.write(getFormattedCurrency(currencyKey));

                                        moreMtiFuncToProcess = true;
                                    }
                                    //end of currency
                                    writer.newLine();
                                    writer.newLine();
                                    writer.write(businessServiceLevelMap.get(bsLevelKey));
                                    writer.newLine();
                                    writer.write("MASTERCARD SETTLED");
                                    writer.newLine();
                                    writer.write("BUSINESS SERVICE ID SUBTOTAL  ");
                                    writer.write(
                                            StringUtils.leftPad(String.format("%,d", perBSIDCount) + "", 12, ' '));
                                    writer.write(
                                            StringUtils.leftPad(formatDecimal(perBSIdReconAmountSum),
                                                    27,
                                                    ' '));
                                    writer.write(
                                            StringUtils.leftPad(getFormattedCurrency(currencyKey), 8, ' '));
                                    writer.write(
                                            StringUtils.leftPad(formatDecimal(perBSIDFeeAmountSum), 24,
                                                    ' '));
                                    writer.write(
                                            StringUtils.leftPad(getFormattedCurrency(currencyKey), 8, ' '));
                                    writer.newLine();
                                }
                                //end of bsId
                            }
                            //end of bsLevel
                        }
                    }
                }

                //addendums
                prepareAddendumsInfo(writer, cycleNumber);

                //summary
                prepareSummaryByBusinessServiceIds(writer, cycleNumber);
            }
        }
    }

    private static void prepareSummaryByBusinessServiceIds(BufferedWriter writer, int cycleNumber) throws IOException {
        Map<String, Map<String, Map<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>>>> summary = getSummaryByBusinessServiceId(
                cycleNumber);

        // Iterate through the first level
        for (Entry<String, Map<String, Map<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>>>> level1Entry : summary.entrySet()) {
            String fileIdKey = level1Entry.getKey();
            Map<String, Map<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>>> level1Value = level1Entry.getValue();

            // Iterate through the second level
            for (Entry<String, Map<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>>> level2Entry : level1Value.entrySet()) {
                String brandKey = level2Entry.getKey();
                Map<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>> level2Value = level2Entry.getValue();

                // Iterate through the third level
                for (Entry<String, Map<String, TreeMap<String, Map<String, List<ReconSummary>>>>> level3Entry : level2Value.entrySet()) {
                    String bsLevelKey = level3Entry.getKey();
                    Map<String, TreeMap<String, Map<String, List<ReconSummary>>>> level3Value = level3Entry.getValue();

                    for (Entry<String, TreeMap<String, Map<String, List<ReconSummary>>>> level4Entry : level3Value.entrySet()) {
                        String currencyKey = level4Entry.getKey();
                        TreeMap<String, Map<String, List<ReconSummary>>> level4Value = level4Entry.getValue();

                        printHeader(writer,
                                new String[]{brandKey, bsLevelKey, null, null, fileIdKey, currencyKey});
                        writer.newLine();
                        writer.write(
                                " BUSINESS                                                                                              ");
                        writer.newLine();
                        writer.write(
                                " SERVICE                               ORIG/                                                           ");
                        writer.newLine();
                        writer.write(
                                "     ID            FILE ID             RVSL          RECON. AMOUNT                   TRANSACTION FEE   ");
                        writer.newLine();
                        writer.write(
                                " -------- ---------------------------- ----        -------------------              -------------------");

                        BigDecimal grandTotalReconAmount = BigDecimal.ZERO;
                        BigDecimal grandTotalFeeAmount = BigDecimal.ZERO;

                        // Iterate through the fifth level
                        for (Entry<String, Map<String, List<ReconSummary>>> level5Entry : level4Value.entrySet()) {
                            String bsIdKey = level5Entry.getKey();
                            Map<String, List<ReconSummary>> level5Value = level5Entry.getValue();

                            for (Entry<String, List<ReconSummary>> level6Entry : level5Value.entrySet()) {
                                String indicatorKey = level6Entry.getKey();
                                List<ReconSummary> reconSummaries = level6Entry.getValue();

                                BigDecimal tmpReconAmount = BigDecimal.ZERO;
                                BigDecimal tmpFeeAmount = BigDecimal.ZERO;

                                // Process each ReconSummary in the list
                                for (ReconSummary reconSummary : reconSummaries) {
                                    tmpReconAmount = tmpReconAmount.add(reconSummary.getReconAmount());
                                    tmpFeeAmount = tmpFeeAmount.add(reconSummary.getTransactionFee());
                                }

                                writer.newLine();
                                writer.write(" ");
                                writer.write(bsIdKey);
                                writer.write("   ");
                                writer.write(formatFileId(fileIdKey));
                                writer.write(" ");
                                writer.write(indicatorKey);
                                writer.write(" ");
                                writer.write(
                                        StringUtils.leftPad(formatDecimal(tmpReconAmount), 26,
                                                ' '));
                                writer.write(" ");
                                writer.write(
                                        StringUtils.leftPad(formatDecimal(tmpFeeAmount), 30,
                                                ' '));

                                grandTotalReconAmount = grandTotalReconAmount.add(tmpReconAmount);
                                grandTotalFeeAmount = grandTotalFeeAmount.add(tmpFeeAmount);
                                //end of currency
                            }
                        }
                        writer.newLine();
                        writer.newLine();

                        writer.write("                             GRAND TOTAL");
                        writer.write(StringUtils.leftPad(formatDecimal(grandTotalReconAmount), 30,
                                ' '));
                        writer.write(StringUtils.leftPad(formatDecimal(grandTotalFeeAmount), 31,
                                ' '));
                        //end of bsId
                    }
                }
            }
        }
    }


    private static void prepareAddendumsInfo(BufferedWriter writer, int cycleNumber) throws IOException {
        Map<String, Map<String, Map<String, Map<String, Long>>>> addendums = getAddendumCountGroupedByBusinessServiceId(
                cycleNumber);

        for (Map.Entry<String, Map<String, Map<String, Map<String, Long>>>> level1Entry : addendums.entrySet()) {
            String fileId = level1Entry.getKey();
            Map<String, Map<String, Map<String, Long>>> level1Value = level1Entry.getValue();

            // Iterate through the second level
            for (Entry<String, Map<String, Map<String, Long>>> level2Entry : level1Value.entrySet()) {
                String brand = level2Entry.getKey();
                Map<String, Map<String, Long>> level2Value = level2Entry.getValue();

                // Iterate through the third level
                for (Entry<String, Map<String, Long>> level3Entry : level2Value.entrySet()) {
                    String bsLevel = level3Entry.getKey();
                    Map<String, Long> level3Value = level3Entry.getValue();

                    // Iterate through the fourth level
                    for (Entry<String, Long> level4Entry : level3Value.entrySet()) {
                        String bsId = level4Entry.getKey();
                        Long level4Value = level4Entry.getValue();

                        printHeader(writer, new String[]{brand, bsLevel, bsId, fileId, fileId});
                        writer.newLine();
                        writer.write(" NON-FINANCIAL DETAIL                              COUNTS");
                        writer.newLine();
                        writer.write(" ORIGINALS:");
                        writer.newLine();
                        writer.write("               FINANCIAL DETAIL ADDENDUM          " + StringUtils.leftPad(
                                (2 * level4Value) + "", 7, '0'));
                    }
                }
            }
        }
    }

    //key[] = brand, bslevel, bsId, currency, fileId
    private static void printHeader(BufferedWriter writer, String[] key) throws IOException {
        writer.newLine();
        if (key.length >= 1) {
            writer.newLine();
            writer.write("ACCEPTANCE BRAND: " + key[0]);
        }
        if (key.length >= 2) {
            writer.newLine();
            writer.write("BUSINESS SERVICE LEVEL: " + businessServiceLevelMap.get(key[1]));
        }
        if (key.length >= 3 && key[2] != null && !key[2].isEmpty()) {
            writer.newLine();
            writer.write("BUSINESS SERVICE ID: " + key[2]);
        }
        if (key.length >= 4 && key[3] != null && !key[3].isEmpty()) {
            writer.newLine();
            writer.write("FILE ID: " + formatFileId(key[3]));
        }
        if (key.length >= 5 && key[4] != null && !key[4].isEmpty()) {
            writer.newLine();
            writer.write("MEMBER ID: " + getMemberId(key[4]));
        }
        if (key.length >= 6 && key[5] != null && !key[5].isEmpty()) {
            writer.newLine();
            writer.write("CURRENCY CODE : " + getFormattedCurrency(key[5]));
        }
        writer.newLine();
    }

    private static String getMsgDescription(String mti, String funcCode) {
        if (mtiFuncMap.containsKey(mti + "." + funcCode)) {
            return mtiFuncMap.get(mti + "." + funcCode);
        }
        return mtiFuncMap.get(mti);
    }

    private static String getTxnDescription(String tranCode, String funcCode) {
        if (transactionCodeMap.containsKey(tranCode + "." + funcCode)) {
            return transactionCodeMap.get(tranCode + "." + funcCode);
        }
        return transactionCodeMap.get(tranCode);
    }

    private static void printReconInfo(BufferedWriter writer, String mtiFunc, String tranCode, String indicator,
            String ird, String reconCurr, String feeCurr,
            int count, BigDecimal sumReconAmount, BigDecimal sumFeeAmount)
            throws IOException {
        try {
            //writer.write(" ");
            //writer.write(StringUtils.rightPad(getMsgDescription(mtiFunc, ""), 12, ' '));
            writer.write(" ");
            writer.write(
                    StringUtils.rightPad(getTxnDescription(tranCode, ""), 11, ' '));
            writer.write(indicator);
            writer.write(" ");
            writer.write(ird);
            writer.write("  ");

            writer.write(
                    StringUtils.leftPad(count + "", 8, ' '));

            //recon amount
            writer.write(
                    StringUtils.leftPad(formatDecimal(sumReconAmount), 27, ' '));

            //recon curr code
            writer.write(" ");
            writer.write(getFormattedCurrency(reconCurr));

            //trans fee
            writer.write(
                    StringUtils.leftPad(formatDecimal(sumFeeAmount), 24, ' '));

            //fee curr code
            writer.write(" ");
            if (feeCurr == null) {
                writer.write("-");
            } else {
                writer.write(getFormattedCurrency(feeCurr));
            }
        } catch (NullPointerException npe) {
            System.err.println(npe.getMessage());
        }
    }

    private static String getFormattedCurrency(String numericCurr) {
        Currency c1 = null;
        if (numericCurr != null && !numericCurr.isEmpty()) {
            c1 = CurrencyHelper.getInstance(numericCurr);
        }
        if (c1 != null) {
            return
                    StringUtils.leftPad(numericCurr + "-" + c1.getCurrencyCode(), 7, ' ');
        } else {
            if (Objects.equals(numericCurr, "157")) {
                return
                        StringUtils.leftPad(numericCurr + "-CNH", 7, ' ');
            }
            return
                    StringUtils.leftPad(numericCurr + "-???", 7, ' ');
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

    private static void processMessages(String key, T112Parser parser) {
        System.out.println(new Date() + " - Starting processing messages..." + key);
        if (key != null) {
            var list = parser.getISOMessages();
            for (ISOMsg m : list) {
                try {
                    //header
                    if (m.getMTI().equals("1644")) {
                        if (m.getString(24).equals("697")) {
                            process1644Header(m, parser);
                        }
                        //recon
                        else if (m.getString(24).equals("685")) {
                            //process1644Reconciliation(m, parser);
                        }
                        //financial addendum
                        else if (m.getString(24).equals("696")) {
                            process1644Addendum(m, parser);
                        } else if (m.getString(24).equals("695")) {
                            process1644Trailer(m, parser);
                        }
                    }
                    //presentment
                    else if (m.getMTI().equals("1240")) {
                        process1240(m, parser);
                    }

                    //fees
                    else if (m.getMTI().equals("1740")) {
                        process1740(m, parser);
                    }
                    //trailer
                    else {
                        //System.out.println("Unknown message: " + m);
                    }
                } catch (ISOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println(new Date() + " - Stopping processing messages..." + key);
    }

    private static void process1740(ISOMsg m, T112Parser parser) {
        int cycle = parser.getCycleNumber();
        PDSParser p = parser.getPDSParser(getKey(m) + "_DE48");
        String mti = m.getString(0);
        String de3 = m.getString(3);
        String de24 = m.getString(24);
        ReconKey reconKey;
        String de5 = m.getString(5);
        String de50 = m.getString(50);
        String ind = "ORIG";
        String bstype = "";
        String bsid = "";
        ReconObject reconObject = null;
        ReconFigures reconFigures;

        if (p.hasElement("0158")) {
            String pds0158 = p.getElementById("0158").getValue();
            bstype = pds0158.substring(3, 4);
            bsid = pds0158.substring(4, 10);
        }

        reconKey = new ReconKey(cycle, null, bstype, bsid, mti, de24, de3.substring(0, 2), ind, null, null,
                parser.getFileId());

        // if (messageReconMap.containsKey(reconKey)) {
        //     reconObject = messageReconMap.get(reconKey);
        // } else {
        reconObject = new ReconObject(mti, de24, de3.substring(0, 2), ind, "  ", "", bstype, bsid, cycle);
        messageReconMap.add(reconObject);
        // }

        reconObject.setReconCurrency(de50);
        reconObject.setFileId(parser.getFileId());
        reconFigures = reconObject.getReconFigures(cycle);

        BigDecimal amount = convertAmountToDecimal(de5, de50);
        amount = amount.negate(new MathContext(0));
        reconFigures.getReconAmounts().add(amount);
    }

    private static void process1644Trailer(ISOMsg m, T112Parser parser) {
        PDSParser p = parser.getPDSParser(getKey(m) + "_DE48");

        if (p.hasElement("0105")) {
            String pds105 = p.getElementById("0105").getValue();
            parser.setFileId(pds105);
            int cycle = Integer.parseInt(pds105.substring(22, 23));
            parser.setCycleNumber(cycle);
        }

        //p0301 File Amount, Checksum

        if (p.hasElement("0306")) {
            //p0306 File Message Counts
        }
    }

    private static void process1644Header(ISOMsg m, T112Parser parser) {
        PDSParser p = parser.getPDSParser(getKey(m) + "_DE48");

        if (p.hasElement("0105")) {
            String pds105 = p.getElementById("0105").getValue();
            parser.setFileId(pds105);
            int cycle = Integer.parseInt(pds105.substring(22, 23));
            parser.setCycleNumber(cycle);
        }
    }

    private static void process1240(ISOMsg m, T112Parser parser) {
        int cycle = parser.getCycleNumber();
        PDSParser p = parser.getPDSParser(getKey(m) + "_DE48");

        String mti = m.getString(0);
        String de3 = m.getString(3);
        String tranCode = de3.substring(0, 2);
        String de24 = m.getString(24);
        ReconKey reconKey;
        String de5 = "", de50 = "";
        if (m.hasField(5)) {
            de5 = m.getString(5);
            de50 = m.getString(50);
        }

        String ind = "ORIG";
        String ird = "";
        String bstype = "";
        String brand = "";
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
            brand = pds0158.substring(0, 3);
            bstype = pds0158.substring(3, 4);
            bsid = pds0158.substring(4, 10);
            ird = pds0158.substring(10, 12);
        }

        if (de3.startsWith(REFUND)) {
            isRefund = true;
        }
        if (de3.startsWith(CASH_WITHDRAWAL)) {
            isATMCash = true;
        }

        if (isReversal) {
            ind = "RVSL";
        }
        reconKey = new ReconKey(cycle, brand, bstype, bsid, mti, de24, tranCode, ind, ird, de50, parser.getFileId());

        ReconObject reconObject;
        ReconFigures reconFigures;
        //if (messageReconMap.containsKey(reconKey)) {
        //    reconObject = messageReconMap.get(reconKey);
        //} else {
        reconObject = new ReconObject(mti, de24, tranCode, ind, ird, brand, bstype, bsid, cycle);
        messageReconMap.add(reconObject);
        // }

        presentmentsMap.put(m.getString(71), reconObject);

        reconObject.setReconCurrency(de50);
        reconObject.setFileId(parser.getFileId());
        reconFigures = reconObject.getReconFigures(cycle);

        BigDecimal de5Decimal = convertAmountToDecimal(de5, de50);

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

        //interchange
        //should not be earned by issuer for reversals, refunds and ATM cash
        if (isReversal || isRefund || isATMCash) {
            feeAmount = feeAmount.negate(mc);
        }

        BigDecimal reconAmount = de5Decimal.add(BigDecimal.ZERO);
        //recon amount
        //should be negative for every transaction except reversal and refunds
        if (!isReversal && !isRefund) {
            reconAmount = reconAmount.negate(mc);
        }
        reconAmount = reconAmount.add(feeAmount);

        reconFigures.getTransactionAmounts().add(de5Decimal);
        reconFigures.getFeeAmounts().add(feeAmount);
        reconFigures.getReconAmounts().add(reconAmount);
    }

    private static void process1644Addendum(ISOMsg m, T112Parser parser) {
        PDSParser p = parser.getPDSParser(getKey(m) + "_DE48");

        int cycle = parser.getCycleNumber();
        //String transactionKey = m.getString(71);

        ReconObject reconObject;

        //find matching presentment
        if (!p.hasElement("0501")) {
            System.out.println("Important PDS0501 not present in financial addendum");
            return;
        }
        String pds501 = p.getElementById("0501").getValue();

        //Addendum.PDS0501.SF4 = Presentment.DE71
        String msgNumber = pds501.substring(8);
        ReconObject matchingPresentment = findAssociatedPresentment(msgNumber);

        reconObject = new ReconObject(m.getString(0), m.getString(24), "", "", matchingPresentment.getIrd(),
                matchingPresentment.getBrand(), matchingPresentment.getBsLevel(),
                matchingPresentment.getBsId(), cycle);
        reconObject.setFileId(parser.getFileId());
        financialAddendumMap.add(reconObject);
    }

    private static ReconObject findAssociatedPresentment(String msgNumber) {
        return presentmentsMap.get(msgNumber);
    }

//    private static void process1644Reconciliation(ISOMsg m, T112Parser parser) {
//        //get interchange amount from PDS395
//        PDSParser p = parser.getPDSParser(getKey(m) + "_DE48");
//        if (!p.hasElement("0394") || !p.hasElement("0395") || !p.hasElement("0396")) {
//            System.out.println("Important PDS not present");
//            return;
//        }
//
//        String currency = m.getString(50);
//        ReconObject reconObject;
//        ReconFigures reconFigures;
//
//        int cycle = parser.getCycleNumber();
//        if (currencyReconMap.containsKey(currency)) {
//            reconObject = currencyReconMap.get(currency);
//        } else {
//            reconObject = new ReconObject();
//            currencyReconMap.put(currency, reconObject);
//        }
//        reconFigures = reconObject.getReconFigures(cycle);
//        reconObject.setFileId(parser.getFileId());
//
//        String pds394 = p.getElementById("0394").getValue();
//        String pds395 = p.getElementById("0395").getValue();
//        String pds396 = p.getElementById("0396").getValue();
//
//        MathContext mc = new MathContext(0);
//        BigDecimal transactionAmount = convertAmountToDecimal(pds394.substring(1), currency);
//        if (!pds394.substring(0, 1).startsWith("C")) {
//            transactionAmount = transactionAmount.negate(mc);
//        }
//        reconFigures.getNetTransactionAmounts().add(transactionAmount);
//
//        BigDecimal feeAmount = convertAmountToDecimal(pds395.substring(1), currency);
//        if (!pds395.substring(0, 1).startsWith("C")) {
//            feeAmount = feeAmount.negate(mc);
//        }
//        reconFigures.getNetFeeAmounts().add(feeAmount);
//
//        BigDecimal totalAmount = convertAmountToDecimal(pds396.substring(1), currency);
//        if (!pds396.substring(0, 1).startsWith("C")) {
//            totalAmount = totalAmount.negate(mc);
//        }
//        reconFigures.getNetTotalAmounts().add(totalAmount);
//    }

    static class ReconObject {

        String id;
        String mti;
        String func;
        String tranCode;
        String indicator;
        String ird;
        String brand;
        String bsLevel;
        String bsId;
        String fileId;
        String reconCurrency;
        String feeCurrency;
        int cycle;

        Map<Integer, ReconFigures> reconFigures = new HashMap<>();

        public ReconObject() {
            this.id = UUID.randomUUID().toString();
        }

        public ReconObject(String mti, String funcCode) {
            this();
            this.mti = mti;
            this.func = funcCode;
        }

        public ReconObject(String mti, String func, String tc, String ind, String ird, String brand, String bsLevel,
                String bsId,
                int cycle) {
            this(mti, func);
            this.tranCode = tc;
            this.indicator = ind;
            this.ird = ird;
            this.brand = brand;
            this.bsLevel = bsLevel;
            this.bsId = bsId;
            this.cycle = cycle;
        }


        public int getCycle() {
            return cycle;
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

        public String getBrand() {
            return brand;
        }

        public String getBsLevel() {
            return bsLevel;
        }

        public String getBsId() {
            return bsId;
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
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

    static class ReconSummary {

        int cycle;
        String bsId;
        String bsLevel;
        String fileId;
        String currency;
        String brand;
        String indicator;
        BigDecimal reconAmount;
        BigDecimal transactionFee;

        public ReconSummary(int cycle, String bsLevel, String bsId, String fileId, String currency, String brand,
                String indicator,
                BigDecimal reconAmount,
                BigDecimal transactionFee) {
            this.cycle = cycle;
            this.bsLevel = bsLevel;
            this.bsId = bsId;
            this.fileId = fileId;
            this.currency = currency;
            this.brand = brand;
            this.indicator = indicator;
            this.reconAmount = reconAmount;
            this.transactionFee = transactionFee;
        }

        public int getCycle() {
            return cycle;
        }

        public void setCycle(int cycle) {
            this.cycle = cycle;
        }

        public String getBsId() {
            return bsId;
        }

        public void setBsId(String bsId) {
            this.bsId = bsId;
        }

        public String getBsLevel() {
            return bsLevel;
        }

        public void setBsLevel(String bsLevel) {
            this.bsLevel = bsLevel;
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getIndicator() {
            return indicator;
        }

        public void setIndicator(String indicator) {
            this.indicator = indicator;
        }

        public BigDecimal getReconAmount() {
            return reconAmount;
        }

        public void setReconAmount(BigDecimal reconAmount) {
            this.reconAmount = reconAmount;
        }

        public BigDecimal getTransactionFee() {
            return transactionFee;
        }

        public void setTransactionFee(BigDecimal transactionFee) {
            this.transactionFee = transactionFee;
        }
    }

    static class ReconKey implements Comparable<ReconKey> {

        int cycle;
        String brand;
        String bsLevel;
        String bsId;
        String mti;
        String de24;
        String de71;
        String tranCode;
        String indicator;
        String ird;
        String currency;
        String fileId;

        public ReconKey(int cycle, String brand, String bstype, String bsid, String mti, String de24, String tranCode,
                String ind, String ird, String currency, String fileId) {
            this.cycle = cycle;
            this.brand = brand;
            this.bsLevel = bstype;
            this.bsId = bsid;
            this.mti = mti;
            this.de24 = de24;
            this.tranCode = tranCode;
            this.indicator = ind;
            this.ird = ird;
            this.currency = currency;
            this.fileId = fileId;
        }

        public ReconKey(String de71) {
            this.de71 = de71;
        }

        //key[] = brand, bslevel, bsId, currency, fileId
        public String[] toKeyArray() {
            List<String> l = new ArrayList<>();
            if (brand != null) {
                l.add(brand);
            }
            if (bsLevel != null) {
                l.add(bsLevel);
            }
            if (bsId != null) {
                l.add(bsId);
            }
            if (currency != null) {
                l.add(currency);
            }
            if (fileId != null) {
                l.add(fileId);
            }
            return l.toArray(new String[0]);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ReconKey that = (ReconKey) o;

            return new EqualsBuilder()
                    .append(cycle, that.cycle)
                    .append(brand, that.brand)
                    .append(bsLevel, that.bsLevel)
                    .append(bsId, that.bsId)
                    .append(mti, that.mti)
                    .append(de24, that.de24)
                    //.append(tranCode, that.tranCode)
                    //.append(indicator, that.indicator)
                    //.append(ird, that.ird)
                    .append(currency, that.currency)
                    //.append(fileId, that.fileId)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(cycle)
                    .append(brand)
                    .append(bsLevel)
                    .append(bsId)
                    .append(mti)
                    .append(de24)
                    //.append(tranCode)
                    //.append(indicator)
                    //.append(ird)
                    .append(currency)
                    //.append(fileId)
                    .toHashCode();
        }

        @Override
        public int compareTo(ReconKey that) {
            return new CompareToBuilder()
                    .append(cycle, that.cycle)
                    .append(brand, that.brand)
                    .append(bsLevel, that.bsLevel)
                    .append(bsId, that.bsId)
                    //.append(mti, that.mti)
                    //.append(de24, that.de24)
                    //.append(tranCode, that.tranCode)
                    //.append(indicator, that.indicator)
                    //.append(ird, that.ird)
                    .append(currency, that.currency)
                    //.append(fileId, that.fileId)
                    .toComparison();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append(cycle)
                    .append(brand)
                    .append(bsLevel)
                    .append(bsId)
                    .append(mti)
                    .append(de24)
                    //.append(tranCode)
                    //.append(indicator)
                    //.append(ird)
                    .append(currency)
                    //.append(fileId)
                    .toString();
        }

    }

    static class ReconFigures {

        String id;
        int cycleNumber;

        //used by 1240 messages
        List<BigDecimal> txnAmountList; //transaction amount before deducting interchange
        List<BigDecimal> reconAmountList; //transaction amount after deducting interchange
        List<BigDecimal> feeAmountList; //amount of interchange

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
