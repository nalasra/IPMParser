package org.hablo.iso;

import java.io.PrintStream;

import org.jpos.iso.ISOField;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.XMLPackager;

public class ISONamedField extends ISOField {
    private String description;

    public void setDescription(String description) {
        this.description = description;
    }

    public ISONamedField(int fieldNumber, String description, String value){
        this.fieldNumber = fieldNumber;
        this.value = value;
        this.description = description;
    }

    @Override
    public void dump (PrintStream p, String indent) {
        if (value != null) {
            if (value.indexOf('<') >= 0) {
                p.print(indent + "<" + XMLPackager.ISOFIELD_TAG + " " +
                        XMLPackager.ID_ATTR + "=\"" + fieldNumber + "\"><![CDATA[");
                p.print(value);
                p.println("]]></" + XMLPackager.ISOFIELD_TAG + ">");
            } else if (value.startsWith("{")) {
                p.print(indent + "<" + XMLPackager.ISOFIELD_TAG + " " +
                        XMLPackager.ID_ATTR + "=\"" + fieldNumber + "\"><![CDATA[");
                p.print(value);
                p.println("]]></" + XMLPackager.ISOFIELD_TAG + ">");
            } else {
                p.println(indent + "<" + XMLPackager.ISOFIELD_TAG + " " +
                        XMLPackager.ID_ATTR + "=\"" + fieldNumber + "\" " +
                        "name" + "=\"" + description + "\" " +
                        XMLPackager.VALUE_ATTR
                        + "=\"" + ISOUtil.normalize(value)
                        + "\"/>");
            }
        }
    }
}
