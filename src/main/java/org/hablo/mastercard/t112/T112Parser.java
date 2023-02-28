package org.hablo.mastercard.t112;

import org.apache.commons.lang3.StringUtils;
import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.hablo.rdw.RDWReader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import static org.hablo.helper.ISOMsgHelper.createISOMsg;

public class T112Parser extends FileParserSupport {

    static String MC_IPM = "mas_ipm.xml";
    static String MC_IPM_EBCDIC = "mas_ipm_ebcdic.xml";
    private String mtiFilter;

    public void setMtiFilter(String mtiFilter) {
        this.mtiFilter = mtiFilter;
    }

    @Override
    public void parse(File file) {
        String ENCODING = MC_IPM_EBCDIC;
        int counter = 0;
        try (RDWReader reader = new RDWReader(Files.newInputStream(file.toPath()))) {
            byte[] r = reader.read();
            GenericPackager packager = new GenericPackager("jar:packager/" + ENCODING);
            packager.setLogger(Logger.getLogger("Q2"), "packager");
            while (r != null && r.length > 0) {
                ISOMsg msg = createISOMsg(r, packager);
                if (StringUtils.isBlank(mtiFilter) || mtiFilter.contains(msg.getMTI())) {
                    if (outputParsedFile) {
                        writer.write(ISOMsgHelper.toString(msg));
                        parsePDS(writer, msg.getString(48));
                    }
                }
                if (counter % 100 == 0) {
                    writer.flush();
                }

                r = reader.read();
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

    public static void parsePDS(BufferedWriter writer, String data) throws IOException {
        try {
            if (data == null || data.isEmpty())
                return;
            int i = 0;
            writer.write("---------------------------\r\nPrivate Data Sub-elements\r\n");
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
            writer.write("---------------------------\r\n");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
