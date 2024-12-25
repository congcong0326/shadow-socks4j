package org.congcong.multi.proxy.encryption.algorithm;

import org.congcong.multi.proxy.common.EnvUtils;

import java.util.Objects;

public class ChaCha20Poly1305Processor implements CryptoProcessor {
    private byte[] secretKey;



    @Override
    public byte[] encrypt(byte[] plaintext, byte[] nonce) throws Exception {
        // jdk 8需要使用三方库
        if (Objects.equals(EnvUtils.JAVA_VERSION_8, EnvUtils.JAVA_VERSION)) {
            //todo
            throw new UnsupportedOperationException();
        }
        return ChaCha20Poly1305ByJava11.encrypt(plaintext, getKey(), nonce);
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] nonce) throws Exception {
        // jdk 8需要使用三方库
        if (Objects.equals(EnvUtils.JAVA_VERSION_8, EnvUtils.JAVA_VERSION)) {
            //todo
            throw new UnsupportedOperationException();
        }
        // jdk11原生支持该算法
        return ChaCha20Poly1305ByJava11.decrypt(ciphertext, getKey(), nonce);
    }


    @Override
    public byte[] getKey() {
        return secretKey;
    }

    @Override
    public void refreshKey(byte[] key) {
        secretKey = key;
    }

    @Override
    public void setKey(byte[] key) {
        this.secretKey = key;
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
