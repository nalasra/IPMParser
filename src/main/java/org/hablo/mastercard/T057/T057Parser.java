package org.hablo.mastercard.T057;

import org.jpos.util.FSDMsg;
import org.jpos.util.Loggeable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class T057Parser {
    private static final String SCHEMA = "file:src/dist/cfg/t057/t057-";

    public List<Loggeable> parse(File file){
        int counter = 0;
        List<Loggeable> msgs = new ArrayList<>();

        FSDMsg msgBase;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null && row.length() > 0) {
                msgBase = new FSDMsg(SCHEMA);
                msgBase.unpack(row.getBytes());
                msgs.add(msgBase);
                counter++;
            }
        } catch (FileNotFoundException e){
            System.out.println("Error at line# " + counter);
            e.printStackTrace();
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return msgs;
    }
}
