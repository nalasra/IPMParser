package org.example.visa.baseii;

import org.jpos.util.FSDMsg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class BaseIIParser {
    private static final String TC_05_TCR_0 = "050";
    private static final String TC_06_TCR_0 = "060";
    private static final String TC_07_TCR_0 = "070";
    private static final String TC_25_TCR_0 = "250";
    private static final String TC_26_TCR_0 = "260";
    private static final String TC_27_TCR_0 = "270";
    private static final String TC_05_TCR_1 = "051";
    private static final String TC_06_TCR_1 = "061";
    private static final String TC_07_TCR_1 = "071";
    private static final String TC_25_TCR_1 = "251";
    private static final String TC_26_TCR_1 = "261";
    private static final String TC_27_TCR_1 = "271";
    private static final String TC_05_TCR_5 = "055";
    private static final String TC_06_TCR_5 = "065";
    private static final String TC_07_TCR_5 = "075";
    private static final String TC_25_TCR_5 = "255";
    private static final String TC_26_TCR_5 = "265";
    private static final String TC_27_TCR_5 = "275";
    private static final String TC_06_TCR_D = "06D";
    private static final String TC_90 = "90";
    private static final String TC_91_TCR_0 = "910";
    private static final String TC_92_TCR_0 = "920";
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
