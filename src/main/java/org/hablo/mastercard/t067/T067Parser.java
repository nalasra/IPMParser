package org.hablo.mastercard.t067;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.util.FSDMsg;

public class T067Parser extends FileParserSupport {
    private static final String DAILY_SCHEMA = "file:src/dist/cfg/mastercard/t067/t067-DAILY-";
    private static final String FULL_SCHEMA = "file:src/dist/cfg/mastercard/t067/t067-FULL-";
    public void parse(File file) {
        int counter = 0;

        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(row.trim().startsWith("UPDATE") ? DAILY_SCHEMA : FULL_SCHEMA);
                msgBase.unpack(row.getBytes());
                if (outputParsedFile)
                    writer.write(ISOMsgHelper.toString(msgBase));
                counter++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error at line# " + counter);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
