package org.congcong.multi.proxy.encryption.algorithm;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AESGCMProcessor implements CryptoProcessor {

    private static final int GCM_TAG_LENGTH = 128;
    private SecretKey secretKey;

    private byte[] key;





    @Override
    public byte[] encrypt(byte[] plaintext, byte[] nonce) throws Exception {
        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters params = new AEADParameters(new KeyParameter(secretKey.getEncoded()), GCM_TAG_LENGTH, nonce);
        cipher.init(true, params);

        byte[] ciphertext = new byte[cipher.getOutputSize(plaintext.length)];
        int len = cipher.processBytes(plaintext, 0, plaintext.length, ciphertext, 0);
        cipher.doFinal(ciphertext, len);

        return ciphertext;
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] nonce) throws Exception {
        GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
        AEADParameters params = new AEADParameters(new KeyParameter(secretKey.getEncoded()), GCM_TAG_LENGTH, nonce);
        cipher.init(false, params);

        byte[] plaintext = new byte[cipher.getOutputSize(ciphertext.length)];
        int len = cipher.processBytes(ciphertext, 0, ciphertext.length, plaintext, 0);
        cipher.doFinal(plaintext, len);

        return plaintext;
    }



    @Override
    public byte[] getKey() {
        return key;
    }

    @Override
    public void refreshKey(byte[] key) {
        this.key = key;
        this.secretKey = new SecretKeySpec(key, "AES");
    }

    @Override
    public void setKey(byte[] key) {
        this.key = key;
    }

    @Override
    public int getKeySize() {
        return 32;
    }

    @Override
    public int getSaltSize() {
        return 32;
    }

    @Override
    public int getNonceSize() {
        return 12;
    }

    @Override
    public int getTagSize() {
        return 16;
    }


}
