package org.jpos.security;

import org.apache.commons.lang3.StringUtils;
import org.jpos.iso.ISOUtil;
import org.jpos.security.jceadapter.JCEHandlerException;
import org.jpos.security.jceadapter.SSM;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TR31 {
    private final KeyBlockVersionID keyBlockVersionID = KeyBlockVersionID.KeyDerivationBindingMethod2010Edition;
    private final KeyUsage keyUsage = KeyUsage.PINENC;
    private final ModeOfUse modeofUse = ModeOfUse.ENCDEC;
    private final Algorithm algorithm = Algorithm.TDES;
    private Exportability exportability = Exportability.TRUSTED;
    private String keyVersionNumber = "00";
    private String numberOptionalBlocks = "00";
    private String reservedField = "00";
    private List<String> zpk;
    private List<String> zmk;
    private List<String> binaryKeyData;
    private List<String> headerAndBinaryKeyData;
    private String mac = "";
    private String k1 = "";
    private String k2 = "";
    private String km1 = "";
    private String km2 = "";
    private List<String> keyblockEncryptionKey;
    private List<String> keyblockMackey;
    private String keyblockHeaderAscii = "";
    private String keyblockHeaderAsciiHex = "";
    private List<String> encryptedkeydata;
    private List<String> tr31keyblock;
    private String tr31keydata = "";
    private String randomNumber = "";
    private final SSM ssm;

    public TR31(SSM ssm) {
        this.ssm = ssm;
    }

    public void setExportability(Exportability exportability) {
        this.exportability = exportability;
    }

    public void setRandomNumber(String randomNumber) {
        this.randomNumber = randomNumber;
    }

    private void initialize() {
        this.zpk = new ArrayList<>();
        this.zmk = new ArrayList<>();
        this.binaryKeyData = new ArrayList<>();
        this.headerAndBinaryKeyData = new ArrayList<>();
        this.keyblockEncryptionKey = new ArrayList<>();
        this.keyblockMackey = new ArrayList<>();
        this.encryptedkeydata = new ArrayList<>();
        this.tr31keyblock = new ArrayList<>();
    }

    public String encryptData(String pek, String kek) {
        this.initialize();
        this.zpk = this.parsedKeys(pek);
        this.zmk = this.parsedKeys(kek);
        this.buildKeyBlockHeader();
        this.buildBinaryKeyData();
        this.buildSubKeys(String.join("", this.zmk), "0000000000000000", DerivationMode.Encryption);
        this.buildKeyBlockKeys();
        this.buildSubKeys(String.join("", this.keyblockMackey), "0000000000000000", DerivationMode.MAC);
        this.generateMAC();
        this.encryptConfidentialData();
        this.buildTR31KeyBlock();
        return this.tr31keydata;
    }

    public String decryptData(String encKeyBlock, String kek) {
        String str1 = "";
        this.zmk = this.parsedKeys(kek);
        this.buildSubKeys(kek, "0000000000000000", DerivationMode.Encryption);
        this.buildKeyBlockKeys();
        String receivedMAC = encKeyBlock.substring(encKeyBlock.length() - 16);
        this.keyblockHeaderAsciiHex = ISOUtil.hexString(encKeyBlock.substring(0, 16).getBytes());
        String kString = encKeyBlock.substring(16, 16 + encKeyBlock.length() - 32);
        this.encryptedkeydata = new ArrayList<>();
        this.encryptedkeydata = this.parsedKeys(kString);
        this.decryptConfidentialData(receivedMAC);
        this.buildSubKeys(String.join("", this.keyblockMackey), "0000000000000000", DerivationMode.MAC);
        this.headerAndBinaryKeyData = new ArrayList<>();
        this.headerAndBinaryKeyData = this.parsedKeys(this.keyblockHeaderAsciiHex);
        this.headerAndBinaryKeyData.addAll(this.binaryKeyData);
        this.generateMAC();
        if (receivedMAC.equals(this.mac)) {
            String str2 = String.join("", this.binaryKeyData);
            int length = Integer.parseInt(str2.substring(0, 4), 16) / 4;
            str1 = str2.substring(4, length + 4);
        }
        return str1;
    }

    private void buildKeyBlockHeader() {
        String asciiData = String.join("",
                String.valueOf(this.keyBlockVersionID.getCode()),
                StringUtils.leftPad(String.valueOf(String.join("", this.zpk).length() + 48), 4, '0'),
                this.keyUsage.getCode(),
                String.valueOf(this.algorithm.getCode()),
                String.valueOf(this.modeofUse.getCode()),
                this.keyVersionNumber,
                String.valueOf(this.exportability.getCode()),
                this.numberOptionalBlocks,
                this.reservedField);
        this.keyblockHeaderAscii = asciiData;
        this.keyblockHeaderAsciiHex = ISOUtil.hexString(asciiData.getBytes());
    }

    private void buildBinaryKeyData() {
        String randNumber = this.getRandomNumber(6);
        String str1 = String.join("", this.zpk);
        String str2 = String.format("%04x", (str1.length() * 4)) + str1 + randNumber;
        String str3 = String.join("", this.keyblockHeaderAsciiHex, str2);

        this.binaryKeyData = new ArrayList<>();
        this.headerAndBinaryKeyData = new ArrayList<>();

        for (int startIndex = 0; startIndex < str3.length(); startIndex += 16)
            this.headerAndBinaryKeyData.add(str3.substring(startIndex, 16 + startIndex));

        for (int startIndex = 0; startIndex < str2.length(); startIndex += 16)
            this.binaryKeyData.add(str2.substring(startIndex, 16 + startIndex));
    }

    private void buildSubKeys(String keyBlock, String data, DerivationMode dMode) {
        String tempStr;
        byte[] d = ISOUtil.hex2byte(data);
        try {
            tempStr = ISOUtil.hexString(ssm.getJCEHandler().encryptData(d, ssm.formDESKey(keyBlock)));
        } catch (JCEHandlerException e) {
            throw new RuntimeException(e);
        }
        String data1 = this.buildSubKey(tempStr);
        String str = this.buildSubKey(data1);
        if (dMode != DerivationMode.Encryption) {
            if (dMode == DerivationMode.MAC) {
                this.km1 = data1;
                this.km2 = str;
            } else {
                this.k1 = data1;
                this.k2 = str;
            }
        } else {
            this.k1 = data1;
            this.k2 = str;
        }
    }

    private String buildSubKey(String data) {
        String str = this.addHexStrings(data, data);
        String data1 = str.substring(str.length() - 16, 16);
        if (this.isBitSet(ISOUtil.hex2byte(data.substring(0, 2))[0], 7))
            data1 = ISOUtil.hexor(data1, "000000000000001B");
        return data1;
    }

    private void buildKeyBlockKeys() {
        String keK = String.join("", this.zmk);
        this.keyblockEncryptionKey = new ArrayList<>();
        this.keyblockMackey = new ArrayList<>();
        for (int counter = 1; counter <= this.zmk.size(); ++counter) {
            String str1 = this.buildKeyBlockKey(this.getKeyDerivationInputData(counter, DerivationMode.Encryption), this.k1, keK);
            String str2 = this.buildKeyBlockKey(this.getKeyDerivationInputData(counter, DerivationMode.MAC), this.k1, keK);
            this.keyblockEncryptionKey.add(str1);
            this.keyblockMackey.add(str2);
        }
    }

    private String buildKeyBlockKey(String iData, String subkey, String keK) {
        byte[] d = ISOUtil.hex2byte(ISOUtil.hexor(iData, subkey));
        try {
            return ISOUtil.hexString(ssm.getJCEHandler().encryptData(d, ssm.formDESKey(keK)));
        } catch (JCEHandlerException e) {
            throw new RuntimeException(e);
        }
    }

    private String getKeyDerivationInputData(int counter, DerivationMode dMode) {
        String str1 = String.format("%02x", counter);
        String str2 = "00";
        String str3 = String.format("%04x", String.join("", this.zmk).length() * 4);
        String str4;
        if (this.zmk.size() == 3) {
            str4 = "0001";
        } else {
            str4 = "0000";
        }
        String str5;
        if (dMode == DerivationMode.MAC) {
            str5 = "0001";
        } else {
            str5 = "0000";
        }
        return String.join("", str1, str5, str2, str4, str3);
    }

    private void generateMAC() {
        String tempStr = "";
        String keyBlock = String.join("", this.keyblockMackey);
        int num = 1;
        for (String data : this.headerAndBinaryKeyData) {
            String tempStr1;
            if (num == 1) {
                tempStr1 = data;
            } else {
                String data2 = num >= this.headerAndBinaryKeyData.size() ? ISOUtil.hexor(data, this.km1) : data;
                tempStr1 = ISOUtil.hexor(tempStr, data2);
            }
            byte[] d = ISOUtil.hex2byte(tempStr1);
            try {
                tempStr = ISOUtil.hexString(ssm.getJCEHandler().encryptData(d, ssm.formDESKey(keyBlock)));
                ++num;
            } catch (JCEHandlerException e) {
                throw new RuntimeException(e);
            }
        }
        this.mac = tempStr;
    }

    private void encryptConfidentialData() {
        String tempStr = "";
        String keyBlock = String.join("", this.keyblockEncryptionKey);
        int num = 1;
        this.encryptedkeydata = new ArrayList<>();
        for (String str : this.binaryKeyData) {
            byte[] d = ISOUtil.hex2byte(num != 1 ? ISOUtil.hexor(tempStr, str) : ISOUtil.hexor(str, this.mac));
            try {
                tempStr = ISOUtil.hexString(ssm.getJCEHandler().encryptData(d, ssm.formDESKey(keyBlock)));
            } catch (JCEHandlerException e) {
                throw new RuntimeException(e);
            }
            this.encryptedkeydata.add(tempStr);
            ++num;
        }
    }

    private void buildTR31KeyBlock() {
        this.tr31keyblock = new ArrayList<>();
        this.tr31keyblock.add(this.keyblockHeaderAscii);
        this.tr31keyblock.addAll(this.encryptedkeydata);
        this.tr31keyblock.add(this.mac);
        this.tr31keydata = String.join("", this.tr31keyblock);
    }

    private void decryptConfidentialData(String rcvdMAC) {
        String keyBlock = String.join("", this.keyblockEncryptionKey);
        String data2 = "";
        int num = 1;
        this.binaryKeyData = new ArrayList<>();
        String data1;
        for (String data : this.encryptedkeydata) {
            try {
                data1 = ISOUtil.hexString(ssm.getJCEHandler().decryptData(ISOUtil.hex2byte(data), ssm.formDESKey(keyBlock)));
            } catch (JCEHandlerException e) {
                throw new RuntimeException(e);
            }

            String str = num != 1 ? ISOUtil.hexor(data1, data2) : ISOUtil.hexor(data1, rcvdMAC);
            data2 = data;
            this.binaryKeyData.add(str);
            ++num;
        }

        String kcv = this.binaryKeyData.get(binaryKeyData.size() - 1);
        this.randomNumber = kcv.substring(kcv.length() - 12, 12 + (kcv.length() - 12));
    }

    private List<String> parsedKeys(String kString) {
        List<String> strings = new ArrayList<>();
        for (int startIndex = 0; startIndex < kString.length(); startIndex += 16)
            strings.add(kString.substring(startIndex, 16 + startIndex));
        return strings;
    }

    private boolean isBitSet(byte b, int pos) {
        return (Byte.toUnsignedInt(b) &
                Integer.parseUnsignedInt(
                        String.valueOf(1 << pos)
                )
        ) > Integer.parseUnsignedInt("0");
    }

    public String addHexStrings(String hex1, String hex2) {
        int maxLen = Math.max(hex1.length(), hex2.length());
        BigInteger c = new BigInteger(hex1, 16).add(new BigInteger(hex2, 16));
        String t = StringUtils.leftPad(
                String.format("%X", c).toUpperCase(), maxLen,
                '0');
        return t.length() > maxLen ? t.substring(t.length() - maxLen) : t;
    }

    private String getRandomNumber(int numBytes) {
        if (StringUtils.isNotEmpty(this.randomNumber))
            return this.randomNumber;
        Random random = new Random();
        StringBuilder StringBuilder = new StringBuilder();
        for (int index = 0; index < numBytes; ++index) {
            String str = String.format("%2X", random.nextInt(Byte.MAX_VALUE));
            StringBuilder.append(str);
        }
        String randomNumber = StringBuilder.toString();
        this.randomNumber = randomNumber;
        return randomNumber;
    }

    enum DerivationMode {
        Encryption,
        MAC
    }
}
