package org.hablo.rdw;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jpos.iso.ISOUtil;

public class RDWWriter implements AutoCloseable {

    private OutputStream f;
    final int DATA_BLOCK_SIZE = 1012;
    final int RDW_SIZE = 4;
    final int UNUSED_SIZE = 2;
    int currentCounter = 0;
    private String filename;

    public RDWWriter(String filename) {
        this.filename = filename;
    }

    public void open() throws IOException {
        f = new BufferedOutputStream(new FileOutputStream(filename));
    }

    /**
     * Close the underlying OutputStream
     */
    @Override
    public void close() throws IOException {
        flush();
        f.close();
    }

    public void write(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Data to write cannot be null or empty.");
        }

        int recordLength = data.length;
        byte[] recordLengthBytes = ISOUtil.hex2byte(String.format("%08X", recordLength));

        f.write(recordLengthBytes);
        f.write(data);

        // Pad the data with unused bytes if necessary
        int remainingInBlock = DATA_BLOCK_SIZE - ((recordLength + RDW_SIZE) % DATA_BLOCK_SIZE);
        if (remainingInBlock < UNUSED_SIZE) {
            remainingInBlock += DATA_BLOCK_SIZE;
        }
        byte[] padding = new byte[remainingInBlock - UNUSED_SIZE];
        f.write(padding);

        currentCounter += recordLength + RDW_SIZE + padding.length;
    }

    private void flush() throws IOException {
        f.flush();
    }
}