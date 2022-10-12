package org.example.visa.baseii;

import org.jpos.util.FSDMsg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class BaseIIParser {
    private static final String TC_90 = "90";
    private static final String HEADER_SCHEMA = "file:src/dist/cfg/baseii/baseii-90-";
    private static final String FOOTER_91_SCHEMA = "file:src/dist/cfg/baseii/baseii-91-";
    private static final String TC_SCHEMA = "file:src/dist/cfg/baseii/baseii-";
    private int counter;
    public List<FSDMsg> parse(File baseiiFile) {
        List<FSDMsg> msgs = new ArrayList<>();

        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(baseiiFile))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(row.startsWith(TC_90) ? HEADER_SCHEMA : TC_SCHEMA);
                msgBase.unpack(row.getBytes());
                msgs.add(msgBase);
                counter++;
            }
        } catch (FileNotFoundException e){
            System.out.println("Error at line# " + counter);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return msgs;
    }
}
