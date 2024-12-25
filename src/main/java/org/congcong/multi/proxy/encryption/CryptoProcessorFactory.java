package org.congcong.multi.proxy.encryption;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.congcong.multi.proxy.encryption.algorithm.*;

import java.security.Security;

public class CryptoProcessorFactory {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }



    public static CryptoProcessor createProcessor(Algorithm algorithm, String key) throws Exception {
        CryptoProcessor cryptoProcessor = null;
        switch (algorithm) {
            case AES_256_GCM:
                cryptoProcessor = new AESGCMProcessor();
                break;
            case AES_128_GCM:
                cryptoProcessor = new AES128GCMProcessor();
                break;
            case CHACHA20_POLY1305:
                cryptoProcessor = new ChaCha20Poly1305Processor();
                break;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
        cryptoProcessor.setKey(HKDF.kdf(key, cryptoProcessor.getKeySize()));
        return cryptoProcessor;
    }

}
