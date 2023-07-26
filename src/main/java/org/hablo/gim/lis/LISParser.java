package org.hablo.gim.lis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.util.FSDMsg;

public class LISParser  extends FileParserSupport {
    private static final String TC_SCHEMA = "file:src/dist/cfg/gim/lis/tc-";
    private int counter;
    @Override
    public void parse(File file) {
        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(TC_SCHEMA);
                msgBase.unpack(row.getBytes());
                if(outputParsedFile){
                    writer.write("TC" + msgBase.get("transactionCode") + (msgBase.hasField("tcrSeqNumber") ? " TCR" + msgBase.get("tcrSeqNumber") : "") + "\n");
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
