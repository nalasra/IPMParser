package org.jpos.security.jceadapter;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.security.*;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.security.Key;

public class SSM extends JCESecurityModule {
    private TR31 tr31;

    public SSM() {
        super();
    }

    /**
     * @param lmkFile Local Master Keys filename of the JCE Security Module
     */
    public SSM(String lmkFile) throws SMException {
        super(lmkFile);
    }

    public SSM(String lmkFile, String jceProviderClassName)
            throws SMException {
        super(lmkFile, jceProviderClassName);
    }

    public SSM(Configuration cfg, Logger logger, String realm) throws ConfigurationException {
        super(cfg, logger, realm);
    }

    public JCEHandler getJCEHandler() {
        return this.jceHandler;
    }

    @Override
    protected Key decryptFromLMK(SecureDESKey secureDESKey) throws JCEHandlerException {
        return jceHandler.formDESKey(secureDESKey.getKeyLength(), secureDESKey.getKeyBytes());
    }

    public Key formDESKey(String key) {
        try {
            return jceHandler.formDESKey(SMAdapter.LENGTH_DES3_2KEY, ISOUtil.hex2byte(key));
        } catch (JCEHandlerException e) {
            throw new RuntimeException(e);
        }
    }

    public static SecureDESKey getSecureKey(String key, int keyLength, String keyType, String kcv) {
        byte[] keyBytes = ISOUtil.hex2byte(key);
        byte[] KeyCheckValue = ISOUtil.hex2byte(kcv);
        return new SecureDESKey((short) keyLength, keyType, keyBytes, KeyCheckValue);
    }

    public String encryptKeyTR31(String pek, String kek, String random, Exportability exportability){
        tr31 = new TR31(this);
        tr31.setRandomNumber(random);
        tr31.setExportability(exportability);
        return tr31.encryptData(pek, kek);
    }

    public String decryptKeyTR31(String keyblock, String kek) {
        return tr31.decryptData(keyblock, kek);
    }

    protected SecureDESKey getKey(String keyName)
            throws SMException, SecureKeyStore.SecureKeyStoreException {
        try {
            SecureKeyStore ks = NameRegistrar.get("ks");
            return ks.getKey(keyName);
        } catch (NameRegistrar.NotFoundException e) {
            throw new SMException(e.getMessage());
        }
    }
}
