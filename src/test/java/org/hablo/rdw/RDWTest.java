package org.hablo.rdw;

import static org.hablo.helper.CurrencyHelper.convertAmountToDecimal;
import static org.hablo.helper.CurrencyHelper.formatDecimal;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RDWTest {

    @Test
    void test1() {
        String t = "0000000000254677";
        BigDecimal d = convertAmountToDecimal(t, "376");
        String s = formatDecimal(d);

        BigDecimal dd = d.negate(new MathContext(0));

        Assertions.assertEquals("    2,546.77 CR", s);
        Assertions.assertEquals(new BigDecimal("-2546.77"), dd);
    }

    @Test
    void test2() {
        String t = "-6582715200.726";
        BigDecimal d = new BigDecimal(t);
        String s = formatDecimal(d);

        Assertions.assertEquals("6,582,715,200.726 DR", s);
    }

    @Test
    void test() throws IOException, ISOException {

        ISOMsg m = new ISOMsg("0200");
        m.setPackager(new GenericPackager("src/main/resources/packager/mas_ipm_ebcdic.xml"));
        m.set(2, "111111111111111");
        m.set(3, "010000");
        m.set(4, "000000000000");
        m.set(5, "000000000000");
        m.set(6, "000000000000");
        m.dump(System.out, ">>m1");

        RDWWriter writer = new RDWWriter("rdw_file.txt");
        writer.open();
        writer.write(m.pack());
        writer.close();

        RDWReader reader = new RDWReader("rdw_file.txt");
        reader.open();
        byte[] d = reader.read();
        reader.close();

        ISOMsg m2 = new ISOMsg();
        m2.setPackager(new GenericPackager("src/main/resources/packager/mas_ipm_ebcdic.xml"));
        m2.unpack(d);
        m2.dump(System.out, ">>m2");
    }
}
