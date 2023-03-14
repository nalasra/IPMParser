package org.hablo.mastercard.util;

import org.hablo.mastercard.util.GenericTLVParser.GenericTag;

public abstract class PDSParserSupport  {
    protected abstract void parse(GenericTag data);
}

