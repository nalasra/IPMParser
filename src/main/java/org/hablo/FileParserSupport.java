package org.hablo;

import java.io.BufferedWriter;
import java.io.File;

public abstract class FileParserSupport {
    protected BufferedWriter writer;
    protected boolean outputParsedFile;
    public abstract void parse(File file);
    protected void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }
    protected void setOutputParsedFile(boolean outputParsedFile){
        this.outputParsedFile = outputParsedFile;
    }
}
