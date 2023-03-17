package org.hablo.mastercard.util;

import org.hablo.mastercard.util.TLVParser.TLV;
import org.jpos.iso.ISOMsg;

import java.io.PrintStream;

/**
 * Created by Arsalan Khan on 09/06/21.
 */
public class DE61Parser implements ParserSupport {

    String de61;

    @Override
    public void parse(ISOMsg m) {
        de61 = m.getString(61);
    }

    public String getPOSTerminalAttendance() {
        return de61.substring(0, 1);
    }

    public String getSubField2RFU() {
        return de61.substring(1, 2);
    }

    public String getPOSTerminalLocation() {
        return de61.substring(2, 3);
    }

    public String getPOSCardholderPresence() {
        return de61.substring(3, 4);
    }

    public String getPOSCardPresence() {
        return de61.substring(4, 5);
    }

    public String getPOSCardCaptureCapabilities() {
        return de61.substring(5, 6);
    }

    public String getPOSTransactionStatusIndicator() {
        return de61.substring(6, 7);
    }

    public String getPOSTransactionSecurityIndicator() {
        return de61.substring(7, 8);
    }

    public String getSubField9RFU() {
        return de61.substring(8, 9);
    }

    public String getCardholderActivatedTerminalLevel() {
        return de61.substring(9, 10);
    }

    public String getPOSCardDataTerminalInputCapabilityIndicator() {
        return de61.substring(10, 11);
    }

    public String getPOSAuthorizationLifeCycle() {
        if (de61.length() > 11) {
            return de61.substring(11, 13);
        }
        return "";
    }

    public String getPOSCountryCode() {
        if (de61.length() > 13) {
            return de61.substring(13, 16);
        }
        return "";
    }

    public String getPOSPostalCode() {
        if (de61.length() > 16) {
            return de61.substring(16);
        }
        return "";
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + de61 + "'");
        if (de61 == null || de61.isEmpty()) {
            return;
        }

        TLV t = new TLV("61", de61, "SE", "Point-of-Service [POS] Data");
        t.add(new TLV("1", getPOSTerminalAttendance(), "SF", "POS Terminal Attendance"));
        t.add(new TLV("2", getSubField2RFU(), "SF", "RFU"));
        t.add(new TLV("3", getPOSTerminalLocation(), "SF", "POS Terminal Location"));
        t.add(new TLV("4", getPOSCardholderPresence(), "SF", "POS Cardholder Presence"));
        t.add(new TLV("5", getPOSCardPresence(), "SF", "POS Card Presence"));
        t.add(new TLV("6", getPOSCardCaptureCapabilities(), "SF", "POS Card Capture Capabilities"));
        t.add(new TLV("7", getPOSTransactionStatusIndicator(), "SF", "POS Transaction Status Indicator"));
        t.add(new TLV("8", getPOSTransactionSecurityIndicator(), "SF", "POS Transaction Security Indicator"));
        t.add(new TLV("9", getSubField9RFU(), "SF", "RFU"));
        t.add(new TLV("10", getCardholderActivatedTerminalLevel(), "SF", "Cardholder-Activated Terminal Level"));
        t.add(new TLV("11", getPOSCardDataTerminalInputCapabilityIndicator(), "SF",
                "POS Card Data Terminal Input Capability Indicator"));
        if (de61.length() > 11) {
            t.add(new TLV("12", getPOSAuthorizationLifeCycle(), "SF", "POS Authorization LifeCycle"));
        }
        if (de61.length() > 13) {
            t.add(new TLV("13", getPOSCountryCode(), "SF", "POS Country Code"));
        }
        if (de61.length() > 16) {
            t.add(new TLV("14", getPOSPostalCode(), "SF", "POS Postal Code"));
        }

        t.dump(p, indent);
    }

}
