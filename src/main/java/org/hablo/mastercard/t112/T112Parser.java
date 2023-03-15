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
import java.nio.file.Files;
import java.util.HashMap;

import static org.hablo.helper.ISOMsgHelper.createISOMsg;

public class T112Parser extends FileParserSupport {

    static String MC_IPM = "mas_ipm.xml";
    static String MC_IPM_EBCDIC = "mas_ipm_ebcdic.xml";
    private String mtiFilter;
    private static HashMap<String, String> mtiMap = new HashMap<>();

    static {
        mtiMap.put("1240 200", "First Presentment");
        mtiMap.put("1240 205", "Second Presentment (Full)");
        mtiMap.put("1240 282", "Second Presentment (Partial)");
        mtiMap.put("1442 450", "First Chargeback (Full)");
        mtiMap.put("1442 451", "Arbitration Chargeback (Full)");
        mtiMap.put("1442 453", "First Chargeback (Partial)");
        mtiMap.put("1442 454", "Arbitration Chargeback (Partial)");
        mtiMap.put("1644 640", "Currency Update");
        mtiMap.put("1644 680", "File Currency Summary from the clearing system");
        mtiMap.put("1644 685", "Financial Position Detail from the clearing system");
        mtiMap.put("1644 688", "Settlement Position Detail from the clearing system");
        mtiMap.put("1644 691", "Message Exception");
        mtiMap.put("1644 693", "Text Message");
        mtiMap.put("1644 695", "File Trailer");
        mtiMap.put("1644 697", "File Header");
        mtiMap.put("1644 696", "Financial Detail Addendum");
        mtiMap.put("1644 699", "File Reject");
        mtiMap.put("1644 603", "Retrieval Request");
        mtiMap.put("1644 605", "Retrieval Request Acknowledgment");
        mtiMap.put("1740 700", "Fee Collection (Customer-generated)");
        mtiMap.put("1740 780", "Fee Collection Return (Customer-generated)");
        mtiMap.put("1740 781", "Fee Collection Resubmission (Customer-generated)");
        mtiMap.put("1740 782", "Fee Collection Arbitration Return (Customer-generated)");
        mtiMap.put("1740 783", "Fee Collection (Clearing System generated)");
        mtiMap.put("1740 790", "Fee Collection (Funds Transfer)");
    }

    public void setMtiFilter(String mtiFilter) {
        this.mtiFilter = mtiFilter;
    }

    @Override
    public void parse(File file) {
        String encoding = MC_IPM_EBCDIC;
        int counter = 0;
        try (RDWReader reader = new RDWReader(Files.newInputStream(file.toPath()))) {
            byte[] r = reader.read();
            GenericPackager packager = new GenericPackager("jar:packager/" + encoding);
            packager.setLogger(Logger.getLogger("Q2"), "packager");
            while (r != null && r.length > 0) {
                ISOMsg msg = createISOMsg(r, packager);
                if (outputParsedFile && (StringUtils.isBlank(mtiFilter) || mtiFilter.contains(msg.getMTI()))) {
                    expandPDS(DE48IPMParser.class, msg);
                    //dump description
                    if (mtiMap.containsKey(msg.getMTI() + " " + msg.getString(24))) {
                        String description = mtiMap.get(msg.getMTI() + " " + msg.getString(24));
                        writer.write("<!-- ########### " + description + " ########### -->");
                        writer.newLine();
                    }
                    writer.write(ISOMsgHelper.toString(msg));
                    writer.newLine();
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

    public static <T> void expandPDS(Class<T> clazz, ISOMsg m) {
        try {
            T o = clazz.newInstance();
            if (o instanceof DEParserSupport) {
                ((DEParserSupport) o).parse(m);
            } else {
                System.err.println("Unknown class type: " + clazz.getSimpleName());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (BLException | ISOException blException) {
            blException.printStackTrace();
        }
    }
}
