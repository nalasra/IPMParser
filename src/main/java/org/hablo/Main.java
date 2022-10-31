package org.hablo;

import org.hablo.mastercard.T057.T057Parser;
import org.hablo.mastercard.T112.T112Parser;
import org.hablo.visa.basei.BaseIParser;
import org.hablo.visa.baseii.BaseIIParser;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.*;

public class Main {
    static Logger l = Logger.getLogger("Q2");
    static String USER_DIR = System.getProperty("user.dir");
    static String FILES_DIR = "/src/dist/files/";
    static String BASEII_FILES = USER_DIR + FILES_DIR + "baseii/";
    static String BASEII_FILES_PARSED = BASEII_FILES + "parsed/";
    static String T112_FILES = USER_DIR + FILES_DIR + "t112/";
    static String T112_FILES_PARSED = T112_FILES + "parsed/";
    static String T057_FILES = USER_DIR + FILES_DIR + "t057/";
    static String T057_FILES_PARSED = T057_FILES + "parsed/";

    public static void main(String[] args) {
        l.addListener(new SimpleLogListener());

        //parseBASEIIFile("");
        //parseBASEIIFile("VISA_OUTCTF0322160157.CTF"); //VISAIN_BAE_410896_090921.txt

        //parseT112File("");
        //parseT112File("agoda"); //MCI.AR.T112.M.E0070571.D220316.T061510.A005
        //parseT112File("MCI.AR.T112.M.E0070571.D220316.T061510.A005");

        //parseT057File("");
        //parseT057File("MCI.AR.T112.M.E0070571.D221001.T184842.A001"); //"TT057T0.2020-10-25-00-00-27.001"
        parseRawMessage("01 04 01 72 d9 1c c3 01 00 76 7e 66 81 28 f0 ba 16 10 42 77 69 00 69 46 44 73 00 00 00 00 00 00 30 00 00 00 00 00 00 24 87 10 04 09 27 52 98 29 00 00 25 29 94 12 27 52 10 04 25 06 10 05 73 99 04 04 05 10 00 00 00 06 45 87 84 25 04 27 76 90 06 94 64 47 3d 25 06 22 00 18 47 64 10 00 02 f2 f2 f7 f7 f1 f3 f2 f8 f0 f5 f1 f0 f0 f0 f1 f8 f0 f9 f4 f0 f8 f2 f0 f9 f6 f1 f9 f1 f1 f3 40 40 40 40 40 d9 c1 c9 d5 d3 c5 e7 40 c9 d5 e5 c5 e2 e3 d4 c5 d5 e3 40 40 40 40 40 40 40 d5 c1 c9 d9 d6 c2 c9 40 40 40 40 40 40 d2 c5 05 40 40 40 40 f2 04 04 08 40 24 60 7d 83 ae 28 01 84 20 01 01 01 00 00 00 00 5f 01 00 5c 9f 34 03 42 03 00 9f 33 03 e0 f8 e8 95 05 00 80 04 80 00 9f 37 04 ea 36 cf b0 9f 10 07 06 01 12 03 a0 30 02 9f 26 08 d5 9d 73 df 43 35 fc e6 9f 36 02 00 03 82 02 38 00 9c 01 00 9f 1a 02 06 43 9a 03 22 10 04 9f 02 06 00 00 00 30 00 00 5f 2a 02 04 04 9f 03 06 00 00 00 00 00 00 04 05 00 00 10 10 40 00 00 00 00 00 00 00 03 82 27 73 40 72 84 81 05 80 00 00 00 02");
    }

    public static File[] getFiles(String desiredFile, String parentFolder) throws Exception {
        File[] fs;
        File fp;
        //process all files under parent
        if (desiredFile == null || desiredFile.isEmpty()) {
            fp = new File(parentFolder);
            if (fp.isDirectory())
                fs = fp.listFiles();
            else
                throw new Exception("Invalid flow. Cannot get files.");
        } else {
            //process desired file
            fp = new File(parentFolder + desiredFile);
            if (!fp.exists())
                throw new FileNotFoundException();
            //process desired folder
            if (fp.isDirectory())
                fs = fp.listFiles();
            else
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
                        BufferedWriter writer = initializeWriter(BASEII_FILES_PARSED, f.getName());
                        BaseIIParser parser = new BaseIIParser();
                        parser.setOutputParsedFile(true);
                        parser.setWriter(writer);
                        parser.parse(f);
                        writer.close();
                    }
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
                        BufferedWriter writer = initializeWriter(T057_FILES_PARSED, f.getName());
                        T057Parser parser = new T057Parser();
                        parser.setOutputParsedFile(true);
                        parser.setWriter(writer);
                        parser.parse(f);
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
                        BufferedWriter writer = initializeWriter(T112_FILES_PARSED, f.getName());
                        T112Parser parser = new T112Parser();
                        parser.setOutputParsedFile(true);
                        parser.setWriter(writer);
//                        parser.setMtiFilter("1644, 1240");
                        parser.parse(f);
                        writer.close();
                    }
                }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    public static void parseRawMessage(String hexString) {
        ISOMsg m = BaseIParser.parseRaw(hexString);
        m.dump(System.out, "");
    }
}