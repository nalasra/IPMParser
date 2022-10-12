package org.example.mastercard.IPM;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jpos.iso.ISOUtil;

public class RDWReader implements AutoCloseable {

    private final InputStream f;
    final int BYTE_BLOCK_SIZE = 1014;
    final int DATA_BLOCK_SIZE = 1012;
    final int RDW_SIZE = 4;
    final int UNUSED_SIZE = 2;
    int counter = 0;

    public RDWReader(java.io.InputStream is) {
        this.f = is;
    }

    /**
     * Close the underlying InputStream
     */
    @Override
    public void close() throws IOException {
        f.close();
    }

    private int getCurrentCounter() {
        return (counter % DATA_BLOCK_SIZE);
    }

    private int readInternal(int numBytes, byte[] bytesRead) throws IOException {
        if (numBytes == 0) {
            return 0;
        }

        //are we on the block edge? -----> 1012|1013|1014, then skip 2 unused bytes
        if (getCurrentCounter() == 0 && counter > 0) {
            long ignored = f.skip(UNUSED_SIZE);
            if (ignored == 0) {
                //TODO: add some handling for bytes skipped
            }
        }
        if ((getCurrentCounter() + numBytes) > DATA_BLOCK_SIZE) {
            //block size ended - only a portion of bytes can be read
            int possibleBytes = DATA_BLOCK_SIZE - getCurrentCounter();
            byte[] tempDataBytes1 = new byte[possibleBytes];
            int dataBytesRead1 = f.read(tempDataBytes1);
            if (dataBytesRead1 != -1) {
                counter += dataBytesRead1;
            }

            //skip 2 unused bytes (1014-1012)
            if (getCurrentCounter() == 0) {
                long ignored = f.skip(UNUSED_SIZE);
                if (ignored == 0) {
                    //TODO: add some handling for bytes skipped
                }
            }

            //read remaining bytes from a new block
            int remainingDataBytes = numBytes - possibleBytes;
            byte[] tempDataBytes2 = new byte[remainingDataBytes];
            int dataBytesRead2 = f.read(tempDataBytes2);
            if (dataBytesRead2 != -1) {
                counter += dataBytesRead2;
                final byte[] t = ArrayUtils.addAll(tempDataBytes1, tempDataBytes2);
                System.arraycopy(t, 0, bytesRead, 0, t.length);
                return dataBytesRead1 + dataBytesRead2;
            }
        } else {
            //sufficient block available for reading
            int lengthBytesRead = f.read(bytesRead);
            if (lengthBytesRead != numBytes) {
                throw new IOException("Required number of bytes couldn't be read.");
            }
            counter += lengthBytesRead;
            return lengthBytesRead;
        }
        return 0;
    }

    /**
     * Read the next record and return as a new byte array
     *
     * @return the byte[] containing the record
     */
    public byte[] read() throws IOException {
        byte[] tempLengthBytes = new byte[RDW_SIZE];
        int nb = readInternal(RDW_SIZE, tempLengthBytes);
        if (nb == -1) {
            return null;
        }
        String rdw = ISOUtil.hexString(tempLengthBytes);
        int recordLength = Integer.parseInt(rdw, 16);

        if (recordLength == 0) {
            return null;
        }
        byte[] tempLengthBytes2 = new byte[recordLength];
        int nb2 = readInternal(recordLength, tempLengthBytes2);
        if (nb2 != recordLength) {
            throw new IOException("Expected number of bytes couldn't be read.");
        }
        return tempLengthBytes2;
    }

    /**
     * Read the next record into the supplied byte array.
     *
     * @param bytes the byte[] to read the record into
     * @return length of record or -1 if EOF
     */
    private int read(byte[] bytes) {
        throw new NotImplementedException("Needs to be implemented");
    }

    /**
     * Read the next record into the supplied byte array starting at offset.
     */
    private int read(byte[] bytes, int offset) {
        throw new NotImplementedException("Needs to be implemented");
    }
}
