package org.hablo.mastercard.util.ipm;

import org.hablo.mastercard.util.TLVParser.TLV;
import org.hablo.mastercard.util.PDSParserSupport;

public class PDS0164Parser extends PDSParserSupport {

    public void parse(TLV tag) {
        int i = 0;
        int counter = 1;
        String data = tag.getValue();
        int startPos = 0;

        while (i < data.length()) {
            startPos = i;
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

            TLV t1 = new TLV();
            t1.setId("[" + counter + "]");
            t1.setType("");
            t1.setValue(data.substring(startPos, i));
            t1.setDescription(tag.getDescription());
            t1.add(createTLV("1", currCode, "Currency Code"));
            t1.add(createTLV("2", currRate, "Conversion Rate"));
            t1.add(createTLV("3", currType, "Conversion Type"));
            t1.add(createTLV("4", date, "Business Date"));
            t1.add(createTLV("5", cycle, "Delivery Cycle"));

            tag.add(t1);
            counter++;
        }
    }
}
