package org.hablo;

import org.hablo.mastercard.T057.T057Parser;
import org.hablo.mastercard.T112.T112Parser;
import org.hablo.visa.baseii.BaseIIParser;
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
        parseT112File("MCI.AR.T112.M.E0072218.D221013.T062131.A005");

        //parseT057File("");
        //parseT057File("MCI.AR.T112.M.E0070571.D221001.T184842.A001"); //"TT057T0.2020-10-25-00-00-27.001"
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
                        parser.setMtiFilter("1644, 1100");
                        parser.parse(f);
                        writer.close();
                    }
                }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}