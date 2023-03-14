package org.hablo.mastercard.util;

public class DE110Parser extends GenericTLVParser {
    static {
    }

    public DE110Parser() {
        super(110, 2, 3, "subelement", 0, 10);
    }
}
