package org.hablo.mastercard.util;

/**
 * Created by Arsalan Khan on 09/06/21.
 * For MC DE62 (IPM Clearing Format)
 */
public class DE62IPMParser extends PDSParser {
    public DE62IPMParser() {
        super(62, 4, 3, "PDS", 0, 9999);
    }
}
