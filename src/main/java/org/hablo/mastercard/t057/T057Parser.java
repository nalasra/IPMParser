package org.hablo.mastercard.t057;

import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.jpos.util.FSDMsg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class T057Parser extends FileParserSupport {
    private static final String SCHEMA = "file:src/dist/cfg/mastercard/t057/t057-";
    public void parse(File file) {
        int counter = 0;

        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(SCHEMA);
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
