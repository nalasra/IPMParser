package org.hablo.rdw;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jpos.iso.ISOUtil;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RDWReader implements AutoCloseable {

    private InputStream f;
    final int BYTE_BLOCK_SIZE = 1014;
    final int DATA_BLOCK_SIZE = 1012;
    final int RDW_SIZE = 4;
    final int UNUSED_SIZE = 2;
    int currentCounter = 0;
    private String filename;

    public RDWReader(String filename) {
        this.filename = filename;
    }

    public void open() throws IOException {
        f = new BufferedInputStream(new FileInputStream(filename));
    }

    /**
     * Close the underlying InputStream
     */
    @Override
    public void close() throws IOException {
        f.close();
    }

    private int getRemainingCounter() {
        return (currentCounter % DATA_BLOCK_SIZE);
    }

    private int readInternal(int numBytes, byte[] bytesRead) throws IOException {
        if (numBytes == 0) {
            System.out.println("Why you are reading zero bytes?");
            return 0;
        }

        //are we on the block edge? -----> 1012|1013|1014, then skip 2 unused bytes
        if (getRemainingCounter() == 0 && currentCounter > 0) {
            long ignored = f.skip(UNUSED_SIZE);
            if (ignored == 0) {
                //TODO: add some handling for bytes skipped
            }
        }
        if ((getRemainingCounter() + numBytes) > DATA_BLOCK_SIZE) {
            //block size ended - only a portion of bytes can be read
            int possibleBytes = DATA_BLOCK_SIZE - getRemainingCounter();
            byte[] tempDataBytes1 = new byte[possibleBytes];
            int dataBytesRead1 = f.read(tempDataBytes1);
            if (dataBytesRead1 != -1) {
                currentCounter += dataBytesRead1;
            }

            //skip 2 unused bytes (1014-1012)
            if (getRemainingCounter() == 0) {
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
                currentCounter += dataBytesRead2;
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
            currentCounter += lengthBytesRead;
            return lengthBytesRead;
        }
        return 0;
    }

    private int readInternal2(int numBytes, byte[] bytesRead) throws IOException {
        if (numBytes == 0) {
            System.out.println("Why you are reading zero bytes?");
            return 0;
        }

        int totalBytesRead = 0;
        while (totalBytesRead < numBytes) {
            int remainingInBlock = DATA_BLOCK_SIZE - getRemainingCounter();
            int bytesToRead = Math.min(numBytes - totalBytesRead, remainingInBlock);

            int bytesReadThisRound = f.read(bytesRead, totalBytesRead, bytesToRead);
            if (bytesReadThisRound == -1) {
                // End of file reached prematurely
                throw new IOException("Premature end of file.");
            }

            currentCounter += bytesReadThisRound;
            totalBytesRead += bytesReadThisRound;

            if (getRemainingCounter() == 0) {
                // At the block edge, skip 2 unused bytes
                long ignored = f.skip(UNUSED_SIZE);
                if (ignored == 0) {
                    // TODO: add some handling for bytes skipped
                }
            }
        }

        return totalBytesRead;
    }
    /**
     * Read the next record and return as a new byte array
     *
     * @return the byte[] containing the record
     */
    public byte[] read() throws IOException {
        byte[] recordLengthBytes = new byte[RDW_SIZE];
        int nb = readInternal2(RDW_SIZE, recordLengthBytes);
        if (nb == -1) {
            return null;
        }
        String rdw = ISOUtil.hexString(recordLengthBytes);
        int recordLength = Integer.parseInt(rdw, 16);

        if (recordLength == 0) {
            return null;
        }
        byte[] dataBytes = new byte[recordLength];
        int nb2 = readInternal2(recordLength, dataBytes);
        if (nb2 != recordLength) {
            throw new IOException(String.format("Expected number of bytes %d couldn't be read.", recordLength));
        }
        return dataBytes;
    }
}
