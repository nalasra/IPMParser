package org.hablo.mastercard.util;

/**
 * Created by Arsalan Khan on 09/06/21.
 * For MC DE48 (IPM Clearing Format)
 */
public class DE48IPMParser extends PDSParser {

    public DE48IPMParser() {
        super(48, 4, 3, "PDS", 0, 9999);
    }
}
