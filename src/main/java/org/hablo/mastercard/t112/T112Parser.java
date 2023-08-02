package org.hablo.mastercard.t112;

import static org.hablo.helper.ISOMsgHelper.createISOMsg;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.hablo.helper.PropertiesLoader;
import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.DE62IPMParser;
import org.hablo.mastercard.util.ParserSupport;
import org.hablo.rdw.RDWReader;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;

public class T112Parser extends FileParserSupport {

    static String MC_IPM = "mas_ipm.xml";
    static String MC_IPM_EBCDIC = "mas_ipm_ebcdic.xml";
    private String mtiFilter;
    private static final PropertiesLoader mtiNameConverter = new PropertiesLoader(
            "mc_ipm_mti_func_code_desc_list.properties");

    public void setMtiFilter(String mtiFilter) {
        this.mtiFilter = mtiFilter;
    }

    @Override
    public void parse(File file) {
        String encoding = MC_IPM;
        int counter = 0;
        try (RDWReader reader = new RDWReader(file.getPath())) {
            reader.open();
            GenericPackager packager = null;
            byte[] r;
            while ((r = reader.read()) != null) {
                if (packager == null) {
                    if (ISOUtil.hexString(new byte[]{r[0]}).startsWith("F")) {
                        encoding = MC_IPM_EBCDIC;
                    }
                    packager = new GenericPackager("jar:packager/" + encoding);
                    packager.setLogger(Logger.getLogger("Q2"), "packager");
                }

                ISOMsg msg = createISOMsg(r, packager);
                if (outputParsedFile && (StringUtils.isBlank(mtiFilter) || mtiFilter.contains(msg.getMTI()))) {
                    writer.write(ISOMsgHelper.toString(msg));
                    //dump description
                    String key = msg.getMTI() + "." + msg.getString(24);
                    if (mtiNameConverter.hasKey(key)) {
                        String description = mtiNameConverter.convert(key);
                        writer.write("<!-- ########### " + description + " ########### -->");
                        writer.newLine();
                    }
                    writer.write("");
                    writer.write(parseDE(DE48IPMParser.class, msg));
                    writer.write(parseDE(DE62IPMParser.class, msg));
                    writer.newLine();
                }
                if (counter % 100 == 0) {
                    writer.flush();
                }
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
            T o = clazz.getDeclaredConstructor().newInstance();
            if (o instanceof ParserSupport) {
                ((ParserSupport) o).parse(m);
                return ISOMsgHelper.toString((ParserSupport) o);
            } else {
                System.err.println("Unknown class type: " + clazz.getSimpleName());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (BLException | ISOException | UnsupportedEncodingException blException) {
            blException.printStackTrace();
        }
        throw new BLException("Error occurred while parsing DE");
    }
}
