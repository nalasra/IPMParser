package org.hablo.mastercard.t112;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hablo.FileParserSupport;
import org.hablo.helper.ISOMsgHelper;
import org.hablo.helper.PropertiesLoader;
import org.hablo.mastercard.iso.MCIPMMsgParser;
import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.DE62IPMParser;
import org.hablo.mastercard.util.PDSParser;
import org.hablo.mastercard.util.ParserSupport;
import org.hablo.rdw.RDWReader;
import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class T112Parser extends FileParserSupport {

    private final MCIPMMsgParser msgParser;
    private String mtiFilter;
    private String funcCodeFilter;
    private String fileId;
    private int cycleNumber;
    private static final PropertiesLoader mtiNameConverter = new PropertiesLoader(
            "mc_ipm_mti_func_code_desc_list.properties");

    private static Map<String, PDSParser> pdsParsersCache = new HashMap<>();


    public T112Parser(){
        this.msgParser = new MCIPMMsgParser();
    }

    public void setMtiFilter(String mtiFilter) {
        this.mtiFilter = mtiFilter;
    }

    public void setFuncCodeFilter(String funcCodeFilter) {
        this.funcCodeFilter = funcCodeFilter;
    }

    public PDSParser getPDSParser(String key) {
        return pdsParsersCache.get(key);
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(int cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    @Override
    public void parse(File file) {
        int counter = 0;
        try (RDWReader reader = new RDWReader(file.getPath())) {
            reader.open();
            byte[] r;
            while ((r = reader.read()) != null) {

                ISOMsg msg = msgParser.parse(r);
                if (outputParsedFile && (
                        StringUtils.isBlank(mtiFilter) || mtiFilter.contains(msg.getMTI()) &&
                                StringUtils.isBlank(funcCodeFilter) || funcCodeFilter.contains(msg.getString(24))
                )) {
                    //dump description
                    String key = msg.getMTI() + "." + msg.getString(24);
                    if (mtiNameConverter.hasKey(key)) {
                        String description = mtiNameConverter.convert(key);
                        writer.write("<!-- ########### " + description + " (" + key + ") ########### -->");
                    } else {
                        writer.write("<!-- ########### Cannot identify description for (" + key + ") ########### -->");
                    }
                    writer.newLine();
                    writer.write(ISOMsgHelper.toString(msg));
                    writer.newLine();

                    if (msg.hasField(48)) {
                        String de48o = parseDE(DE48IPMParser.class, msg);
                        writer.write(de48o);
                    }
                    if (msg.hasField(62)) {
                        String de62o = parseDE(DE62IPMParser.class, msg);
                        writer.write(de62o);
                    }

                    writer.newLine();
                }
                addISOMessage(msg);
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

    public static String getKey(ISOMsg m) {
        return String.format("%s_%s_%s_%s", m.getString(0), m.getString(3), m.getString(24), m.getString(71));
    }

    public static <T> String parseDE(Class<T> clazz, ISOMsg m) throws BLException {
        try {
            T parserObj = clazz.getDeclaredConstructor().newInstance();
            if (parserObj instanceof ParserSupport) {
                ((ParserSupport) parserObj).parse(m);
                pdsParsersCache.put(getKey(m) + "_" + clazz.getSimpleName().substring(0, 4), (PDSParser) parserObj);
                return ISOMsgHelper.toString((ParserSupport) parserObj);
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
