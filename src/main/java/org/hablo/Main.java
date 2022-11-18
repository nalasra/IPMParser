package org.hablo;

import org.hablo.mastercard.t057.T057Parser;
import org.hablo.mastercard.t112.T112Parser;
import org.hablo.mastercard.util.DE110Parser;
import org.hablo.mastercard.util.DE48Parser;
import org.hablo.mastercard.util.DE61Parser;
import org.hablo.mastercard.util.DEParserSupport;
import org.hablo.visa.baseii.BaseIIParser;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.security.SecureKeyBlock;
import org.jpos.security.SecureKeyBlockBuilder;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
    static List<Class> mcDEParsers = new ArrayList<>();

    public static void main(String[] args) {
        init();

        /* Visa BASE II */
        //parseBASEIIFile("");
        //parseBASEIIFile("VISA_OUTCTF0322160157.CTF"); //VISAIN_BAE_410896_090921.txt

        /* Mastercard IPM Clearing */
        //parseT112File("");
        //parseT112File("agoda"); //MCI.AR.T112.M.E0070571.D220316.T061510.A005
        //parseT112File("MCI.AR.T112.M.E0070571.D221001.T184842.A001");

        /* Mastercard Currency Exchange Rates */
        //parseT057File("");
        //parseT057File("MCI.AR.T112.M.E0070571.D221001.T184842.A001"); //"TT057T0.2020-10-25-00-00-27.001"

        //parseRawMessage(VisaBaseIParser.class, null, "01 00 76 7e 66 81 28 f0 ba 16 10 42 77 69 00 69 46 44 73 00 00 00 00 00 00 30 00 00 00 00 00 00 24 87 10 04 09 27 52 98 29 00 00 25 29 94 12 27 52 10 04 25 06 10 05 73 99 04 04 05 10 00 00 00 06 45 87 84 25 04 27 76 90 06 94 64 47 3d 25 06 22 00 18 47 64 10 00 02 f2 f2 f7 f7 f1 f3 f2 f8 f0 f5 f1 f0 f0 f0 f1 f8 f0 f9 f4 f0 f8 f2 f0 f9 f6 f1 f9 f1 f1 f3 40 40 40 40 40 d9 c1 c9 d5 d3 c5 e7 40 c9 d5 e5 c5 e2 e3 d4 c5 d5 e3 40 40 40 40 40 40 40 d5 c1 c9 d9 d6 c2 c9 40 40 40 40 40 40 d2 c5 05 40 40 40 40 f2 04 04 08 40 24 60 7d 83 ae 28 01 84 20 01 01 01 00 00 00 00 5f 01 00 5c 9f 34 03 42 03 00 9f 33 03 e0 f8 e8 95 05 00 80 04 80 00 9f 37 04 ea 36 cf b0 9f 10 07 06 01 12 03 a0 30 02 9f 26 08 d5 9d 73 df 43 35 fc e6 9f 36 02 00 03 82 02 38 00 9c 01 00 9f 1a 02 06 43 9a 03 22 10 04 9f 02 06 00 00 00 30 00 00 5f 2a 02 04 04 9f 03 06 00 00 00 00 00 00 04 05 00 00 10 10 40 00 00 00 00 00 00 00 03 82 27 73 40 72 84 81 05 80 00 00 00 02");
        //parseRawMessage(VisaBaseIParser.class, null, "0100fefd66912ae0e216000000000000000410476134000000001900000000000000400000000002500000000002500009191351156100000061000000000028175115091922120000597208400120000108c4f0f0f0f0f0f0f0f00b012345678901204761340000000019d221212312345129f0f2f6f3f1f3f0f0f0f0f2f8f9f6e3c5d9d4c9c4f0f1c3c1d9c440c1c3c3c5d7e3d6d94040c1c3d8e4c9d9c5d940d5c1d4c5404040404040404040404040c3c9e3e840d5c1d4c540404040e4e2084008400840690100669f3303204000950580000100009f37049badbcab9f100706011203a000009f2608696ab26dc89ca9b79f360200ff820200009c01019f1a0208409a032011179f02060000000123005f2a0208409f03060000000000009f6e04000000018407a000000003101001001a3030303031303030303030303030303030303030353330303030098100000002123456788e34363046303030303030303030303030303046314631463146314631463146314631303930303030303030303030303030303030303030303030303232323232323232323041303030303030303030303030303030303030303030303938373635343332313031323334353637383930313233343536373839303132333435363738393031323334353637383930");

        //parseRawMessage(MCDMSParser.class, mcDEParsers, "F0F8F0F0C2200000800000020400000000040000F0F4F0F0F0F1F1F1F0F3F1F3F2F1F3F9F0F0F0F0F1F0F0F6F0F2F2F0F2F0F0F0F9D4C3C3F0F1F1F0F5D3F1F6F1F0F9F6F0F9F0F8F0C2F0F0F8F0D7F0E3C2F0F0E2F0F0F0F0C4C6C6F2F1F6C2F3C3C6F8C2C4F0F7C4F6F2C3F1C6C1F9F8F8C5F6C3F9F9F4F4C6F4F8F8F8F6C2F9F7C6C4F4F4F5F2F8F7F2C6F2C3F6C2F1C3F3F3F4C6F0C4F7F1F0F0F0F6F5F8C4F4C3F6");
        //parseRawMessage(MCDMSParser.class, mcDEParsers, "F0F1F0F07EFF444128E1F80AF1F6F5F2F0F4F7F3F0F0F0F0F0F0F0F0F1F3F0F0F0F0F0F0F0F0F0F0F0F0F0F0F8F5F0F0F0F0F0F0F0F0F0F0F8F5F0F0F0F0F0F0F0F0F0F0F8F5F0F0F0F2F2F1F1F7F3F7F2F0F6F1F0F0F0F0F0F0F6F1F0F0F0F0F0F0F0F0F0F0F0F2F1F7F3F7F2F0F0F2F2F1F1F8F1F2F0F2F2F1F0F2F2F1F5F9F9F9F9F0F1F0F4F0F6F0F0F9F6F8F5F3F2F5F2F0F4F7F3F0F0F0F0F0F0F0F0F1F3C4F1F8F1F2F1F0F1F0F1F0F0F0F1F1F1F1F2F3F4F5F6F7F8F9F0F1F2D98587F0F0F0F0F4F6F8F7F5F5F5F5F3F7F8F7F7F4F6F4D48984A685A2A340C59497969989A49440404040404040C39693A494828981404040404040D4D640F0F0F7D9F8F0F0F2E3E5F8F4F0F8F4F0F8F4F03F8DF3A90A6C9D81F9F7F0F1F0F1F0F0F0F1F0F0F0F0F0F0F0F2F1F0F0F0F0F0F0F0F0F0F0F2F0F0F8F4F0F9F0F2F1F0F0F0F9D4C3E2F0F1F1F0F6F1");

        /* TCP Socket Channels
        * For VTS/MAS Simulator
        * */
        startQ2();

        //keyblock();
    }

    private static void init(){
        l.addListener(new SimpleLogListener());

        mcDEParsers.add(DE61Parser.class);
        mcDEParsers.add(DE48Parser.class);
        mcDEParsers.add(DE110Parser.class);
    }

    private static void startQ2() {
        Q2 q2 = new Q2("src/dist/deploy");
        q2.start();
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

    public static <T> void parseRawMessage(Class<T> clazz, List<Class> dEParsers, String hexString) {
        try {
            T o = clazz.newInstance();
            if (o instanceof ISOMsgParserSupport) {
                byte[] d = ISOUtil.hex2byte(hexString.replace(" ", ""));
                ISOMsg m = ((ISOMsgParserSupport) o).parse(d);
                m.dump(System.out, "");

                if (dEParsers != null)
                    expandDE(m, dEParsers);
            } else System.out.println("Unknown class type: " + clazz.getSimpleName());
        } catch (InstantiationException | IllegalAccessException | ISOException | BLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void expandDE(ISOMsg m, List<Class> dEParsers) throws BLException, ISOException {
        for (Class<T> c : dEParsers) {
            try {
                T o = c.getDeclaredConstructor(new Class[]{ISOMsg.class}).newInstance(m);
                if (o instanceof DEParserSupport) {
                    ((DEParserSupport) o).dump(System.out, "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void keyblock(){
        try {
            SecureKeyBlock skb = SecureKeyBlockBuilder.newBuilder().build("B0080P0TB00S0000E4F7A99DC42E026B6DC3964C9CEB3A1DD8DDA3D1B48B63B2EDAA578F8A31CEE0");
            skb.dump(System.out, "");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}