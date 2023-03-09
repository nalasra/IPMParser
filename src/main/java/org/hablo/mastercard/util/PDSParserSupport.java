package org.hablo.mastercard.util;

import java.io.PrintStream;
import java.util.ArrayList;

import org.jpos.util.Loggeable;

public abstract class PDSParserSupport implements Loggeable {
    protected ArrayList<String> elements;

    protected PDSParserSupport(){
        elements = new ArrayList<>();
    }

    protected abstract void parse(String data);

    @Override
    public void dump(PrintStream p, String indent) {
        for (String s : elements) {
            p.println(indent + s);
        }
    }
}

