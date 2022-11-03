package org.hablo.mastercard.util;

import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.util.Loggeable;

public interface DEParserSupport extends Loggeable {
    void parse(ISOMsg m) throws ISOException, BLException;
}

