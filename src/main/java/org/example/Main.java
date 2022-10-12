package org.example;

import org.example.mastercard.T057.T057Parser;
import org.example.mastercard.T112.T112Parser;
import org.example.visa.baseii.BaseIIParser;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.FSDMsg;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.*;
import java.util.List;

import static org.example.helper.ISOMsgHelper.loggeableToString;

public class Main {
    static Logger l = Logger.getLogger("Q2");
    static String USER_DIR = System.getProperty("user.dir");
    static String BASEII_FILES = USER_DIR + "/src/dist/files/baseii/";
    static String BASEII_FILES_PARSED = BASEII_FILES + "parsed/";
    static String T112_FILES = USER_DIR + "/src/dist/files/t112/";
    static String T112_FILES_PARSED = T112_FILES + "parsed/";
    static String T057_FILES = USER_DIR + "/src/dist/files/t057/";
    static String T057_FILES_PARSED = T057_FILES + "parsed/";

    public static void main(String[] args) throws ISOException, IOException {
        l.addListener(new SimpleLogListener());

        parseBASEIIFile("VISA_OUTCTF0322160157.CTF"); //VISAIN_BAE_410896_090921.txt
        //parseT112File("MCI.AR.T112.M.E0070571.D220316.T061510.A005"); //MCI.AR.T112.M.E0070571.D220316.T061510.A005
        //parseT057File("TT057T0.2020-10-25-00-00-27.001"); //"TT057T0.2020-10-25-00-00-27.001"
    }

    public static File[] getFiles(String desiredFile, String folder) throws Exception {
        File[] fs;
        File fp;
        if (desiredFile == null || desiredFile.isEmpty()) {
            fp = new File(folder);
            if (fp.isDirectory())
                fs = fp.listFiles();
            else
                throw new Exception("Invalid flow. Cannot get files.");
        } else {
            fp = new File(folder + desiredFile);
            if (!fp.exists())
                throw new FileNotFoundException();
            fs = new File[]{fp};
        }
        return fs;
    }

    public static BufferedWriter initializeWriter(String path, String fileName) throws IOException {
        File theDir = new File(path);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        return new BufferedWriter(new FileWriter(path + fileName + "_" + System.currentTimeMillis()));
    }

    public static void parseBASEIIFile(String fileName) {
        try {
            File[] fs = getFiles(fileName, BASEII_FILES);
            if (fs != null)
                for (File f : fs) {
                    if (f.isFile()) {
                        BaseIIParser parser = new BaseIIParser();
                        List<Loggeable> msgs = parser.parse(f);
                        BufferedWriter writer = initializeWriter(BASEII_FILES_PARSED, f.getName());
                        for (Loggeable l : msgs) {
                            FSDMsg m = (FSDMsg)l;
                            writer.write("TC" + m.get("transactionCode") + (m.hasField("transactionComponentSeq") ? " TCR" + m.get("transactionComponentSeq") : "") + "\n");
                            writer.write(loggeableToString(m));
                        }
                        writer.close();
                    }
                    //else do some handling
                }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static void parseT057File(String fileName) {
        try {
            File[] fs = getFiles(fileName, T057_FILES);
            if (fs != null)
                for (File f : fs) {
                    if (f.isFile()) {
                        T057Parser parser = new T057Parser();
                        List<Loggeable> msgs = parser.parse(f);
                        BufferedWriter writer = initializeWriter(T057_FILES_PARSED, f.getName());
                        for (Loggeable m : msgs) {
                            writer.write(loggeableToString(m));
                        }
                        writer.close();
                    }
                }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void parseT112File(String fileName) {
        try {
            File[] fs = getFiles(fileName, T112_FILES);
            if (fs != null)
                for (File f : fs) {
                    if (f.isFile()) {
                        T112Parser parser = new T112Parser();
                        List<Loggeable> msgs = parser.parse(f);
                        BufferedWriter writer = initializeWriter(T112_FILES_PARSED, f.getName());
                        for (Loggeable m : msgs) {
                            writer.write(loggeableToString(m));
                            dumpPDS(writer, ((ISOMsg) m).getString(48));
                        }
                        writer.close();
                    }
                }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public static void dumpPDS(BufferedWriter writer, String data) throws IOException {
        int i = 0;
        while (i < data.length()) {
            String t = data.substring(i, i + 4);
            i = i + 4;
            String l = data.substring(i, i + 3);
            i = i + 3;
            int len = Integer.parseInt(l);
            String v = data.substring(i, i + len);
            i = i + v.length();
            writer.write("PDS" + t + " " + l + " " + v + "\r\n");
        }
    }
}