package org.hablo.visa.baseii;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void parse(File file) {
        List<FSDMsg> rawData = null;

        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(row.startsWith("90") ? HEADER_SCHEMA : TC_SCHEMA);
                msgBase.unpack(row.getBytes());
                if (outputParsedFile) {
                    writer.write("TC" + msgBase.get("transactionCode") + (msgBase.hasField("transactionComponentSeq") ?
                            " TCR" + msgBase.get("transactionComponentSeq") : "") + "\n");
                    writer.write(ISOMsgHelper.toString(msgBase));
                }
                counter++;
                lastRecord = row;

                //if TC33 is found
                if (msgBase.get("transactionCode").equals("33")) {
                    if (rawData == null) {
                        rawData = new ArrayList<>();
                    }
                    rawData.add(msgBase);
                }
            }

            if (rawData != null && !rawData.isEmpty()) {
                generateRawDataReports(rawData);
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

    private void generateRawDataReports(List<FSDMsg> list) throws IOException, JDOMException {
        System.out.println(list.size());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("SMS_RAW_DATA.txt"))) {
            for (FSDMsg m : list) {
                if (
                        m.get("reportText").startsWith("V22000") ||
                        m.get("reportText").startsWith("V22200") ||
                        m.get("reportText").startsWith("V22120")
                ) {
                    FSDMsg r = new FSDMsg(RAW_DATA_SCHEMA);
                    r.unpack(m.get("reportText").getBytes());
                    writer.write(ISOMsgHelper.toString(r));
                } else {
//                    System.out.println("ID="+m.get("reportText"));
                }
            }
        }
    }
}
