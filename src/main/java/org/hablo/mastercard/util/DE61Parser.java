package org.hablo.mastercard.util;

import org.jpos.ee.BLException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import java.io.PrintStream;

/**
 * Created by Arsalan Khan on 09/06/21.
 */
public class DE61Parser implements DEParserSupport {

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
        if (de61.length() > 11)
            return de61.substring(11, 13);
        return "";
    }

    public String getPOSCountryCode() {
        if (de61.length() > 13)
            return de61.substring(13, 16);
        return "";
    }

    public String getPOSPostalCode() {
        if (de61.length() > 16)
            return de61.substring(16);
        return "";
    }

    @Override
    public void dump(PrintStream p, String indent) {
        p.println(indent + getClass().getName() + " value='" + de61 + "'");
        if (de61 == null || de61.isEmpty()) return;
        p.println(indent + " SF01 POS Terminal Attendance " + getPOSTerminalAttendance());
        p.println(indent + " SF02 RFU " + getSubField2RFU());
        p.println(indent + " SF03 POS Terminal Location " + getPOSTerminalLocation());
        p.println(indent + " SF04 POS Cardholder Presence " + getPOSCardholderPresence());
        p.println(indent + " SF05 POS Card Presence " + getPOSCardPresence());
        p.println(indent + " SF06 POS Card Capture Capabilities " + getPOSCardCaptureCapabilities());
        p.println(indent + " SF07 POS Transaction Status Indicator " + getPOSTransactionStatusIndicator());
        p.println(indent + " SF08 POS Transaction Security Indicator " + getPOSTransactionSecurityIndicator());
        p.println(indent + " SF09 RFU " + getSubField9RFU());
        p.println(indent + " SF10 Cardholder-Activated Terminal Level " + getCardholderActivatedTerminalLevel());
        p.println(indent + " SF11 POS Card Data Terminal Input Capability Indicator " + getPOSCardDataTerminalInputCapabilityIndicator());
        if (de61.length() > 11)
            p.println(indent + " SF12 POS Authorization LifeCycle " + getPOSAuthorizationLifeCycle());
        if (de61.length() > 13)
            p.println(indent + " SF13 POS Country Code " + getPOSCountryCode());
        if (de61.length() > 16)
            p.println(indent + " SF14 POS Postal Code " + getPOSPostalCode());
    }

}
