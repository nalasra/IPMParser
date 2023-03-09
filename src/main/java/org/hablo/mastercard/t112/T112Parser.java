package org.hablo.mastercard.t112;

import org.apache.commons.lang3.StringUtils;
import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.DEParserSupport;
import org.hablo.rdw.RDWReader;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
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
                if (outputParsedFile && (StringUtils.isBlank(mtiFilter) || mtiFilter.contains(msg.getMTI()))) {
                    writer.write(ISOMsgHelper.toString(msg));
                    writer.write(parseDE(DE48IPMParser.class, msg));
                }
                if (counter % 100 == 0) {
                    writer.flush();
                }

                r = reader.read();
                counter++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error at line# " + counter);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static <T> String parseDE(Class<T> clazz, ISOMsg m) throws BLException {
        try {
            T o = clazz.newInstance();
            if (o instanceof DEParserSupport) {
                ((DEParserSupport) o).parse(m);
                return ISOMsgHelper.toString((DEParserSupport) o);
            } else {
                System.err.println("Unknown class type: " + clazz.getSimpleName());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (BLException | ISOException | UnsupportedEncodingException blException) {
            blException.printStackTrace();
        }
        throw new BLException("Error occurred while parsing DE");
    }
}
