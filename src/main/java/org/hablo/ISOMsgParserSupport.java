package org.hablo;

import org.jpos.iso.ISOMsg;

public interface ISOMsgParserSupport {
    ISOMsg parse(byte[] d);
}
