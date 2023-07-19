package org.hablo.visa.baseii;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.util.FSDMsg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class BaseIIParser extends FileParserSupport {
    private static final String HEADER_SCHEMA = "file:src/dist/cfg/visa/baseii/baseii-90-";
    private static final String TC_SCHEMA = "file:src/dist/cfg/visa/baseii/baseii-";
    private int counter;
    @Override
    public void parse(File file) {
        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(row.startsWith("90") ? HEADER_SCHEMA : TC_SCHEMA);
                msgBase.unpack(row.getBytes());
                if(outputParsedFile){
                    writer.write("TC" + msgBase.get("transactionCode") + (msgBase.hasField("transactionComponentSeq") ? " TCR" + msgBase.get("transactionComponentSeq") : "") + "\n");
                    writer.write(ISOMsgHelper.toString(msgBase));
                }
                counter++;
            }
        } catch (Exception e){
            System.out.println("Error at line# " + counter);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
