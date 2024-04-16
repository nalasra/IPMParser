package org.hablo.mastercard.iso;

import java.util.ArrayList;
import java.util.List;

import org.hablo.ISOMsgParserSupport;
import org.hablo.mastercard.util.DE48IPMParser;
import org.hablo.mastercard.util.ParserSupport;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;

public class MCIPMMsgParser implements ISOMsgParserSupport {

    private boolean debug = false;
    GenericPackager packager;
    static List<Class> mcDEParsers = new ArrayList<>();

    static String MC_IPM = "mas_ipm.xml";
    static String MC_IPM_EBCDIC = "mas_ipm_ebcdic.xml";

    static {
        mcDEParsers.add(DE48IPMParser.class);
    }

    public MCIPMMsgParser() {

    }

    public MCIPMMsgParser(boolean debug) {
        this.debug = debug;
    }

    public ISOMsg parse(byte[] d) {
        String encoding = MC_IPM;
        ISOMsg m = null;
        try {
            if (packager == null) {
                if (ISOUtil.hexString(new byte[]{d[0]}).startsWith("F")) {
                    encoding = MC_IPM_EBCDIC;
                }
                packager = new GenericPackager("jar:packager/" + encoding);
                packager.setLogger(Logger.getLogger("Q2"), "packager");
            }
            m = packager.createISOMsg();
            m.setPackager(packager);
            m.unpack(d);
            if (debug && (mcDEParsers != null)) {
                System.out.println("----- Parsers ------");
                expandDE(m, mcDEParsers);
            }
        } catch (ISOException e) {
            System.out.println("Exception occurred...");
            e.printStackTrace();
        } finally {
            if (debug) {
                m.dump(System.out, "");
            }
        }
        return m;
    }


    public <T> void expandDE(ISOMsg m, List<Class> dEParsers) {
        for (Class<T> c : dEParsers) {
            try {
                T o = c.newInstance();
                if (o instanceof ParserSupport) {
                    ((ParserSupport) o).parse(m);
                    ((ParserSupport) o).dump(System.out, "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
