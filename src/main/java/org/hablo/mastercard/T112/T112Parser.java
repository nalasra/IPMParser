package org.hablo.mastercard.T112;

import org.hablo.rdw.RDWReader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Loggeable;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.hablo.helper.ISOMsgHelper.createISOMsg;

public class T112Parser {

    static String MC_IPM = "mas_ipm.xml";
    static String MC_IPM_EBCDIC = "mas_ipm_ebcdic.xml";

    public List<Loggeable> parse(File file) {
        String ENCODING = MC_IPM_EBCDIC;
        int counter = 0;
        List<Loggeable> msgs = new ArrayList<>();
        try (RDWReader reader = new RDWReader(Files.newInputStream(file.toPath()))) {
            byte[] r = reader.read();
            GenericPackager packager = new GenericPackager("jar:packager/" + ENCODING);
            while (r != null && r.length > 0) {
                ISOMsg msg = createISOMsg(r, packager);
                msgs.add(msg);
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
        return msgs;
    }
}
