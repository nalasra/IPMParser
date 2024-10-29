package org.hablo.rdw;

import java.io.IOException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.junit.jupiter.api.Test;

class RDWTest {

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
