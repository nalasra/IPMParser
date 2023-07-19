package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.TLVParser.TLV;
import org.junit.jupiter.api.Test;

public class PDS0164ParserTest {

    @Test
    void testPDS0146Parser(){
        TLV t = new TLV("0146", "002901986000000034182986000000034182", "PDS");
        PDS0146Parser parser = new PDS0146Parser();
        parser.parse(t);

        t.dump(System.out, "");
    }
    @Test
    void testPDS0164Parser(){
        TLV t = new TLV("0164", "06488288500000B2302230106896860000000B2302230107281319767260B2302230108491982500000B2302230109098064516200B2302230109691337817000B2302230110462100000000B2302230110862074726311B2302230111664047513600B2302230112491351126000B23022301", "PDS");
        PDS0164Parser parser = new PDS0164Parser();
        parser.parse(t);

        t.dump(System.out, "");
    }
}
