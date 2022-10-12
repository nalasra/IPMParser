import org.jpos.iso.ISOUtil;
import org.jpos.security.SMAdapter;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.SecureKey;
import org.jpos.security.jceadapter.JCEHandler;
import org.jpos.security.jceadapter.JCEHandlerException;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.SimpleMsg;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.util.*;

public class SM {

    JCEHandler jceHandler;
    String jceProviderClassName;
    Provider provider;

    static final String ALG_DES = "DES";
    static final String ALG_TRIPLE_DES = "DESede";
    static final String DES_MODE_ECB = "ECB";
    static final String DES_MODE_CBC = "CBC";
    static final String DES_NO_PADDING = "NoPadding";

    public SM() throws SMException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (jceProviderClassName == null || jceProviderClassName.isEmpty()) {
            jceProviderClassName = "com.sun.crypto.provider.SunJCE";
        }
        provider = (Provider) Class.forName(jceProviderClassName).newInstance();
        Security.addProvider(provider);
        jceHandler = new JCEHandler(provider);
    }

    @Test
    void calculateCVV_iCVV() throws Exception {
        String bin = "409887285";
        String expiryDate = "";
        String cvk = "459794F4861A23DA3E89BC3EEA0B4A3D";
        SecureDESKey cvk2A = getSecureKey(cvk, 128, "CVK", "AAAAAA");
        Map<String, Integer> pans = new HashMap<>();

        BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir") + "/src/dist/cfg/" + "results.txt"));
        writer.write("BIN [" + bin + "] CVK [" + cvk + "]\n");

        for (int y = 22; y <= 30; y++) {
            writer.write("---------------\nYear = [20" + y + "]\n---------------\n");
            for (int m = 1; m <= 12; m++) {
                writer.write("Month = [" + m + "]\n");
                expiryDate = y + ISOUtil.padleft(String.valueOf(m), 2, '0');
                for (int i = 1; i <= 10000; i++) {
                    String pan = bin + ISOUtil.padleft(String.valueOf(i), 6, '0');
                    pan = pan + ISOUtil.calcLUHN(pan);
                    String cvv = calculateCVD(pan, cvk2A, expiryDate, "221");
                    String icvv = calculateCVD(pan, cvk2A, expiryDate, "999");
                    if (cvv.equals(icvv)) {
                        writer.write("PAN [" + pan + "] EXP [" + expiryDate + "] CVV [" + cvv + "] iCVV [" + icvv + "]\n");
                        if (pans.containsKey(pan)) {
                            pans.put(pan, pans.get(pan) + 1);
                            writer.write("Duplicate PAN/CVV/iCVV detected " + pan + "\n");
                        } else
                            pans.put(pan, 1);
                    }
                }
            }
        }

        //dump map
        writer.write("\n*** Duplicate PAN/CVV/iCVV combinations..\n");
        for (Map.Entry<String, Integer> entry : pans.entrySet())
            writer.write("PAN = " + entry.getKey() + ", Count = " + entry.getValue() + "\n");
        writer.close();
    }

    @Test
    public void test() throws SMException {
        String pan = "4123456789012345";

        String expiryDate = "8701";
        String serviceCode = "101";

        // 1. Fetch key from ctx. Split it into CVKA and CVKB

        String combinedKey = "0123456789ABCDEFFEDCBA9876543210";
        SecureDESKey cvk2A = getSecureKey(combinedKey, 128, "CVK", "AAAAAA");

        String r = calculateCVD(pan, cvk2A, expiryDate, serviceCode);
        System.out.println(r);
    }

    public String calculateCVD(String accountNo, SecureDESKey cvkA,
                               String expDate, String serviceCode) throws SMException {

        List<Loggeable> cmdParameters = new ArrayList<>();
        LogEvent evt = new LogEvent("", "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Calculate CVV/CVC", cmdParameters));
        String result = null;
        try {
            result = calculateCVD2Impl(accountNo, cvkA, expDate, serviceCode);
            evt.addMessage(new SimpleMsg("result", "Calculated CVV/CVC", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return result;
    }

    int getBytesLength(short keyLength) throws JCEHandlerException {
        int bytesLength = 0;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES:
                bytesLength = 8;
                break;
            case SMAdapter.LENGTH_DES3_2KEY:
                bytesLength = 16;
                break;
            case SMAdapter.LENGTH_DES3_3KEY:
                bytesLength = 24;
                break;
            default:
                throw new JCEHandlerException("Unsupported key length: " + keyLength + " bits");
        }
        return bytesLength;
    }

    Key formDESKey(short keyLength, byte[] clearKeyBytes) throws JCEHandlerException {
        Key key = null;
        switch (keyLength) {
            case SMAdapter.LENGTH_DES: {
                key = new SecretKeySpec(clearKeyBytes, ALG_DES);
            }
            break;
            case SMAdapter.LENGTH_DES3_2KEY: {
                // make it 3 components to work with JCE
                clearKeyBytes = ISOUtil.concat(clearKeyBytes, 0, getBytesLength(SMAdapter.LENGTH_DES3_2KEY), clearKeyBytes, 0,
                        getBytesLength(SMAdapter.LENGTH_DES));
            }
            case SMAdapter.LENGTH_DES3_3KEY: {
                key = new SecretKeySpec(clearKeyBytes, ALG_TRIPLE_DES);
            }
        }
        if (key == null)
            throw new JCEHandlerException("Unsupported DES key length: " + keyLength + " bits");
        return key;
    }

    String calculateCVD2Impl(String accountNo, SecureDESKey cvk,
                             String expDate, String serviceCode) throws SMException {
        Key udka = formDESKey(SMAdapter.LENGTH_DES
                , Arrays.copyOfRange(cvk.getKeyBytes(), 0, 8));

        byte[] block = ISOUtil.hex2byte(
                ISOUtil.zeropadRight(accountNo
                        + expDate
                        + serviceCode, 32));
        byte[] ba = Arrays.copyOfRange(block, 0, 8);
        byte[] bb = Arrays.copyOfRange(block, 8, 16);

        //Encrypt ba with udka
        byte[] bc = jceHandler.encryptData(ba, udka);
        byte[] bd = ISOUtil.xor(bc, bb);
        //Encrypt bd Tripple DES
        Key cvkk = formDESKey(SMAdapter.LENGTH_DES3_2KEY
                , Arrays.copyOfRange(cvk.getKeyBytes(), 0, 16));
        byte[] be = jceHandler.encryptData(bd, cvkk);
        return decimalizeVisa(be).substring(0, 3);
    }

    private static String decimalizeVisa(byte[] b) {
        char[] bec = ISOUtil.hexString(b).toUpperCase().toCharArray();
        char[] bhc = new char[bec.length];
        int k = 0;
        //Select 0-9 chars
        for (char c : bec)
            if (c < 'A')
                bhc[k++] = c;
        //Select A-F chars and map them to 0-5
        char adjust = 'A' - '0';
        for (char c : bec)
            if (c >= 'A')
                bhc[k++] = (char) (c - adjust);
        return new String(bhc);
    }

    public static SecureDESKey getSecureKey(String key, int keyLength, String keyType, String kcv) {
        byte[] keyBytes = ISOUtil.hex2byte(key);
        byte[] KeyCheckValue = ISOUtil.hex2byte(kcv);
        return new SecureDESKey((short) keyLength, keyType, keyBytes, KeyCheckValue);
    }

}
