package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0164Parser extends PDSParserSupport {

    public void parse(String data) {
        int i = 0;
        int counter = 1;
        while (i < data.length()) {
            String currCode = data.substring(i, i + 3);
            i += 3;
            String currRate = data.substring(i, i + 11);
            i += 11;
            String currType = data.substring(i, i + 1);
            i += 1;
            String date = data.substring(i, i + 6);
            i += 6;
            String cycle = data.substring(i, i + 2);
            i += 2;
            elements.add(
                    "PDS0164_" + counter + " Currency Code=" + currCode + " Conversion Rate=" + currRate
                            + " Conversion Type=" + currType + " Business Date=" + date + " Delivery Cycle=" + cycle);
            counter++;
        }
    }
}
