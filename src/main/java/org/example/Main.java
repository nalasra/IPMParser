package org.example;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;

public class Main {

    static String MC_IPM = "mas_ipm.xml";
    static String MC_IPM_EBCDIC = "mas_ipm_ebcdic.xml";

    public static void main(String[] args) throws ISOException, IOException {
        String fileName = "MCI.AR.T112.M.E0070571.D220316.T061510.A005";
        String ENCODING = MC_IPM_EBCDIC;

        //this will write all ISO msgs into a separate file
        BufferedWriter writer = new BufferedWriter(new FileWriter(
                System.getProperty("user.dir") + "/src/dist/cfg/ipm_files/" + fileName + "_clear"));
        File ipmFile = new File(System.getProperty("user.dir") + "/src/dist/cfg/ipm_files/" + fileName);
        if (!ipmFile.exists()) {
            System.out.println(fileName + ": File not exists");
            return;
        }
        try (RDWReader reader = new RDWReader(Files.newInputStream(ipmFile.toPath()))) {
            byte[] r = reader.read();
            GenericPackager packager = new GenericPackager("jar:packager/" + ENCODING);
            while (r != null && r.length > 0) {
                ISOMsg msg = createISOMsg(r, packager);
                if (msg.getMTI().equals("1644") && msg.getString(24).equals("680")) {
                    writer.write(dumpISOMsg(msg));
                    System.out.println("---------");
                    dumpPDS(msg.getString(48));
                }
                r = reader.read();
            }
        }
        writer.close();
    }

    public static void parseFile(String fileName, String encoding) throws IOException, ISOException {

    }

    public static ISOMsg createISOMsg(byte[] data, GenericPackager genericPackager) throws ISOException {
        ISOMsg msg;
        msg = new ISOMsg();
        genericPackager.setLogger(Logger.getLogger("Q2"), "packager");
        msg.setPackager(genericPackager);
        msg.unpack(data);
        return msg;
    }

    public static void dumpPDS(String data) {
        int i = 0;
        while (i < data.length()) {
            String t = data.substring(i, i + 4);
            i = i + 4;
            String l = data.substring(i, i + 3);
            i = i + 3;
            int len = Integer.parseInt(l);
            String v = data.substring(i, i + len);
            i = i + v.length();
            System.out.println("PDS" + t + " " + l + " " + v);
        }
    }

    private static String dumpISOMsg(ISOMsg m) throws UnsupportedEncodingException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();
        try (PrintStream ps = new PrintStream(baos, false, utf8)) {
            m.dump(ps, "");
        }
        return baos.toString(utf8);
    }
}
