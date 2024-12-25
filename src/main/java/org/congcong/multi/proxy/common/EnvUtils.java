package org.congcong.multi.proxy.common;

public class EnvUtils {

    public static final String JAVA_VERSION;

    public static final String JAVA_VERSION_8 = "8";

    public static final String JAVA_VERSION_11 = "11";


    static {
        JAVA_VERSION = System.getProperty("java.version");
    }
}
