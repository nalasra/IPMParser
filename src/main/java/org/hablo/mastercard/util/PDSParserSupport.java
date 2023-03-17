package org.hablo.mastercard.util;

import org.hablo.mastercard.util.TLVParser.TLV;

public abstract class PDSParserSupport  {
    protected abstract void parse(TLV data);
}

