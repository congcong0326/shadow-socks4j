package org.congcong.multi.proxy.config;


import org.congcong.multi.proxy.encryption.algorithm.Algorithm;

import java.util.List;

public class ServiceConfig {
    private List<Service> services;

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public static class Service {
        private String name;
        private int port;
        private ServiceType type;
        private List<Credentials> credentials;
        private boolean enable;
        private boolean supportDecryption;        // 是否支持解密
        private Algorithm decryptionAlgorithm;       // 解密算法
        private String decryptionKey;             // 解密密码

        private int upstreamBandwidth;

        public int getUpstreamBandwidth() {
            return upstreamBandwidth;
        }

        public void setUpstreamBandwidth(int upstreamBandwidth) {
            this.upstreamBandwidth = upstreamBandwidth;
        }

        public int getDownstreamBandwidth() {
            return downstreamBandwidth;
        }

        public void setDownstreamBandwidth(int downstreamBandwidth) {
            this.downstreamBandwidth = downstreamBandwidth;
        }

        private int downstreamBandwidth;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public ServiceType getType() {
            return type;
        }

        public void setType(ServiceType type) {
            this.type = type;
        }

        public List<Credentials> getCredentials() {
            return credentials;
        }

        public void setCredentials(List<Credentials> credentials) {
            this.credentials = credentials;
        }

        public boolean isSupportDecryption() {
            return supportDecryption;
        }

        public void setSupportDecryption(boolean supportDecryption) {
            this.supportDecryption = supportDecryption;
        }

        public Algorithm getDecryptionAlgorithm() {
            return decryptionAlgorithm;
        }

        public void setDecryptionAlgorithm(Algorithm decryptionAlgorithm) {
            this.decryptionAlgorithm = decryptionAlgorithm;
        }

        public String getDecryptionKey() {
            return decryptionKey;
        }

        public void setDecryptionKey(String decryptionKey) {
            this.decryptionKey = decryptionKey;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }

    public static class Credentials {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
