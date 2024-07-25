package org.hablo.visa.baseii;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jdom2.JDOMException;
import org.jpos.util.FSDMsg;

public class BaseIIParser extends FileParserSupport {

    private static final String RAW_DATA_SCHEMA = "file:src/dist/cfg/visa/rawdata/rawdata-";
    private static final String HEADER_SCHEMA = "file:src/dist/cfg/visa/baseii/baseii-90-";
    private static final String TC_SCHEMA = "file:src/dist/cfg/visa/baseii/baseii-";
    private int counter;
    private String lastRecord;
    private List<FSDMsg> rawData = null;
    static String TRANSACTION_CODE = "Transaction Code";
    static String TRANSACTION_COMPONENT_SEQ = "Transaction Component Sequence";

    static Map<String, String> reportTypes = new HashMap<>();

    static {
        reportTypes.put("V22000", "Header Record");
        reportTypes.put("V22120", "Settlement Information");
        reportTypes.put("V22200", "Financial Transaction 1");
        reportTypes.put("V22201", "Financial Transaction 1.1");
        reportTypes.put("V22210", "Financial Transaction 2");
        reportTypes.put("V22220", "Financial Transaction 3");
        reportTypes.put("V22225", "Financial Transaction 4");
        reportTypes.put("V22226", "Financial Transaction 5");
        reportTypes.put("V22227", "E-Commerce 3-D Secure Record");
        reportTypes.put("V22228", "Financial Transaction Record/mVisa Record");
        reportTypes.put("V22230", "Financial Transaction Record Payment Service-Specific");
        reportTypes.put("V22231", "Financial Transaction Record/Loan-Specific");
        reportTypes.put("V22235", "Financial Transaction Record/Recurring Payments");
        reportTypes.put("V22240", "Financial Transaction Record/Interlink-Specific");
        reportTypes.put("V22250", "Financial Transaction Record/ATM-Specific");
        reportTypes.put("V22260", "Financial Transaction Record/Multicurrency-Specific");
        reportTypes.put("V22261", "Financial Transaction Fee Record");
        //22280
        //22281
        //22282
        reportTypes.put("V22900", "Trailer Record");
    }

    @Override
    public FSDMsg parseRecord(String f) throws IOException, JDOMException {
        FSDMsg msgBase = new FSDMsg(f.startsWith("90") ? HEADER_SCHEMA : TC_SCHEMA);
        msgBase.unpack(f.getBytes());

        //if TC33 is found
        if (msgBase.get(TRANSACTION_CODE).equals("33")) {
            if (rawData == null) {
                rawData = new ArrayList<>();
            }
            rawData.add(msgBase);
        }
        return msgBase;
    }

    @Override
    public void parse(File file) {
        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = parseRecord(row);
                if (outputParsedFile) {
                    writer.write("TC" + msgBase.get(TRANSACTION_CODE) + (msgBase.hasField(TRANSACTION_COMPONENT_SEQ) ?
                            " TCR" + msgBase.get(TRANSACTION_COMPONENT_SEQ) : "") + "\n");
                    writer.write(ISOMsgHelper.toString(msgBase));
                }
                counter++;
                lastRecord = row;
            }
        } catch (Exception e) {
            System.out.println("Error at line# " + counter);
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Last processed record line#: " + counter);
            System.out.println("Last processed record line: " + lastRecord);
        }
    }

    public void generateRawDataReports() throws IOException, JDOMException {
        if (rawData == null || rawData.isEmpty()) {
            return;
        }

        int unknownRecords = 0;
        FileWriter fw;
        if (outputDir == null) {
            fw = new FileWriter(FileDescriptor.out);
        } else {
            fw = new FileWriter(outputDir + "/" + sessionId + "/TC33_SMSRawData.txt");
        }
        try (BufferedWriter writer = new BufferedWriter(fw)) {
            for (FSDMsg m : rawData) {
                if (m.hasField("Report Text")) {
                    String reportId = m.get("Report Text").substring(0, 6);
                    if (reportTypes.containsKey(reportId)) {
                        String reportName = reportTypes.get(reportId);
                        FSDMsg r = new FSDMsg(RAW_DATA_SCHEMA);
                        r.unpack(m.get("Report Text").getBytes());
                        writer.write("###### " + reportName);
                        writer.newLine();
                        writer.write(ISOMsgHelper.toString(r));
                    } else {
                        System.out.println("ID=" + m.get("Report Text"));
                        unknownRecords++;
                    }
                }
            }
            if (unknownRecords > 0) {
                System.out.println("generateRawDataReports -> Unknown records: " + unknownRecords);
            }
        }
    }
}
