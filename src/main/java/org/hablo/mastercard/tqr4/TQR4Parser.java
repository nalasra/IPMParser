package org.hablo.mastercard.tqr4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.util.FSDMsg;

public class TQR4Parser extends FileParserSupport {

    private static final String SCHEMA = "file:src/dist/cfg/mastercard/tqr4/tqr4-";
    private int counter;
    private String lastRecord;

    @Override
    public void parse(File file) {
        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(SCHEMA);
                msgBase.unpack(row.getBytes());
                if (outputParsedFile) {
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
}
