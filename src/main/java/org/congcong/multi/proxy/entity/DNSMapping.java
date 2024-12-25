package org.congcong.multi.proxy.entity;

import java.util.List;

public class DNSMapping {
    private List<DnsMappingEntry> dnsMappings;

    public List<DnsMappingEntry> getDnsMappings() {
        return dnsMappings;
    }

    public void setDnsMappings(List<DnsMappingEntry> dnsMappings) {
        this.dnsMappings = dnsMappings;
    }

    public static class DnsMappingEntry {
        private String domain;
        private String ip;

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        private Integer port;

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }


}